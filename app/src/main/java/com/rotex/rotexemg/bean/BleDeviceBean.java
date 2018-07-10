package com.rotex.rotexemg.bean;

/**
 * Created by QingWuyong on 2016/11/28.
 */
public class BleDeviceBean {
    String mac;
    int rssi;
    String name;

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

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "BleDeviceBean{" +
                "mac='" + mac + '\'' +
                ", rssi=" + rssi +
                ", name='" + name + '\'' +
                '}';
    }
}
