package Fitahianafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import Fitahianafw.annotation.Controller;
import Fitahianafw.annotation.UrlMapping;
import Fitahianafw.err.UrlAlreadyDefinedException;
import Fitahianafw.err.UrlNotSupportedException;
import Fitahianafw.mapping.UrlHTTPMethod;
import Fitahianafw.mapping.UrlKey;
import Fitahianafw.mapping.UrlProcessor;
import Fitahianafw.model.ModelView;
import Fitahianafw.util.ClasspathScanner;

class FrameworkTest {

    @Test
    void classpathScannerFindsAnnotatedControllers() throws Exception {
        List<Class<?>> controllers = ClasspathScanner.getClassesAnnotatedWith(Controller.class, "Fitahianafw");

        assertTrue(controllers.contains(SampleController.class));
    }

    @Test
    void detectsAnnotatedMethod() throws Exception {
        UrlProcessor processor = new UrlProcessor();
        processor.processAnnotatedClass(SampleController.class);

        assertTrue(processor.getUrlMappings().containsKey(new UrlKey("/hello", UrlHTTPMethod.GET)));
    }

    @Test
    void executesExistingMappedMethod() throws Exception {
        UrlProcessor processor = new UrlProcessor();
        processor.processAnnotatedClass(SampleController.class);
        SampleController.lastMessage = null;

        Object result = processor.executeRequest(new UrlKey("/hello", UrlHTTPMethod.GET));

        assertEquals("bonjour depuis FitahianaFw", SampleController.lastMessage);
        assertNull(result);
    }

    @Test
    void executesMethodReturningString() throws Exception {
        UrlProcessor processor = processorWith(StringController.class);

        Object result = processor.executeRequest(new UrlKey("/string", UrlHTTPMethod.GET));

        assertEquals("réponse texte", result);
    }

    @Test
    void executesMethodReturningModelViewWithData() throws Exception {
        UrlProcessor processor = processorWith(ModelViewController.class);

        Object result = processor.executeRequest(new UrlKey("/model-view", UrlHTTPMethod.GET));

        ModelView modelView = assertInstanceOf(ModelView.class, result);
        assertEquals("/test", modelView.getViewName());
        assertEquals("Bonjour", modelView.getData().get("message"));
        assertNotNull(modelView.getData());
    }

    @Test
    void modelViewProtectsAgainstNullData() {
        ModelView modelView = new ModelView("/test", null);
        modelView.addObject("framework", "FitahianaFw");
        modelView.setData(null);

        assertNotNull(modelView.getData());
        assertTrue(modelView.getData().isEmpty());
    }

    @Test
    void rejectsUnknownRoute() throws Exception {
        UrlProcessor processor = processorWith(StringController.class);

        assertThrows(UrlNotSupportedException.class,
                () -> processor.executeRequest(new UrlKey("/absente", UrlHTTPMethod.GET)));
    }

    @Test
    void rejectsDuplicateRoute() throws Exception {
        UrlProcessor processor = processorWith(DuplicateControllerA.class);

        assertThrows(UrlAlreadyDefinedException.class,
                () -> processor.processAnnotatedClass(DuplicateControllerB.class));
    }

    @Test
    void distinguishesGetAndPostForSamePath() throws Exception {
        UrlProcessor processor = processorWith(HttpMethodController.class);

        assertEquals("GET", processor.executeRequest(new UrlKey("/method", UrlHTTPMethod.GET)));
        assertEquals("POST", processor.executeRequest(new UrlKey("/method", UrlHTTPMethod.POST)));
    }

    private UrlProcessor processorWith(Class<?> controllerClass) throws Exception {
        UrlProcessor processor = new UrlProcessor();
        processor.processAnnotatedClass(controllerClass);
        return processor;
    }

    @Controller
    public static class StringController {
        @UrlMapping("/string")
        public String value() {
            return "réponse texte";
        }
    }

    @Controller
    public static class ModelViewController {
        @UrlMapping("/model-view")
        private ModelView value() {
            return new ModelView("/test").addObject("message", "Bonjour");
        }
    }

    @Controller
    public static class DuplicateControllerA {
        @UrlMapping("/duplicate")
        public void first() {
        }
    }

    @Controller
    public static class DuplicateControllerB {
        @UrlMapping("/duplicate")
        public void second() {
        }
    }

    @Controller
    public static class HttpMethodController {
        @UrlMapping(value = "/method", httpMethod = UrlHTTPMethod.GET)
        public String get() {
            return "GET";
        }

        @UrlMapping(value = "/method", httpMethod = UrlHTTPMethod.POST)
        public String post() {
            return "POST";
        }
    }
}
