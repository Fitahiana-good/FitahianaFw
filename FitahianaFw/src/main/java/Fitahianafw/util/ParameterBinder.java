package Fitahianafw.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import Fitahianafw.err.ParameterBindingException;
import jakarta.servlet.http.HttpServletRequest;

public final class ParameterBinder {
    private ParameterBinder() {
    }

    public static Object[] resolveArguments(Method method, HttpServletRequest request)
            throws ReflectiveOperationException {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            if (type == HttpServletRequest.class) {
                arguments[i] = request;
                continue;
            }
            if (!isSimpleType(type)) {
                arguments[i] = fillObject(type, request);
                continue;
            }
            if (!parameter.isNamePresent()) {
                throw new IllegalArgumentException("Configurer <parameters>true</parameters> dans le pom.xml du contrôleur : "
                        + method.getName());
            }
            String name = parameter.getName();
            arguments[i] = convert(request == null ? null : request.getParameter(name), type, name);
        }
        return arguments;
    }

    private static Object fillObject(Class<?> type, HttpServletRequest request)
            throws ReflectiveOperationException {
        // Comme dans NathaFw, l'objet doit avoir un constructeur sans argument.
        Object instance = type.getDeclaredConstructor().newInstance();
        for (Class<?> current = type; current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                // Les noms HTTP correspondent directement aux noms des champs : nom, age, etc.
                String value = request == null ? null : request.getParameter(field.getName());
                if (value == null) {
                    continue;
                }
                if (!isSimpleType(field.getType())) {
                    throw new IllegalArgumentException("Type de champ non supporté : "
                            + type.getName() + "." + field.getName());
                }
                Object converted = convert(value, field.getType(), field.getName());
                field.setAccessible(true);
                field.set(instance, converted);
            }
        }
        return instance;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class || type == Byte.class || type == Short.class
                || type == Integer.class || type == Long.class || type == Float.class || type == Double.class
                || type == Boolean.class || type == Character.class;
    }

    private static Object convert(String value, Class<?> type, String name) {
        if (value == null || (value.isEmpty() && type != String.class)) {
            if (type.isPrimitive()) {
                throw new ParameterBindingException("Paramètre obligatoire manquant : " + name);
            }
            return null;
        }
        try {
            if (type == String.class) return value;
            if (type == byte.class || type == Byte.class) return Byte.parseByte(value);
            if (type == short.class || type == Short.class) return Short.parseShort(value);
            if (type == int.class || type == Integer.class) return Integer.parseInt(value);
            if (type == long.class || type == Long.class) return Long.parseLong(value);
            if (type == float.class || type == Float.class) return Float.parseFloat(value);
            if (type == double.class || type == Double.class) return Double.parseDouble(value);
            if (type == boolean.class || type == Boolean.class) {
                if ("true".equalsIgnoreCase(value)) return true;
                if ("false".equalsIgnoreCase(value)) return false;
                throw new IllegalArgumentException("Booléen attendu : true ou false");
            }
            if (type == char.class || type == Character.class) {
                if (value.length() == 1) return value.charAt(0);
                throw new IllegalArgumentException("Un seul caractère attendu");
            }
        } catch (IllegalArgumentException e) {
            throw new ParameterBindingException("Valeur invalide pour le paramètre " + name
                    + " (" + type.getSimpleName() + ")", e);
        }
        throw new IllegalArgumentException("Type de conversion non supporté : " + type.getName());
    }
}
