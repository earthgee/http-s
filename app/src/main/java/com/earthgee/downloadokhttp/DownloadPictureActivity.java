package com.earthgee.downloadokhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.earthgee.downloadokhttp.download.DownloadCallback;
import com.earthgee.downloadokhttp.download.FileDownloader;

/**
 * Created by earthgee on 17/9/27.
 */

public class DownloadPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_pic);

        final ImageView imageView= (ImageView) findViewById(R.id.img);
        FileDownloader.getInstance().download(
                "http://www.sinaimg.cn/dy/slidenews/1_img/2017_38/63957_1415585_513017.jpg",
                new DownloadCallback() {
            @Override
            public void onDownloadSuccess(String filePath) {
                Toast.makeText(DownloadPictureActivity.this,
                        "下载成功",Toast.LENGTH_SHORT).show();
                Bitmap img=BitmapFactory.decodeFile(filePath);
                imageView.setImageBitmap(img);
            }

            @Override
            public void onDownloadFail() {
                Toast.makeText(DownloadPictureActivity.this,
                        "下载失败，请重试",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
