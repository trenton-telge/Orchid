package com.subgraph.orchid.http;

import com.subgraph.orchid.sockets.sslengine.SSLEngineSSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import javax.net.ssl.SSLContext;

@SuppressWarnings("unused")
public abstract class TorSocketStream extends InputStream {
    protected final String url;
    protected final List<NameValuePair> params;
    protected InputStream inputStream;
    protected int responseCode;
    protected Socket socket;
    protected SSLEngineSSLSocket sslSocket;
    protected final SSLContext sslContext;
    
    protected TorSocketStream(String url, SSLContext sslContext){
        this.url = url;
        this.params = new ArrayList<>();
        this.sslContext = sslContext;
    }
    
    protected TorSocketStream(String url, List<NameValuePair> params, SSLContext sslContext){
        this.url = url;
        this.params = params;
        this.sslContext = sslContext;
    }

    public abstract void executeRequest() throws Exception;
    
    protected String getHost(){
        try{
            return new URI(url.replace(" ", "%20")).getHost();
        } catch(Exception e){
            //swallow
            return null;
        }
    }
    
    protected String getPath(){
        try{
            String path = new URI(url.replace(" ", "%20")).getPath();
            if(path.isEmpty()){
                return "/";
            } else{
                return path;
            }
        } catch(Exception e){
            //swallow
            return "";
        }
    }
    
    protected String getParams(){
        if(params==null){
            return "";
        } else{
            StringBuilder sb = new StringBuilder();
            String ampersand = "";
            for(NameValuePair param : params){
                String name = "";
                if(param.getName()!=null){
                    try{
                        name = URLEncoder.encode(param.getName(), StandardCharsets.UTF_8);
                    } catch(Exception e){
                        //swallow
                    }
                }
                String value = "";
                if(param.getValue()!=null){
                    try{
                        value = URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8);
                    } catch(Exception e){
                        //swallow
                    }
                }
                sb.append(ampersand).append(name).append("=").append(value);
                ampersand = "&";
            }
            return sb.toString();
        }
    }
    
    protected String getQuery(){
        try{
            String query = new URI(url.replace(" ", "+")).getQuery();
            if(query.isEmpty()){
                return "";
            } else {
                return "?"+query;
            }
        } catch(Exception e){
            //swallow
            return "";
        }
    }

    @Override
    public int read() throws IOException{
        if(inputStream==null){
            return -1;
        }
        try{
            return inputStream.read();
        } catch(Exception e){
            return -1;
        }
    }

    @Override
    public void close() {
        try{
            inputStream.close();
        } catch(IOException e){
            //Could not close input stream. Probably already closed.
        } catch(NullPointerException e){
            //The request probubally failed
        } finally {
            try{
                if(socket!=null){
                    socket.close();
                }
            } catch(IOException e){
                //Could not close socket
            }
        }
    }
}