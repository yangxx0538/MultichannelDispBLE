package com.rotex.rotexemg.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.OnCancelListener;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.rotex.loopview.LoopView;
import com.rotex.rotexhand.R;
import com.rotex.rotexemg.MainActivity;
import com.rotex.rotexemg.base.BaseActivity;
import com.rotex.rotexemg.ble.BleService;
import com.rotex.rotexemg.socket.SocketTransceiver;
import com.rotex.rotexemg.socket.TcpServer;
import com.rotex.rotexemg.utils.DialogAdapter;
import com.rotex.rotexemg.views.ShowWaveView;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.rotex.rotexemg.application.EMGApplication.UUID_SERVICE;
import static com.rotex.rotexemg.application.EMGApplication.UUID_WRITE_CHAR;


/**
 * Created by QingWuyong on 2016/12/1.
 */
public class EmgWaveMActivity extends BaseActivity {
    private static final String TAG = "EmgWaveActivity";
    ShowWaveView showWaveView1;
    ShowWaveView showWaveView2;
    ShowWaveView showWaveView3;
    ShowWaveView showWaveView4;
    ShowWaveView showWaveView5;
    Button mBtStartWave;
    Button mBtReSet;
    boolean isStart = true;
    ImageView mBtsetting;
    BleService mBleService;
    TcpServer server;
    ImageView mImgBt;

    TextView tv_roll;
    TextView tv_pitch;
    TextView tv_yaw;

    int[] initStartData = new int[5];
    int[] initEndData = new int[5];
    boolean isRecordData = false;


    Button open_demo;
    final int SEND_WAVE_DATA = 0x001;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SEND_WAVE_DATA:
                    int[] data = (int[]) msg.obj;

                    // Log.e("tag", "msg.arg1 = " + (float) msg.arg1);


                    showWaveView1.setLinePoint((float) data[0]);

                    showWaveView2.setLinePoint((float) data[1]);

                    showWaveView3.setLinePoint((float) data[2]);

                    showWaveView4.setLinePoint((float) data[3]);

                    showWaveView5.setLinePoint((float) data[4]);
                    if (initFlag == 0 && isRecordData == true) {
                        initStartData[0] = (int) (data[0] *     1.2);
                        initStartData[1] = (int) (data[1] *     1.2);
                        initStartData[2] = (int) (data[2] *     1.2);
                        initStartData[3] = (int) (data[3] *     1.2);
                        initStartData[4] = (int) (data[4] *     1.2);
                    } else if (initFlag == 1 && isRecordData == true) {
                        initEndData[0] = (int) (data[0]);
                        initEndData[1] = (int) (data[1]);
                        initEndData[2] = (int) (data[2]);
                        initEndData[3] = (int) (data[3]);
                        initEndData[4] = (int) (data[4]);
                    }


                    tv_roll.setText(data[5] / 10 + "");
                    tv_pitch.setText(data[6] / 10 + "");
                    tv_yaw.setText(data[7] / 10 + "");
                    break;

                case 0x0111:

                    break;

                case 0x00110011:

                    showNormalDialog();
                    TastyToast.makeText(EmgWaveMActivity.this,

                            getString(R.string.ble_disconnected), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    break;
            }
        }
    };


    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_emgwave_m);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setMargins(R.id.re_title, 2, 0, getStatusBarHeight(), 0, 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    boolean isSetHz = false;

    @Override
    protected void initView() {
        showWaveView1 = (ShowWaveView) findViewById(R.id.rotex_main_wave1);
        showWaveView1.setMaxPointAmount(800);
        showWaveView1.setRemovedPointNum(60);
        showWaveView1.setEveryNPoint(10);
        showWaveView1.setEveryNPointRefresh(5);
        showWaveView1.setEffticeValue(10);
        showWaveView1.setMaxYNumber(1200f);

        showWaveView2 = (ShowWaveView) findViewById(R.id.rotex_main_wave2);
        showWaveView2.setMaxPointAmount(800);
        showWaveView2.setRemovedPointNum(60);
        showWaveView2.setEveryNPoint(10);
        showWaveView2.setEveryNPointRefresh(5);
        showWaveView2.setEffticeValue(10);
        showWaveView2.setMaxYNumber(1200f);

        showWaveView3 = (ShowWaveView) findViewById(R.id.rotex_main_wave3);
        showWaveView3.setMaxPointAmount(800);
        showWaveView3.setRemovedPointNum(60);
        showWaveView3.setEveryNPoint(10);
        showWaveView3.setEveryNPointRefresh(5);
        showWaveView3.setEffticeValue(10);
        showWaveView3.setMaxYNumber(1200f);

        showWaveView4 = (ShowWaveView) findViewById(R.id.rotex_main_wave4);
        showWaveView4.setMaxPointAmount(800);
        showWaveView4.setRemovedPointNum(60);
        showWaveView4.setEveryNPoint(10);
        showWaveView4.setEveryNPointRefresh(5);
        showWaveView4.setEffticeValue(10);
        showWaveView4.setMaxYNumber(1200f);

        showWaveView5 = (ShowWaveView) findViewById(R.id.rotex_main_wave5);
        showWaveView5.setMaxPointAmount(800);
        showWaveView5.setRemovedPointNum(60);
        showWaveView5.setEveryNPoint(10);
        showWaveView5.setEveryNPointRefresh(5);
        showWaveView5.setEffticeValue(10);
        showWaveView5.setMaxYNumber(1200f);

        tv_pitch = (TextView) findViewById(R.id.text_p);
        tv_roll = (TextView) findViewById(R.id.text_r);
        tv_yaw = (TextView) findViewById(R.id.text_y);


        mBtsetting = (ImageView) findViewById(R.id.title_setting);
        mBtReSet = (Button) findViewById(R.id.emg_reset);
        mBleService = BleService.getInstance();
        mImgBt = (ImageView) findViewById(R.id.title_back);
        open_demo = (Button) findViewById(R.id.emg_open_untiy);
        mBtStartWave = (Button) findViewById(R.id.emg_start_wave);
        setListener(mBtStartWave, mBtReSet, mBtsetting, open_demo);
        setBleServiceListener();


        //edit by yxx
        //initDialog(this);
        //loadDialog(EmgWaveActivity.this,0);
        final int port = 55550;
        server = new TcpServer(port) {

            @Override
            public void onConnect(final SocketTransceiver client) {
                printInfo(client, "Connect  " + client.getInetAddress());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TastyToast.makeText(EmgWaveMActivity.this, "Socket已连接", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                    }
                });


            }

            @Override
            public void onConnectFailed() {
                System.out.println("Client Connect Failed");

            }

            @Override
            public void onReceive(SocketTransceiver client, String s) {
                if (s.length() != 0) {
                    printInfo(client, "Send Data: " + s);
                    //  client.send(s);
                    System.out.println("--------Server onReceive--------");
                }
            }

            @Override
            public void onDisconnect(SocketTransceiver client) {

                printInfo(client, "Disconnect");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv.setText("无连接");
//                    }
//                });
                //   TastyToast.makeText(EmgWaveActivity.this,"Socket已连接",TastyToast.LENGTH_LONG,TastyToast.SUCCESS);
            }

            @Override
            public void onServerStop() {
                System.out.println("--------Server Stopped--------");
            }
        };

        server.start();


        if (mBleService == null) {
            Log.e("tag", "Wavew mbleservice 为空");
        } else {
            Log.e("tag", "Wavew mbleservice bu为空");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.emg_start_wave:
//                if (isSetHz == false) {
//                    TastyToast.makeText(EmgWaveActivity.this, "您还未设置频率，请设置频率！！", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
//                }else {
                if (mBtStartWave.getText().equals(getString(R.string.show_wave))) {
                    isStart = true;
                    //   mBtStartGame.setVisibility(View.VISIBLE);
                    mBtStartWave.setText(getString(R.string.stop_wave));

                    String path = Environment.getExternalStorageDirectory().getAbsoluteFile().getPath() + "/RotextBreathData/";
                    File destDir = new File(path);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                        Log.e(TAG, "不存在");
                    }

                    //  setfos();


                } else if (mBtStartWave.getText().equals(getString(R.string.stop_wave))) {
                    isStart = false;
                    mBtStartWave.setText(getString(R.string.show_wave));
                    //  mBtStartGame.setVisibility(View.INVISIBLE);
                    handler.sendEmptyMessage(0x0111);
                }
//                }


                break;

            case R.id.emg_reset:
                //  doStartApplicationWithPackageName("com.rotex.spacewar");

                mBleService.writeCharacteristic(UUID_SERVICE, UUID_WRITE_CHAR, new byte[]{int2Bytes(0x56)[0], int2Bytes(10)[0]});
                isStart = false;
                mBtStartWave.setText("SHOW WAVEFORM");
                //  mBtStartGame.setVisibility(View.INVISIBLE);
                handler.sendEmptyMessage(0x0111);
                break;

            case R.id.title_setting:
                //  loadDialog(EmgWaveActivity.this,0);

                ShowSelectLaung(Gravity.TOP);
                break;

            case R.id.emg_open_untiy:
                doStartApplicationWithPackageName("com.rotex.handsDemo");
                break;
        }
    }

    private void showCompleteDialog(Holder holder, int gravity, BaseAdapter adapter,
                                    OnClickListener clickListener, OnItemClickListener itemClickListener,
                                    OnDismissListener dismissListener, OnCancelListener cancelListener,
                                    boolean expanded) {
        final DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(holder)
                .setHeader(R.layout.header)
                .setFooter(R.layout.footer)
                .setCancelable(true)
                .setGravity(gravity)
                .setAdapter(adapter)
                .setOnClickListener(clickListener)
                .setOnItemClickListener(itemClickListener
                )
                .setOnDismissListener(dismissListener)
                .setExpanded(expanded)
//        .setContentWidth(800)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setOnCancelListener(cancelListener)
                .setOverlayBackgroundResource(android.R.color.transparent)
//        .setContentBackgroundResource(R.drawable.corner_background)
                //                .setOutMostMargin(0, 100, 0, 0)
                .create();
        dialog.show();
    }

    public void ShowSelectLaung(int gravity) {
        boolean isGrid;
        Holder holder;

        holder = new GridHolder(2);
        isGrid = true;
        DialogAdapter adapter = new DialogAdapter(EmgWaveMActivity.this, false);
        showCompleteDialog(holder, gravity, adapter, clickListener, itemClickListener, dismissListener, cancelListener,
                false);
    }

    OnDismissListener dismissListener = new OnDismissListener() {
        @Override
        public void onDismiss(DialogPlus dialog) {
            //        Toast.makeText(MainActivity.this, "dismiss listener invoked!", Toast.LENGTH_SHORT).show();
        }
    };

    OnCancelListener cancelListener = new OnCancelListener() {
        @Override
        public void onCancel(DialogPlus dialog) {
            //        Toast.makeText(MainActivity.this, "cancel listener invoked!", Toast.LENGTH_SHORT).show();

        }
    };
    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
//            TextView textView = (TextView) view.findViewById(R.id.text_view);
//            String clickedAppName = textView.getText().toString();
            //        dialog.dismiss();
            //        Toast.makeText(MainActivity.this, clickedAppName + " clicked", Toast.LENGTH_LONG).show();
            Log.d("DialogPlus", "onItemClick() called with: " + "item = [" +
                    item + "], position = [" + position + "]");


            if (position == 0) {
                switchLanguage("zh");
            } else {
                switchLanguage("en");
            }
            finish();
            Intent it = new Intent(EmgWaveMActivity.this, MainActivity.class);
            startActivity(it);
        }
    };

    OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(DialogPlus dialog, View view) {
            dialog.dismiss();

        }
    };

    private void showNormalDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(EmgWaveMActivity.this);
        normalDialog.setTitle(getString(R.string.ble_disconnected));
        normalDialog.setMessage(getString(R.string.no_ble_title));
        normalDialog.setPositiveButton(getString(R.string.no_ble_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        dialog.dismiss();
                        finish();
                        Intent intent = new Intent(EmgWaveMActivity.this, SercthBleActivity.class);
                        startActivity(intent);

                    }
                });
        normalDialog.setNegativeButton(getString(R.string.no_ble_cancle),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        // 显示
        normalDialog.show();
    }

    void printInfo(SocketTransceiver st, String msg) {
        Log.e("this", "Client " + st.getInetAddress().getHostAddress());
        Log.e("this", "  " + msg);
    }


    private void setBleServiceListener() {
//        mBleService.setOnServicesDiscoveredListener(new BleService.OnServicesDiscoveredListener() {
//            @Override
//            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                }
//            }
//        });
//        //Ble扫描回调
//        mBleService.setOnLeScanListener(new BleService.OnLeScanListener() {
//            @Override
//            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//                //每当扫描到一个Ble设备时就会返回，（扫描结果重复的库中已处理）
//            }
//        });
        //Ble连接回调
        mBleService.setOnConnectListener(new BleService.OnConnectionStateChangeListener() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //Ble连接已断开
                    //   data_len();
                    handler.sendEmptyMessage(0x00110011);
                    //  ToastUttils.showShortToast(EcgNubra.this, "蓝牙连接已断开");

                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    //Ble正在连接
                } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //Ble已连接


                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    //Ble正在断开连接
                    TastyToast.makeText(EmgWaveMActivity.this, "BLE is disconnected", TastyToast.LENGTH_LONG, TastyToast.ERROR);
                }
            }
        });
        //Ble服务发现回调
//        mBleService.setOnServicesDiscoveredListener(new BleService.OnServicesDiscoveredListener() {
//            @Override
//            public void onServicesDiscovered(BluetoothGatt gatt, int status) {tt4
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    Log.e("haha", " handler_src.sendEmptyMessage(1)");
//                } else {
//                }
//            }
//        });


        //Ble数据回调
        mBleService.setOnDataAvailableListener(new BleService.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                if (isStart) {
                    decodeDataManyDevice(characteristic.getValue());
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            }
        });

//        mBleService.setOnReadRemoteRssiListener(new BleService.OnReadRemoteRssiListener() {
//            @Override
//            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//
//            }
//        });
    }


    int eletricityPre = 0;


    StringBuilder result = new StringBuilder();
    boolean isCarGo = false;
    int num3;
    int int_xuanzhuan;
    int int_xuanzhuan_fangxiang;

    int int_shangxia;
    int int_shangxia_fangxiang;

    int int_zuoyou;
    int int_zuoyou_fangxiang;

    int int_fashe;
    int int_qiehuan;
    int isGo;

    /**
     * 描述：获取表示当前日期时间的字符串.
     *
     * @param format 格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @return String String类型的当前日期时间
     */
    public static String getCurrentDate(String format) {

        System.out.println("getCurrentDate:" + format);
        String curDateTime = null;
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            Calendar c = new GregorianCalendar();
            curDateTime = mSimpleDateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curDateTime;

    }


    // 判断sd卡是否存在
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }
        return sdDir.toString() + "/RotextBreathData/";

    }

    private FileOutputStream fos1;
    private File file;


    public void setfos() {
        if (TextUtils.isEmpty(getSDPath())) { // 连接就打开一个文件写文件到sd中

        } else {


            String date = getCurrentDate("yyyy-MM-dd HH-mm-ss");
            File dirFirstFolder = new File(getSDPath() + date);//方法二：通过变量文件来获取需要创建的文件夹名字
            if (!dirFirstFolder.exists()) { //如果该文件夹不存在，则进行创建
                boolean isOk = dirFirstFolder.mkdirs();//创建文件夹
                Log.e(TAG, "isOk=" + isOk);
            }
            Log.e("getSDPath", "getSDPath=" + dirFirstFolder.getPath());
            file = new File(dirFirstFolder.getPath(),
                    date + ".breath");
//                Log.e(TAG, "file=" + file.getPath());
//                // Toast.makeText(getApplicationContext(),
//                // "file path="+file.getPath(), 1).show();
//                fos1 = new FileOutputStream(file);
            try {
                fos1 = new FileOutputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //fw=new FileWriter(f.getAbsoluteFile()); //表示不追加


        }
    }

    synchronized void decodeDataManyDevice(byte[] data) {
        // Log.e(TAG, "decodeDataManyDevice: have data" );
        if ((data[0] & 0xff) == 127 && (data[1] & 0xff) == 247) {

            byte[] finger_1 = new byte[2];
            finger_1[0] = data[2];
            finger_1[1] = data[3];
            byte[] finger_2 = new byte[2];
            finger_2[0] = data[4];
            finger_2[1] = data[5];
            byte[] finger_3 = new byte[2];
            finger_3[0] = data[6];
            finger_3[1] = data[7];
            byte[] finger_4 = new byte[2];
            finger_4[0] = data[8];
            finger_4[1] = data[9];
            byte[] finger_5 = new byte[2];
            finger_5[0] = data[10];
            finger_5[1] = data[11];

            byte[] roll = new byte[2];
            roll[0] = data[12];
            roll[1] = data[13];
            byte[] pitch = new byte[2];
            pitch[0] = data[14];
            pitch[1] = data[15];
            byte[] yaw = new byte[2];
            yaw[0] = data[16];
            yaw[1] = data[17];
            int[] data_hand = new int[8];
            data_hand[0] = byte2Int(finger_1);
            data_hand[1] = byte2Int(finger_2);
            data_hand[2] = byte2Int(finger_3);
            data_hand[3] = byte2Int(finger_4);
            data_hand[4] = byte2Int(finger_5);
            data_hand[5] = bytes2int(roll);
            data_hand[6] = bytes2int(pitch);
            data_hand[7] = bytes2int(yaw);


            int[] temp = new int[8];

            temp[0] = data_hand[0];
            temp[1] = data_hand[1];
            temp[2] = data_hand[2];
            temp[3] = data_hand[3];
            temp[4] = data_hand[4];
//            Log.e("e","0:"+data_hand[0]+"   1:"+data_hand[1]+"   2:"+data_hand[2]+"   3:"+data_hand[3]+"   4:"+data_hand[4]+"    r = "+data_hand[5]
//                    +"    p = "+data_hand[6]+"    y = "+data_hand[7]);

            if (data_hand[5] > 60000) {
                temp[5] = data_hand[5] - 65536;
            } else {
                temp[5] = data_hand[5];
            }

            if (data_hand[6] > 60000) {
                temp[6] = data_hand[6] - 65536;
            } else {
                temp[6] = data_hand[6];
            }
            if (data_hand[7] > 60000) {
                temp[7] = data_hand[7] - 65536;
            } else {
                temp[7] = data_hand[7];
            }


            if (initFlag == 2) {
//                if (data_hand[0] >= initStartData[0]) {
////                    temp[0] = initStartData[0];
////                } else if (data_hand[0] < initEndData[0]) {
////                    temp[0] = initEndData[0];
////
////                } else {
////                    temp[0] = data_hand[0];
////                }
////
////                if (data_hand[1] >= initStartData[1]) {
////                    temp[1] = initStartData[1];
////                } else if (data_hand[1] < initEndData[1]) {
////                    temp[1] = initEndData[1];
////
////                } else {
////                    temp[1] = data_hand[1];
////                }
////
////
////                if (data_hand[2] >= initStartData[2]) {
////                    temp[2] = initStartData[2];
////                } else if (data_hand[2] < initEndData[2]) {
////                    temp[2] = initEndData[2];
////
////                } else {
////                    temp[2] = data_hand[2];
////                }
////
////                if (data_hand[3] >= initStartData[3]) {
////                    temp[3] = initStartData[3];
////                } else if (data_hand[3] < initEndData[3]) {
////                    temp[3] = initEndData[3];
////
////                } else {
////                    temp[3] = data_hand[3];
////                }
////
////                if (data_hand[4] >= initStartData[4]) {
////                    temp[4] = initStartData[4];
////                } else if (data_hand[4] < initEndData[4]) {
////                    temp[4] = initEndData[4];
////
////                } else {
////                    temp[4] = data_hand[4];
////                }

                if (data_hand[0] >= initStartData[0]) {
                    temp[0] = initStartData[0];
                } else if (data_hand[0] < initEndData[0]) {
                    temp[0] = initEndData[0];

                } else {
                    temp[0] = data_hand[0];
                }

                if (data_hand[1] >= initStartData[1]) {
                    temp[1] = initStartData[1];
                } else if (data_hand[1] < initEndData[1]) {
                    temp[1] = initEndData[1];

                } else {
                    temp[1] = data_hand[1];
                }


                if (data_hand[2] >= initStartData[2]) {
                    temp[2] = initStartData[2];
                } else if (data_hand[2] < initEndData[2]) {
                    temp[2] = initEndData[2];

                } else {
                    temp[2] = data_hand[2];
                }

                if (data_hand[3] >= initStartData[3]) {
                    temp[3] = initStartData[3];
                } else if (data_hand[3] < initEndData[3]) {
                    temp[3] = initEndData[3];

                } else {
                    temp[3] = data_hand[3];
                }

                if (data_hand[4] >= initStartData[4]) {
                    temp[4] = initStartData[4];
                } else if (data_hand[4] < initEndData[4]) {
                    temp[4] = initEndData[4];

                } else {
                    temp[4] = data_hand[4];
                }
                if (initEndData[0] - initStartData[0] != 0) {
                    temp[0] = -(temp[0] - initEndData[0]) * 75 / (initEndData[0] - initStartData[0]);
                }
                if (initEndData[1] - initStartData[1] != 0) {
                    temp[1] = -(temp[1] - initEndData[1]) * 75 / (initEndData[1] - initStartData[1]);
                }
                if (initEndData[2] - initStartData[2] != 0) {
                    temp[2] = -(temp[2] - initEndData[2]) * 75 / (initEndData[2] - initStartData[2]);
                }
                if (initEndData[3] - initStartData[3] != 0) {
                    temp[3] = -(temp[3] - initEndData[3]) * 75 / (initEndData[3] - initStartData[3]);
                }
                if (initEndData[4] - initStartData[4] != 0) {
                    temp[4] = -(temp[4] - initEndData[4]) * 75 / (initEndData[4] - initStartData[4]);

                }

                Log.e("data_temp", "temp[0]= " + temp[0] + "temp[1]= " + temp[1] + "temp[2]= " + temp[2] + "temp[3]= " + temp[3] + "temp[4]= " + temp[4]);
            }
            Message msg = new Message();
            msg.what = SEND_WAVE_DATA;
            msg.obj = temp;
            handler.sendMessage(msg);

            try {

                byte[] tmpFiger1 = int2byte2(temp[0]);
                byte[] tmpFiger2 = int2byte2(temp[1]);
                byte[] tmpFiger3 = int2byte2(temp[2]);
                byte[] tmpFiger4 = int2byte2(temp[3]);
                byte[] tmpFiger5 = int2byte2(temp[4]);
                byte[] r = int2byte2(temp[5]);
                byte[] p = int2byte2(temp[6]);
                byte[] y = int2byte2(temp[7]);
                server.clients.get(0).send(new byte[]{tmpFiger1[0], tmpFiger1[1], tmpFiger2[0], tmpFiger2[1], tmpFiger3[0],
                        tmpFiger3[1], tmpFiger4[0], tmpFiger4[1], tmpFiger5[0], tmpFiger5[1], r[0], r[1], p[0], p[1], y[0], y[1]});
            } catch (Exception e) {
                Log.e("send", "发送失败");
            }
        }

    }

    //高位在前，低位在后
    public int bytes2int(byte[] bytes) {
        int result = 0;
        if (bytes.length == 2) {
//            int a = (bytes[0] & 0xff) << 24;//说明二
//            int b = (bytes[1] & 0xff) << 16;
            int c = (bytes[0] & 0xff) << 8;
            int d = (bytes[1] & 0xff);
            result = c | d;
        }
        return result;
    }

    public int byte2Int(byte[] b) {
        int intValue = 0;
        for (int i = 0; i < b.length; i++) {
            intValue += (b[i] & 0xFF) << (8 * (1 - i));
        }
        return intValue;
    }

    public byte[] int2byte2(int res) {
        byte[] targets = new byte[2];
        targets[1] = (byte) (res & 0xff);// 最低位
        targets[0] = (byte) ((res >> 8) & 0xff);// 次低位
        return targets;
    }

    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleService = null;

    }

    public void loadDialog(Context context, int resId) {
        final Dialog dialog = new Dialog(context, R.style.mydialog);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_hz, null);
        //	dialog.setContentView(R.layout.dialog) ;
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        Button cancle = (Button) view.findViewById(R.id.cancle);
        Button ok = (Button) view.findViewById(R.id.ok);


        final LoopView loopView = (LoopView) view.findViewById(R.id.loopView);

        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i < 14; i++) {
            list.add(i * 100 + "  Hz");
        }
        //设置原始数据
        loopView.setItems(list);
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSetHz = true;
                mBleService.writeCharacteristic(UUID_SERVICE, UUID_WRITE_CHAR, new byte[]{int2Bytes(0x56)[0], int2Bytes((loopView.getSelectedItem() + 1) * 10)[0]});
                TastyToast.makeText(EmgWaveMActivity.this, "selected:" + loopView.getSelectedItem(), TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                dialog.cancel();

            }
        });
        dialog.show();
    }

    int initFlag = 0;
    TextView textView;
    Button bt_next;
    ImageView imageView;

    public void initDialog(Context context) {


        final Dialog dialog = new Dialog(context, R.style.mydialog);
        View view = LayoutInflater.from(context).inflate(R.layout.init_hand_data, null);
        //	dialog.setContentView(R.layout.dialog) ;
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        bt_next = (Button) view.findViewById(R.id.dialog_next);
        textView = (TextView) view.findViewById(R.id.dialog_msg);
        imageView = (ImageView) view.findViewById(R.id.img_hand);
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initFlag == 0) {
                    if (bt_next.getText().toString().equals("开始")) {
                        isRecordData = true;
                        //bt_next.setText("下一步");
                        dialogThread();
                    } else if (bt_next.getText().toString().equals("下一步")) {

                        initFlag = 1;
                        isRecordData = false;
                        textView.setText("请伸直手掌，并且点击开始");
                        bt_next.setText("开始");
                        imageView.setImageResource(R.mipmap.hand_s);
                    }
                } else if (initFlag == 1) {
                    if (bt_next.getText().toString().equals("开始")) {
                        isRecordData = true;
                        bt_next.setText("结束");
                    } else if (bt_next.getText().toString().equals("结束")) {
                        isRecordData = false;
                        initFlag = 2;
                        dialog.cancel();

                    }
                }


            }
        });

        dialog.show();
    }

    int count = 0;

    public void dialogThread() {
        new Thread() {
            @Override
            public void run() {

                while (count <= 10) {
                    try {

                        sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bt_next.setText((10 - count) + " s");
                            }
                        });
                        if (count == 10) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bt_next.setText("下一步");
                                }
                            });

                        }
                        count++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }
        }.start();
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
}
