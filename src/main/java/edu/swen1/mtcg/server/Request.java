package edu.swen1.mtcg.server;

import edu.swen1.mtcg.http.RestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Request {
    private RestMethod method;
    private String url;
    private String path;
    private List<String> pathParts;
    private HashMap<String, String> params = new HashMap<>();
    private HeaderMap headerMap = new HeaderMap();
    private String body;
    public final int PARAM_LIMIT = 5;

    public String getRoute() {
        if(this.pathParts == null || this.pathParts.isEmpty()) {
            return null;
        }
        return '/' + this.pathParts.get(0);
    }

    public String getUrl() { return url; }

    public void setUrl(String url) {
        this.url = url;

        if(url.contains("?")) {
            String[] parts = url.split("\\?", 2);
            this.setPath(parts[0]);
            this.setParams(parts[1]);
        }
        else {
            this.setPath(url);
            this.setParams(null);
        }

    }

    public String getPath() { return path; }

    public void setPath(String pathName) {
        this.path = pathName;
        String[] parts = pathName.split("/");
        this.pathParts = new ArrayList<>();
        for(String part : parts) {
            if(part != null && part.length() > 0) {
                this.pathParts.add(part);
            }
        }
    }

    // Create Hashmap for provided params
    public void setParams(String params) {
        if(params.contains("=")) {
            String[] parts = params.split("&", PARAM_LIMIT);
            for(String part : parts) {
                if(part != null && !part.isEmpty() && !this.params.containsKey(part)) {
                    String[] paramParts = part.split("=", 2);
                    this.params.put(paramParts[0], paramParts[1]);
                }
            }
        }
        this.params = null;
    }

    public HashMap<String, String> getParams() { return params; }


    public HeaderMap getHeaderMap() { return headerMap; }

    public String getHeader(String headerName) {
        return this.headerMap.getHeader(headerName);
    }


    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public RestMethod getMethod() { return method; }
    public void setMethod(RestMethod method) { this.method = method; }

    public List<String> getPathParts() { return pathParts; }

}
