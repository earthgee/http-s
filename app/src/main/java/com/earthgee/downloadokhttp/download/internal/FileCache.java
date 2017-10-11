package com.earthgee.downloadokhttp.download.internal;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.cache.CacheRequest;
import okhttp3.internal.cache.CacheStrategy;
import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.cache.InternalCache;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSource;
import okio.ByteString;
import okio.ForwardingSink;
import okio.Sink;

/**
 * 磁盘缓存下载文件
 * Created by earthgee on 17/10/9.
 */

public class FileCache {

    private static final int VERSION=1;
    private static final int ENTRY=0;
    private static final int ENTRY_COUNT=1;

    final DiskLruCache cache;

    private File fileSaveDirectory;

    public FileCache(File directory,long maxSize){
        this(directory,maxSize,FileSystem.SYSTEM);
    }

    public FileCache(File directory, long maxSize, FileSystem fileSystem){
       this.cache=DiskLruCache.create(fileSystem,directory,VERSION,ENTRY_COUNT,maxSize);
       fileSaveDirectory=directory;
    }

    public static String key(HttpUrl url){
        return ByteString.encodeUtf8(url.toString()).md5().hex();
    }

    public String get(Request request){
        String filePath="";

        if(isExternalStorageReadable()){
            String key=key(request.url());
            DiskLruCache.Snapshot snapshot;
            try {
                snapshot=cache.get(key);
                if(snapshot==null){
                    return "";
                }
            } catch (IOException e) {
                return "";
            }

            snapshot.close();
            return fileSaveDirectory.getAbsolutePath()+"/"+
                    key(request.url())+"."+ENTRY;
        }

        return filePath;
    }

    public String put(Response response){
        String filePath="";
        if(isExternalStorageWriteable()) {
            if (!fileSaveDirectory.exists()) {
                fileSaveDirectory.mkdir();
            }
            DiskLruCache.Editor editor=null;

            try{
                editor=cache.edit(key(response.request().url()));
            }catch (IOException e){
                return "";
            }

            if(editor==null){
               return "";
            }

            Sink cacheOut=editor.newSink(ENTRY);

            BufferedSource source=response.body().source();
            try{
                source.readAll(cacheOut);
                cacheOut.close();
                editor.commit();
                filePath=fileSaveDirectory.getAbsolutePath()+"/"+
                        key(response.request().url())+"."+ENTRY;
            }catch (IOException e){
                filePath="";
            }
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
