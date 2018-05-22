package com.rdc.p2p.bean;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class MessageBean {

    private String userIp;
    private int userImageId;
    private String nickName;
    private String text;
    private String time;
    private String imageUrl;
    private String AudioUrl;
    private int msgType;//消息类型 音频/图片/文字
    private boolean isMine;//是否是本人的消息

    @Override
    public String toString() {
        return "MessageBean{" +
                "userIp='" + userIp + '\'' +
                ", userImageId=" + userImageId +
                ", nickName='" + nickName + '\'' +
                ", text='" + getText() + '\'' +
                ", time='" + time + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", AudioUrl='" + AudioUrl + '\'' +
                ", msgType=" + msgType +
                ", isMine=" + isMine +
                '}';
    }

    public PeerBean transformToPeerBean(){
        PeerBean peerBean = new PeerBean();
        peerBean.setRecentMessage(getText());
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

    public String getText() {
        if (text == null || text.equals("")){
            if (AudioUrl != null){
                return "音频";
            }else if (imageUrl != null){
                return "图片";
            }
        }
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

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    public String getAudioUrl() {
        return AudioUrl == null ? "" : AudioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.AudioUrl = audioUrl == null ? "" : audioUrl;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
