package com.rotex.rotexemg.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daasuu.bl.BubbleLayout;
import com.daasuu.bl.BubblePopupHelper;
import com.rotex.loopview.LoopView;
import com.rotex.rotexemg.BubbleText.BubbleView;
import com.rotex.rotexemg.BubbleText.LeBubbleTextViewHelper;
import com.rotex.rotexemg.BubbleText.LeBubbleTitleTextView;
import com.rotex.rotexhand.R;
import com.rotex.rotexemg.base.BaseActivity;
import com.rotex.rotexemg.bean.BleDeviceBean;
import com.rotex.rotexemg.ble.BleService;
import com.rotex.rotexemg.utils.DensityUtil;
import com.rotex.rotexemg.views.DialogView;
import com.sdsmdg.tastytoast.TastyToast;
import com.skyfishjy.library.RippleBackground;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.rotex.rotexemg.application.EMGApplication.UUID_RX_CHAR;
import static com.rotex.rotexemg.application.EMGApplication.UUID_SERVICE;

/**
 * Created by QingWuyong on 2016/11/24.
 */

public class SercthBleActivity extends BaseActivity {
    final String TAG = "SercthBleActivity";
    LinearLayout mLiClick;
    RelativeLayout mReRi;
    TextView mTvBleName;
    RippleBackground rippleBackground;
    //  private LeBubbleTextViewHelper helper;
    private boolean inited;
    int ripWidth, ripHeight;
    int liWidth, liHeight;
    BleDeviceBean bleDeviceBean;
    List<BluetoothDevice> deviceList;
    Button mBtConnect;
    BleService mBleService;
    ImageView mImgBle;
    private static final long SCAN_PERIOD = 4000; // 10 seconds
    private BluetoothAdapter mBluetoothAdapter;
    public Dialog dialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x0011:
                    TastyToast.makeText(SercthBleActivity.this, "Connect successful!", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);

//                    Intent intent = new Intent(SercthBleActivity.this, StartStudyActivity.class);
//                    startActivity(intent);
//                    finish();
                    Intent intent = new Intent(SercthBleActivity.this, EmgWaveMActivity.class);
                    startActivity(intent);
                    finish();

                    break;
            }
        }
    };

    private void startConnectingBle() {

        dialog = DialogView.loadDialog(this, R.string.connecting_ble);
        dialog.show();
    }


    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sercthble);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // scanLeDevice(true);
    }

    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    protected void initView() {
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        mLiClick = (LinearLayout) findViewById(R.id.centerImage_li);
        mReRi = (RelativeLayout) findViewById(R.id.serch_re);

        mHandler = new Handler();
        bleDeviceBean = new BleDeviceBean();
        deviceList = new ArrayList<>();
        bleDeviceBean.setMac("aa");
        bleDeviceBean.setRssi(-1000);
        mBtConnect = (Button) findViewById(R.id.emg_connect);
        mTvBleName = (TextView) findViewById(R.id.emg_connect_text);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mBleService = BleService.getInstance();

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        if (mBluetoothAdapter == null) {
            TastyToast.makeText(this, getResources().getString(R.string.ble_not_supported), TastyToast.LENGTH_LONG, TastyToast.ERROR);
            return;
        }
        setBleServiceListener();
        setListener(mLiClick, mBtConnect);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.centerImage_li:
                //helper.dismissBubblePopupWindow();
                popupWindow.dismiss();
                rippleBackground.startRippleAnimation();
                if (mImgBle != null) {
                    mReRi.removeView(mImgBle);
                }
                mTvBleName.setText("");
                scanLeDevice(true);

                break;

            case R.id.emg_connect:
                startConnectingBle();
                mBleService.connect(bleDeviceBean.getMac());
                break;
        }
    }

    private Handler mHandler;
    private boolean mScanning;

    @SuppressLint("NewApi")
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.e("a", "sdf");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //   Log.e("MaxRssi", "MaxRssi=" + bleDeviceBean.toString());
                    if (bleDeviceBean.getRssi() == -1000) {
                        TastyToast.makeText(SercthBleActivity.this, getResources().getString(R.string.no_device), TastyToast.LENGTH_LONG, TastyToast.WARNING);
                    } else {
                        addView(bleDeviceBean.getRssi());
                    }
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }

    private PopupWindow popupWindow;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !inited) {
            ripWidth = mReRi.getWidth();
            ripHeight = mReRi.getHeight();
            liWidth = mLiClick.getWidth();
            liHeight = mLiClick.getHeight();

            //  inited = true;
            //  helper = new LeBubbleTextViewHelper();
            ////   helper.init(mLiClick, R.layout.view_demo_bubble4);
            //  helper.show();
            final BubbleLayout bubbleLayout = (BubbleLayout) LayoutInflater.from(this).inflate(R.layout.layout_tt_popup, null);
            popupWindow = BubblePopupHelper.create(this, bubbleLayout);
            int[] location = new int[2];
            mLiClick.getLocationInWindow(location);
            try {
                popupWindow.showAtLocation(mLiClick, Gravity.NO_GRAVITY, location[0] + liWidth / 4, location[1] - mLiClick.getHeight() + 10);
            } catch (Exception e) {

            }

        }
    }

    private void addView(int rssi) {
        mImgBle = new ImageView(this);
        mImgBle.setImageResource(R.mipmap.found_device);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 30), DensityUtil.dip2px(this, 30));
        int[] xy = getPosition(-rssi);
        Log.e("xy", "xy=" + xy[0] + "  " + xy[1]);
        layoutParams.setMargins(xy[0], xy[1], 0, 0);
        mImgBle.setLayoutParams(layoutParams);
        mImgBle.setVisibility(View.GONE);

        mReRi.addView(mImgBle);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                foundDevice(mImgBle);
                mBtConnect.setVisibility(View.VISIBLE);
                mTvBleName.setText(bleDeviceBean.getName());
            }
        }, 500);

    }

    private void foundDevice(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList = new ArrayList<Animator>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "ScaleX", 0f, 1.2f, 1f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "ScaleY", 0f, 1.2f, 1f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        view.setVisibility(View.VISIBLE);
        animatorSet.start();
    }

    @SuppressLint("NewApi")
    @Override
    public void onStop() {
        super.onStop();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

    }

    @SuppressLint("NewApi")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        //    unregisterReceiver(broadcastToIntent);
//        handler.removeCallbacksAndMessages(null);
        // mBleService = null;
    }

    private int[] getPosition(int signal) {
        int[] xy = new int[2];
        if (signal > -20) {
            xy = getN(1);
        } else if (signal > -35) {
            xy = getN(2);
        } else if (signal > -55) {
            xy = getN(3);
        } else if (signal > -70) {
            xy = getN(4);
        } else {
            xy = getN(5);
        }

        return xy;

    }


    private int[] getN(int le) {
        int r = liWidth / 2;
        r = r + ripWidth / 5 * (le);
        int temp1 = (int) ((ripWidth / 2) - ripWidth / 10 * le - liHeight / 2 + Math.random() * (ripWidth / 10));
        int temp2 = (int) (1 + Math.random() * (6));
//        if (temp2 % 2 == 0) {
//            temp1 = ripWidth - temp1-liHeight/2;
//        }

        Log.e("haha", "aaaaa =" + temp1 + "   " + ripWidth / 5 + "  " + le);
        int x = 0;
        x = gen(1, -2 * (ripWidth / 2), (ripWidth / 2) * (ripWidth / 2) + (temp1 - ripWidth / 2) * (temp1 - ripWidth / 2) - r * r);
        return new int[]{x, temp1};
    }

    /**
     * 求一元二次方程的根
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    private int gen(int a, int b, int c) {
        int j = 0;
        Log.e("haha", "aaaa a b c=" + a + "  " + b + "   " + c);
        int i = b * b - 4 * a * c;

        if (i >= 0) {
            int temp2 = (int) (1 + Math.random() * (6));
            if (temp2 % 2 == 0) {
                j = (int) (((-b) + Math.sqrt(i)) / (2 * a));
            } else {
                j = (int) (((-b) - Math.sqrt(i)) / (2 * a));
            }
        }

        return j;
    }

    /**
     * 获取信号最好的设备
     *
     * @param device
     * @param rssi
     */
    private void getMaxRssiDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;
        for (BluetoothDevice lis : deviceList) {
            if (lis.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }
        if (!deviceFound) {

            if (device.getName() != null && device.getName().contains("YXX")) {
                deviceList.add(device);
                if (rssi > bleDeviceBean.getRssi()) {
                    bleDeviceBean.setMac(device.getAddress());
                    bleDeviceBean.setRssi(rssi);
                    bleDeviceBean.setName(device.getName());
                }
            }
        }

    }

    @SuppressLint("NewApi")
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getMaxRssiDevice(device, rssi);

                        }
                    });
                }
            });


        }
    };


    private void setBleServiceListener() {
        mBleService.setOnServicesDiscoveredListener(new BleService.OnServicesDiscoveredListener() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                }
            }
        });
        //Ble扫描回调
        mBleService.setOnLeScanListener(new BleService.OnLeScanListener() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //每当扫描到一个Ble设备时就会返回，（扫描结果重复的库中已处理）
            }
        });
        //Ble连接回调
        mBleService.setOnConnectListener(new BleService.OnConnectionStateChangeListener() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //Ble连接已断开
                    //   data_len();

                    //  ToastUttils.showShortToast(EcgNubra.this, "蓝牙连接已断开");

                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    //Ble正在连接
                } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //Ble已连接
                    Log.e("sdf", "ddaaddaa");
                    dialog.dismiss();
                    handler.sendEmptyMessage(0x0011);

                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    //Ble正在断开连接
                }
            }
        });
        //Ble服务发现回调
        mBleService.setOnServicesDiscoveredListener(new BleService.OnServicesDiscoveredListener() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.e("haha", " handler_src.sendEmptyMessage(1)");
                } else {
                }
                mBleService.setCharacteristicNotification(UUID_SERVICE, UUID_RX_CHAR, true);
            }
        });


        //Ble数据回调
        mBleService.setOnDataAvailableListener(new BleService.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                //  Log.e("setOnDataAvailableListener", "onCharacteristicChanged");
                //处理通知返回的数据
                // EventBus.getDefault().post(new EcgDataEvent(characteristic.getUuid().toString(),characteristic.getValue()));
                //处理特性读取返回的数据
//                if (isToRotexEcgShowActivity) {
//                    EventBus.getDefault().post(characteristic.getValue());
//                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            }
        });

        mBleService.setOnReadRemoteRssiListener(new BleService.OnReadRemoteRssiListener() {
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

            }
        });
    }



}
