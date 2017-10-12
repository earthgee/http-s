package com.earthgee.downloadokhttp.download;

/**
 * Created by earthgee on 17/10/9.
 */

public interface UploadCallback {

    void onUploadSuccess();

    //此方法未运行在主线程!!!
    void onUpdate(long bytesWritten,long contentLength,boolean done);

    void onUploadFail();

}
