package com.fxk.myopencvdemo;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fxk.bsdiff.NativeLib;
import com.fxk.bsdiff.utils.FxkBsDiffUtil;

import java.io.File;

import androidx.lifecycle.ViewModel;
import kotlin.time.MeasureTimeKt;

/**
 * @author fenxi
 * @date 2023/3/10
 * @time 10:29
 */
public class BsDiffViewModel{

    /*文件后缀名*/
    private static String suffix = "apk";
    /*旧文件*/
    private static File oldFile = new File(PathUtils.getInternalAppFilesPath(), String.format("old.%s", suffix));
    /*新文件*/
    private static File newFile = new File(PathUtils.getInternalAppFilesPath(), String.format("new.%s", suffix));
    /*补丁文件*/
    private static File patchFile = new File(PathUtils.getInternalAppFilesPath(), String.format("patch.%s", suffix));
    /*合并后的文件*/
    private static File combineFile = new File(PathUtils.getInternalAppFilesPath(), String.format("combine.%s", suffix));

    public static void fileDiff(){

        long start = System.currentTimeMillis();
        if (!oldFile.exists() || !newFile.exists()) {
            ToastUtils.showShort("对比包缺失");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    new FxkBsDiffUtil().diff(newFile.getAbsolutePath(),oldFile.getAbsolutePath(),patchFile.getAbsolutePath());
                    long measureTimeMillis = System.currentTimeMillis() - start;

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

    public static void filePatch(){

        long start = System.currentTimeMillis();
        if (!oldFile.exists() || !newFile.exists()) {
            ToastUtils.showShort("补丁文件或旧文件缺失");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FxkBsDiffUtil().patch(oldFile.getAbsolutePath(),patchFile.getAbsolutePath(),combineFile.getAbsolutePath());
                long measureTimeMillis = System.currentTimeMillis() - start;

                LogUtils.i(String.format("合并补丁文件耗时:%d", measureTimeMillis));
                LogUtils.i("newFile MD5:" + FileUtils.getFileMD5ToString(newFile));
                LogUtils.i("combineFile MD5:" + FileUtils.getFileMD5ToString(combineFile));
            }
        }).start();

    }

}
