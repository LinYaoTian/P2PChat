package com.rdc.p2p.bean;

import com.rdc.p2p.config.Protocol;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lin Yaotian on 2018/5/16.
 */
public class PeerBean {
    private int userImageId;
    private String nickName;
    private String recentMessage;
    private String time;
    private String userIp;

    public MessageBean transformToMessageBean(int msgType,boolean isMine){
        MessageBean messageBean = new MessageBean();
        messageBean.setNickName(getNickName());
        messageBean.setUserImageId(getUserImageId());
        messageBean.setUserIp(getUserIp());
        messageBean.setMessage(getRecentMessage());
        messageBean.setMsgType(msgType);
        messageBean.setMine(isMine);
        return messageBean;
    }

    @Override
    public String toString() {
        return "PeerBean{" +
                "userImageId=" + userImageId +
                ", nickName='" + nickName + '\'' +
                ", recentMessage='" + recentMessage + '\'' +
                ", time='" + time + '\'' +
                ", userIp='" + userIp + '\'' +
                '}';
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

    public String getRecentMessage() {
        return recentMessage == null ? "" : recentMessage;
    }

    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage == null ? "" : recentMessage;
    }

    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        return time == null ? sdf.format(date) : time;
    }

    public void setTime(String time) {
        this.time = time == null ? "" : time;
    }

    public String getUserIp() {
        return userIp == null ? "" : userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp == null ? "" : userIp;
    }
}
