package Fitahianafw;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import Fitahianafw.annotation.Controller;
import Fitahianafw.annotation.UrlMapping;
import Fitahianafw.err.ParameterBindingException;
import Fitahianafw.mapping.UrlHTTPMethod;
import Fitahianafw.mapping.UrlKey;
import Fitahianafw.mapping.UrlProcessor;
import Fitahianafw.servlet.FrontServletController;
import Fitahianafw.servlet.listener.FrontServletContextListener;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ParameterBindingTest {
    @Test
    void bindsAllPrimitivesAndString() throws Exception {
        Object[] values = (Object[]) execute("/primitives", Map.of(
                "b", "127", "s", "32000", "i", "42", "l", "9000000000", "f", "1.5",
                "d", "2.75", "ok", "true", "c", "é", "text", "Bonjour"));
        assertArrayEquals(new Object[] {(byte) 127, (short) 32000, 42, 9000000000L,
                1.5f, 2.75d, true, 'é', "Bonjour"}, values);
    }

    @Test
    void bindsWrappersAndPreservesEmptyString() throws Exception {
        assertArrayEquals(new Object[] {(byte) 1, (short) 2, 3, 4L, 5.5f, 6.5d, false, 'A', ""},
                (Object[]) execute("/wrappers", Map.of("b", "1", "s", "2", "i", "3", "l", "4",
                        "f", "5.5", "d", "6.5", "ok", "false", "c", "A", "text", "")));
    }

    @Test
    void missingWrappersAreNull() throws Exception {
        Object[] values = (Object[]) execute("/wrappers", Map.of());
        assertEquals(9, values.length);
        assertTrue(Arrays.stream(values).allMatch(value -> value == null));
        assertNull(execute("/optional", Map.of("number", "")));
    }

    @Test
    void usesJavaParameterNameFromCompilerConfiguration() throws Exception {
        assertEquals(21, execute("/named", Map.of("age", "21")));
    }

    @Test
    void rejectsMissingInvalidAndOverflowingPrimitiveValues() {
        for (Map<String, String> parameters : Arrays.asList(Map.<String, String>of(),
                Map.of("age", ""), Map.of("age", "abc"), Map.of("age", "2147483648"))) {
            assertThrows(ParameterBindingException.class, () -> execute("/named", parameters));
        }
        assertThrows(ParameterBindingException.class, () -> execute("/boolean", Map.of("ok", "yes")));
        assertThrows(ParameterBindingException.class, () -> execute("/character", Map.of("c", "AB")));
    }

    @Test
    void injectsHttpRequest() throws Exception {
        HttpServletRequest request = request(Map.of());
        assertSame(request, processor().executeRequest(new UrlKey("/request", UrlHTTPMethod.GET), request));
    }

    @Test
    void bindsObjectFieldsAndPrimitiveArgumentTogether() throws Exception {
        Object[] result = (Object[]) execute("/object", Map.of("nom", "Zoé", "age", "24",
                "actif", "true", "note", "15.5", "page", "2"));
        Person person = assertInstanceOf(Person.class, result[0]);
        assertEquals("Zoé", person.nom);
        assertEquals(24, person.age);
        assertTrue(person.actif);
        assertEquals(15.5, person.note);
        assertEquals(2, result[1]);
    }

    @Test
    void absentObjectFieldsKeepConstructorDefaultsAndEmptyWrapperBecomesNull() throws Exception {
        Object[] result = (Object[]) execute("/object", Map.of("page", "1", "note", ""));
        Person person = (Person) result[0];
        assertEquals("inconnu", person.nom);
        assertEquals(18, person.age);
        assertFalse(person.actif);
        assertNull(person.note);
    }

    @Test
    void rejectsInvalidObjectFields() {
        for (Map<String, String> values : Arrays.asList(Map.of("age", "abc"), Map.of("age", ""),
                Map.of("actif", "yes"), Map.of("note", "invalide"))) {
            assertThrows(ParameterBindingException.class, () -> execute("/object", values));
        }
    }

    @Test
    void bindsInheritedFieldsButDoesNotModifyStaticOrFinalFields() throws Exception {
        ChildPerson person = (ChildPerson) execute("/inherited", Map.of("nom", "Jean", "age", "30",
                "code", "7", "constant", "99", "shared", "99"));
        Person parent = person;
        assertEquals("Jean", parent.nom);
        assertEquals(30, parent.age);
        assertEquals(7, person.code);
        assertEquals(10, person.constant);
        assertEquals(20, ChildPerson.shared);
    }

    @Test
    void requiresNoArgumentConstructorForObjects() {
        assertThrows(NoSuchMethodException.class, () -> execute("/no-constructor", Map.of()));
    }

    @Test
    void servletBindsGetAndPostAndReturns400ForInvalidInput() throws Exception {
        UrlProcessor processor = processor();
        ServletContext context = (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {ServletContext.class}, (proxy, method, args) ->
                        method.getName().equals("getAttribute")
                                && FrontServletContextListener.URL_PROCESSOR_ATTR.equals(args[0]) ? processor : null);
        ServletConfig config = (ServletConfig) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {ServletConfig.class}, (proxy, method, args) ->
                        method.getName().equals("getServletContext") ? context : null);
        FrontServletController servlet = new FrontServletController();
        servlet.init(config);
        for (String verb : new String[] {"GET", "POST"}) {
            for (String value : new String[] {"21", "invalid"}) {
                StringWriter body = new StringWriter();
                int[] status = {200};
                HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                        getClass().getClassLoader(), new Class<?>[] {HttpServletResponse.class},
                        (proxy, method, args) -> {
                            if (method.getName().equals("getWriter")) return new PrintWriter(body);
                            if (method.getName().equals("setStatus")) status[0] = (int) args[0];
                            return null;
                        });
                servlet.service(request(Map.of("age", value), verb), response);
                assertEquals(value.equals("21") ? 200 : 400, status[0]);
                assertTrue(body.toString().contains(value.equals("21") ? "21" : "Paramètre invalide"));
            }
        }
    }

    private Object execute(String path, Map<String, String> parameters) throws Exception {
        return processor().executeRequest(new UrlKey(path, UrlHTTPMethod.GET), request(parameters));
    }

    private UrlProcessor processor() throws Exception {
        UrlProcessor processor = new UrlProcessor();
        processor.processAnnotatedClass(BindingController.class);
        return processor;
    }

    private HttpServletRequest request(Map<String, String> parameters) {
        return request(parameters, "GET");
    }

    private HttpServletRequest request(Map<String, String> parameters, String verb) {
        return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {HttpServletRequest.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParameter": return parameters.get(args[0]);
                        case "getMethod": return verb;
                        case "getRequestURI": return "/app/named";
                        case "getContextPath": return "/app";
                        case "getProtocol": return "HTTP/1.1";
                        default: return null;
                    }
                });
    }

    @Controller
    public static class BindingController {
        @UrlMapping("/primitives")
        public Object[] primitives(byte b, short s, int i,
                long l, float f, double d,
                boolean ok, char c, String text) {
            return new Object[] {b, s, i, l, f, d, ok, c, text};
        }

        @UrlMapping("/wrappers")
        public Object[] wrappers(Byte b, Short s, Integer i,
                Long l, Float f, Double d,
                Boolean ok, Character c, String text) {
            return new Object[] {b, s, i, l, f, d, ok, c, text};
        }

        @UrlMapping("/optional")
        public Integer optional(Integer number) { return number; }

        @UrlMapping("/named")
        public int named(int age) { return age; }

        @UrlMapping(value = "/named", httpMethod = UrlHTTPMethod.POST)
        public int namedPost(int age) { return age; }

        @UrlMapping("/boolean")
        public boolean bool(boolean ok) { return ok; }

        @UrlMapping("/character")
        public char character(char c) { return c; }

        @UrlMapping("/request")
        public HttpServletRequest request(HttpServletRequest request) { return request; }

        @UrlMapping("/object")
        public Object[] object(Person person, int page) { return new Object[] {person, page}; }

        @UrlMapping("/inherited")
        public ChildPerson inherited(ChildPerson person) { return person; }

        @UrlMapping("/no-constructor")
        public NoDefaultConstructor noConstructor(NoDefaultConstructor person) { return person; }
    }

    public static class Person {
        private String nom = "inconnu";
        private int age = 18;
        private boolean actif;
        private Double note = 10.0;
    }

    public static class ChildPerson extends Person {
        private int code;
        private final int constant = 10;
        private static int shared = 20;
    }

    public static class NoDefaultConstructor {
        public NoDefaultConstructor(String nom) {
        }
    }
}
