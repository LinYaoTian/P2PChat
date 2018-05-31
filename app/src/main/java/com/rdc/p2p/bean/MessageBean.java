package com.rdc.p2p.bean;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class MessageBean implements Cloneable {

    private String userIp;//消息发送方IP
    private String text;
    private String time;
    private String imagePath;
    private String audioPath;
    private FileBean fileBean;
    private int msgType;//消息类型 音频/图片/文字/文件
    private boolean isMine;//是否是本人发的消息
    private int sendStatus;//若为本人发的音频/图片/文字消息，发送状态有 SEND_FILE_ING ,SEND_FINISH,SEND_ERROR

    @Override
    public MessageBean clone() {
        MessageBean messageBean = null;
        try {
            messageBean = (MessageBean) super.clone();
            messageBean.fileBean = (FileBean) fileBean.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return messageBean;
    }

    @Override
    public String toString() {
        return "MessageBean{" +
                "userIp='" + userIp + '\'' +
                ", text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", audioPath='" + audioPath + '\'' +
                ", fileBean=" + fileBean +
                ", msgType=" + msgType +
                ", isMine=" + isMine +
                '}';
    }

    /**
     * @param targetIp 相对于本用户来说的聊天对象IP
     * @return
     */
    public MessageEntity transformMessageEntity(String targetIp){
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setUserIp(userIp);
        messageEntity.setTargetIp(targetIp);
        messageEntity.setText(text);
        messageEntity.setTime(time);
        messageEntity.setImagePath(imagePath);
        messageEntity.setAudioPath(audioPath);
        messageEntity.setMsgType(msgType);
        messageEntity.setMine(isMine);
        messageEntity.setSendStatus(sendStatus);
        if (fileBean != null){
            messageEntity.setFilePath(fileBean.getFilePath());
            messageEntity.setFileName(fileBean.getFileName());
            messageEntity.setFileSize(fileBean.getFileSize());
            messageEntity.setStates(fileBean.getStates());
            messageEntity.setTransmittedSize(fileBean.getTransmittedSize());
        }
        return messageEntity;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public FileBean getFileBean() {
        return fileBean;
    }

    public void setFileBean(FileBean fileBean) {
        this.fileBean = fileBean;
    }

    public String getUserIp() {
        return userIp == null ? "" : userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp == null ? "" : userIp;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getText() {
        if (text == null || text.equals("")){
            if (audioPath != null){
                return "音频";
            }else if (imagePath != null){
                return "图片";
            }else if (fileBean != null){
                return "文件";
            }
        }
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public String getTime() {
        if (time == null){
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = new Date();
            time = sdf.format(date);
        }
        return time;
    }

    public void setTime(String time) {
        this.time = time == null ? "" : time;
    }

    public String getImagePath() {
        return imagePath == null ? "" : imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public String getAudioPath() {
        return audioPath == null ? "" : audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath == null ? "" : audioPath;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
