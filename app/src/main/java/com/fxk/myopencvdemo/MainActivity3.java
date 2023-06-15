package com.fxk.myopencvdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fxk.bsdiff.utils.FxkBsDiffUtil;

import java.io.File;

public class MainActivity3 extends AppCompatActivity {

    TextView tv_old;
    TextView tv_new;
    TextView tv_patch;
    TextView tv_difftime;
    TextView tv_patchtime;
    TextView tv_newMd5;
    TextView tv_combineMd5;
    Button btn_diff;
    Button btn_patch;
    MainViewModel vm ;
    long measureTimeMillis;

    private String suffix = "apk";
    /*旧文件*/
    private File oldFile = new File(PathUtils.getInternalAppFilesPath(), String.format("old.%s", suffix));
    /*新文件*/
    private File newFile = new File(PathUtils.getInternalAppFilesPath(), String.format("new.%s", suffix));
    /*补丁文件*/
    private File patchFile = new File(PathUtils.getInternalAppFilesPath(), String.format("patch.%s", suffix));
    /*合并后的文件*/
    private File combineFile = new File(PathUtils.getInternalAppFilesPath(), String.format("combine.%s", suffix));

    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:

                    tv_difftime.setText(String.format("生成补丁文件耗时:%d", measureTimeMillis));
                    tv_old.setText("oldFileSize:" + FileUtils.getSize(oldFile));
                    tv_new.setText("newFileSize:" + FileUtils.getSize(newFile));
                    tv_patch.setText("patchFileSize:" + FileUtils.getSize(patchFile));
                    break;
                case 2:
                    tv_patchtime.setText(String.format("合并补丁文件耗时:%d", measureTimeMillis));
                    tv_newMd5.setText("newFile MD5:\n" + FileUtils.getFileMD5ToString(newFile));
                    tv_combineMd5.setText("combineFile MD5:\n" + FileUtils.getFileMD5ToString(combineFile));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        tv_old = findViewById(R.id.tv_old);
        tv_new = findViewById(R.id.tv_new);
        tv_patch = findViewById(R.id.tv_patch);
        tv_difftime = findViewById(R.id.tv_difftime);
        tv_patchtime = findViewById(R.id.tv_patchtime);
        tv_newMd5 = findViewById(R.id.tv_newMd5);
        tv_combineMd5 = findViewById(R.id.tv_combineMd5);
        btn_diff = findViewById(R.id.btn_diff);
        btn_patch = findViewById(R.id.btn_patch);
        vm = new ViewModelProvider(this).get(MainViewModel.class);

        btn_diff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //vm.fileDiff();
                //BsDiffViewModel.fileDiff();
                fileDiff();
            }
        });

        btn_patch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //vm.filePatch();
                //BsDiffViewModel.filePatch();
                filePatch();
            }
        });
    }

    private void fileDiff(){

        long start = System.currentTimeMillis();
        if (!oldFile.exists() || !newFile.exists()) {
            ToastUtils.showShort("对比包缺失");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    new FxkBsDiffUtil().diff(newFile.getAbsolutePath(),oldFile.getAbsolutePath(),patchFile.getAbsolutePath());
                    measureTimeMillis = System.currentTimeMillis() - start;
                    handler.sendEmptyMessage(1);
                    LogUtils.i(String.format("生成补丁文件耗时:%d", measureTimeMillis));
                    LogUtils.i("oldFileSize:" + FileUtils.getSize(oldFile));
                    LogUtils.i("newFileSize:" + FileUtils.getSize(newFile));
                    LogUtils.i("patchFileSize:" + FileUtils.getSize(patchFile));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void filePatch(){

        long start = System.currentTimeMillis();
        if (!oldFile.exists() || !newFile.exists()) {
            ToastUtils.showShort("补丁文件或旧文件缺失");
        }
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new FxkBsDiffUtil().patch(oldFile.getAbsolutePath(),patchFile.getAbsolutePath(),combineFile.getAbsolutePath());
                    measureTimeMillis = System.currentTimeMillis() - start;
                    handler.sendEmptyMessage(2);
                    LogUtils.i(String.format("合并补丁文件耗时:%d", measureTimeMillis));
                    LogUtils.i("newFile MD5:" + FileUtils.getFileMD5ToString(newFile));
                    LogUtils.i("combineFile MD5:" + FileUtils.getFileMD5ToString(combineFile));
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}