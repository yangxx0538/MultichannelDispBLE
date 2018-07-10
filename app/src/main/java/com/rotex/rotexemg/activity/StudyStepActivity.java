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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rotex.rotexhand.R;
import com.rotex.rotexemg.base.BaseActivity;
import com.rotex.rotexemg.ble.BleService;
import com.rotex.rotexemg.views.ProgressView;
import com.sdsmdg.tastytoast.TastyToast;

import static com.rotex.rotexemg.application.EMGApplication.UUID_RX_CHAR;
import static com.rotex.rotexemg.application.EMGApplication.UUID_SERVICE;
import static com.rotex.rotexemg.application.EMGApplication.UUID_WRITE_CHAR;

/**
 * Created by QingWuyong on 2016/12/1.
 */

public class StudyStepActivity extends BaseActivity {
    ProgressView progressView1;
    ProgressView progressView2;
    ViewFlipper viewFlipper;
    ImageView mImgOk1;
    ImageView mImgOk2;
    ImageView mImgOk3;
    ImageView mImgOk4;
    LinearLayout mLiStep;
    TextView mTvBtnStr;
    BleService mBleService;

    boolean isStartStudy = false;

    static int RESET_DEVICE = 0xaa;

    final int STUDY_MSG = 0x001;
    boolean isShowSuccese = false;
    boolean isJumpChildren1 = false;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STUDY_MSG:
                    if (data7 == 0x01) {
                        mImgOk4.setImageResource(R.mipmap.yes);
                        mLiStep.setVisibility(View.GONE);
                        if (!isShowSuccese) {
                            TastyToast.makeText(StudyStepActivity.this, "学习成功", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                            isShowSuccese = true;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(StudyStepActivity.this, EmgWaveActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1000);
                        }

                    } else {
                        if (Data8 == 0x01) {
                            Log.e("xuexi","学习第一步");
                        } else if (Data8 == 0x02) {
                            Log.e("xuexi","学习第2步");
                            mImgOk1.setImageResource(R.mipmap.yes);

                        } else if (Data8 == 0x11) {
                            Log.e("xuexi","学习第3步");
                            mImgOk2.setImageResource(R.mipmap.yes);

                            handler.sendEmptyMessageDelayed(0x0011,1000);



                        } else if (Data8 == 0x12) {
                            Log.e("xuexi","学习第4步");
                            mImgOk3.setImageResource(R.mipmap.yes);

                        }

                    }

                    break;
                case 0x0011:
                    if (!isJumpChildren1) {
                        viewFlipper.setDisplayedChild(1);
                        // 设定ViewFlipper的时间间隔
                        viewFlipper.setFlipInterval(100);
                        isJumpChildren1 = true;
                    }
                    break;
            }
        }
    };


    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_study_step);
    }

    @Override
    protected void initView() {
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        progressView1 = (ProgressView) findViewById(R.id.pro_view);
        progressView1.setMaxCount(100f);
        progressView1.setCurrentCount(100f);
        progressView2 = (ProgressView) findViewById(R.id.pro_view1);
        progressView2.setMaxCount(100f);
        progressView2.setCurrentCount(100f);
        mImgOk1 = (ImageView) findViewById(R.id.img_ok1);
        mImgOk2 = (ImageView) findViewById(R.id.img_ok2);
        mImgOk3 = (ImageView) findViewById(R.id.img_ok3);
        mImgOk4 = (ImageView) findViewById(R.id.img_ok4);
        mLiStep = (LinearLayout) findViewById(R.id.emg_step_btn);
        mTvBtnStr = (TextView) findViewById(R.id.emg_tv_btnsting);
        setListener(mLiStep);
        mBleService = BleService.getInstance();
        if (mBleService == null) {
            Log.e("tag", "StudyStrp mbleservice 为空");
        }else {
            Log.e("tag", "StudyStrp mbleservice bu为空");
        }
        setBleServiceListener();

        viewFlipper.setInAnimation(this, R.anim.hyperspace_in);
        viewFlipper.setOutAnimation(this, R.anim.hyperspace_out);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.emg_step_btn:


                isStartStudy = true;
                if (mTvBtnStr.getText().equals("开始学习")) {
                    if (mBleService == null) {

                        Log.e("hahah", "null1111111");
                        return;
                    }
                    try {
                        Log.e("Exception", "指令发送...");
                    //    mBleService.writeCharacteristic(UUID_SERVICE, UUID_WRITE_CHAR,int2Bytes(RESET_DEVICE));
                        mTvBtnStr.setText("下一步");
                        mLiStep.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Log.e("Exception", "指令发送失败");
                        TastyToast.makeText(this,"指令发送失败",TastyToast.LENGTH_LONG,TastyToast.ERROR);
                    }

                } else if (mTvBtnStr.getText().equals("下一步")) {
//                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.hyperspace_in));
//                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.hyperspace_out));

                    viewFlipper.setDisplayedChild(1);
                    viewFlipper.setAnimation(AnimationUtils.loadAnimation(this, R.anim.hyperspace_in));
                }

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
//        mBleService.setOnConnectListener(new BleService.OnConnectionStateChangeListener() {
//            @Override
//            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//
//                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
//
//                } else if (newState == BluetoothProfile.STATE_CONNECTED) {
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
//
//                    //ToastUttils.showShortToast(MainActivity.this,"掉线："+gatt.getDevice().getAddress());
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//
//                }
//            }
//        });
//
//        mBleService.setOnServicesDiscoveredListener(new BleService.OnServicesDiscoveredListener() {
//            @Override
//            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                // if (status == BluetoothGatt.GATT_SUCCESS) {
//                mBleService.setCharacteristicNotification(UUID_SERVICE, UUID_RX_CHAR, true);
//            }
//        });

        mBleService.setOnDataAvailableListener(new BleService.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {


            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.e("aaaa", "characteristic   " + characteristic.getValue());
                if (isStartStudy) {
                    decodeDataManyDevice(characteristic.getValue());
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            }
        });
    }

    int eletricityPre;
    int data7, Data8;
    void decodeDataManyDevice(byte[] data) {
        if ((data[0] & 0xff) == 127&&(data[1] & 0xff) == 247) {
//            byte[] one = new byte[1];
//            one[0] = data[4];
//            one[1] = data[3];
//            byte[] two = new byte[2];
//            two[0] = data[1];
//            two[1] = data[2];
            //  Log.e(TAG, "value  " + "   " + byte2Int(two));
            Message msg = new Message();
            msg.what = STUDY_MSG;
            //  msg.arg1 = byte2Int(one);
//            msg.arg1 = data[6] & 0xff;
//            msg.arg2 = data[7] & 0xff;
            data7 = data[7] & 0xff;
            Data8 = data[8] & 0xff;
            eletricityPre = data[9] & 0xff;
            handler.sendMessage(msg);

        }

    }

    public int byte2Int(byte[] b) {
        int intValue = 0;
        for (int i = 0; i < b.length; i++) {
            intValue += (b[i] & 0xFF) << (8 * (1 - i));
        }
        return intValue;
    }
}
