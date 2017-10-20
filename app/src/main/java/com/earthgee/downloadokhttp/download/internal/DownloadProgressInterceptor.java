package com.earthgee.downloadokhttp.download.internal;

import android.support.annotation.Nullable;

import com.earthgee.downloadokhttp.download.DownloadCallback;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import okio.Timeout;

/**
 * Created by earthgee on 17/10/11.
 */

public class DownloadProgressInterceptor implements Interceptor{

    private DownloadCallback progressListener;
    private long bytesRead;

    /**
     *
     * @param bytesRead 如果是断点续传此长度为上一次下载的文件长度
     * @param progressListener
     */
    public DownloadProgressInterceptor(long bytesRead,DownloadCallback progressListener){
        this.bytesRead=bytesRead;
        this.progressListener=progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originResponse=chain.proceed(chain.request());
        return originResponse.newBuilder().body(new ProgressResponseBody(bytesRead,originResponse.body(),progressListener)).build();
    }

    private static class ProgressResponseBody extends ResponseBody{

        private final ResponseBody responseBody;
        private final DownloadCallback progressListener;
        private BufferedSource bufferedSource;
        private long bytesRead;

        public ProgressResponseBody(long bytesRead,ResponseBody responseBody,DownloadCallback progressListener){
            this.bytesRead=bytesRead;
            this.responseBody=responseBody;
            this.progressListener=progressListener;
        }

        @Nullable
        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength()+bytesRead;
        }

        @Override
        public BufferedSource source() {
            if(bufferedSource==null){
                bufferedSource= Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source){
            return new ForwardingSource(source) {

                long totalBytesRead=bytesRead;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead=super.read(sink,byteCount);
                    totalBytesRead+=bytesRead!=-1?bytesRead:0;
                    progressListener.onUpdate(totalBytesRead,contentLength(),bytesRead==-1);
                    return bytesRead;
                }

            };
        }

    }

}
