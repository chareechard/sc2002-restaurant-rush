package rush.vp;

import com.vp.plugin.*;
import com.vp.plugin.diagram.*;
import com.vp.plugin.diagram.shape.IClassUIModel;
import com.vp.plugin.diagram.shape.ITextBoxUIModel;
import com.vp.plugin.model.*;
import com.vp.plugin.model.factory.IModelElementFactory;
import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Creates genuine Visual Paradigm models and saves them through its public API. */
public final class StageDiagramPlugin implements VPPlugin, VPPluginCommandLineSupport {
    public void loaded(VPPluginInfo info) { }
    public void unloaded() { }
    private static final Color INK = Color.decode("#20364F");
    private static final Color ORANGE = Color.decode("#D16D00");
    private static final double SCALE = 1.0;
    private final ApplicationManager app = ApplicationManager.instance();
    private final IModelElementFactory factory = IModelElementFactory.instance();

    public void invoke(String[] args) {
        try {
            System.out.println("VP_NATIVE_START " + app.getProductInfo().getName()+" "+app.getProductInfo().getVersion());
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
        diagram.setName("Stage " + stage + " - Class Diagram - Richard Draft");
        diagram.setDocumentation("Source commit: " + project.getAttribute("sha") + "\nAll 23 production types; 24 selected relationships. Stage 2 adds VIPCustomer and CriticCustomer. Enumeration links and most parameter/return dependencies are omitted. Association navigability is not shown; fields and operations identify references. White = retained Stage 1 class. Blue = introduced in Stage 2. Orange border = existing class changed for Stage 2. Scenario changes the customer factory; ConsoleApp changes its stage banner. Routine accessors and constructors are omitted.");
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
                op.setName(o.getAttribute("name")); op.setReturnType(o.getAttribute("returns"));
                op.setVisibility(o.getAttribute("visibility")); op.setStatic(truth(o,"static")); op.setAbstract(truth(o,"abstract"));
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
            shape.setShowParameterNameInOperationSignature(true);
            shape.setShowOperationSignature(true);
            shape.setShowOperationParameterDirection(false);
            shape.getElementFont().setValues("Arial",false,false,14,INK);
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
            IConnectorUIModel connector=(IConnectorUIModel)dm.createConnector(diagram,relationship,shapes.get(fromName),shapes.get(toName),null);
            connector.getElementFont().setValues("Arial",false,false,12,INK);
            connector.getLineModel().setColor(Color.decode("#6B7F92"),false);
        }
        // Native layout routes connectors around class shapes while retaining their positions.
        dm.layout(diagram,DiagramManager.LAYOUT_ROUTE_CONNECTORS_ORTHOGONAL);
        maxX=0; maxY=0;
        for (IShapeUIModel s:shapes.values()) { maxX=Math.max(maxX,s.getX()+s.getWidth()); maxY=Math.max(maxY,s.getY()+s.getHeight()); }
        IShapeUIModel title=note(diagram,"Restaurant Rush - Stage 2 Class Diagram",60,20,2970,44,Color.WHITE);
        title.getElementFont().setValues("Arial",true,false,26,INK);
        ((ITextBoxUIModel)title).setHTMLText(html("Restaurant Rush - Stage 2 Class Diagram",26,true,Color.WHITE));
        note(diagram,"Richard draft | Code "+project.getAttribute("sha").substring(0,12)+" | 23 production types | 24 selected relationships",60,76,2970,34,Color.WHITE);
        heading(diagram,"CLI AND TURN CONTROL",60,130,1090);
        heading(diagram,"CUSTOMER LIFECYCLE, ORDERS AND TABLES",1280,130,1380);
        heading(diagram,"MENU",2700,130,330);
        heading(diagram,"STAFF",520,820,680);
        heading(diagram,"MIXED CUSTOMER FACTORY",1430,1110,480);
        heading(diagram,"RESULT SNAPSHOTS",60,1140,350);
        heading(diagram,"ENUMS AND UTILITIES",60,1510,2970);
        note(diagram,"POLYMORPHISM: Customer.placeOrder calls chooseMeal and discountedBill; losePatience calls waitingLoss.",1280,1010,1380,35,Color.decode("#EEF5FD"));
        note(diagram,"Regular: -8 / rotating meal | VIP: -5 / Burger / 10% off | Critic: -12 / Pasta. Shared lifecycle; no type checks in GameEngine.",1280,1050,1380,40,Color.decode("#EEF5FD"));
        note(diagram,"Scenario creates C2 as VIP (turn 4), C3 as Critic (turn 7), and C1/C4/C5/C6 as Regular. Six arrivals across 18 turns.",1280,1420,1750,42,Color.WHITE);
        note(diagram,"Solid line: stored reference | Filled diamond: composition | Dashed arrow: uses/creates | Hollow triangle: inherits",60,1850,2970,36,Color.WHITE);
        note(diagram,"Selected fields/operations are verified against compiled code. Routine constructors/accessors and most parameter/return dependencies are omitted.",60,1895,2970,36,Color.WHITE);
        note(diagram,"Multiplicities describe one base-setting game: 2 tables, up to 6 customers/orders, up to 5 paid receipts. RuleViolation extends Java IllegalArgumentException.",60,1940,2970,36,Color.WHITE);
        note(diagram,"Retained Stage 1 type",60,2000,650,42,Color.WHITE);
        note(diagram,"Stage 2 addition",790,2000,650,42,Color.decode("#D8EAFE"));
        note(diagram,"Orange border: existing class changed for Stage 2 (Scenario factory; ConsoleApp banner)",1520,2000,1510,42,Color.WHITE);
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
        if (connectorCount!=Integer.parseInt(project.getAttribute("relationships"))) throw new IOException("Relationship count mismatch: "+connectorCount);
        ModelConvertionManager convert=app.getModelConvertionManager();
        ExportXMLOption xmlOption=ExportXMLOption.toExportProject();
        xmlOption.setSimpleStructure(true);
        xmlOption.setExportImages(false);
        convert.exportXML(out.resolve("model-xml").toFile(),xmlOption);
        for (String format:new String[]{"png","svg","pdf"}) {
            int type=format.equals("png")?ExportDiagramAsImageOption.IMAGE_TYPE_PNG:format.equals("svg")?ExportDiagramAsImageOption.IMAGE_TYPE_SVG:ExportDiagramAsImageOption.IMAGE_TYPE_PDF;
            ExportDiagramAsImageOption options=new ExportDiagramAsImageOption(type);
            if (format.equals("pdf")) {
                PdfOption pdf=new PdfOption();
                pdf.setOrientation(Orientation.Landscape);
                pdf.setPageSize(PageSize.A1);
                pdf.setMargin(new java.awt.Insets(24,24,24,24));
                options.setPdfOption(pdf);
            }
            convert.exportDiagramAsImage(saved,out.resolve("class-diagram."+format).toFile(),options);
        }
        if (!pm.saveProject()) throw new IOException("Final caption save failed: "+file);
        convert.exportXML(out.resolve("model-xml").toFile(),xmlOption);
        System.out.println("VP_NATIVE_STAGE_OK stage="+stage+" shapes="+shapeCount+" relationships="+connectorCount+" bytes="+file.length());
    }

    private IShapeUIModel note(IDiagramUIModel diagram,String text,int x,int y,int w,int h,Color fill) {
        ITextBoxUIModel shape=(ITextBoxUIModel)app.getDiagramManager().createDiagramElement(diagram,IShapeTypeConstants.SHAPE_TYPE_TEXT_BOX);
        shape.setPlainText(text);
        shape.getElementFont().setValues("Arial",false,false,16,INK);
        shape.setHTMLText(html(text,16,false,fill));
        shape.setBounds(x,y,w,h); shape.setBackground(fill); shape.getFillColor().setColor1(fill,false);
        shape.getLineModel().setColor(fill,false);
        return shape;
    }
    private void heading(IDiagramUIModel diagram,String text,int x,int y,int width) {
        IShapeUIModel shape=note(diagram,text,x,y,width,30,Color.decode("#F1F5F9"));
        shape.getElementFont().setValues("Arial",true,false,16,INK);
        ((ITextBoxUIModel)shape).setHTMLText(html(text,16,true,Color.decode("#F1F5F9")));
    }
    private static String html(String text,int size,boolean bold,Color fill) {
        String escaped=text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        String background=String.format("#%02x%02x%02x",fill.getRed(),fill.getGreen(),fill.getBlue());
        return "<html><body style=\"margin:0;font-family:Arial;font-size:"+size+"pt;color:#20364F;background-color:"+background+";font-weight:"+(bold?"bold":"normal")+"\">"+escaped+"</body></html>";
    }
    private static int scaled(Element e,String name) { return (int)Math.round(Double.parseDouble(e.getAttribute(name))*SCALE); }
    private static boolean truth(Element e,String name) { return "true".equals(e.getAttribute(name)); }
    private static Element child(Element e,String name) { for(Element c:children(e,name))return c; throw new IllegalArgumentException(name); }
    private static List<Element> children(Element e,String name) {
        List<Element> result=new ArrayList<>();
        for(Node n=e.getFirstChild();n!=null;n=n.getNextSibling()) if(n instanceof Element && n.getNodeName().equals(name))result.add((Element)n);
        return result;
    }
}
