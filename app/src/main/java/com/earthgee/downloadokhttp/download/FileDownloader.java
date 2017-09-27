package com.earthgee.downloadokhttp.download;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
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
        client=new OkHttpClient.Builder().retryOnConnectionFailure(false).build();
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
