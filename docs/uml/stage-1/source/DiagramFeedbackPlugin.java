package rush.vp;

import com.vp.plugin.*;
import com.vp.plugin.diagram.*;
import com.vp.plugin.diagram.connector.IAssociationUIModel;
import com.vp.plugin.diagram.shape.IClassUIModel;
import com.vp.plugin.diagram.shape.IPolygonUIModel;
import com.vp.plugin.diagram.shape.ITextBoxUIModel;
import com.vp.plugin.model.*;
import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.nio.file.*;
import java.util.*;

/** Applies presentation feedback to an existing project without changing its Java model. */
public final class DiagramFeedbackPlugin implements VPPlugin, VPPluginCommandLineSupport {
    private static final Color INK = Color.decode("#20364F");
    private final ApplicationManager app = ApplicationManager.instance();

    public void loaded(VPPluginInfo info) { }
    public void unloaded() { }

    public void invoke(String[] args) {
        try {
            Path output = Paths.get(System.getProperty("rush.vp.output"));
            Files.createDirectories(output);
            ProjectManager pm = app.getProjectManager();
            IClassDiagramUIModel diagram = (IClassDiagramUIModel) pm.getProject().toDiagramArray()[0];
            if (Boolean.getBoolean("rush.vp.removeOnly")) {
                int removed=0;
                for (IDiagramElement element:diagram.toDiagramElementArray()) {
                    if (!(element instanceof ITextBoxUIModel)) continue;
                    ITextBoxUIModel text=(ITextBoxUIModel)element;
                    String value=text.getPlainText();
                    if (value.startsWith("Selected members")) {
                        text.deleteViewOnly();
                        removed++;
                    } else if (value.startsWith("Stage 1: white") || value.startsWith("Stage 2 additions:")
                            || value.startsWith("Stage 3 additions:") || value.startsWith("Later stages:")) {
                        text.setY(1826);
                    }
                }
                if (removed!=1) throw new IllegalStateException("Expected exactly one explanatory note");
                saveAndExport(output);
                System.exit(0);
                return;
            }
            diagram.setPaintConnectorThroughLabel(IDiagramUIModel.PAINT_CONNECTOR_THROUGH_LABEL_YES);
            Map<String,IClassUIModel> shapes = new LinkedHashMap<>();
            List<IConnectorUIModel> connectors = new ArrayList<>();
            for (IDiagramElement element : diagram.toDiagramElementArray()) {
                if (element instanceof IClassUIModel) {
                    shapes.put(element.getModelElement().getName(), (IClassUIModel) element);
                } else if (element instanceof IConnectorUIModel) {
                    connectors.add((IConnectorUIModel) element);
                } else if (element instanceof ITextBoxUIModel) {
                    ITextBoxUIModel text = (ITextBoxUIModel) element;
                    String value = text.getPlainText();
                    if (value.startsWith("Wesley review |") || value.startsWith("Solid line:")
                            || value.startsWith("Selected members.") || value.startsWith("Stage 1: white")
                            || value.startsWith("Stage 2 additions:") || value.startsWith("Stage 3 additions:")
                            || value.startsWith("Later stages:")) {
                        text.deleteViewOnly();
                    }
                }
            }
            if (shapes.size() != 21 || connectors.size() != 19) throw new IllegalStateException("Unexpected starting diagram");

            // Retain the operation and all three parameter types in the model.
            // Hide parameter names and wrap the member in a narrower class box.
            IClassUIModel cashier = shapes.get("Cashier");
            cashier.setShowParameterNameInOperationSignature(false);
            cashier.setWrapMembers(true);
            cashier.setBounds(430,1000,360,110);
            cashier.setRequestFitSize(false);
            cashier.setRequestDefaultSize(false);

            for (IConnectorUIModel connector : connectors) {
                connector.setPaintThroughLabel(IDiagramUIModel.PAINT_CONNECTOR_THROUGH_LABEL_YES);
                String from = connector.getFromShape().getModelElement().getName();
                String to = connector.getToShape().getModelElement().getName();
                String key = from + ">" + to;
                if (key.equals("GameEngine>Order")) {
                    route(connector,730,169,730,105,1745,105,1745,169);
                } else if (key.equals("GameEngine>Customer")) {
                    route(connector,1031,230,1149,230);
                } else if (key.equals("GameEngine>Cashier")) {
                    route(connector,1031,300,1115,300,1115,1048,791,1048);
                } else if (key.equals("GameEngine>Table")) {
                    route(connector,1031,370,1135,370,1135,910,1200,910,1200,939);
                } else if (key.equals("GameEngine>Receipt")) {
                    route(connector,1031,440,1095,440,1095,1266,1149,1266);
                } else if (key.equals("GameEngine>Menu")) {
                    route(connector,600,519,600,645,1777,645,1777,649);
                } else if (key.equals("GameEngine>Chef")) {
                    route(connector,900,519,900,635,1910,635,1910,999);
                } else if (key.equals("GameEngine>Scenario")) {
                    route(connector,429,320,365,320,365,788,292,788);
                } else if (key.equals("GameEngine>Waiter")) {
                    route(connector,429,460,395,460,395,788,429,788);
                } else if (key.equals("Cashier>Customer")) {
                    route(connector,791,1080,1125,1080,1125,344,1149,344);
                }

                if (!(connector instanceof IAssociationUIModel)) continue;
                IAssociationUIModel association = (IAssociationUIModel) connector;
                // Short, deliberately positioned label rectangles stay clear of lines.
                int[] labels = null;
                if (key.equals("ConsoleApp>GameEngine")) labels = new int[]{278,257,397,257};
                if (key.equals("ConsoleApp>ConsoleView")) labels = new int[]{173,406,173,473};
                if (key.equals("GameEngine>Scenario")) labels = new int[]{398,294,302,762};
                if (key.equals("GameEngine>Waiter")) labels = new int[]{398,434,400,762};
                if (key.equals("GameEngine>Menu")) labels = new int[]{612,530,1792,619};
                if (key.equals("GameEngine>Chef")) labels = new int[]{912,530,1922,971};
                if (key.equals("GameEngine>Cashier")) labels = new int[]{1042,274,804,1022};
                if (key.equals("GameEngine>Table")) labels = new int[]{1042,344,1212,914};
                if (key.equals("GameEngine>Customer")) labels = new int[]{1042,204,1100,204};
                if (key.equals("GameEngine>Order")) labels = new int[]{742,140,1757,140};
                if (key.equals("GameEngine>Receipt")) labels = new int[]{1042,414,1107,1240};
                if (key.equals("Customer>Order")) labels = new int[]{1596,273,1609,306};
                if (key.equals("Table>Customer")) labels = new int[]{1404,977,1463,530};
                if (key.equals("Menu>MenuItem")) labels = new int[]{1920,672,2004,706};
                if (key.equals("Order>MenuItem")) labels = new int[]{1852,273,2222,681};
                if (labels != null) {
                    association.setMultiplicityARectangle(labels[0],labels[1],38,18);
                    association.setMultiplicityBRectangle(labels[2],labels[3],38,18);
                }
                connector.setRequestResetCaption(false);
                connector.setRequestResetCaptionSize(false);
            }

            // Draw graphical examples as presentation-only shapes, not extra UML relations.
            int y = 1725;
            line(diagram,60,y,84);
            note(diagram,"Association (reference)",160,y-15,420,34);
            polygon(diagram,new int[]{620,y,632,y-8,644,y,632,y+8},INK);
            line(diagram,644,y,62);
            note(diagram,"Composition",720,y-15,410,34);
            for (int i=0;i<6;i++) line(diagram,1180+i*12,y,7);
            polygon(diagram,new int[]{1250,y-6,1258,y,1250,y+6},INK);
            note(diagram,"Dependency (uses)",1280,y-15,420,34);
            line(diagram,1740,y,68);
            polygon(diagram,new int[]{1808,y-9,1826,y,1808,y+9},Color.WHITE);
            note(diagram,"Inheritance",1845,y-15,440,34);
            ITextBoxUIModel staticNote = note(diagram,"Underlined = static (class member). Italic = abstract.",60,1770,2200,34);
            staticNote.setHTMLText(html("<u>Underlined</u> = static (class member). <i>Italic</i> = abstract.",16,Color.WHITE));
            colorNote(diagram,"Stage 1: white (baseline)",60,1826,480,Color.WHITE);
            colorNote(diagram,"Stage 2 additions: blue",560,1826,480,Color.decode("#D8EAFE"));
            colorNote(diagram,"Stage 3 additions: green",1060,1826,480,Color.decode("#DDF1DF"));
            colorNote(diagram,"Later stages: orange border = modified class",1560,1826,750,Color.WHITE);

            saveAndExport(output);
            System.exit(0);
        } catch (Throwable error) {
            error.printStackTrace();
            System.exit(1);
        }
    }

    private void saveAndExport(Path output) throws Exception {
            ProjectManager pm=app.getProjectManager();
            File file = output.resolve("Restaurant_Rush_Stage1_Wesley_Draft.vpp").toFile();
            if (!pm.saveProjectAs(file)) throw new IllegalStateException("Save failed");
            if (!pm.openProject(file)) throw new IllegalStateException("Reopen failed");
            IDiagramUIModel saved = pm.getProject().toDiagramArray()[0];
            int classCount=0, relationCount=0;
            for (IDiagramElement e:saved.toDiagramElementArray()) {
                if (e instanceof IClassUIModel) classCount++;
                if (e instanceof IConnectorUIModel) relationCount++;
            }
            if (classCount!=21 || relationCount!=19) throw new IllegalStateException("Reopened counts changed");
            ModelConvertionManager convert=app.getModelConvertionManager();
            ExportXMLOption xml=ExportXMLOption.toExportProject();
            xml.setSimpleStructure(true); xml.setExportImages(false);
            convert.exportXML(output.resolve("model-xml").toFile(),xml);
            for (String format:new String[]{"png","svg"}) {
                int type=format.equals("png")?ExportDiagramAsImageOption.IMAGE_TYPE_PNG:ExportDiagramAsImageOption.IMAGE_TYPE_SVG;
                convert.exportDiagramAsImage(saved,output.resolve("Restaurant_Rush_Stage1_Wesley_Draft."+format).toFile(),new ExportDiagramAsImageOption(type));
            }
            if (!pm.saveProject()) throw new IllegalStateException("Final save failed");
            convert.exportXML(output.resolve("model-xml").toFile(),xml);
            System.out.println("VP_NATIVE_FEEDBACK_OK classes="+classCount+" relationships="+relationCount+" version="+app.getProductInfo().getVersion());
    }

    private void route(IConnectorUIModel connector,int... coordinates) {
        int left=Integer.MAX_VALUE,top=Integer.MAX_VALUE,right=Integer.MIN_VALUE,bottom=Integer.MIN_VALUE;
        for (int i=0;i<coordinates.length;i+=2) {
            left=Math.min(left,coordinates[i]);top=Math.min(top,coordinates[i+1]);
            right=Math.max(right,coordinates[i]);bottom=Math.max(bottom,coordinates[i+1]);
        }
        // The exporter clips a connector to its stored bounds, including labels.
        connector.setBounds(left-60,top-60,right-left+120,bottom-top+120);
        connector.clearPoints();
        for (int i=0;i<coordinates.length;i+=2) connector.addPoint(new Point(coordinates[i],coordinates[i+1]));
        connector.setRequestRebuild(false);
        connector.setRequestRebuildFromEnd(false);
        connector.setRequestRebuildToEnd(false);
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
