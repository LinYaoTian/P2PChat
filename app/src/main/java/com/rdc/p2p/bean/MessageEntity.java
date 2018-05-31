package com.rdc.p2p.bean;

import com.rdc.p2p.config.Protocol;

import org.litepal.crud.DataSupport;

/**
 * Created by Lin Yaotian on 2018/5/31.
 */
public class MessageEntity extends DataSupport {
    private String userIp;//消息发送方IP
    private String targetIp;//消息接收方IP
    private String text;
    private String time;
    private String imagePath;
    private String audioPath;
    private int msgType;//消息类型 音频/图片/文字/文件
    private boolean isMine;//是否是本人发的消息
    private int sendStatus;//若为本人发的音频/图片/文字消息，发送状态有 SEND_FILE_ING ,SEND_FINISH,SEND_ERROR
    private String filePath;
    private String fileName;
    private int fileSize;
    private int states;//传输状态
    private int transmittedSize;//已经传输的字节

    @Override
    public String toString() {
        return "MessageEntity{" +
                "userIp='" + userIp + '\'' +
                ", targetIp='" + targetIp + '\'' +
                ", text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", audioPath='" + audioPath + '\'' +
                ", msgType=" + msgType +
                ", isMine=" + isMine +
                ", sendStatus=" + sendStatus +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", states=" + states +
                ", transmittedSize=" + transmittedSize +
                '}';
    }

    public String getTargetIp() {
        return targetIp == null ? "" : targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp == null ? "" : targetIp;
    }

    public MessageBean transformMessageBean(){
        MessageBean messageBean = new MessageBean();
        messageBean.setUserIp(userIp);
        messageBean.setText(text);
        messageBean.setTime(time);
        messageBean.setImagePath(imagePath);
        messageBean.setAudioPath(audioPath);
        messageBean.setMsgType(msgType);
        messageBean.setMine(isMine);
        messageBean.setSendStatus(sendStatus);
        if (msgType == Protocol.FILE){
            FileBean fileBean = new FileBean();
            fileBean.setFilePath(filePath);
            fileBean.setFileName(fileName);
            fileBean.setFileSize(fileSize);
            fileBean.setStates(states);
            fileBean.setTransmittedSize(transmittedSize);
            messageBean.setFileBean(fileBean);
        }
        return messageBean;
    }

    public String getUserIp() {
        return userIp == null ? "" : userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp == null ? "" : userIp;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public String getTime() {
        return time == null ? "" : time;
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

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getFilePath() {
        return filePath == null ? "" : filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? "" : filePath;
    }

    public String getFileName() {
        return fileName == null ? "" : fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? "" : fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getStates() {
        return states;
    }

    public void setStates(int states) {
        this.states = states;
    }

    public int getTransmittedSize() {
        return transmittedSize;
    }

    public void setTransmittedSize(int transmittedSize) {
        this.transmittedSize = transmittedSize;
    }
}
