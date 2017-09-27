package com.earthgee.downloadokhttp.download;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

/**
 * Created by earthgee on 17/9/26.
 */

public class FileSaver {

    public String saveFile(Response response,String url){
        String filePath="";
        if(isExternalStorageWriteable()) {
            File externalStorageFile = Environment.getExternalStorageDirectory();
            File saveFileDir = new File(externalStorageFile, "earthgee_file_save");
            if (!saveFileDir.exists()) {
                saveFileDir.mkdir();
            }

            File saveFile = new File(saveFileDir, Cache.key(HttpUrl.parse(url)));
            ResponseBody responseBody = response.body();
            BufferedSource source= responseBody.source();

            try {
                Sink sink= Okio.sink(saveFile);
                source.readAll(sink);
                filePath=saveFile.getAbsolutePath();
                sink.close();
            } catch (Exception e) {
                filePath="";
            }
            responseBody.close();
        }

        return filePath;
    }

    private boolean isExternalStorageWriteable(){
        String state= Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }

        return false;
    }

    private boolean isExternalStorageReadable(){
        String state=Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)
                ||Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            return true;
        }

        return false;
    }

}
