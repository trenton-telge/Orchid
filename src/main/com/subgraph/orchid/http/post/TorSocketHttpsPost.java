package com.subgraph.orchid.http.post;

import com.subgraph.orchid.http.NameValuePair;
import com.subgraph.orchid.http.TorClientFactory;
import com.subgraph.orchid.http.TorSocketStream;
import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.sockets.sslengine.SSLEngineSSLSocket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.net.ssl.SSLContext;

@SuppressWarnings("DuplicatedCode")
public class TorSocketHttpsPost extends TorSocketStream {
    
    private TorSocketHttpsPost(String url, List<NameValuePair> params, SSLContext sslContext){
        super(url, params, sslContext);
    }
    
    public static TorSocketHttpsPost getInstance(String url, List<NameValuePair> params, SSLContext sslContext){
        return new TorSocketHttpsPost(url, params, sslContext);
    }
    
    @Override
    public void executeRequest() throws Exception{
        TorClient client = TorClientFactory.getTorClient();
        socket = client.getSocketFactory().createSocket(getHost(), 80);
        sslSocket = new SSLEngineSSLSocket(socket, sslContext);
        try{
            PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);
            inputStream = sslSocket.getInputStream();
            writer.println("POST "+getPath()+getQuery()+" HTTP/1.0");
            writer.println("Host: "+getHost());
            writer.println("Content-Type: application/x-www-form-urlencoded");
            writer.println("Content-Length: "+getParams().length());
            writer.println("Connection: close");
            writer.println("");
            writer.println(getParams());
            writer.flush();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Thread.sleep(800L);
            int character;
            try{
                while((character = inputStream.read())!=-1){
                    byteArrayOutputStream.write(character);
                }
            } catch(Exception e){
                //swallow
                byteArrayOutputStream.write(-1);
            } finally{
                try{
                    inputStream.close();
                } catch(IOException e){
                    //Could not close stream
                }
            }
            inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            if(!sslSocket.getSession().isValid()){
                throw new Exception("SSL Session is not valid. Either import the SSL Certificate; configure ApplicationProperties.setEnforceSslCertificates(false); or configure [TorRequest].setEnforceSslCertificates(false);");
            }
        } finally{
            sslSocket.close();
            socket.close();
        }
    }
}