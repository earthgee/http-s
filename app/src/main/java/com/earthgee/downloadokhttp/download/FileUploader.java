package com.earthgee.downloadokhttp.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by earthgee on 17/10/9.
 */

public class FileUploader {

    private static final String TAG="upload_earthgee";

    private static FileUploader fileUploader;

    public static FileUploader getInstance(){
        if(fileUploader==null){
            fileUploader=new FileUploader();
        }

        return fileUploader;
    }

    private OkHttpClient client;

    private Handler handler;

    private final int UPLOAD_SUCCESS=1;
    private final int UPLOAD_FAIL=0;

    private FileUploader(){
        client=ClientCommon.createClient().newBuilder().writeTimeout(30, TimeUnit.SECONDS).build();
        handler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case UPLOAD_SUCCESS:
                        UploadCallback successCallback= (UploadCallback) msg.obj;
                        successCallback.onUploadSuccess();
                        break;
                    case UPLOAD_FAIL:
                        UploadCallback failCallback= (UploadCallback) msg.obj;
                        failCallback.onUploadFail();
                        break;
                }
            }
        };
    }

    /**
     * 上传文件（无其他参数）
     * @param url
     * @param filePath
     * @param uploadCallback
     */
    public void uploadFile(final String url, String filePath, final UploadCallback uploadCallback){
        File file=new File(filePath);
        MediaType mediaType=MediaType.parse("application/octet-stream;");
        RequestBody requestBody=RequestBody.create(mediaType,file);
        Request request=new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"url:"+url+", upload fail:"+e.getMessage());

                Message message=new Message();
                message.what=UPLOAD_FAIL;
                message.obj= uploadCallback;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d(TAG,"url:"+url+", upload success\n");

                    Message message=new Message();
                    message.what=UPLOAD_SUCCESS;
                    message.obj=uploadCallback;
                    handler.sendMessage(message);
                }else{
                    Log.d(TAG,"url:"+url+", upload fail,errorcode is:"+response.code());

                    Message message=new Message();
                    message.what=UPLOAD_FAIL;
                    message.obj=uploadCallback;
                    handler.sendMessage(message);
                }
            }
        });
    }

    /**
     * 上传文件(带参数)
     * @param url
     * @param formParams
     * @param filePath
     * @param uploadCallback
     */
    public void uploadFile(final String url, HashMap<String,String> formParams,
                           String filePath, final UploadCallback uploadCallback){
        MultipartBody.Builder builder=new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for(String key:formParams.keySet()){
            String value=formParams.get(key);
            builder.addFormDataPart(key,value);
        }

        File file=new File(filePath);
        builder.addFormDataPart("uploadfile",file.getName(),RequestBody.create(null,file));

        MultipartBody body=builder.build();
        Request request=new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"url:"+url+", upload fail:"+e.getMessage());

                Message message=new Message();
                message.what=UPLOAD_FAIL;
                message.obj= uploadCallback;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d(TAG,"url:"+url+", upload success\n");

                    Message message=new Message();
                    message.what=UPLOAD_SUCCESS;
                    message.obj=uploadCallback;
                    handler.sendMessage(message);
                }else{
                    Log.d(TAG,"url:"+url+", upload fail,errorcode is:"+response.code());

                    Message message=new Message();
                    message.what=UPLOAD_FAIL;
                    message.obj=uploadCallback;
                    handler.sendMessage(message);
                }
            }
        });
    }

}
