package Fitahianafw;

import Fitahianafw.annotation.Controller;
import Fitahianafw.mapping.UrlHTTPMethod;
import Fitahianafw.mapping.UrlKey;
import Fitahianafw.mapping.UrlProcessor;
import Fitahianafw.util.ScanUtil;
import junit.framework.TestCase;

public class FrameworkTest extends TestCase {

    public void testScanUtilFindsAnnotatedControllers() throws Exception {
        UrlProcessor processor = new UrlProcessor();

        ScanUtil.fillUrlProcessor("Fitahianafw", processor);

        assertTrue(processor.getControllerClasses().stream()
                .anyMatch(clazz -> clazz.isAnnotationPresent(Controller.class)));
    }

    public void testUrlProcessorExecutesMappedMethod() throws Exception {
        UrlProcessor processor = new UrlProcessor();
        ScanUtil.fillUrlProcessor("Fitahianafw", processor);

        SampleController.lastMessage = null;
        processor.executeRequest(new UrlKey("/hello", UrlHTTPMethod.GET));

        assertEquals("bonjour depuis FitahianaFw", SampleController.lastMessage);
    }
}
