package com.rdc.p2p.listener;

import com.rdc.p2p.bean.FileBean;

/**
 * Created by Lin Yaotian on 2018/5/30.
 */
public interface OnSocketSendListener {
//    void sendTextSuccess();
//    void sendTextError();
//    void sendImageSuccess();
//    void sendImageError();
//    void sendAudioSuccess();
//    void sendAudioError();

    /**
     * 发送消息成功(图片、文字、音频)
     */
    void sendMsgSuccess();

    /**
     * 发送消息失败
     */
    void sendMsgError();

    /**
     * 发送文件中...
     * @param fileBean
     */
    void fileSending(FileBean fileBean);
}
