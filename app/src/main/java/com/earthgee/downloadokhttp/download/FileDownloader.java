package com.earthgee.downloadokhttp.download;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by earthgee on 17/9/18.
 */

public class FileDownloader {

    private static final String TAG="FileDownloader";

    private static FileDownloader fileDownloader;

    public static FileDownloader getInstance(){
        if(fileDownloader==null){
            fileDownloader=new FileDownloader();
        }

        return fileDownloader;
    }

    private OkHttpClient client;

    private Handler handler;

    private final int DOWNLOAD_SUCCESS=1;
    private final int DOWNLOAD_FAIL=0;

    private FileDownloader(){
        client=new OkHttpClient.Builder().retryOnConnectionFailure(false).
                eventListener(new EventListener() {
            @Override
            public void callStart(Call call) {
                super.callStart(call);
                Log.d(TAG,"call start");
            }

            @Override
            public void dnsStart(Call call, String domainName) {
                super.dnsStart(call, domainName);
                Log.d(TAG,"dns start,domainName="+domainName);
            }

            @Override
            public void dnsEnd(Call call, String domainName, @Nullable List<InetAddress> inetAddressList) {
                super.dnsEnd(call, domainName, inetAddressList);
                StringBuilder stringBuilder=new StringBuilder();
                for(int i=0;i<inetAddressList.size();i++){
                    stringBuilder.append(inetAddressList.get(i).toString()).append("\n");
                }
                Log.d(TAG,"dns end,domainName="+domainName+",InetAddress="+stringBuilder.toString());
            }

            @Override
            public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
                super.connectStart(call, inetSocketAddress, proxy);
                Log.d(TAG,"connectStart:"+inetSocketAddress.toString()+","+proxy.toString());
            }

            @Override
            public void secureConnectStart(Call call) {
                super.secureConnectStart(call);
            }

            @Override
            public void secureConnectEnd(Call call, @Nullable Handshake handshake) {
                super.secureConnectEnd(call, handshake);
            }

            @Override
            public void connectEnd(Call call, InetSocketAddress inetSocketAddress, @Nullable Proxy proxy, @Nullable Protocol protocol) {
                super.connectEnd(call, inetSocketAddress, proxy, protocol);
                Log.d(TAG,"connectEnd:"+inetSocketAddress.toString()+","+proxy.toString());
            }

            @Override
            public void connectFailed(Call call, InetSocketAddress inetSocketAddress, @Nullable Proxy proxy, @Nullable Protocol protocol, @Nullable IOException ioe) {
                super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
                Log.d(TAG,"connectEnd:"+inetSocketAddress.toString()+","+proxy.toString()+","+ioe.getMessage());
            }

            @Override
            public void connectionAcquired(Call call, Connection connection) {
                super.connectionAcquired(call, connection);
                Log.d(TAG,"connection acquire");
            }

            @Override
            public void connectionReleased(Call call, Connection connection) {
                super.connectionReleased(call, connection);
                Log.d(TAG,"connection release");
            }

            @Override
            public void requestHeadersStart(Call call) {
                super.requestHeadersStart(call);
                Log.d(TAG,"requestHeadersStart");
            }

            @Override
            public void requestHeadersEnd(Call call, Request request) {
                super.requestHeadersEnd(call, request);
                Log.d(TAG,"requestHeadersEnd");
            }

            @Override
            public void requestBodyStart(Call call) {
                super.requestBodyStart(call);
                Log.d(TAG,"requestBodyStart");
            }

            @Override
            public void requestBodyEnd(Call call, long byteCount) {
                super.requestBodyEnd(call, byteCount);
                Log.d(TAG,"requestBodyEnd");
            }

            @Override
            public void responseHeadersStart(Call call) {
                super.responseHeadersStart(call);
                Log.d(TAG,"responseHeadersStart");
            }

            @Override
            public void responseHeadersEnd(Call call, Response response) {
                super.responseHeadersEnd(call, response);
                Log.d(TAG,"responseHeadersEnd");
            }

            @Override
            public void responseBodyStart(Call call) {
                super.responseBodyStart(call);
                Log.d(TAG,"responseBodyStart");
            }

            @Override
            public void responseBodyEnd(Call call, long byteCount) {
                super.responseBodyEnd(call, byteCount);
                Log.d(TAG,"responseBodyEnd");
            }

            @Override
            public void callEnd(Call call) {
                super.callEnd(call);
                Log.d(TAG,"callEnd");
            }

            @Override
            public void callFailed(Call call, IOException ioe) {
                super.callFailed(call, ioe);
                Log.d(TAG,"callFailed");
            }
        }).addInterceptor(new RetryInterceptor()).build();
        handler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case DOWNLOAD_SUCCESS:
                        Object[] objs= (Object[]) msg.obj;
                        DownloadCallback successCallback= (DownloadCallback) objs[0];
                        String filePath= (String) objs[1];
                        successCallback.onDownloadSuccess(filePath);
                        break;
                    case DOWNLOAD_FAIL:
                        DownloadCallback failCallback= (DownloadCallback) msg.obj;
                        failCallback.onDownloadFail();
                        break;
                }
            }
        };
    }

    /**
     * @param url 文件地址，一般是cdn上的静态内容
     */
    public void download(final String url, final DownloadCallback downloadCallback){
        Request request=constructDownloadRequest(url);
        //callback执行线程非ui线程
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"url:"+url+", download fail:"+e.getMessage());
                Message message=new Message();
                message.what=DOWNLOAD_FAIL;
                message.obj=downloadCallback;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"url:"+url+", download success");

                String filePath="";
                FileSaver fileSaver=new FileSaver();
                filePath=fileSaver.saveFile(response,url);

                if("".equals(filePath)){
                    Message message=new Message();
                    message.what=DOWNLOAD_FAIL;
                    message.obj=downloadCallback;
                    handler.sendMessage(message);
                }else{
                    Message message=new Message();
                    message.what=DOWNLOAD_SUCCESS;
                    message.obj=new Object[]{downloadCallback,filePath};
                    handler.sendMessage(message);
                }
            }
        });
    }

    /**
     * 构造下载Request，GET，RequestBody=null
     *
     * @param url
     * @return
     */
    private Request constructDownloadRequest(String url){
        HttpUrl httpUrl=HttpUrl.parse(url);
        Request request=new Request.Builder().url(httpUrl).build();
        return request;
    }

}
