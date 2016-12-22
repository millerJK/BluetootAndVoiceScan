package com.example.javris.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class BluetoothScanUtil {

    public static final String TAG = "BluetoothScanUtil";

    private static BluetoothScanUtil scanDevice;

    private static final int SEARCH_TIMEOUT = 0;

    //不同设备之间连接的UUID不同，具体参考android_UUID_standard文件
    private static final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";

    private UUID mUUID;

    private boolean isCancelBySystem;

    //处在扫描设备状态用户，终止了设备扫描，开始设备连接
    private boolean isCancelByUser;
    private boolean isTimeout;

    private Context context;

    private BluetoothAdapter adapter;
    private OnScanReceiveListener listener;
    private BroadcastReceiver receiver;

    BluetoothSocket mBluetoothSocket;

    private int timeout;

    private HashMap<String, DevInfo> devInfos = new LinkedHashMap<String, DevInfo>();

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SEARCH_TIMEOUT:
                    isTimeout = true;
                    stopDiscovery();
                    break;
            }
        }
    };

    /**
     * 单利模式 创建工具实例
     *
     * @param context
     * @return
     */
    public static BluetoothScanUtil getInstance(Context context) {

        if (scanDevice == null) {
            synchronized (BluetoothScanUtil.class) {
                if (scanDevice == null) {
                    scanDevice = new BluetoothScanUtil(context);
                }
            }
        }
        return scanDevice;
    }

    private BluetoothScanUtil(Context c) {
        context = c.getApplicationContext();
        registerReceiver();
    }


    private void registerReceiver() {

//        mUUID = UUID.randomUUID(); //if you are connecting to an Android peer then please generate your own unique UUID.
        mUUID = UUID.fromString(UUID_STR);

        receiver = new BTBroadcastReceiver();
        adapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
//        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); //设备配对

        context.registerReceiver(receiver, filter);
        Log.e(TAG, "register bluetooth device success");

    }

    /**
     * 启动搜索蓝牙设备
     *
     * @param limitTime             蓝牙搜索时间 系统蓝牙设备默认扫描12秒，
     *                              但是这也不是准确的值，我的MI4手机默认是扫描时间在23秒左右
     *                              ，估计不同的手机默认的扫描时间不同。总之，如果你设置的
     *                              limitTime大于默认扫描时间的话，则在蓝牙扫描停止之后重新启动继续扫描
     *                              直到limitTime.若果limitTime小于0的话，则表示不设置扫描时间。
     *                              同时建议您至少设置扫描时间为10s(扫描时间断了，设备都搜索不到...)要设置
     *                              就设置30s吧！
     * @param onScanReceiveListener 蓝牙设备回调
     */
    public void startDevSearch(int limitTime, OnScanReceiveListener onScanReceiveListener) {

        if (limitTime < 0)
            timeout = 0;
        else
            timeout = limitTime;

        isTimeout = false;
        isCancelByUser = false;
        listener = onScanReceiveListener;

        if (devInfos != null && devInfos.size() != 0)
            devInfos.clear();

        Log.e(TAG, "first step my bluetooth device was  " + adapter.getState());
        //get current Bluetooth device state
        switch (adapter.getState()) {

            case BluetoothAdapter.STATE_ON://12
                startDiscovery();
                break;
            case BluetoothAdapter.STATE_OFF://10
                //Two Methods open bluetooth

                // method one
                adapter.enable();

//                method two
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
                break;
        }
    }

    private void startDiscovery() {

        if (adapter.isDiscovering())
            adapter.cancelDiscovery();

        boolean res = adapter.startDiscovery();
        Log.e(TAG, "startDiscovery() method results  " + (res ? "true" : "false"));

        if (!res) {
            Log.e(TAG, "run startDiscovery() failed .Maybe your bluetooth didn't turn on");
            adapter.disable();
            if (listener != null) {
                listener.startDiscoveryFail();
            }
        }
    }

    public void stopDiscovery() {
        if (adapter != null && adapter.isDiscovering())
            adapter.cancelDiscovery();
        handler.removeMessages(SEARCH_TIMEOUT);
    }


    public boolean isDiscovingNow() {
        if (adapter != null) {
            if (adapter.isDiscovering())
                return true;
            else
                return false;
        }
        return false;
    }


    /**
     * 和特定设备进行配对
     *
     * @param deviceMac 设备的mac 地址
     */
    public void bondDevice(String deviceMac) {

        BluetoothDevice device = null;

        if (!isTimeout)
            isCancelByUser = true;

        stopDiscovery();

        if (adapter != null && deviceMac != null)
            device = adapter.getRemoteDevice(deviceMac);

        int state = device.getBondState();
        Log.e(TAG, "The state with device was  " + state);

        switch (state) {
            case BluetoothDevice.BOND_BONDED://12
                connectDevice(device);
                break;
            case BluetoothDevice.BOND_BONDING://11
                break;
            case BluetoothDevice.BOND_NONE://10
                // 利用反射方法调用BluetoothDevice.createBond();
                Log.d(TAG, "device isn't bond now try bond device");
                Method createBondState = null;
                try {
                    createBondState = BluetoothDevice.class.getMethod("createBond");
                    createBondState.invoke(device);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 连接设备
     *
     * @param device
     */
    private void connectDevice(BluetoothDevice device) {
        //// TODO: 2016/12/22  can't connenct romate device  连接远程设备
//        Log.e(TAG, "start connect with remote device and uuid was  " + UUID_STR);
//        try {
//            mBluetoothSocket = device.createRfcommSocketToServiceRecord(mUUID);
//            Log.e(TAG, "start connect with device waiting.......");
//            mBluetoothSocket.connect();
////            mBluetoothSocket.getOutputStream();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public BluetoothSocket getCurrentSocket() {
        if (mBluetoothSocket.isConnected())
            return mBluetoothSocket;
        return null;
    }

    /**
     * 通过蓝牙设备分享文件
     */
    public void shareFile(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(intent);
    }


    /**
     * 蓝牙广播
     */
    private class BTBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("broadcast receive deal", intent.getAction());

            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    bluetoothStateChanged(intent);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    discoveryStarted();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    deviceFound(intent);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    discoveryFinish();
                    break;
//                case BluetoothDevice.ACTION_NAME_CHANGED:
//                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    disConnected(intent);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    bondStateChange(intent);
                    break;
            }
        }
    }

    /**
     * 设备之间配对状态改变
     *
     * @param intent
     */
    private void bondStateChange(Intent intent) {

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_NONE:
                Log.e(TAG, "cancel bond device");
                break;
            case BluetoothDevice.BOND_BONDING:
                Log.e(TAG, "device is bonding now");
                break;
            case BluetoothDevice.BOND_BONDED:
                Log.e(TAG, "device is bonded success");
                connectDevice(device);
                break;
        }
    }

    /**
     * 搜索设备正式开始
     */
    private void discoveryStarted() {

        if (!handler.hasMessages(SEARCH_TIMEOUT) && timeout != 0) {
            handler.sendEmptyMessageDelayed(SEARCH_TIMEOUT, timeout);
        }

        if (listener != null)
            listener.onOpendedBtooth();
    }

    /**
     * 发现设备 将设备的名字和mac 添加到集合中
     *
     * @param intent
     */
    private void deviceFound(Intent intent) {

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String macAddress = device.getAddress();
        String btName = device.getName();

        // sometimes,getName() method can't get device name , so make sure  get Device name by using getString();
        String extraName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
        DevInfo info = devInfos.get(macAddress);

        if (TextUtils.isEmpty(btName)) {
            btName = extraName;
        }

        if (info == null) {
            info = new DevInfo(btName, macAddress);
            devInfos.put(macAddress, info);
        } else if (btName != null) {
            info.setName(btName);
        }
        if (listener != null) {
            listener.onFindDevReceive((HashMap<String, DevInfo>) devInfos.clone());
        }
    }

    private void bluetoothStateChanged(Intent intent) {

        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, adapter.getState());

        switch (state) {
            case BluetoothAdapter.STATE_TURNING_OFF:
                if (listener != null)
                    listener.stateTurningOff();
                break;
            case BluetoothAdapter.STATE_ON:
                startDiscovery();
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                if (listener != null) {
                    listener.stateTuningOn();
                }
                break;
            case BluetoothAdapter.STATE_OFF:
                stopDiscovery();
                if (listener != null) {
                    listener.onClosedBtooth();
                }
                break;
            default:
                break;
        }
    }


    private void discoveryFinish() {

        if (!isTimeout && !isCancelByUser) {
            Log.e(TAG, "current device scan didn't timeout so start discovery again!");
            startDiscovery();
        } else if (!isTimeout && isCancelByUser) {
            Log.e(TAG, "device scan was interrupted by user");
            handler.removeMessages(SEARCH_TIMEOUT);
            if (listener != null) {
                listener.onFindFinished();
            }
        } else if (isTimeout) {
            Log.e(TAG, "current device scan timeout, so scan success !");
            isTimeout = false;
            if (listener != null) {
                listener.onFindFinished();
            }
        }
    }

    private void disConnected(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String macAddress = device.getAddress();
        DevInfo info = devInfos.get(macAddress);
        if (listener != null) {
            listener.onConnectLose(info);
        }
    }


    private void unregisterReceiver() {
        context.unregisterReceiver(receiver);
        receiver = null;
    }

    // TODO: 2016/8/2 该方法没有被调用
    public void destroy() {
        stopDiscovery();
        unregisterReceiver();
    }

}
