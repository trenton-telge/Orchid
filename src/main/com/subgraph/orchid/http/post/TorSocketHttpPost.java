package com.subgraph.orchid.http.post;

import com.subgraph.orchid.http.NameValuePair;
import com.subgraph.orchid.http.TorClientFactory;
import com.subgraph.orchid.http.TorSocketStream;
import com.subgraph.orchid.TorClient;
import java.io.PrintWriter;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class TorSocketHttpPost extends TorSocketStream {
    
    protected TorSocketHttpPost(String url, List<NameValuePair> params){
        super(url, params, null);
    }
    
    public static TorSocketHttpPost getInstance(String url, List<NameValuePair> params){
        return new TorSocketHttpPost(url, params);
    }
    
    @Override
    public void executeRequest() throws Exception{
        TorClient client = TorClientFactory.getTorClient();
        socket = client.getSocketFactory().createSocket(getHost(), 80);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        inputStream = socket.getInputStream();
        writer.println("POST "+getPath()+getQuery()+" HTTP/1.0");
        writer.println("Host: "+getHost());
        writer.println("Content-Type: application/x-www-form-urlencoded");
        writer.println("Content-Length: "+getParams().length());
        writer.println("Connection: close");
        writer.println("");
        writer.println(getParams());
        writer.flush();
    }
}