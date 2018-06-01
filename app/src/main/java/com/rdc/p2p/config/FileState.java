package com.rdc.p2p.config;

/**
 * Created by Lin Yaotian on 2018/6/1.
 */
public class FileState {
    public static final int SEND_FILE_ING = 0;//正在发送
    public static final int RECEIVE_FILE_ING = 1;//正在接收
    public static final int RECEIVE_FILE_START = 2;
    public static final int CANCEL_SEND_FILE = 3;//取消发送
    public static final int CANCEL_RECEIVE_FILE = 4;//取消接收
    public static final int SEND_FILE_FINISH = 5;//发送完成
    public static final int RECEIVE_FILE_FINISH = 6;//接收完成
    public static final int SEND_FILE_ERROR = 7;//发送失败
    public static final int RECEIVE_FILE_ERROR = 8;//接收失败
}
