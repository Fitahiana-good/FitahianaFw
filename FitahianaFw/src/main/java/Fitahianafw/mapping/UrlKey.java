package Fitahianafw.mapping;

import java.util.Objects;

public class UrlKey {
    private final String urlString;
    private final UrlHTTPMethod methodHttp;

    public UrlKey(String urlString, UrlHTTPMethod methodHttp) {
        this.urlString = urlString;
        this.methodHttp = methodHttp;
    }

    public String getUrlString() {
        return urlString;
    }

    public UrlHTTPMethod getMethodHttp() {
        return methodHttp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UrlKey other)) {
            return false;
        }
        return Objects.equals(urlString, other.urlString) && methodHttp == other.methodHttp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlString, methodHttp);
    }

    @Override
    public String toString() {
        return "UrlKey{" + "urlString='" + urlString + '\'' + ", methodHttp=" + methodHttp + '}';
    }
}
