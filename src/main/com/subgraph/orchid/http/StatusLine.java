package com.subgraph.orchid.http;

@SuppressWarnings("unused")
public class StatusLine {
    private final String protocolVersion;
    private final String reasonPhrase;
    private final int statusCode;
    
    private StatusLine(String protocolVersion, String reasonPhrase, int statusCode){
        this.protocolVersion = protocolVersion;
        this.reasonPhrase = reasonPhrase;
        this.statusCode = statusCode;
    }
    
    public static StatusLine getInstance(String protocolVersion, String reasonPhrase, int statusCode){
        return new StatusLine(protocolVersion, reasonPhrase, statusCode);
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public int getStatusCode() {
        return statusCode;
    }
}