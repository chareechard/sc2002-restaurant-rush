package rush.vp;

import com.vp.plugin.*;
import com.vp.plugin.diagram.*;
import com.vp.plugin.diagram.shape.IClassUIModel;
import com.vp.plugin.diagram.shape.ITextBoxUIModel;
import com.vp.plugin.diagram.shape.IPolygonUIModel;
import com.vp.plugin.model.*;
import com.vp.plugin.model.factory.IModelElementFactory;
import java.awt.Color;
import java.awt.Point;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Creates genuine Visual Paradigm models and saves them through its public API. */
public final class ClassDiagramExportPlugin implements VPPlugin, VPPluginCommandLineSupport {
    public void loaded(VPPluginInfo info) { }
    public void unloaded() { }
    private static final Color INK = Color.decode("#20364F");
    private static final Color ORANGE = Color.decode("#D16D00");
    private static final double SCALE = 1.0;
    private final ApplicationManager app = ApplicationManager.instance();
    private final IModelElementFactory factory = IModelElementFactory.instance();

    public void invoke(String[] args) {
        try {
            if(!"18.0".equals(app.getProductInfo().getVersion()))throw new IllegalStateException("Expected Desktop 18.0");
            System.out.println("VP_NATIVE_START " + app.getProductInfo().getName()+" "+app.getProductInfo().getVersion()+" build "+app.getProductInfo().getBuildNumber());
            String modelPath = System.getProperty("rush.vp.model", args.length > 0 ? args[0] : "");
            String outputPath = System.getProperty("rush.vp.output", args.length > 1 ? args[1] : "");
            if (modelPath.isEmpty() || outputPath.isEmpty()) throw new IllegalArgumentException("Expected model XML and output folder");
            Path output = Paths.get(outputPath);
            Files.createDirectories(output);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(modelPath));
            for (Element project : children(doc.getDocumentElement(), "project")) {
                generate(project, output);
            }
            System.out.println("VP_NATIVE_SUCCESS");
            if (Boolean.getBoolean("rush.vp.cli")) System.exit(0);
        } catch (Throwable error) {
            error.printStackTrace();
            if (Boolean.getBoolean("rush.vp.cli")) System.exit(1);
            throw new RuntimeException(error);
        }
    }

    private void generate(Element project, Path out) throws Exception {
        int stage = Integer.parseInt(project.getAttribute("stage"));
        out=out.resolve("stage-"+stage); Files.createDirectories(out);
        ProjectManager pm = app.getProjectManager();
        // The command-line host has no editor window for newProject(). Clear only
        // the in-memory seed; saveProjectAs below always targets the branch draft.
        for (IDiagramUIModel previous : pm.getProject().toDiagramArray()) previous.delete();
        for (IModelElement previous : pm.getProject().toModelElementArray()) {
            try { previous.delete(); }
            catch (UnsupportedOperationException protectedSetting) {
                System.out.println("VP_NATIVE_KEEP_SETTING " + previous.getModelType());
            }
        }
        for (IModelElement previous : pm.getProject().toAllLevelModelElementArray()) {
            if (previous instanceof IClass) throw new IOException("Seed class was retained: " + previous.getName());
        }
        pm.getProject().getProjectProperties().setDescription("Fresh Stage " + stage + " model from source " + project.getAttribute("sha"));
        DiagramManager dm = app.getDiagramManager();
        IClassDiagramUIModel diagram = (IClassDiagramUIModel) dm.createDiagram(DiagramManager.DIAGRAM_TYPE_CLASS_DIAGRAM);
        diagram.setName("Stage " + stage + " - Reviewed Class Diagram");
        diagram.setDocumentation("Verified Java source: "+project.getAttribute("sha")+". Approved class diagram, with selected relationships and stage colors.");
        diagram.setShowStereotypes(true);
        diagram.setShowClassEmptyCompartments(IClassDiagramUIModel.SHOW_CLASS_EMPTY_COMPARTMENTS_NO);
        diagram.setShowAttributesType(IClassDiagramUIModel.SHOW_ATTRIBUTES_TYPE_YES);
        diagram.setShowOperationsParameters(IClassDiagramUIModel.SHOW_OPERATIONS_PARAMETERS_YES);
        diagram.setShowOperationsReturnType(IClassDiagramUIModel.SHOW_OPERATIONS_RETURN_TYPE_YES);
        diagram.setShowClassReferencedAttributes(false);
        diagram.setAutoFitShapesSize(false);
        diagram.setDiagramBackground(Color.WHITE);
        Map<String,IClass> classes = new LinkedHashMap<>();
        Map<String,IShapeUIModel> shapes = new LinkedHashMap<>();
        Map<String,IPackage> packages = new LinkedHashMap<>();
        int maxX = 0, maxY = 0;

        Element classList = child(project,"classes");
        for (Element source : children(classList,"class")) {
            String name = source.getAttribute("name"), kind = source.getAttribute("kind");
            IClass model = factory.createClass();
            model.setName(name);
            model.setVisibility(IClass.VISIBILITY_PUBLIC);
            model.setAbstract(kind.equals("abstract class"));
            model.setLeaf(truth(source,"final"));
            if (kind.equals("interface")) model.addStereotype("interface");
            if (kind.equals("enum")) model.addStereotype("enumeration");
            if (truth(source,"record")) model.addStereotype("record");
            if (truth(source,"exception")) model.addStereotype("exception");
            model.setDocumentation("Java package: " + source.getAttribute("package") + "\nIntroduced in Stage " + source.getAttribute("introduced") + ".\nSource commit: " + project.getAttribute("sha"));
            String pkgName = source.getAttribute("package");
            IPackage pkg = packages.get(pkgName);
            if (pkg == null) { pkg = factory.createPackage(); pkg.setName(pkgName); packages.put(pkgName,pkg); }
            pkg.addChild(model);
            classes.put(name,model);
            for (Element a : children(source,"attribute")) {
                IAttribute attr = model.createAttribute();
                attr.setName(a.getAttribute("name")); attr.setType(a.getAttribute("type"));
                attr.setVisibility(a.getAttribute("visibility")); attr.setReadOnly(truth(a,"readonly"));
                attr.setScope(truth(a,"static") ? IAttribute.SCOPE_CLASSIFIER : IAttribute.SCOPE_INSTANCE);
            }
            for (Element o : children(source,"operation")) {
                IOperation op = model.createOperation();
                op.setName(o.getAttribute("name")); if(o.hasAttribute("returns")) op.setReturnType(o.getAttribute("returns"));
                op.setVisibility(o.getAttribute("visibility")); op.setStatic(truth(o,"static"));op.setScope(truth(o,"static")?IOperation.SCOPE_CLASSIFIER:IOperation.SCOPE_INSTANCE); op.setAbstract(truth(o,"abstract"));
                for (Element p : children(o,"parameter")) {
                    IParameter par = op.createParameter(); par.setName(p.getAttribute("name")); par.setType(p.getAttribute("type"));
                }
            }
            for (Element l : children(source,"literal")) model.createEnumerationLiteral().setName(l.getAttribute("name"));
            IClassUIModel shape = (IClassUIModel) dm.createDiagramElement(diagram,model);
            shape.setInterfaceBall(false);
            shape.setShowOwnerOption(IClassUIModel.SHOW_OWNER_NONE);
            shape.setShowAttributeType(IClassUIModel.ATTR_SHOW_TYPE_ALL);
            shape.setShowOperationType(IClassUIModel.OP_SHOW_TYPE_ALL);
            shape.setShowParameterNameInOperationSignature(!source.getAttribute("showParameterNames").equals("false"));
            shape.setWrapMembers(truth(source,"wrapMembers"));
            shape.setShowOperationSignature(true);
            shape.setShowOperationParameterDirection(false);
            shape.getElementFont().setValues("Arial",false,false,16,INK);
            Color fill = Color.decode(source.getAttribute("color"));
            shape.setBackground(fill);
            shape.getFillColor().setColor1(fill,false);
            shape.getLineModel().setColor(truth(source,"modified") ? ORANGE : Color.decode("#52677D"),false);
            shape.getLineModel().setWeight(truth(source,"modified") ? 2.5f : 1.2f,false);
            // Fit also initializes native class-name/stereotype compartments.
            shape.fitSize();
            shape.setRequestFitSize(false);
            shape.setRequestDefaultSize(false);
            int x = scaled(source,"x"), y = scaled(source,"y");
            int w = scaled(source,"width");
            int h = scaled(source,"height");
            shape.setBounds(x,y,w,h);
            shape.setRequestFitSize(false);
            shape.setRequestResetCaption(true);
            shape.setRequestResetCaptionSize(false);
            shape.setRequestResetCaptionFitWidth(false);
            shapes.put(name,shape);
            maxX = Math.max(maxX,x+w); maxY = Math.max(maxY,y+h);
        }
        for (Element edge : children(child(project,"relationships"),"relationship")) {
            String kind = edge.getAttribute("kind"), fromName=edge.getAttribute("source"), toName=edge.getAttribute("target");
            IClass from=classes.get(fromName), to=classes.get(toName);
            IModelElement relationship;
            if (kind.equals("generalization")) {
                // Visual Paradigm stores generalizations from superclass to subclass.
                IGeneralization r = factory.createGeneralization(); r.setFrom(to); r.setTo(from); relationship=r;
                String swap=fromName; fromName=toName; toName=swap;
            } else if (kind.equals("realization")) {
                IRealization r = factory.createRealization(); r.setFrom(to); r.setTo(from); relationship=r;
                String swap=fromName; fromName=toName; toName=swap;
            } else if (kind.equals("dependency")) {
                IDependency r = factory.createDependency(); r.setFrom(from); r.setTo(to); relationship=r;
            } else {
                IAssociation r = factory.createAssociation();
                IAssociationEnd fe=(IAssociationEnd)r.getFromEnd(), te=(IAssociationEnd)r.getToEnd();
                fe.setModelElement(from); te.setModelElement(to);
                if (!edge.getAttribute("sourceMultiplicity").isEmpty()) fe.setMultiplicity(edge.getAttribute("sourceMultiplicity"));
                if (!edge.getAttribute("targetMultiplicity").isEmpty()) te.setMultiplicity(edge.getAttribute("targetMultiplicity"));
                // This structural view omits navigability arrowheads. Types in
                // the class members show which objects hold each reference.
                fe.setNavigable(IAssociationEnd.NAVIGABLE_UNSPECIFIED);
                te.setNavigable(IAssociationEnd.NAVIGABLE_UNSPECIFIED);
                if (kind.equals("composition")) fe.setAggregationKind(IAssociationEnd.AGGREGATION_KIND_COMPOSITED);
                if (kind.equals("aggregation")) fe.setAggregationKind(IAssociationEnd.AGGREGATION_KIND_SHARED);
                relationship=r;
            }
            relationship.setName(edge.getAttribute("name"));
            java.util.List<java.awt.Point> points=new java.util.ArrayList<>();
            for(Element p:children(edge,"point")) points.add(new java.awt.Point(Integer.parseInt(p.getAttribute("x")),Integer.parseInt(p.getAttribute("y"))));
            boolean reversed=kind.equals("generalization")||kind.equals("realization");
            if(reversed) java.util.Collections.reverse(points);
            IConnectorUIModel connector=(IConnectorUIModel)dm.createConnector(diagram,relationship,shapes.get(fromName),shapes.get(toName),points.toArray(new java.awt.Point[0]));
            int left=points.stream().mapToInt(p->p.x).min().getAsInt(),top=points.stream().mapToInt(p->p.y).min().getAsInt();
            int right=points.stream().mapToInt(p->p.x).max().getAsInt(),bottom=points.stream().mapToInt(p->p.y).max().getAsInt();
            connector.setBounds(left-70,top-70,right-left+140,bottom-top+140);
            connector.clearPoints(); for(java.awt.Point p:points)connector.addPoint(p);
            connector.setRequestRebuild(false);connector.setRequestRebuildFromEnd(false);connector.setRequestRebuildToEnd(false);
            if(connector instanceof com.vp.plugin.diagram.connector.IAssociationUIModel) {
                com.vp.plugin.diagram.connector.IAssociationUIModel a=(com.vp.plugin.diagram.connector.IAssociationUIModel)connector;
                a.setShowFromMultiplicity(false);a.setShowToMultiplicity(false);
                for(String end:new String[]{"source","target"})for(Element label:children(edge,end+"Label")) {
                    String value=edge.getAttribute(end+"Multiplicity");
                    if(value.isEmpty()||value.equals("Unspecified"))continue;
                    ITextBoxUIModel caption=note(diagram,value,number(label,"x"),number(label,"y"),number(label,"width"),number(label,"height"));
                    caption.setHTMLText(html(value,14,Color.WHITE));caption.setCoverConnector(true);
                }
            }
            connector.setRequestResetCaption(false);connector.setRequestResetCaptionSize(false);
            connector.getElementFont().setValues("Arial",false,false,12,INK);
            connector.getLineModel().setColor(Color.decode("#6B7F92"),false);
        }
        drawPresentation(diagram,project);
        String basename=project.getAttribute("basename");
        if (basename.isEmpty()) basename=String.format("%02d_Stage_%d_UML",stage+3,stage);
        File file=out.resolve(basename+".vpp").toFile();
        if (!pm.saveProjectAs(file)) throw new IOException("Save failed: "+file);
        if (!pm.openProject(file)) throw new IOException("Reopen failed: "+file);
        IDiagramUIModel saved=pm.getProject().toDiagramArray()[0];
        int shapeCount=0;
        for (IDiagramElement element:saved.toDiagramElementArray()) if(element.getModelElement() instanceof IClass) shapeCount++;
        if (shapeCount!=Integer.parseInt(project.getAttribute("count"))) throw new IOException("Class shape count mismatch: "+shapeCount);
        int connectorCount=0;
        for (IDiagramElement element:saved.toDiagramElementArray()) if(element instanceof IConnectorUIModel) connectorCount++;
        if (connectorCount!=Integer.parseInt(project.getAttribute("relationshipCount"))) throw new IOException("Relationship count mismatch: "+connectorCount);
        ModelConvertionManager convert=app.getModelConvertionManager();
        ExportXMLOption xmlOption=ExportXMLOption.toExportProject();
        xmlOption.setSimpleStructure(true);
        xmlOption.setExportImages(false);
        convert.exportXML(out.resolve("model-xml").toFile(),xmlOption);
        String[] formats = new String[]{"png","svg"};
        for (String format:formats) {
            int type=format.equals("png")?ExportDiagramAsImageOption.IMAGE_TYPE_PNG:format.equals("svg")?ExportDiagramAsImageOption.IMAGE_TYPE_SVG:ExportDiagramAsImageOption.IMAGE_TYPE_PDF;
            ExportDiagramAsImageOption options=new ExportDiagramAsImageOption(type);
            if (format.equals("pdf")) {
                PdfOption pdf=new PdfOption();
                pdf.setOrientation(Orientation.Landscape);
                pdf.setPageSize(PageSize.A2);
                pdf.setMargin(new java.awt.Insets(24,24,24,24));
                options.setPdfOption(pdf);
            }
            convert.exportDiagramAsImage(saved,out.resolve("native-render."+format).toFile(),options);
        }
        if (!pm.saveProject()) throw new IOException("Final caption save failed: "+file);
        convert.exportXML(out.resolve("model-xml").toFile(),xmlOption);
        System.out.println("VP_NATIVE_STAGE_OK stage="+stage+" shapes="+shapeCount+" relationships="+connectorCount+" bytes="+file.length());
    }


    private void drawPresentation(IDiagramUIModel diagram,Element project) {
        int stage=Integer.parseInt(project.getAttribute("stage"));
        Element p=child(project,"presentation");int width=number(p,"width"),y=number(p,"legendY");
        ITextBoxUIModel title=note(diagram,"Restaurant Rush - Stage "+stage+" Class Diagram",60,10,width-60,50);
        title.setHTMLText(html("<b>Restaurant Rush - Stage "+stage+" Class Diagram</b>",30,Color.WHITE));
        for(Element h:children(p,"heading")) {
            ITextBoxUIModel heading=note(diagram,h.getAttribute("text"),number(h,"x"),number(h,"y"),number(h,"width"),30);
            Color fill=Color.decode("#F1F5F9");heading.setBackground(fill);heading.getFillColor().setColor1(fill,false);heading.getLineModel().setColor(fill,false);
            heading.setHTMLText(html("<b>"+h.getAttribute("text")+"</b>",19,fill));
        }
        String[] kinds={"association","composition","aggregation","dependency","generalization","realization"};
        String[] labels={"Association (reference)","Composition","Shared aggregation","Dependency (uses)","Inheritance","Interface implementation"};
        int cols=3;double step=(width-120.0)/cols;
        for(int i=0;i<kinds.length;i++) {
            if(stage<3&&(kinds[i].equals("aggregation")||kinds[i].equals("realization")))continue;
            int x=(int)Math.round(60+(i%cols)*step),yy=y+(i/cols)*52;String k=kinds[i];
            if(k.equals("dependency")||k.equals("realization")){int length=k.equals("realization")?72:90;for(int j=0;j*13<length;j++)line(diagram,x+j*13,yy,Math.min(8,length-j*13));}else line(diagram,x,yy,k.equals("generalization")?72:90);
            if(k.equals("composition")||k.equals("aggregation"))polygon(diagram,new int[]{x,yy,x+13,yy-7,x+26,yy,x+13,yy+7,x,yy},k.equals("composition")?INK:Color.WHITE);
            if(k.equals("dependency")) {
                polygon(diagram,new int[]{x+76,yy-6,x+90,yy,x+76,yy+6},Color.WHITE);
            } else if(k.equals("generalization")||k.equals("realization")){polygon(diagram,new int[]{x+90,yy,x+72,yy-9,x+72,yy+9,x+90,yy},Color.WHITE);IShapeUIModel base=(IShapeUIModel)app.getDiagramManager().createDiagramElement(diagram,IShapeTypeConstants.SHAPE_TYPE_RECTANGLE);base.setBounds(x+72,yy-9,1,18);base.setBackground(INK);base.getFillColor().setColor1(INK,false);base.getLineModel().setColor(INK,false);}
            note(diagram,labels[i],x+110,yy-16,(int)step-115,34);
        }
        int rows=(kinds.length+cols-1)/cols,noteY=y+rows*52;
        ITextBoxUIModel meanings=note(diagram,"Underlined = static (class member). Italic = abstract.",60,noteY,width-120,34);
        meanings.setHTMLText(html("<u>Underlined</u> = static (class member). <i>Italic</i> = abstract.",17,Color.WHITE));
        int colorY=noteY+40,swatch=(width-180)/4;
        colorNote(diagram,"Stage 1: white",60,colorY,swatch,Color.WHITE);
        colorNote(diagram,"Stage 2 additions: blue",60+(width-120)/4,colorY,swatch,Color.decode("#D8EAFE"));
        colorNote(diagram,"Stage 3 additions: green",60+2*(width-120)/4,colorY,swatch,Color.decode("#DDF1DF"));
        colorNote(diagram,"Orange border: modified existing class",60+3*(width-120)/4,colorY,swatch,Color.WHITE);
    }
    private static int number(Element e,String name){return Integer.parseInt(e.getAttribute(name));}
    private static int scaled(Element e,String name){return number(e,name);}
    private static boolean truth(Element e,String name){return "true".equals(e.getAttribute(name));}
    private static Element child(Element e,String name){for(Element c:children(e,name))return c;throw new IllegalArgumentException(name);}
    private static java.util.List<Element> children(Element e,String name){
        java.util.List<Element> result=new java.util.ArrayList<>();
        for(Node n=e.getFirstChild();n!=null;n=n.getNextSibling())if(n instanceof Element&&n.getNodeName().equals(name))result.add((Element)n);
        return result;
    }
    private ITextBoxUIModel note(IDiagramUIModel diagram,String text,int x,int y,int width,int height) {
        ITextBoxUIModel shape=(ITextBoxUIModel)app.getDiagramManager().createDiagramElement(diagram,IShapeTypeConstants.SHAPE_TYPE_TEXT_BOX);
        shape.setPlainText(text);
        shape.setHTMLText(html(text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"),16,Color.WHITE));
        shape.getElementFont().setValues("Arial",false,false,16,INK);
        shape.setBounds(x,y,width,height);
        shape.setBackground(Color.WHITE); shape.getFillColor().setColor1(Color.WHITE,false);
        shape.getLineModel().setColor(Color.WHITE,false);
        return shape;
    }

    private void colorNote(IDiagramUIModel diagram,String text,int x,int y,int width,Color color) {
        ITextBoxUIModel shape=note(diagram,text,x,y,width,40);
        shape.setBackground(color);shape.getFillColor().setColor1(color,false);shape.getLineModel().setColor(color,false);
        shape.setHTMLText(html(text,16,color));
    }

    private void line(IDiagramUIModel diagram,int x,int y,int width) {
        IShapeUIModel shape=(IShapeUIModel)app.getDiagramManager().createDiagramElement(diagram,IShapeTypeConstants.SHAPE_TYPE_RECTANGLE);
        shape.fitSize();
        shape.setRequestFitSize(false);shape.setRequestDefaultSize(false);
        shape.setPresentationOption(IShapeUIModel.PRESENTATION_OPTION_PRIMITIVE);
        shape.setPrimitiveShapeType(IShapeUIModel.PRIMITIVE_SHAPE_TYPE_RECTANGLE);
        shape.setBounds(x,y,width,2);
        shape.setBackground(INK);shape.getFillColor().setColor1(INK,false);
        shape.getLineModel().setColor(INK,false);shape.getLineModel().setWeight(1.3f,false);
        shape.getCaptionUIModel().setVisible(false);
    }

    private void polygon(IDiagramUIModel diagram,int[] coordinates,Color fill) {
        IPolygonUIModel shape=(IPolygonUIModel)app.getDiagramManager().createDiagramElement(diagram,IShapeTypeConstants.SHAPE_TYPE_POLYGON);
        Point[] points=new Point[coordinates.length/2];
        int left=Integer.MAX_VALUE,top=Integer.MAX_VALUE,right=Integer.MIN_VALUE,bottom=Integer.MIN_VALUE;
        for(int i=0;i<points.length;i++) {
            points[i]=new Point(coordinates[i*2],coordinates[i*2+1]);
            left=Math.min(left,points[i].x);top=Math.min(top,points[i].y);
            right=Math.max(right,points[i].x);bottom=Math.max(bottom,points[i].y);
        }
        shape.setPresentationOption(IShapeUIModel.PRESENTATION_OPTION_STANDARD);
        shape.setRequestFitSize(false);shape.setRequestDefaultSize(false);
        shape.setBounds(left,top,right-left,bottom-top);
        // Polygon coordinates are relative to the graphic shape's bounds.
        for (Point point:points) point.translate(-left,-top);
        shape.setPoints(points);
        shape.setBackground(fill);shape.getFillColor().setColor1(fill,false);
        shape.getLineModel().setColor(INK,false);shape.getLineModel().setWeight(1.3f,false);
        shape.getCaptionUIModel().setVisible(false);
    }

    private static String html(String body,int size,Color color) {
        String background=String.format("#%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue());
        return "<html><body style=\"margin:0;font-family:Arial;font-size:"+size+"pt;color:#20364F;background-color:"+background+"\">"+body+"</body></html>";
    }
}
