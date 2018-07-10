package com.rotex.rotexemg.ble;

/**
 * Created by QingWuyong on 2016/6/21.
 */

public interface Constants {
    //Connection state
    int STATE_DISCONNECTED = 0;
    int STATE_CONNECTING = 1;
    int STATE_CONNECTED = 2;
    int STATE_DISCONNECTING = 3;

    //Action
    String ACTION_GATT_DISCONNECTED = "com.bltech.BleLib.ACTION_GATT_DISCONNECTED";
    String ACTION_GATT_CONNECTING = "com.bltech.BleLib.ACTION_GATT_CONNECTING";
    String ACTION_GATT_CONNECTED = "com.bltech.BleLib.ACTION_GATT_CONNECTED";
    String ACTION_GATT_DISCONNECTING = "com.bltech.BleLib.ACTION_GATT_DISCONNECTING";
    String ACTION_GATT_SERVICES_DISCOVERED = "com.bltech.BleLib.ACTION_GATT_SERVICES_DISCOVERED";
    String ACTION_BLUETOOTH_DEVICE = "com.bltech.BleLib.ACTION_BLUETOOTH_DEVICE";
    String ACTION_SCAN_FINISHED = "com.bltech.BleLib.ACTION_SCAN_FINISHED";
}
