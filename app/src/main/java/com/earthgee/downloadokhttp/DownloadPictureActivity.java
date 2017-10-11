package com.earthgee.downloadokhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.earthgee.downloadokhttp.download.DownloadCallback;
import com.earthgee.downloadokhttp.download.FileDownloader;

/**
 * Created by earthgee on 17/9/27.
 */

public class DownloadPictureActivity extends AppCompatActivity {

    public static int time=1;

    private ProgressView progressView;

    //http://www.sinaimg.cn/dy/slidenews/1_img/2017_38/63957_1415585_513017.jpg
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_pic);

        final ImageView imageView= (ImageView) findViewById(R.id.img);
        progressView= (ProgressView) findViewById(R.id.progress);

        String url="";

            url="http://www.sinaimg.cn/dy/slidenews/1_img/2017_38/63957_1415585_513017.jpg";


        time++;
        progressView.setVisibility(View.VISIBLE);
        FileDownloader.getInstance().downloadNeedProgress(
                url,
                new DownloadCallback() {
            @Override
            public void onDownloadSuccess(String filePath) {
                Toast.makeText(DownloadPictureActivity.this,
                        "下载成功",Toast.LENGTH_SHORT).show();
                Bitmap img=BitmapFactory.decodeFile(filePath);
                imageView.setImageBitmap(img);
            }

                    @Override
                    public void onUpdate(final long bytesRead, final long contentLength, final boolean done) {
                       // Log.d("earthgee","已下载字节数:"+bytesRead+",总长度:"+contentLength);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressView.setProgress((float) bytesRead/(float) contentLength);
                                if(done){
                                    progressView.setVisibility(View.GONE);
                                }
                            }
                        });
                    }

                    @Override
            public void onDownloadFail() {
                Toast.makeText(DownloadPictureActivity.this,
                        "下载失败，请重试",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
