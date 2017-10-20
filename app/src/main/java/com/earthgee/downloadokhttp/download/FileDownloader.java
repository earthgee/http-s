package com.earthgee.downloadokhttp.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.earthgee.downloadokhttp.download.internal.DownloadProgressInterceptor;
import com.earthgee.downloadokhttp.download.internal.FileCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

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
    private FileCache cache;

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

        File externalStorageFile = Environment.getExternalStorageDirectory();
        File saveFileDir = new File(externalStorageFile, "earthgee_file_save");
        cache=new FileCache(saveFileDir,50*1024*1024);
    }

    /**
     * @param url 文件地址，一般是cdn上的静态内容
     */
    public void download(final String url, final DownloadCallback downloadCallback){
        Request request=constructDownloadRequest(url);
        String filePath=cache.get(request);
        if(!"".equals(filePath)){
            Log.d(TAG,"url:"+url+", get cache success\n");
            Message message=new Message();
            message.what=DOWNLOAD_SUCCESS;
            message.obj=new Object[]{downloadCallback,filePath};
            handler.sendMessage(message);
            return;
        }

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
                    filePath=cache.put(response);

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

    public void downloadNeedProgress(final String url, final DownloadCallback downloadCallback){
        Request request=constructDownloadRequest(url);
        String filePath=cache.get(request);
        if(!"".equals(filePath)){
            Log.d(TAG,"url:"+url+", get cache success\n");
            downloadCallback.onUpdate(1,1,true);
            Message message=new Message();
            message.what=DOWNLOAD_SUCCESS;
            message.obj=new Object[]{downloadCallback,filePath};
            handler.sendMessage(message);
            return;
        }

        //callback执行线程非ui线程
        client.newBuilder().addInterceptor(new DownloadProgressInterceptor(0,downloadCallback)).
                build().newCall(request).enqueue(new Callback() {
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
                    filePath=cache.put(response);

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
     * 支持断点续传的下载
     *
     * @param url
     * @param downloadCallback
     */
    public Call downloadWithPause(final String url, final DownloadCallback downloadCallback){
        //先从缓存中寻找
        Request tmpRequest=constructDownloadRequest(url);
        String filePath=cache.get(tmpRequest);
        if(!"".equals(filePath)){
            Log.d(TAG,"url:"+url+", get cache success\n");
            Message message=new Message();
            message.what=DOWNLOAD_SUCCESS;
            message.obj=new Object[]{downloadCallback,filePath};
            handler.sendMessage(message);
            return client.newCall(tmpRequest);
        }

        File externalStorageFile = Environment.getExternalStorageDirectory();
        File saveFileDir = new File(externalStorageFile, "earthgee_file_save/downloading");
        if(!saveFileDir.exists()){
            saveFileDir.mkdir();
        }

        final HttpUrl httpUrl=HttpUrl.parse(url);
        final String downloadingFileName= Cache.key(httpUrl);
        final File downloadingFile=new File(saveFileDir,downloadingFileName);
        Request request=null;
        long bytesRead=0;
        if(downloadingFile.exists()&&downloadingFile.length()>0){
            request=new Request.Builder().url(httpUrl).header("Range","bytes="+downloadingFile.length()+"-").build();
            bytesRead=downloadingFile.length();
        }else{
            request=new Request.Builder().url(httpUrl).build();
        }

        //callback执行线程非ui线程
        Call requestCall=client.newBuilder().addInterceptor(new DownloadProgressInterceptor(bytesRead,downloadCallback)).build().
                newCall(request);
        requestCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"url:"+url+", download fail:"+e.getMessage());
                Message message=new Message();
                message.what=DOWNLOAD_FAIL;
                message.obj= downloadCallback;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException{
                if(response.isSuccessful()){
                    String filePath="";

                    try {
                        BufferedSink sink=Okio.buffer(Okio.appendingSink(downloadingFile));
                        sink.writeAll(response.body().source());

                        Util.closeQuietly(sink);
                        filePath=downloadingFile.getAbsolutePath();
                    } catch (IOException e) {
                        filePath="";
                    }

                    filePath=cache.put(httpUrl,filePath);
                    if("".equals(filePath)){
                        Log.d(TAG,"url:"+url+", download fail,save fail\n");
                        Message message=new Message();
                        message.what=DOWNLOAD_FAIL;
                        message.obj= downloadCallback;
                        handler.sendMessage(message);
                    }else{
                        Log.d(TAG,"url:"+url+", download success\n");
                        //如果成功将文件倒到缓存里

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
        return requestCall;
    }



}
