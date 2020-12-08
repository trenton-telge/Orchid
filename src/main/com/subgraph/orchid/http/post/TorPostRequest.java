package com.subgraph.orchid.http.post;

import com.subgraph.orchid.http.NameValuePair;
import com.subgraph.orchid.http.TorRequest;
import java.util.List;

public class TorPostRequest extends TorRequest{
    private final List<NameValuePair> params;
    
    private TorPostRequest(String url, List<NameValuePair> params){
        super(url);
        this.params = params;
    }
    
    public static TorPostRequest getInstance(String url, List<NameValuePair> params){
        return new TorPostRequest(url, params);
    }
    
    @Override
    public void executeRequest() throws Exception{
        if(url.toLowerCase().startsWith("https")){
            request = TorSocketHttpsPost.getInstance(url, params, super.getSslContext());
        } else{
            request = TorSocketHttpPost.getInstance(url, params);
        }
        
        super.executeRequest();
    }
}