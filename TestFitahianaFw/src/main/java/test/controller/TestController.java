package test.controller;

import Fitahianafw.annotation.Controller;
import Fitahianafw.annotation.UrlMapping;
import Fitahianafw.model.ModelView;

@Controller
public class TestController {

    @UrlMapping("/test")
    public ModelView test() {
        ModelView modelView = new ModelView("/test");
        modelView.addObject("message", "Bonjour depuis FitahianaFw");
        modelView.addObject("nomFramework", "FitahianaFw");
        return modelView;
    }
}
