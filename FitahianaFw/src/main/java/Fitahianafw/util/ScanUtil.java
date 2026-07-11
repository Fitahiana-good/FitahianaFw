package Fitahianafw.util;

import Fitahianafw.annotation.Controller;
import Fitahianafw.mapping.UrlProcessor;
import Fitahianafw.util.interfaces.AnnotatedClassesProcessor;

public class ScanUtil {
    @Deprecated
    public static void handleAnnotatedClasses(String packageName, AnnotatedClassesProcessor processor) throws Exception {
        for (Class<?> clazz : ClasspathScanner.getClassesAnnotatedWith(Controller.class, packageName)) {
            processor.processAnnotatedClass(clazz);
        }
    }

    public static void fillUrlProcessor(String packageName, UrlProcessor urlProcessor) throws Exception {
        handleAnnotatedClasses(packageName, urlProcessor);
    }
}
