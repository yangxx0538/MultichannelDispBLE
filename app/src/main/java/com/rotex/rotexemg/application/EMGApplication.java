package com.rotex.rotexemg.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.rotex.rotexemg.ble.BleService;

/**
 * Created by QingWuyong on 2016/11/29.
 */

public class EMGApplication extends Application {
    BleService mBleService;
   static EMGApplication instance=null;

    //UUID定义
    public final static String UUID_SERVICE = "acdcdcd0-0451-9d97-cc4b-f5a1b93e25ba";

    public final static String UUID_RX_CHAR = "acdcdcd2-0451-9d97-cc4b-f5a1b93e25ba";
    public final static String UUID_WRITE_CHAR = "acdcdcd1-0451-9d97-cc4b-f5a1b93e25ba";
    public final static String UUID_RX_DES = "00002902-0000-1000-8000-00805f9b34fb";
    public final static String UUID_TX_CHAR = "0783b03e-8535-b5a0-7140-a304d2495cba";
    public static EMGApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.instance = this;
        doBindService();
    }

    public  void setclose() {
        doUnBindService();
        System.exit(0);
    }

    /**
     * 绑定服务
     */
    private void doBindService() {
        Intent serviceIntent = new Intent(this, BleService.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        mIsBind = true;
    }

    /**
     * 解绑服务
     */
    private void doUnBindService() {
        if (mIsBind) {
            unbindService(mServiceConnection);
            mBleService = null;
            mIsBind = false;
        }
    }

    private boolean mIsBind = false;
    /**
     * UART service connected/disconnected
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder rawBinder) {
            mBleService = ((BleService.LocalBinder) rawBinder).getService();
            if (mBleService != null) {
                //      mHandler.sendEmptyMessage(SERVICE_BIND);

            }
            if (!mBleService.initialize()) {
                Log.e("mService", "为空");
                return;
            }

            if (mBleService.enableBluetooth(true)) {
                Log.e("mService", "不为空");
                //   connectBleService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
            mIsBind = false;
        }
    };

}
