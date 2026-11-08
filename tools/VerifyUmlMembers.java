import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Compares a selected UML descriptor with compiled project classes. */
public final class VerifyUmlMembers {
    private VerifyUmlMembers() { }

    private static String shortType(Type type) {
        return type.getTypeName().replaceAll("(?:[a-zA-Z_$][\\w$]*\\.)+", "").replace(" ", "");
    }

    private static String visibility(int modifiers) {
        return Modifier.isPublic(modifiers) ? "public" : Modifier.isPrivate(modifiers) ? "private"
            : Modifier.isProtected(modifiers) ? "protected" : "package";
    }

    private static boolean flag(Element element, String name) {
        return element.getAttribute(name).equals("true");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }

    private static boolean matches(Executable method, Element element) {
        NodeList parameters = element.getElementsByTagName("parameter");
        Type[] actual = method.getGenericParameterTypes();
        if (actual.length != parameters.getLength()) return false;
        for (int i = 0; i < actual.length; i++) {
            if (!shortType(actual[i]).equals(((Element) parameters.item(i)).getAttribute("type"))) return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        NodeList types = factory.newDocumentBuilder().parse(new File(args[0])).getElementsByTagName("class");
        int fields = 0;
        int operations = 0;
        int literals = 0;
        for (int i = 0; i < types.getLength(); i++) {
            Element type = (Element) types.item(i);
            Class<?> actual = Class.forName(type.getAttribute("package") + "." + type.getAttribute("name"));
            String kind = type.getAttribute("kind");
            check(actual.isInterface() == kind.equals("interface"), actual + " interface");
            check(actual.isEnum() == kind.equals("enum"), actual + " enum");
            if (!actual.isInterface() && !actual.isEnum()) {
                check(Modifier.isAbstract(actual.getModifiers()) == kind.equals("abstract class"), actual + " abstract");
            }
            for (Node node = type.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (!(node instanceof Element)) continue;
                Element member = (Element) node;
                String name = member.getAttribute("name");
                String label = actual.getName() + "." + name;
                if (member.getTagName().equals("attribute")) {
                    Field field = actual.getDeclaredField(name);
                    int modifiers = field.getModifiers();
                    check(shortType(field.getGenericType()).equals(member.getAttribute("type")), label + " type");
                    check(Modifier.isStatic(modifiers) == flag(member, "static"), label + " static");
                    check(Modifier.isFinal(modifiers) == flag(member, "readonly"), label + " final");
                    check(visibility(modifiers).equals(member.getAttribute("visibility")), label + " visibility");
                    fields++;
                } else if (member.getTagName().equals("operation")) {
                    Executable match = null;
                    if (flag(member, "constructor")) {
                        for (Constructor<?> method : actual.getDeclaredConstructors()) {
                            if (matches(method, member)) match = method;
                        }
                    } else {
                        for (Method method : actual.getDeclaredMethods()) {
                            if (method.getName().equals(name) && matches(method, member)) match = method;
                        }
                    }
                    check(match != null, label + " signature");
                    int modifiers = match.getModifiers();
                    check(visibility(modifiers).equals(member.getAttribute("visibility")), label + " visibility");
                    check(Modifier.isStatic(modifiers) == flag(member, "static"), label + " static");
                    check(Modifier.isAbstract(modifiers) == flag(member, "abstract"), label + " abstract");
                    if (match instanceof Method) {
                        check(shortType(((Method) match).getGenericReturnType()).equals(member.getAttribute("returns")), label + " return");
                    }
                    operations++;
                } else if (member.getTagName().equals("literal")) {
                    check(actual.getDeclaredField(name).isEnumConstant(), label + " enum literal");
                    literals++;
                }
            }
        }
        System.out.printf("{\"types\":%d,\"fields\":%d,\"operations\":%d,\"enum_literals\":%d}%n",
            types.getLength(), fields, operations, literals);
    }
}
