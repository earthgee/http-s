package com.earthgee.downloadokhttp.download;

/**
 * Created by earthgee on 17/9/26.
 */

public interface DownloadCallback {

    void onDownloadSuccess(String filePath);

    //此回调不在主线程,contentLength可能为-1！！！(响应header contentlength为-1时)
    void onUpdate(long bytesRead,long contentLength,boolean done);

    void onDownloadFail();

}
