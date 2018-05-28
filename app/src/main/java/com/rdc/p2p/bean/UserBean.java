package com.rdc.p2p.bean;

/**
 * Created by Lin Yaotian on 2018/5/1.
 */
public class UserBean {

    private int userImageId;
    private String nickName;

    public UserBean(){

    }

    public UserBean(int userImageId,String nickName){
        this.userImageId = userImageId;
        this.nickName = nickName;
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
}
