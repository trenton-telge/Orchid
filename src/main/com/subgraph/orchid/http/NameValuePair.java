package com.subgraph.orchid.http;

public class NameValuePair {
    private final String name;
    private final String value;
    
    private NameValuePair(String name, String value){
        this.name = name;
        this.value = value;
    }
    
    public static NameValuePair getInstance(String name, String value){
        return new NameValuePair(name, value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
