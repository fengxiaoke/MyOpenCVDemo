package com.fxk.myopencvdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    Button btn_jump;

    //OpenCV的相机接口
    private CameraBridgeViewBase mCVCamera;
    //缓存相机每帧输入的数据
    private Mat mRgba;
    CascadeClassifier classifier;
    int mAbsoluteFaceSize = 200;
    int fps = 0;

    List<Rect> facesCache = new ArrayList<>();

    ImageView iv;

    int cameraindex = CameraBridgeViewBase.CAMERA_ID_BACK;

    /**通过OpenCV管理Android服务，初始化OpenCV**/
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("TAG", "OpenCV loaded successfully");
                    mCVCamera.enableView();
                    break;
                default:break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mCVCamera = (CameraBridgeViewBase) findViewById(R.id.camera);
        mCVCamera.setVisibility(CameraBridgeViewBase.VISIBLE);
        btn_jump = (Button) findViewById(R.id.btn_jump);
        iv = (ImageView) findViewById(R.id.iv);
        mCVCamera.setCvCameraViewListener(this);
        mCVCamera.setCameraIndex(cameraindex);

        btn_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRgba!=null){
                    if (!mRgba.empty()){
                        Mat inter = new Mat(mRgba.width(), mRgba.height(), CvType.CV_8UC4);
                        Mat dst = new Mat(mRgba.width(), mRgba.height(), CvType.CV_8UC4);
                        //将四通道的RGBA转为三通道的BGR，重要！！
                        //Imgproc.cvtColor(mRgba, inter, Imgproc.COLOR_RGBA2BGR);
                        Imgproc.cvtColor(mRgba, inter, Imgproc.COLOR_RGBA2GRAY);
//                        Imgproc.threshold(inter,dst,125,255,Imgproc.THRESH_BINARY);
                        Imgproc.adaptiveThreshold(inter,dst,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,13,5);
                        Bitmap bitmap = Bitmap.createBitmap(inter.width(),inter.height(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(dst,bitmap);
                        iv.setImageBitmap(bitmap);
//                        File sdDir = null;
//                        //判断是否存在机身内存
//                        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
//                        if(sdCardExist) {
//                            //获得机身储存根目录
//                            sdDir = Environment.getExternalStorageDirectory();
//                        }
//                        //将拍摄准确时间作为文件名
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                        String filename = sdf.format(new Date());
//                        String filePath = sdDir + "/Pictures/OpenCV/" + filename + ".png";
//                        //将转化后的BGR矩阵内容写入到文件中
//                        Imgcodecs.imwrite(filePath, inter);
//                        Toast.makeText(MainActivity2.this, "图片保存到: "+ filePath, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        Log.d("TAG", "OpenCV onCameraViewStarted");
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        Log.d("TAG", "OpenCV onCameraViewStopped");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();  //一定要有！！！不然数据保存不进MAT中！！！
        //判断横竖屏用于进行图像的旋转
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //判断是前置摄像头还是后置摄像头,翻转的角度不一样
            switch (cameraindex) {
                case CameraBridgeViewBase.CAMERA_ID_FRONT:
                    Core.rotate(mRgba, mRgba, Core.ROTATE_90_COUNTERCLOCKWISE);
                    break;
                case CameraBridgeViewBase.CAMERA_ID_BACK:
                    Core.rotate(mRgba, mRgba, Core.ROTATE_90_CLOCKWISE);
                    break;
                default:
                    Core.rotate(mRgba, mRgba, Core.ROTATE_90_CLOCKWISE);
                    break;
            }
            //把旋转后的Mat图像根据摄像头屏幕的大小进行缩放
            Size size = new Size(mCVCamera.getWidth(), mCVCamera.getHeight());
            Imgproc.resize(mRgba, mRgba, size);
            Imgproc.putText(mRgba,"Hello World",new Point(mCVCamera.getWidth()/5,mCVCamera.getHeight()*0.9),
                    2,3,new Scalar(0,255,0),3);

            //隔6帧进行一次人脸检测
            if (fps == 6) {
                float mRelativeFaceSize = 0.2f;
                if (mAbsoluteFaceSize == 0) {
                    int height = mRgba.rows();
                    if (Math.round(height * mRelativeFaceSize) > 0) {
                        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                    }
                }
                MatOfRect faces = new MatOfRect();
                if (classifier != null) {
                    classifier.detectMultiScale(mRgba, faces, 1.05, 2, 2,
                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                }
                facesCache = faces.toList();
                fps = 0;
            }
            for (Rect rect:facesCache){
                Imgproc.rectangle(mRgba, rect.tl(), rect.br(), new Scalar(0,255,0), 3);
            }

                fps++;

//            Rect[] facesArray = faces.toArray();
//            for (int i = 0; i < facesArray.length; i++) {
//                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0,255,0), 2);
//            }
        }
        //直接返回输入视频预览图的RGB数据并存放在Mat数据中
        return mRgba;
    }

    @Override
    protected void onResume() {
        if(Permission.isPermissionGranted(this)) {
            Log.i("PERMISSION","请求权限成功");
        }
        /***强制横屏***/
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else {
            if(!OpenCVLoader.initDebug()) {
                Log.d("TAG", "OpenCV library not found!");
            } else {
                Log.d("TAG", "OpenCV library found inside package. Using it!");
                if (Permission.checkPermission(this)){
                    mCVCamera.setCameraPermissionGranted();
                }
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }

        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initClassifier();
    }

    public void initClassifier() {
        try {
            //读取存放在raw的文件
            InputStream is = getResources()
                    .openRawResource(R.raw.lbpcascade_frontalface_improved);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir,"lbpcascade_frontalface_improved.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while((bytesRead = is.read(buffer))!=-1){
                os.write(buffer,0,bytesRead);
            }
            is.close();
            os.close();
            //通过classifier来操作人脸检测， 在外部定义一个CascadeClassifier classifier，做全局变量使用
            classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
            cascadeFile.delete();
            cascadeDir.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        if (mCVCamera!=null){
            mCVCamera.disableView();
        }
        super.onDestroy();
    }
}