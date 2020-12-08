package com.subgraph.orchid.http;

public class HttpHeader {
    private final String name;
    private final String value;
    
    private HttpHeader(String name, String value){
        this.name = name;
        this.value = value;
    }
    
    public static HttpHeader getInstance(String name, String value){
        return new HttpHeader(name, value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
