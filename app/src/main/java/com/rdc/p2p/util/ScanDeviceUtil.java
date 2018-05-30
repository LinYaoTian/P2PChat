package com.rdc.p2p.util;

/**
 * Created by Lin Yaotian on 2018/5/14.
 */
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 扫描局域网端口
 * 在开始扫描前，必须先调用 getLocalAddressPrefix() 获取本机ip前缀
 */
public class ScanDeviceUtil {

    private static final String TAG = "ScanDeviceUtil";

    /** 核心池大小 **/
    private static final int CORE_POOL_SIZE = 5;
    /** 线程池最大线程数 **/
    private static final int MAX_POOL_SIZE = 255;
    /**等待队列长度**/
    private static final int QUEUE_LENGTH = 125;

    private String mDevAddress;// 本机IP地址-完整
    private String mLocAddress;// 局域网IP地址头,如：192.168.1.
    private Runtime mRun = Runtime.getRuntime();// 获取当前运行环境，来执行ping，相当于windows的cmd
    private String mPing = "ping -c 1 -w 3 ";// 其中 -c 1为发送的次数，-w 表示发送后等待响应的时间
    private CopyOnWriteArrayList<String> mIpList;// ping成功的IP地址
    private ThreadPoolExecutor mExecutor;// 线程池对象
    private static ScanDeviceUtil mScanDeviceUtil;
    private ScanDeviceUtil(){
        mIpList = new CopyOnWriteArrayList<>();
    }
    public static ScanDeviceUtil getInstance(){
        if (mScanDeviceUtil == null){
            synchronized (ScanDeviceUtil.class){
                if (mScanDeviceUtil == null){
                    mScanDeviceUtil = new ScanDeviceUtil();
                }
            }
        }
        return mScanDeviceUtil;
    }

    /**
     * 判断是否扫描完成
     * @return boolean
     */
    public boolean isFinish(){
        return mExecutor.isTerminated();
    }

    /**
     * 获取本机IP
     * @return
     */
    public String getDevAddress(){
        return mDevAddress == null ? "" :mDevAddress;
    }

    public void gc(){
        mRun.gc();
    }

    /**
     * 获取局域网内的连接设备ip地址列表
     * @return List<String>
     */
    public List<String> getIpList(){
        return mIpList;
    }

    /**
     * 获取本机IP前缀
     * @return true 获取成功 ; false 获取失败
     */
    public boolean getLocalAddressPrefix() {
        mDevAddress = getLocAddress();// 获取本机IP地址
        mLocAddress = getLocalAddressIndex(mDevAddress);// 获取本地ip前缀
        if (TextUtils.isEmpty(mLocAddress)){
            return false;
        }
        return true;
    }

    /**
     * TODO<扫描局域网内ip，找到对应服务器>
     *
     * @return void
     */
    public void scan() {
        mIpList.clear();
        Log.d(TAG, "开始扫描设备,本机Ip为：" + mDevAddress);
        /**
         * 1.核心池大小 2.线程池最大线程数 3.表示线程没有任务执行时最多保持多久时间会终止
         * 4.参数keepAliveTime的时间单位，有7种取值,当前为毫秒
         * 5.一个阻塞队列，用来存储等待执行的任务，这个参数的选择也很重要，会对线程池的运行过程产生重大影响
         * ，一般来说，这里的阻塞队列有以下几种选择：
         */
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                QUEUE_LENGTH));


        // 新建线程池
        for (int i = 1; i < 256; i++) {
            // 创建256个线程分别去ping
            final int lastAddress = i;// 存放ip最后一位地址 1-255
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    String ping = ScanDeviceUtil.this.mPing + mLocAddress
                            + lastAddress;
                    String currentIp = mLocAddress + lastAddress;
                    if (mDevAddress.equals(currentIp)){
                        // 如果与本机IP地址相同,跳过
                        return;
                    }
                    Process process = null;
                    try {
                         process = mRun.exec(ping);
                        int result = process.waitFor();
                        if (result == 0) {
//                            Log.d(TAG, "扫描成功,Ip地址为：" + currnetIp);
                            mIpList.add(currentIp);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "扫描异常" + e.toString());
                    } finally {
                        if (process != null){
                            process.destroy();
                        }
                    }
                }
            };
            mExecutor.execute(run);
        }
        mExecutor.shutdown();
    }

    /**
     * TODO<销毁正在执行的线程池>
     *
     * @return void
     */
    public void destory() {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
        }
    }

    /**
     * TODO<获取本地ip地址>
     *
     * @return String
     */
    public String getLocAddress() {
        String ipAddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();
                // 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> address = networks.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress()
                            && ip instanceof Inet4Address) {
                        ipAddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
//            Log.e("", "获取本地ip地址失败");
            e.printStackTrace();
        }
//        Log.i(TAG, "本机IP:" + ipAddress);
        return ipAddress;
    }

    /**
     * TODO<获取本机IP前缀>
     *
     * @param devAddress
     *            // 本机IP地址
     * @return String
     */
    private String getLocalAddressIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }

}
