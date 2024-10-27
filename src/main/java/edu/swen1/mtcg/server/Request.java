package edu.swen1.mtcg.server;

import edu.swen1.mtcg.http.RestMethod;

import java.util.ArrayList;
import java.util.List;


public class Request {
    private RestMethod method;
    private String url;
    private String path;
    private List<String> pathParts;
    private String params;
    private HeaderMap headerMap = new HeaderMap();
    private String body;

    public String getRoute() {
        if(this.pathParts == null || this.pathParts.isEmpty()) {
            return null;
        }
        return '/' + this.pathParts.get(0);
    }

    public String getUrl() { return url; }

    public void setUrl(String url) {
        this.url = url;

        if(url.indexOf("?") != -1) {
            String[] parts = url.split("\\?");
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

    public String getParams() { return params; }
    public void setParams(String params) {  this.params = params; }

    public void setHeaderMap(HeaderMap headerMap) {  this.headerMap = headerMap; }
    public HeaderMap getHeaderMap() { return headerMap; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public RestMethod getMethod() { return method; }
    public void setMethod(RestMethod method) { this.method = method; }

    public List<String> getPathParts() { return pathParts; }
    public void setPathParts(List<String> pathParts) { this.pathParts = pathParts; }

}
