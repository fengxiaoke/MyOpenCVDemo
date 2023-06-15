package com.fxk.myopencvdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import android_serialport_api.SerialPort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class MainActivity4 extends AppCompatActivity {

    TextView tv_status,tv_content;

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private byte[] mBuffer;
    private BufferedReader mBufferedReader;

    private SendingThread mSendingThread;
    private byte[] mSendBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        tv_status = findViewById(R.id.tv_status);
        tv_content = findViewById(R.id.tv_content);

        findViewById(R.id.btn_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //mSerialPort = new SerialPort(new File("/dev/ttyS0"), 9600,0);
                    mSerialPort = startSerialPort(mSerialPort);
                    mOutputStream = mSerialPort.getOutputStream();
                    mInputStream = mSerialPort.getInputStream();
                    mReadThread = new ReadThread();
                    mReadThread.start();
                    tv_status.setText("open");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSendBuffer = new byte[1024];
//                Arrays.fill(mSendBuffer, (byte) 0x55);
                if (mSerialPort != null) {
                    mSendingThread = new SendingThread();
                    mSendingThread.start();
                }
            }
        });
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSerialPort(mSerialPort);
                try {
                    if (mInputStream!=null){
                        mInputStream.close();
                        mInputStream = null;
                    }
                    if (mOutputStream!=null){
                        mOutputStream.close();
                        mOutputStream = null;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                tv_status.setText("close");
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mReadThread!=null){
            mReadThread.interrupt();
        }
        super.onDestroy();
    }

    private SerialPort startSerialPort(SerialPort serialPort) throws SecurityException, IOException, InvalidParameterException {
        if (serialPort==null){
            /* Read serial port parameters */
//            SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
//            String path = sp.getString("DEVICE", "");
//            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
            String path = "/dev/ttyS1";//指定端口
            int baudrate = 9600;//指定速率
            /* Check parameters */
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            serialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return serialPort;
    }

    private void closeSerialPort(SerialPort serialPort){
        if (serialPort!=null){
            serialPort.close();
            serialPort = null;
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            Log.e("ReadThread","start");
            while(!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[256];
                    if (mInputStream == null) return;
                    //mBufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        //处理串口
                        Log.i("ReadThread","receiving"+buffer[0]);
                        onDateReceived(buffer,size);
                    }else{
                        Log.e("ReadThread","receiving.........");
                    }
                    try {
                        Thread.sleep(50);//延时50ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    Log.e("ReadThread",e.getMessage()+"....");
                    return;
                }
            }
        }
    }

    private class SendingThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(new String("send").getBytes());
                        mOutputStream.flush();
                        interrupt();
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void onDateReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_content.append(new String(buffer,0,size));
            }
        });
    }

}