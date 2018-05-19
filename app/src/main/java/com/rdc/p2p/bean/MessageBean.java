package com.rdc.p2p.bean;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class MessageBean {

    private String userIp;
    private int userImageId;
    private String nickName;
    private String message;
    private String time;
    private String imageUrl;
    private String fileUrl;
    private int msgType;//消息类型 文件/图片/文字
    private boolean isMine;//是否是本人的消息

    @Override
    public String toString() {
        return "MessageBean{" +
                "userIp='" + userIp + '\'' +
                ", userImageId=" + userImageId +
                ", nickName='" + nickName + '\'' +
                ", message='" + getMessage() + '\'' +
                ", time='" + time + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", msgType=" + msgType +
                ", isMine=" + isMine +
                '}';
    }

    public PeerBean transformToPeerBean(){
        PeerBean peerBean = new PeerBean();
        peerBean.setRecentMessage(getMessage());
        peerBean.setNickName(nickName);
        peerBean.setUserImageId(userImageId);
        peerBean.setUserIp(userIp);
        return peerBean;
    }

    public UserBean transformToUserBean(){
        UserBean userBean = new UserBean();
        userBean.setUserImageId(userImageId);
        userBean.setNickName(nickName);
        return userBean;
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
        if (message == null || message.equals("")){
            if (fileUrl != null){
                return "文件";
            }else if (imageUrl != null){
                return "图片";
            }
        }
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
