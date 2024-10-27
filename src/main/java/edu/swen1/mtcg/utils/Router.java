package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.server.IService;

import java.util.HashMap;
import java.util.Map;

// Route directory
public class Router {
    private Map<String, IService> services = new HashMap<>();
    public void newService(String route, IService service) {
        this.services.put(route, service);
    }
    public void deleteService(String route) {
        this.services.remove(route);
    }
    public IService getService(String route) {
        return this.services.get(route);
    }

}
