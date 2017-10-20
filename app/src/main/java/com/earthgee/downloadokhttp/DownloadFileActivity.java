package com.earthgee.downloadokhttp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.earthgee.downloadokhttp.download.DownloadCallback;
import com.earthgee.downloadokhttp.download.FileDownloader;

import okhttp3.Call;

/**
 * Created by earthgee on 17/10/13.
 */

public class DownloadFileActivity extends AppCompatActivity{

    private Button btn;
    private ProgressBar progressBar;

    boolean isDownloading=false;

    private Call requestCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);

        btn= (Button) findViewById(R.id.btn);
        progressBar= (ProgressBar) findViewById(R.id.progress);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDownloading){
                    btn.setText("继续下载");
                    requestCall.cancel();
                }else{
                    btn.setText("暂停下载");
                    requestCall=FileDownloader.getInstance().downloadWithPause("http://oss.ucdl.pp.uc.cn/fs01/union_pack/Wandoujia_110644_web_direct_binded.apk?x-oss-process=udf/pp-udf,Jjc3LiMnJ3FxdnJ1fnE=", new DownloadCallback() {
                        @Override
                        public void onDownloadSuccess(String filePath) {
                            Toast.makeText(DownloadFileActivity.this,"下载成功",Toast.LENGTH_SHORT).show();
                            btn.setText("下载完成");
                            btn.setClickable(false);
                        }

                        @Override
                        public void onUpdate(long bytesRead, long contentLength, boolean done) {
                            progressBar.setProgress((int)((float)bytesRead/(float)contentLength*100));
                        }

                        @Override
                        public void onDownloadFail() {
                            if(!isDownloading) return;
                            Toast.makeText(DownloadFileActivity.this,"下载失败",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                isDownloading=!isDownloading;
            }
        });
    }

}
