package com.rdc.p2p.util;

import android.content.SharedPreferences;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.UserBean;

/**
 * Created by Lin Yaotian on 2018/5/1.
 */
public class UserUtil {

    /**
     * 保存用户信息
     * @param userBean
     */
    public static void saveUser(UserBean userBean){
        SharedPreferences.Editor editor = App.getContxet().getSharedPreferences("user",0).edit();
        editor.putInt("imageId",userBean.getUserImageId());
        editor.putString("nickName",userBean.getNickName());
        editor.apply();
    }


    /**
     * 获取用户信息
     */
    public static UserBean getUser(){
        UserBean userBean = new UserBean();
        SharedPreferences sp = App.getContxet().getSharedPreferences("user",0);
        userBean.setUserImageId(sp.getInt("imageId",0));
        userBean.setNickName(sp.getString("nickName",""));
        return userBean;
    }
}
