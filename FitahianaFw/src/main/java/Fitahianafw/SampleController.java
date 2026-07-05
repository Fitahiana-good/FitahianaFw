package Fitahianafw;

import Fitahianafw.annotation.Controller;
import Fitahianafw.annotation.UrlMapping;

@Controller
public class SampleController {
    public static String lastMessage;

    @UrlMapping(value = "/hello")
    public void hello() {
        lastMessage = "bonjour depuis FitahianaFw";
    }
}
