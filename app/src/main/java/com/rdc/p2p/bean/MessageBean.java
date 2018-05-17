package com.rdc.p2p.bean;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class MessageBean {
    private int userImageId;
    private String nickName;
    private String message;
    private String time;
    private String imageUrl;
    private String fileUrl;
    private int msgType;//消息类型 文件/图片/文字
    private boolean isMine;//是否是本人的消息

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public int getUserImageId() {
        return userImageId;
    }

    public void setUserImageId(int userImageId) {
        this.userImageId = userImageId;
    }

    public String getNickName() {
        return nickName == null ? "" : nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? "" : nickName;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message == null ? "" : message;
    }

    public String getTime() {
        return time == null ? "" : time;
    }

    public void setTime(String time) {
        this.time = time == null ? "" : time;
    }

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    public String getFileUrl() {
        return fileUrl == null ? "" : fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl == null ? "" : fileUrl;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
