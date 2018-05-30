package com.rdc.p2p.manager;

import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.thread.SocketThread;
import com.rdc.p2p.util.GsonUtil;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lin Yaotian on 2018/5/17.
 */
public class SocketManager {

    private static final String TAG = "SocketManager";

    private ConcurrentHashMap<String,Socket> mClients;//与客户端的连接集合
    private ConcurrentHashMap<String,SocketThread> mSocketThreads;
    private static SocketManager mSocketManager = null;

    @Override
    public String toString() {
        Collection<Socket> socketCollection = mClients.values();
        StringBuilder s = new StringBuilder();
        for (Socket socket : socketCollection) {
            s.append(socket.isClosed()).append(",");
        }
        Collection<SocketThread> socketThreads = mSocketThreads.values();
        StringBuilder s1 = new StringBuilder();
        for (SocketThread socketThread : socketThreads) {
            s1.append(socketThread.getState()).append(",");
        }

        return "SocketManager{" +
                "socketIp:"+mClients.keySet()+","+
                "isClosedSocket:"+s1+
                '}';
    }

    private SocketManager(){
        mClients = new ConcurrentHashMap<>();
        mSocketThreads = new ConcurrentHashMap<>();
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



    /**
     * 根据ip获取SocketThread
     * @param ip
     * @return
     */
    public SocketThread getSocketThreadByIp(String ip){
        SocketThread socketThread = mSocketThreads.get(ip);
        if (socketThread == null){
            mSocketThreads.remove(ip);
            return null;
        }
        return socketThread;
    }

    /**
     * 根据ip移除SocketThread
     * @param ip
     */
    public void removeSocketThreadByIp(String ip){
        SocketThread socketThread = mSocketThreads.remove(ip);
        if (socketThread != null){
            socketThread.interrupt();
        }
    }

    /**
     * 添加一个SocketThread
     * @param ip
     * @param socketThread
     */
    public void addSocketThread(String ip,SocketThread socketThread){
//        if (mSocketThreads.containsKey(ip)){
//            removeSocketByIp(ip);
//        }
        mSocketThreads.put(ip, socketThread);
    }

    /**
     * 获取当前所有正在通信Socket的set集合
     * @return
     */
    public Set<Map.Entry<String, Socket>> getSocketSet(){
        return  mClients.entrySet();
    }

    /**
     * 获取当前正在通信的socket数量
     * @return
     */
    public int socketCount(){
        return mClients.size();
    }

    /**
     * 销毁保存的socket和socketThread
     */
    public void destroy(){
        Collection<Socket> socketCollection = mClients.values();
        for (Socket socket : socketCollection) {
            if (socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mClients.clear();
        Collection<SocketThread> socketThreadCollection = mSocketThreads.values();
        for (SocketThread socketThread : socketThreadCollection) {
            if (socketThread != null){
                socketThread.interrupt();
            }
        }
        mSocketThreads.clear();
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
            Socket socket = mClients.remove(ip);
            if (socket != null){
                socket.close();
            }
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
     * 判断指定IP的socket是否关闭
     * @param ip
     * @return return true if socket is closed or null ,else return false
     */
    public boolean isClosedSocket(String ip){
        Socket socket = mClients.get(ip);
        return socket == null || socket.isClosed();
    }

    /**
     * 添加一个Socket进缓存
     * @param ip
     * @param s
     */
    public void addSocket(String ip,Socket s){
        mClients.put(ip, s);
    }
}
