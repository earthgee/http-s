package com.earthgee.downloadokhttp.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by earthgee on 17/9/18.
 */

public class FileDownloader{

    private static final String TAG="download_earthgee";

    private static FileDownloader fileDownloader;

    public static FileDownloader getInstance(){
        if(fileDownloader ==null){
            fileDownloader =new FileDownloader();
        }

        return fileDownloader;
    }

    private OkHttpClient client;

    private Handler handler;

    private final int DOWNLOAD_SUCCESS=1;
    private final int DOWNLOAD_FAIL=0;

    private FileDownloader(){
        client=ClientCommon.createClient();
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
                message.obj= downloadCallback;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String filePath="";
                    FileSaver fileSaver=new FileSaver();
                    filePath=fileSaver.saveFile(response,url);

                    if("".equals(filePath)){
                        Log.d(TAG,"url:"+url+", download fail,save fail\n");
                        Message message=new Message();
                        message.what=DOWNLOAD_FAIL;
                        message.obj= downloadCallback;
                        handler.sendMessage(message);
                    }else{
                        Log.d(TAG,"url:"+url+", download success\n");
                        Message message=new Message();
                        message.what=DOWNLOAD_SUCCESS;
                        message.obj=new Object[]{downloadCallback,filePath};
                        handler.sendMessage(message);
                    }
                }else{
                    Log.d(TAG,"url:"+url+", download fail,errorcode is:"+response.code());
                    Message message=new Message();
                    message.what=DOWNLOAD_FAIL;
                    message.obj= downloadCallback;
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
