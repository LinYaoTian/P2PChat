package com.rdc.p2p.bean;

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
    private String ip;

    @Override
    public String toString() {
        return "PeerBean{" +
                "userImageId=" + userImageId +
                ", nickName='" + nickName + '\'' +
                ", recentMessage='" + recentMessage + '\'' +
                ", time='" + time + '\'' +
                ", ip='" + ip + '\'' +
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

    public String getIp() {
        return ip == null ? "" : ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? "" : ip;
    }
}
