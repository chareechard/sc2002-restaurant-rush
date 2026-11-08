import java.io.File;
import java.lang.reflect.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Read-only comparison of selected UML members with the compiled branch code. */
public class VerifyMembers {
    static String shortType(Type t) {
        return t.getTypeName().replaceAll("(?:[a-zA-Z_$][\\w$]*\\.)+", "").replace(" ", "");
    }
    static boolean truth(Element e,String key) { return e.getAttribute(key).equals("true"); }
    static void require(boolean ok,String label) { if(!ok) throw new AssertionError(label); }
    public static void main(String[] args) throws Exception {
        NodeList classes=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(args[0])).getElementsByTagName("class");
        int fields=0,methods=0,literals=0;
        for(int i=0;i<classes.getLength();i++) {
            Element c=(Element)classes.item(i);
            Class<?> actual=Class.forName(c.getAttribute("package")+"."+c.getAttribute("name"));
            require(Modifier.isAbstract(actual.getModifiers())==c.getAttribute("kind").equals("abstract class"),actual+" abstract");
            for(Node n=c.getFirstChild();n!=null;n=n.getNextSibling()) {
                if(!(n instanceof Element)) continue;
                Element member=(Element)n;String name=member.getAttribute("name");
                if(member.getTagName().equals("attribute")) {
                    Field f=actual.getDeclaredField(name);int m=f.getModifiers();
                    require(shortType(f.getGenericType()).equals(member.getAttribute("type")),actual+"."+name+" field type");
                    require(Modifier.isStatic(m)==truth(member,"static"),actual+"."+name+" static");
                    require(Modifier.isFinal(m)==truth(member,"readonly"),actual+"."+name+" readonly");
                    require((Modifier.isPublic(m)?"public":Modifier.isPrivate(m)?"private":"protected").equals(member.getAttribute("visibility")),actual+"."+name+" visibility");
                    fields++;
                } else if(member.getTagName().equals("operation")) {
                    NodeList params=member.getElementsByTagName("parameter");
                    Method match=null;
                    for(Method method:actual.getDeclaredMethods()) {
                        if(!method.getName().equals(name)||method.getParameterCount()!=params.getLength())continue;
                        Type[] types=method.getGenericParameterTypes();boolean same=true;
                        for(int p=0;p<types.length;p++) if(!shortType(types[p]).equals(((Element)params.item(p)).getAttribute("type")))same=false;
                        if(same)match=method;
                    }
                    require(match!=null,actual+"."+name+" signature");
                    require(shortType(match.getGenericReturnType()).equals(member.getAttribute("returns")),actual+"."+name+" return type");
                    require(Modifier.isStatic(match.getModifiers())==truth(member,"static"),actual+"."+name+" static");
                    require(Modifier.isAbstract(match.getModifiers())==truth(member,"abstract"),actual+"."+name+" abstract");
                    require(Modifier.isPublic(match.getModifiers()),actual+"."+name+" visibility");
                    methods++;
                } else if(member.getTagName().equals("literal")) {
                    require(actual.getDeclaredField(name).isEnumConstant(),actual+"."+name+" enum literal");literals++;
                }
            }
        }
        System.out.printf("Verified %d types, %d selected fields, %d selected methods, %d enum literals against compiled Stage 1 code.%n",classes.getLength(),fields,methods,literals);
    }
}
