package com.example.javris.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class PlugActivity extends AppCompatActivity {

    public static final String TAG = "PlugActivity";

    private TextView mShowTv;
    private UsbReceiverUtil mUsbReceiverUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plug);
        mShowTv = (TextView) findViewById(R.id.show_tv);
        mUsbReceiverUtil = UsbReceiverUtil.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUsbReceiverUtil.startScanDevice(new AudioReceiverListener() {
            @Override
            public void onLoseConnect() {
                mShowTv.setText("设备被拔出");
            }

            @Override
            public void onDevicePlugged() {
                mShowTv.setText("设备插入");
            }

        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUsbReceiverUtil.unregisterReceiver();
    }
}
