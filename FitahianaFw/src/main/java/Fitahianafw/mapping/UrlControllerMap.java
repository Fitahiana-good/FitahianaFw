package Fitahianafw.mapping;

import java.lang.reflect.Method;
import java.util.Objects;

public class UrlControllerMap {
    private final Method reflectMethod;
    private final Class<?> controllerClass;

    public UrlControllerMap(Method reflectMethod, Class<?> controllerClass) {
        this.reflectMethod = reflectMethod;
        this.controllerClass = controllerClass;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getReflectMethod() {
        return reflectMethod;
    }

    public Object getPrototypeSeed() throws ReflectiveOperationException {
        return controllerClass.getDeclaredConstructor().newInstance();
    }

    @Override
    public String toString() {
        return "UrlControllerMap{" + "method=" + reflectMethod + ", controllerClass=" + controllerClass + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UrlControllerMap other)) {
            return false;
        }
        return Objects.equals(reflectMethod, other.reflectMethod)
                && Objects.equals(controllerClass, other.controllerClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reflectMethod, controllerClass);
    }
}
