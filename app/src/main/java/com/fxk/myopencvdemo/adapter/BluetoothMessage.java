package com.fxk.myopencvdemo.adapter;

import android.bluetooth.BluetoothDevice;

/**
 * @author fenxi
 * @date 2023/4/4
 * @time 14:24
 */
public class BluetoothMessage {

    private String name;
    private String mac;
    private int rssi;
    private BluetoothDevice device;

    public BluetoothMessage(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
