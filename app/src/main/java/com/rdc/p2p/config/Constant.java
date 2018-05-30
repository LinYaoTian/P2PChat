package com.rdc.p2p.config;

/**
 * Created by Lin Yaotian on 2018/5/27.
 */
public class Constant {
    public static final int SEND_FILE_ING = 0;//正在发送
    public static final int RECEIVE_FILE_ING = 1;//正在接收
    public static final int RECEIVE_FILE_START = 8;
    public static final int CANCEL_SEND_FILE = 2;//取消发送
    public static final int CANCEL_RECEIVE_FILE = 3;//取消接收
    public static final int SEND_FILE_FINISH = 4;//发送完成
    public static final int RECEIVE_FILE_FINISH = 5;//接收完成
    public static final int SEND_FILE_ERROR = 6;//发送失败
    public static final int RECEIVE_FILE_ERROR = 7;//接收失败

    public static final int UPDATE_SEND_MSG_STATE = 5;//发送音频/图片/文字
    public static final int UPDATE_FILE_STATE = 6;

    public static final int SEND_MSG_ING = 0;
    public static final int SEND_MSG_ERROR = 1;
    public static final int SEND_MSG_FINISH = 2;
}
