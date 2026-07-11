package Fitahianafw.servlet.listener;

import Fitahianafw.annotation.Controller;
import Fitahianafw.mapping.UrlProcessor;
import Fitahianafw.util.ClasspathScanner;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class FrontServletContextListener implements ServletContextListener {
    public static final String URL_PROCESSOR_ATTR = "urlProcessor";
    public static final String CONTROLLER_PACKAGE = "CONTROLLER_PACKAGE";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String controllerPackage = context.getInitParameter(CONTROLLER_PACKAGE);
        if (controllerPackage == null || controllerPackage.isBlank()) {
            controllerPackage = "Fitahianafw";
        }

        UrlProcessor urlProcessor = new UrlProcessor();
        try {
            for (Class<?> controllerClass : ClasspathScanner.getClassesAnnotatedWith(
                    Controller.class, controllerPackage)) {
                urlProcessor.processAnnotatedClass(controllerClass);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation du UrlProcessor", e);
        }

        context.setAttribute(URL_PROCESSOR_ATTR, urlProcessor);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
