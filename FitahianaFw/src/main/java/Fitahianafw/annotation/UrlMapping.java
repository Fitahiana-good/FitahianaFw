package Fitahianafw.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import Fitahianafw.mapping.UrlHTTPMethod;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlMapping {
    String value();
    UrlHTTPMethod httpMethod() default UrlHTTPMethod.GET;
}
