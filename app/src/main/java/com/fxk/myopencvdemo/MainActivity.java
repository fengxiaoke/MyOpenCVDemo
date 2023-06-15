package com.fxk.myopencvdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView iv_old;
    ImageView iv_new;
    Button btn_gray;
    Button btn_jump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLoadOpenCV();
        iv_old = (ImageView) findViewById(R.id.iv_old);
        iv_new = (ImageView) findViewById(R.id.iv_new);
        btn_gray = (Button) findViewById(R.id.btn_gray);
        btn_jump = (Button) findViewById(R.id.btn_jump);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.doreamen);
        iv_old.setImageBitmap(bitmap);
        btn_gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RGB2GRAY(iv_old,bitmap);
            }
        });
        iv_old.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_old.setImageBitmap(bitmap);
            }
        });
        btn_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));
            }
        });

    }

    public void RGB2GRAY(ImageView iv,Bitmap bitmap) {
        Mat src = new Mat();
        Mat dst = new Mat();
        try {
            src = Utils.loadResource(getApplicationContext(),R.mipmap.doreamen);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Utils.bitmapToMat(bitmap,src);
        //转换为灰度图模式
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_RGB2GRAY);
        bitmap = Bitmap.createBitmap(dst.width(),dst.height(),Bitmap.Config.ARGB_8888);
        //把mat转换回bitmap
        Utils.matToBitmap(dst,bitmap);
        iv.setImageBitmap(bitmap);
        src.release();
        dst.release();
    }


    public void initLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Log.d("init", "initLoadOpenCV: openCV load success");
        } else {
            Log.e("init", "initLoadOpenCV: openCV load failed");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permission.checkPermission(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Permission.isPermissionGranted(this)) {
            Log.i("PERMISSION","请求权限成功");
        }
    }
}