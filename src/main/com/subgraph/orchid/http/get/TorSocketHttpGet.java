package com.subgraph.orchid.http.get;

import com.subgraph.orchid.http.TorClientFactory;
import com.subgraph.orchid.http.TorSocketStream;
import com.subgraph.orchid.TorClient;
import java.io.PrintWriter;

public class TorSocketHttpGet extends TorSocketStream {
    
    private TorSocketHttpGet(String url){
        super(url, null);
    }
    
    public static TorSocketHttpGet getInstance(String url){
        return new TorSocketHttpGet(url);
    }
    
    @Override
    public void executeRequest() throws Exception{
        TorClient client = TorClientFactory.getTorClient();
        socket = client.getSocketFactory().createSocket(getHost(), 80);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        inputStream = socket.getInputStream();
        writer.println("GET "+getPath()+getQuery()+" HTTP/1.0");
        writer.println("Host: "+getHost());
        writer.println("Connection: close");
        writer.println("");
        writer.flush();
    }
}