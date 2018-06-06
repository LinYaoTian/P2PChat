package com.rdc.p2p.config;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class Protocol {

    public static final int TEXT = 0;//文本
    public static final int IMAGE = 1;//图片
    public static final int AUDIO = 2;//音频
    public static final int FILE = 7;
    public static final int FILE_RECEIVED = 8;//文件接收完成！
    public static final int CONNECT = 3;//连接请求
    public static final int CONNECT_RESPONSE = 4;//连接请求响应
    public static final int DISCONNECT = 5;//断开连接
    public static final int KEEP_USER = 6;//保留用户
    public static final int KEEP_USER_RESPONSE = 9;
}
