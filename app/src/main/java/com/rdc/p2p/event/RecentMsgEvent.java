package com.rdc.p2p.event;

/**
 * Created by Lin Yaotian on 2018/6/1.
 */
public class RecentMsgEvent {

    private String text;
    private String targetIp;

    public RecentMsgEvent(String text,String targetIp){
        this.text = text;
        this.targetIp = targetIp;
    }

    public String getTargetIp() {
        return targetIp == null ? "" : targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp == null ? "" : targetIp;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }
}
