package com.example.javris.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , AdapterView.OnItemClickListener {

    public static final String TAG = "MainActivity";
    public static final int LIMIT_TIME = 30000;

    private Button mSearchBt, mConnBt;
    private ListView mListView;
    private BluetoothScanUtil mScanDeviceUtil;
    private DeviceAdapter mDeviceAdapter;
    private ArrayList<DevInfo> mDevInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();
    }

    private void initView() {
        mSearchBt = (Button) findViewById(R.id.button_search);
        mConnBt = (Button) findViewById(R.id.button_connect);
        mListView = (ListView) findViewById(R.id.list_devices);
        mDeviceAdapter = new DeviceAdapter(this, mDevInfos);
        mListView.setAdapter(mDeviceAdapter);
        mScanDeviceUtil = BluetoothScanUtil.getInstance(MainActivity.this);
    }

    private void initListener() {
        mSearchBt.setOnClickListener(this);
        mConnBt.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_search:
                searchDevice();
                break;
            case R.id.button_connect:
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_SEND);
//                intent.setType("image/png");
//                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory().toString() + "/wifi_config.log")));
//                startActivity(intent);

//                Intent intent = new Intent(this, PlugActivity.class);
//                startActivity(intent);
                break;
        }
    }

    private void searchDevice() {
        mScanDeviceUtil.startDevSearch(LIMIT_TIME, new OnScanReceiveListener() {

            @Override
            public void stateTuningOn() {
                Log.e(TAG, "蓝牙设备正开启");
            }

            @Override
            public void onOpendedBtooth() {
                Log.e(TAG, "蓝牙设备已经开启");
            }

            @Override
            public void onFindDevReceive(HashMap<String, DevInfo> devInfos) {

                mDevInfos.clear();
                Set<Map.Entry<String, DevInfo>> set = devInfos.entrySet();
                Iterator<Map.Entry<String, DevInfo>> es = set.iterator();
                while (es.hasNext()) {
                    Map.Entry<String, DevInfo> entry = es.next();
                    mDevInfos.add(new DevInfo(entry.getValue().getName(), entry.getKey()));
                    mDeviceAdapter.changeData(mDevInfos);
                }
            }

            @Override
            public void stateTurningOff() {
                Log.e(TAG, "蓝牙设备正在关闭");
            }

            @Override
            public void onClosedBtooth() {
                Log.e(TAG, "蓝牙设备已经关闭");
            }

            @Override
            public void startDiscoveryFail() {
                Log.e(TAG, "开启蓝牙设备失败");
            }

            @Override
            public void onFindFinished() {
                Log.e(TAG, "扫描完成");
            }

            @Override
            public void onConnectLose(DevInfo info) {
                Log.e(TAG, "和" + info.getName() + "断开连接");
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mScanDeviceUtil.bondDevice(mDevInfos.get(position).getMacAddress());
    }
}


