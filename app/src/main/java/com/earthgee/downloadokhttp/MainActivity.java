package com.earthgee.downloadokhttp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.earthgee.downloadokhttp.download.FileUploader;
import com.earthgee.downloadokhttp.download.UploadCallback;

import java.io.File;
import java.util.HashMap;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1= (Button) findViewById(R.id.download_pic);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,DownloadPictureActivity.class));
            }
        });

        Button btn2= (Button) findViewById(R.id.upload_pic);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imgFile=new File(Environment.getExternalStorageDirectory(),"test");
                FileUploader.getInstance().uploadFile("http://api.nohttp.net/upload", imgFile.getAbsolutePath(), new UploadCallback() {
                    @Override
                    public void onUploadSuccess() {
                        Toast.makeText(MainActivity.this,
                                "上传成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUploadFail() {
                        Toast.makeText(MainActivity.this,
                                "上传失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button btn3= (Button) findViewById(R.id.upload_pic_with_params);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imgFile=new File(Environment.getExternalStorageDirectory(),"test");
                HashMap<String,String> params=new HashMap<>();
                params.put("test1","test");
                params.put("name","earthgee");
                FileUploader.getInstance().uploadFile("http://api.nohttp.net/upload", params,
                        imgFile.getAbsolutePath(), new UploadCallback() {
                    @Override
                    public void onUploadSuccess() {
                        Toast.makeText(MainActivity.this,
                                "上传成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUploadFail() {
                        Toast.makeText(MainActivity.this,
                                "上传失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
