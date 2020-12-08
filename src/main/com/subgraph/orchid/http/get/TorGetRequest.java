package com.subgraph.orchid.http.get;

import com.subgraph.orchid.http.TorRequest;

public class TorGetRequest extends TorRequest{
    protected TorGetRequest(String url){
        super(url);
    }
    
    public static TorGetRequest getInstance(String url){
        return new TorGetRequest(url);
    }
    
    @Override
    public void executeRequest() throws Exception{
        if(url.toLowerCase().startsWith("https")){
            request = TorSocketHttpsGet.getInstance(url, super.getSslContext());
        } else{
            request = TorSocketHttpGet.getInstance(url);
        }
        
        super.executeRequest();
    }
}