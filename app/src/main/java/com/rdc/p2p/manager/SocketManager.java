package com.rdc.p2p.manager;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class SocketManager {

    private ConcurrentHashMap<String,Socket> mClients;//与客户端的连接集合
    private static SocketManager mSocketManager = null;

    private SocketManager(){
        mClients = new ConcurrentHashMap<>();
    }

    public static SocketManager getInstance(){
        if (mSocketManager == null){
            synchronized (SocketManager.class){
                if (mSocketManager == null){
                    mSocketManager = new SocketManager();
                }
            }
        }
        return mSocketManager;
    }


    public void destroy(){
        Set<Map.Entry<String, Socket>> entry = mClients.entrySet();
        for (Map.Entry<String, Socket> entry1 : entry) {
            try {
                entry1.getValue().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据IP获取获取对应缓存的Socket
     * @param ip
     * @return socket or null
     */
    public Socket querySocketByIp(String ip){
        return mClients.get(ip);
    }

    /**
     * 根据Ip删除对应缓存的Socket
     * @param ip
     */
    public void removeSocketByIp(String ip){
        try {
            mClients.remove(ip).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否缓存过指定ip的Socket连接
     * @param ip
     * @return
     */
    public boolean isContainSocket(String ip){
        return mClients.containsKey(ip);
    }

    /**
     * 添加一个Socket进缓存
     * @param ip
     * @param socket
     */
    public void addSocket(String ip,Socket socket){
        if (!mClients.containsKey(ip)){
            mClients.put(ip, socket);
        }
    }
}
