package com.example.javris.myapplication;

import java.util.HashMap;

public interface OnScanReceiveListener {


    /**
     * 蓝牙正在打开
     */
    void stateTuningOn();

    /**
     * 蓝牙已经打开
     */
    void onOpendedBtooth();

    /**
     * 搜索蓝牙的回调方法
     *
     * @param devInfos 蓝牙设备的信息对象，包含蓝牙name，蓝牙mac地址
     */
    void onFindDevReceive(HashMap<String, DevInfo> devInfos);

    /**
     * 蓝牙正在关闭
     */
    void stateTurningOff();

    /**
     * 蓝牙已经关闭
     */
    void onClosedBtooth();


    /**
     * 启动蓝牙搜索失败
     */
    void startDiscoveryFail();

    /**
     * 搜索完毕
     */
    void onFindFinished();

    /**
     * 设备关闭或者距离过远...，导致的连接丢失
     */
    void onConnectLose(DevInfo info);
}
