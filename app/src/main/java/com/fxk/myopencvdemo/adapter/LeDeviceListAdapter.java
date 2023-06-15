package com.fxk.myopencvdemo.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fxk.myopencvdemo.R;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;

/**
 * @author fenxi
 * @date 2023/4/4
 * @time 14:20
 */
public class LeDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothMessage> mLeDevices;
    private Context mContext;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<>();
        mContext = context;
    }

    public void addDevice(BluetoothMessage message) {
        for (BluetoothMessage bluetoothMessage : mLeDevices) {
            if (bluetoothMessage.getDevice().getAddress().equals(message.getDevice().getAddress())) {
                return;
            }
        }
        mLeDevices.add(message);
    }

    public BluetoothMessage getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        BluetoothMessage bluetoothMessage = mLeDevices.get(i);
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_device_layout, null);
        TextView tv_name = itemView.findViewById(R.id.tv_name);
        TextView tv_address = itemView.findViewById(R.id.tv_address);
        tv_name.setText("Rssi:"+bluetoothMessage.getRssi());
        tv_address.setText(bluetoothMessage.getDevice().getName()+"--"+bluetoothMessage.getDevice().getAddress());
        return itemView;
    }
}
