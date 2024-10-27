package edu.swen1.mtcg.server;

import java.util.HashMap;
import java.util.Map;

public class HeaderMap {
    public static final String LENGTH_HEADER = "Content-Length";
    public static final String HEADER_SEPARATOR = ":";
    private Map<String, String> headers = new HashMap<>();

    public void splitLine(String line) {
        final String[] parts = line.split(HEADER_SEPARATOR, 2);
        headers.put(parts[0], parts[1].trim());
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public int getContentLength() {
        final String header = headers.get(LENGTH_HEADER);
        if (header == null) {
            return 0;
        }

        return Integer.parseInt(header);
    }

    public void print() {
        System.out.println("HeaderMap: " + headers);
    }

}
