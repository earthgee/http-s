package com.earthgee.downloadokhttp.download;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by earthgee on 17/10/9.
 */

public class ClientCommon {

    private static final String TAG="common_earthgee";

    private static OkHttpClient client;

    public static OkHttpClient createClient(){
        if(client==null){
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
        }
        return client;
    }

}
