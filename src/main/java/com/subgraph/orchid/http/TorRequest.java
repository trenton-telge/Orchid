package com.subgraph.orchid.http;

import com.subgraph.orchid.http.get.TorGetRequest;
import com.subgraph.orchid.http.post.TorPostRequest;
import com.subgraph.orchid.http.ssl.EnforceSslCertificates;
import com.subgraph.orchid.http.ssl.IgnoreSslCertificates;
import java.util.List;
import javax.net.ssl.SSLContext;

public abstract class TorRequest {
    public static final SSLContext ENFORCE_SSL_CERTIFICATES = EnforceSslCertificates.getSSLContext();
    public static final SSLContext IGNORE_SSL_CERTIFICATES = IgnoreSslCertificates.getSSLContext();
    protected final String url;
    protected SSLContext sslContext = ENFORCE_SSL_CERTIFICATES;
    protected int maxRetryAttempts = 3;
    protected TorSocketStream request;
    protected HttpResponse response;
    
    protected TorRequest(String url){
        this.url = url;
    }
    
    public static TorRequest getInstance(String url){
        return TorGetRequest.getInstance(url);
    }
    
    public static TorRequest getInstance(String url, List<NameValuePair> params){
        return TorPostRequest.getInstance(url, params);
    }
    
    public static void openTunnel(){
        TorClientFactory.openTunnel();
    }
    
    public void executeRequest() throws Exception{
        if(!TorClientFactory.hasOpenTorTunnel()){
            throw new Exception("Please do TorRequest.openTunnel() before "
                    + "making an http request over the tor network. Don't "
                    + "forget to do TorRequest.closeTunnel() when you "
                    + "are finished making requests over the tunnel.");
        }

        request.executeRequest();
        int currentRetryAttempts = 0;
        while(currentRetryAttempts<maxRetryAttempts && (response = HttpResponse.getInstance(request)).getStatusLine().getStatusCode()>399){
            request.executeRequest();
            currentRetryAttempts++;
        }
    }
    
    public HttpResponse getResponse(){
        return this.response;
    }
    
    public static void closeTunnel(){
        TorClientFactory.closeTunnel();
    }

    public TorRequest setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
        return this;
    }

    public TorRequest setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }
}