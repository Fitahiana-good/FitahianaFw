package Fitahianafw.model;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String viewName;
    private Map<String, Object> data;

    public ModelView() {
        this(null, null);
    }

    public ModelView(String viewName) {
        this(viewName, null);
    }

    public ModelView(String viewName, Map<String, Object> data) {
        this.viewName = viewName;
        setData(data);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getData() {
        if (data == null) {
            data = new HashMap<>();
        }
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data == null ? new HashMap<>() : new HashMap<>(data);
    }

    public ModelView addObject(String key, Object value) {
        getData().put(key, value);
        return this;
    }
}
