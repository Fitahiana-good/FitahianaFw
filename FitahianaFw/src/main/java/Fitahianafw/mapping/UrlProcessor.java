package Fitahianafw.mapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Fitahianafw.annotation.Controller;
import Fitahianafw.annotation.UrlMapping;
import Fitahianafw.err.UrlAlreadyDefinedException;
import Fitahianafw.err.UrlNotSupportedException;
import Fitahianafw.util.interfaces.AnnotatedClassesProcessor;

public class UrlProcessor implements AnnotatedClassesProcessor {
    private final List<Class<?>> controllerClasses = new ArrayList<>();
    private final HashMap<UrlKey, UrlControllerMap> urlMappings = new HashMap<>();

    @Override
    public void processAnnotatedClass(Class<?> clazz) throws ReflectiveOperationException, UrlAlreadyDefinedException {
        if (!clazz.isAnnotationPresent(Controller.class)) {
            return;
        }

        controllerClasses.add(clazz);
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(UrlMapping.class)) {
                continue;
            }

            UrlMapping mapping = method.getAnnotation(UrlMapping.class);
            method.setAccessible(true);
            UrlKey key = new UrlKey(mapping.value(), mapping.httpMethod());
            if (urlMappings.containsKey(key)) {
                throw new UrlAlreadyDefinedException(key, urlMappings.get(key));
            }
            urlMappings.put(key, new UrlControllerMap(method, clazz));
        }
    }

    public Object executeRequest(UrlKey url) throws UrlNotSupportedException, ReflectiveOperationException {
        if (!urlMappings.containsKey(url)) {
            throw new UrlNotSupportedException(url, urlMappings);
        }
        UrlControllerMap mapping = urlMappings.get(url);
        return mapping.getReflectMethod().invoke(mapping.getPrototypeSeed());
    }

    public List<Class<?>> getControllerClasses() {
        return controllerClasses;
    }

    public HashMap<UrlKey, UrlControllerMap> getUrlMappings() {
        return urlMappings;
    }
}
