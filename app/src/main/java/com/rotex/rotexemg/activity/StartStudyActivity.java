package com.rotex.rotexemg.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.rotex.rotexhand.R;
import com.rotex.rotexemg.base.BaseActivity;
import com.rotex.rotexemg.ble.BleService;
import com.sdsmdg.tastytoast.TastyToast;

import static com.rotex.rotexemg.activity.StudyStepActivity.RESET_DEVICE;
import static com.rotex.rotexemg.application.EMGApplication.UUID_RX_CHAR;
import static com.rotex.rotexemg.application.EMGApplication.UUID_SERVICE;
import static com.rotex.rotexemg.application.EMGApplication.UUID_WRITE_CHAR;

/**
 * Created by QingWuyong on 2016/11/30.
 */

public class StartStudyActivity extends BaseActivity {
    Button mBtStartStudy;
    Button mBtPass;
    BleService mBleService;
    final int IS_STUDY_ACTION = 0x001;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case IS_STUDY_ACTION:
                    if (((int)msg.obj) == 0x01) {

                    }else{
                        mBtPass.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };


    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_start_study);
    }

    @Override
    protected void initView() {
        mBtStartStudy = (Button) findViewById(R.id.btn_start_study);
        mBtPass = (Button) findViewById(R.id.btn_pass);
        setListener(mBtStartStudy,mBtPass);
        mBleService = BleService.getInstance();
        setBleServiceListener();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_study:
                try {
                    Log.e("Exception", "指令发送...");
                    mBleService.writeCharacteristic(UUID_SERVICE, UUID_WRITE_CHAR,int2Bytes(0xaa));
                } catch (Exception e) {
                    Log.e("Exception", "指令发送失败");
                    TastyToast.makeText(this,"指令发送失败",TastyToast.LENGTH_LONG,TastyToast.ERROR);
                }
                Intent intent = new Intent(StartStudyActivity.this, StudyStepActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_pass:
                Intent intent1 = new Intent(StartStudyActivity.this, EmgWaveActivity.class);
                startActivity(intent1);
                finish();
                break;
        }
    }

    public byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[1];
//        for (int ix = 0; ix < 1; ++ix) {
//            int offset = 32 - (ix + 1) * 8;
//            byteNum[ix] = (byte) ((num >> offset) & 0xff);
//        }
        byteNum[0] = (byte) num;
        return byteNum;
    }

    private void setBleServiceListener() {
        mBleService.setOnConnectListener(new BleService.OnConnectionStateChangeListener() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                } else if (newState == BluetoothProfile.STATE_CONNECTING) {

                } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {

                    //ToastUttils.showShortToast(MainActivity.this,"掉线："+gatt.getDevice().getAddress());
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                }
            }
        });

        mBleService.setOnServicesDiscoveredListener(new BleService.OnServicesDiscoveredListener() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                // if (status == BluetoothGatt.GATT_SUCCESS) {
                mBleService.setCharacteristicNotification(UUID_SERVICE, UUID_RX_CHAR, true);
            }
        });

        mBleService.setOnDataAvailableListener(new BleService.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {


            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] date = characteristic.getValue();
                decodeDataManyDevice(date);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            }
        });
    }
    void decodeDataManyDevice(byte[] data) {
        if ((data[0] & 0xff) == 127&&(data[1] & 0xff) == 247) {
//            byte[] one = new byte[1];
//            one[0] = data[4];
//            one[1] = data[3];
            byte[] two = new byte[2];
            two[0] = data[2];
            two[1] = data[3];
            //  Log.e(TAG, "value  " + "   " + byte2Int(two));
            Message msg = new Message();
            msg.what = IS_STUDY_ACTION;
            msg.obj= data[7] & 0xff;
            //  Log.e(TAG, "Electricity=" + Electricity);
            handler.sendMessage(msg);

        }

    }


    public  int byte2Int(byte[] b) {
        int intValue = 0;
        for (int i = 0; i < b.length; i++) {
            intValue += (b[i] & 0xFF) << (8 * (1 - i));
        }
        return intValue;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  mBleService = null;
    }
}
