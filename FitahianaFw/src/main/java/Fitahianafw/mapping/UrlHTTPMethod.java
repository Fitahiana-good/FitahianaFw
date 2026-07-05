package Fitahianafw.mapping;

public enum UrlHTTPMethod {
    GET,
    POST;

    public static UrlHTTPMethod buildUrlHTTPMethod(String method) {
        if ("GET".equalsIgnoreCase(method)) {
            return GET;
        }
        if ("POST".equalsIgnoreCase(method)) {
            return POST;
        }
        return GET;
    }
}
