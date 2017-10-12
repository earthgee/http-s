package com.earthgee.downloadokhttp.download.internal;

import android.support.annotation.Nullable;

import com.earthgee.downloadokhttp.download.UploadCallback;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by earthgee on 17/10/12.
 */

public class ProgressRequestBody extends RequestBody{

    private final RequestBody requestBody;
    private final UploadCallback progressListener;
    private BufferedSink bufferedSink;

    public ProgressRequestBody(RequestBody requestBody,UploadCallback progressListener){
        this.requestBody=requestBody;
        this.progressListener=progressListener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if(bufferedSink==null){
            bufferedSink= Okio.buffer(sink(sink));
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private Sink sink(Sink originSink){
        return new ForwardingSink(originSink) {

            long bytesWritten=0;
            long contentLength=0;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if(contentLength==0){
                    contentLength=contentLength();
                }

                bytesWritten+=byteCount;
                progressListener.onUpdate(bytesWritten,contentLength,bytesWritten==contentLength);
            }
        };
    }

}
