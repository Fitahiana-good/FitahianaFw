package Fitahianafw.err;

import Fitahianafw.mapping.UrlControllerMap;
import Fitahianafw.mapping.UrlKey;

public class UrlAlreadyDefinedException extends Exception {
    public UrlAlreadyDefinedException(UrlKey url, UrlControllerMap existing) {
        super("Url " + url + " est déjà définie pour la valeur " + existing);
    }
}
