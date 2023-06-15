package com.fxk.myopencvdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fxk.myopencvdemo.adapter.BluetoothMessage;
import com.fxk.myopencvdemo.adapter.LeDeviceListAdapter;

import java.util.List;

public class MainActivity5 extends AppCompatActivity {

    final int REQUEST_CODE = 1;
    final int REQUEST_ENABLE_BT = 2;
    final int REQUEST_ENABLE_SCAN = 3;
    TextView tv_feature, tv_open;
    Button btn_openBle,btn_scan;
    ListView lv_device;

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ScanSettings mSetting;

    private boolean mScanning;
    private boolean scan_flag = true;
    private LeDeviceListAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        tv_feature = findViewById(R.id.tv_feature);
        tv_open = findViewById(R.id.tv_open);
        btn_openBle = findViewById(R.id.btn_openBle);
        btn_scan = findViewById(R.id.btn_scan);
        lv_device = findViewById(R.id.lv_device);

        mDeviceAdapter = new LeDeviceListAdapter(getApplicationContext());
        lv_device.setAdapter(mDeviceAdapter);

        btn_openBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity5.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity5.this,new String[]{Manifest.permission.BLUETOOTH_CONNECT},REQUEST_CODE);
                }
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent,REQUEST_ENABLE_BT);
                }else{
                    bluetoothAdapter.disable();
                    btn_openBle.setText("open");
                    tv_open.setText("BLE disabled");
                }
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanDevice(scan_flag);
            }
        });

        if (checkSupport()) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            } else {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
                tv_open.setText("BLE disabled");
                btn_openBle.setText("open");
            }else{
                tv_open.setText("BLE enabled");
                btn_openBle.setText("close");
            }

            ScanSettings.Builder builder = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED);
            if (Build.VERSION.SDK_INT >= 23){
                builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
                builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
            }
            //芯片组支持批处理芯片上的扫描
            if (bluetoothAdapter.isOffloadedScanBatchingSupported()) {
                //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
                //设置为0以立即通知结果
                builder.setReportDelay(0L);
            }
            mSetting = builder.build();
        }



    }

    private void scanDevice(boolean b) {
        if (b){
            mScanning = true;
            scan_flag = false;
            btn_scan.setText("stop scan");
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.S&&ActivityCompat.checkSelfPermission(MainActivity5.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity5.this,new String[]{Manifest.permission.BLUETOOTH_SCAN},REQUEST_ENABLE_SCAN);
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    bluetoothLeScanner.startScan(null,mSetting,mCallback);
                }else{
                    bluetoothAdapter.startLeScan(mLeCallback);
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scan_flag = true;
                    btn_scan.setText("scan");
                    if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.S&&ActivityCompat.checkSelfPermission(MainActivity5.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity5.this,new String[]{Manifest.permission.BLUETOOTH_SCAN},REQUEST_CODE);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        bluetoothLeScanner.stopScan(mCallback);
                    }else{
                        bluetoothAdapter.stopLeScan(mLeCallback);
                    }
                }
            },20*1000);
        }else{
            Log.i("stop","Stopping.........");
            mScanning = false;
            btn_scan.setText("scan");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                bluetoothLeScanner.stopScan(mCallback);
            }else{
                bluetoothAdapter.stopLeScan(mLeCallback);
            }

            scan_flag = true;
        }
    }

    private boolean checkSupport() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            tv_feature.setText("support BLE");
            return true;
        }else{
            tv_feature.setText("no support BLE");
            return false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(Permission.isBlePermissionGranted(this)) {
            Log.i("PERMISSION","请求BLE权限成功");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE){
            if (grantResults.length!=0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
            }else{
                Toast.makeText(this, "蓝牙连接权限开启失败", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQUEST_ENABLE_SCAN){
            if (grantResults.length!=0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "蓝牙扫描权限开启成功", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "蓝牙扫描权限开启失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_CANCELED){
                tv_open.setText("BLE disabled");
                btn_openBle.setText("open");
            }else{
                tv_open.setText("BLE enabled");
                btn_openBle.setText("close");
            }
        }
    }

    private ScanCallback mCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothMessage message = new BluetoothMessage(result.getDevice());
            message.setRssi(result.getRssi());
            message.setName(result.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDeviceAdapter.addDevice(message);
                    mDeviceAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothMessage message = new BluetoothMessage(bluetoothDevice);
                    mDeviceAdapter.addDevice(message);
                    mDeviceAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}