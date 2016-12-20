package com.example.javris.myapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class UsbReceiverUtil {

    private static final String TAG = "UsbReceiverUtil";

    private AudioReceiverListener listener;
    private Context context;
    private boolean isConnecting;
    private static UsbReceiverUtil sUsbReceiverUtil;


    public static UsbReceiverUtil getInstance(Context ctx) {

        if (sUsbReceiverUtil == null) {
            synchronized (UsbReceiverUtil.class) {
                if (sUsbReceiverUtil == null)
                    sUsbReceiverUtil = new UsbReceiverUtil(ctx);
            }
        }
        return sUsbReceiverUtil;
    }

    private UsbReceiverUtil(Context ctx) {
        context = ctx;
    }


    /**
     * @param listener
     */
    public void startScanDevice(AudioReceiverListener listener) {

        isConnecting = false;
        this.listener = listener;
        registerReceiver(context);

    }

    private void registerReceiver(Context c) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        c.registerReceiver(receiver, filter);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (listener != null) {
                if (intent.getIntExtra("state", 0) == 0) {
                    Log.e(TAG, "The usb plug has been pulled out ..........");
                    isConnecting = false;
                    listener.onLoseConnect();
                } else {
                    Log.e(TAG, "The usb plug has been pulled in ..........");
                    isConnecting = true;
                    listener.onDevicePlugged();
                }
            }
        }
    };

    public void unregisterReceiver() {
        context.unregisterReceiver(receiver);
        listener = null;
    }

}
