package com.subgraph.orchid.http;

import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@SuppressWarnings("unused")
public class HttpResponse {
    private final String rawResponse;
    private List<HttpHeader> headers;
    private StatusLine statusLine;
    private String content;
    
    private HttpResponse(String rawResponse){
        this.rawResponse = rawResponse;
    }
    
    public static HttpResponse getInstance(InputStream inputStream) throws Exception{
        return new HttpResponse(StreamReader.read(inputStream));
    }
    
    public static HttpResponse getInstance(String rawResponse){
        return new HttpResponse(rawResponse);
    }
    
    public List<HttpHeader> getHeaders(){
        if(headers==null){
            populateHeadersAndContent();
        }
        return headers;
    }
    
    public HttpHeader getHeader(String name){
        if(headers==null){
            populateHeadersAndContent();
        }
        if(headers==null){
            headers = new ArrayList<>();
        }
        for(HttpHeader header : headers){
            if(header.getName()!=null && header.getName().equals(name)){
                return header;
            }
        }
        return null;
    }
    
    public String getContent(){
        if(content==null){
            populateHeadersAndContent();
        }
        return content;
    }

    public StatusLine getStatusLine() {
        if(statusLine==null){
            populateHeadersAndContent();
        }
        if(statusLine==null){
            statusLine = StatusLine.getInstance("Unknown", "Unknown", 0);
        }
        return statusLine;
    }
    
    private void populateHeadersAndContent(){
        if(rawResponse==null || rawResponse.length()<4 || !rawResponse.substring(0, 4).equalsIgnoreCase("HTTP")){
            content = rawResponse;
        } else{
            String[] splitResponse = rawResponse.split("\r\n\r\n", 2);
            content="";
            if(splitResponse.length>1){
                content = splitResponse[1];
            }
            BufferedReader bufferedHeaders = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(splitResponse[0].getBytes())));
            try{
                String rawStatusLine = bufferedHeaders.readLine();
                if(rawStatusLine!=null){
                    String[] splitStatusLine = rawStatusLine.split(" ");
                    statusLine = StatusLine.getInstance(splitStatusLine[0], splitStatusLine[2], Integer.parseInt(splitStatusLine[1]));
                    headers = new ArrayList<>();
                    String rawHeaderLine;
                    try{
                        while((rawHeaderLine = bufferedHeaders.readLine())!=null){
                            String[] splitHeaderLine = rawHeaderLine.split(":", 2);
                            if(splitHeaderLine.length == 2){
                                headers.add(HttpHeader.getInstance(splitHeaderLine[0].trim(), splitHeaderLine[1].trim()));
                            }
                        }
                    } catch(Exception e){
                        //swallow
                    }
                }
            } catch(Exception e){
                //swallow
            }
        }
    }
}