package com.subgraph.orchid.http;

@SuppressWarnings("unused")
public class LogMessage {
    private final String message;
    private final Throwable e;
    
    public LogMessage(String message){
        this.message = message;
        this.e = null;
    }
    
    public LogMessage(String message, Throwable e){
        this.message = message;
        this.e = e;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getE() {
        return e;
    }
}
