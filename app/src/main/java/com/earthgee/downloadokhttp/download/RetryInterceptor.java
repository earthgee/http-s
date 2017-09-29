package com.earthgee.downloadokhttp.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by earthgee on 17/9/27.
 */

public class RetryInterceptor implements Interceptor{

    private static final int MAX_RETRY_COUNT=3;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request=chain.request();
        Response response=null;
        int tryCount=0;
        while (tryCount<MAX_RETRY_COUNT){
            tryCount++;
            try{
                response=chain.proceed(request);
            }catch (IOException e){
                if(tryCount>=MAX_RETRY_COUNT){
                    throw e;
                }
            }

            if(response==null||!response.isSuccessful()){
                continue;
            }else{
                break;
            }
        }

        return response;
    }

}
