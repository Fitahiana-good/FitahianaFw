package Fitahianafw.err;

import java.util.HashMap;
import Fitahianafw.mapping.UrlControllerMap;
import Fitahianafw.mapping.UrlKey;

public class UrlNotSupportedException extends Exception {
    private final UrlKey urlGot;
    private final HashMap<UrlKey, UrlControllerMap> supportedUrl;

    public UrlNotSupportedException(UrlKey urlGot, HashMap<UrlKey, UrlControllerMap> supportedUrl) {
        super("URL non supportée : " + urlGot);
        this.urlGot = urlGot;
        this.supportedUrl = supportedUrl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("L'URL \"").append(urlGot).append("\" n'est pas supportée.\n\n");
        sb.append("URLs supportées :\n");
        for (UrlKey url : supportedUrl.keySet()) {
            sb.append(" - ").append(url).append("; \n");
        }
        return sb.toString();
    }
}
