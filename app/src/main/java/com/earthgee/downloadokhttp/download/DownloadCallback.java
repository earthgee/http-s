package com.earthgee.downloadokhttp.download;

/**
 * Created by earthgee on 17/9/26.
 */

public interface DownloadCallback {

    void onDownloadSuccess(String filePath);

    void onDownloadFail();

}
