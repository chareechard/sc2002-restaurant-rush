import java.io.File;
import java.lang.reflect.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public final class VerifyPreviewMembers {
    static String shortType(Type type) {
        return type.getTypeName().replaceAll("(?:[a-zA-Z_$][\\w$]*\\.)+", "").replace(" ", "");
    }
    static String visibility(int modifiers) {
        return Modifier.isPublic(modifiers) ? "public" : Modifier.isPrivate(modifiers) ? "private"
            : Modifier.isProtected(modifiers) ? "protected" : "package";
    }
    static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
    static boolean flag(Element element, String name) { return element.getAttribute(name).equals("true"); }
    static boolean matches(Executable method, Element element) {
        NodeList parameters=element.getElementsByTagName("parameter");
        Type[] actual=method.getGenericParameterTypes();
        if (actual.length!=parameters.getLength()) return false;
        for(int i=0;i<actual.length;i++) {
            if(!shortType(actual[i]).equals(((Element)parameters.item(i)).getAttribute("type"))) return false;
        }
        return true;
    }
    public static void main(String[] args) throws Exception {
        NodeList types=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(args[0])).getElementsByTagName("class");
        int fields=0,operations=0,literals=0;
        for(int i=0;i<types.getLength();i++) {
            Element c=(Element)types.item(i);
            Class<?> actual=Class.forName(c.getAttribute("package")+"."+c.getAttribute("name"));
            check(actual.isInterface()==c.getAttribute("kind").equals("interface"),actual+" interface");
            check(actual.isEnum()==c.getAttribute("kind").equals("enum"),actual+" enum");
            for(Node node=c.getFirstChild();node!=null;node=node.getNextSibling()) {
                if(!(node instanceof Element)) continue;
                Element member=(Element)node;
                String name=member.getAttribute("name");
                if(member.getTagName().equals("attribute")) {
                    Field f=actual.getDeclaredField(name);int m=f.getModifiers();
                    check(shortType(f.getGenericType()).equals(member.getAttribute("type")),actual+"."+name+" type");
                    check(Modifier.isStatic(m)==flag(member,"static"),actual+"."+name+" static");
                    check(Modifier.isFinal(m)==flag(member,"readonly"),actual+"."+name+" final");
                    check(visibility(m).equals(member.getAttribute("visibility")),actual+"."+name+" visibility");
                    fields++;
                } else if(member.getTagName().equals("operation")) {
                    Executable match=null;
                    if(flag(member,"constructor")) {
                        for(Constructor<?> m:actual.getDeclaredConstructors()) if(matches(m,member)) match=m;
                    } else {
                        for(Method m:actual.getDeclaredMethods()) if(m.getName().equals(name)&&matches(m,member)) match=m;
                    }
                    check(match!=null,actual+"."+name+" parameters");
                    int m=match.getModifiers();
                    check(visibility(m).equals(member.getAttribute("visibility")),actual+"."+name+" visibility");
                    check(Modifier.isStatic(m)==flag(member,"static"),actual+"."+name+" static");
                    check(Modifier.isAbstract(m)==flag(member,"abstract"),actual+"."+name+" abstract");
                    if(match instanceof Method) check(shortType(((Method)match).getGenericReturnType()).equals(member.getAttribute("returns")),actual+"."+name+" return");
                    operations++;
                } else if(member.getTagName().equals("literal")) {
                    check(actual.getDeclaredField(name).isEnumConstant(),actual+"."+name+" enum literal");literals++;
                }
            }
        }
        System.out.printf("PASS: %d types, %d selected fields, %d selected operations (including constructors), %d enum values.%n",types.getLength(),fields,operations,literals);
    }
}
