package com.rdc.p2p.config;

/**
 * Created by Lin Yaotian on 2018/5/27.
 */
public class Constant {
    public static final int SEND_ING = 0;//正在发送文件
    public static final int RECEIVE_ING = 1;//正在接收文件
    public static final int CANCEL_SEND = 2;//取消发送
    public static final int CANCEL_RECEIVE = 3;//取消接收
    public static final int SEND_FINISH = 4;//发送完成
    public static final int RECEIVE_FINISH = 5;//接收完成
    public static final int SEND_ERROR = 6;//发送失败
    public static final int RECEIVE_ERROR = 7;//接收失败
}
