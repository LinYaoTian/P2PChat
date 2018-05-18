package com.rdc.p2p.manager;

import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.util.GsonUtil;

import java.io.DataOutputStream;
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

    /**
     * 核心池大小
     **/
    private static final int CORE_POOL_SIZE = 1;
    /**
     * 线程池最大线程数
     **/
    private static final int MAX_IMUM_POOL_SIZE = 255;

    private ConcurrentHashMap<String,Socket> mClients;//与客户端的连接集合
    private static SocketManager mSocketManager = null;

    @Override
    public String toString() {
        Collection<Socket> socketCollection = mClients.values();
        StringBuilder s = new StringBuilder();
        for (Socket socket : socketCollection) {
            s.append(socket.isClosed()).append(",");
        }
        return "SocketManager{" +
                "socketIp:"+mClients.keySet()+","+
                "isClosed:"+s+
                '}';
    }

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

    public Set<Map.Entry<String, Socket>> getSocketSet(){
        return  mClients.entrySet();
    }

    public int socketNum(){
        return mClients.size();
    }

    /**
     * 对目标ip发送文本消息,需要在子线程中执行
     * @param targetIp
     * @param messageBean
     * @return false 发送失败, true 发送成功
     */
    public boolean sendMsg(final String targetIp, MessageBean messageBean) {
        UserBean userBean = messageBean.transformToUserBean();
        userBean.setUserImageId(messageBean.getUserImageId());
        Socket socket = querySocketByIp(targetIp);
        if (socket == null){
            return false;
        }
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(Protocol.MSG);
            dos.writeUTF(GsonUtil.gsonToJson(userBean));
            dos.writeUTF(messageBean.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void destroy(){
        Set<Map.Entry<String, Socket>> entry = mClients.entrySet();
        for (Map.Entry<String, Socket> entry1 : entry) {
            try {
                Socket socket = entry1.getValue();
                if (socket != null){
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mClients.clear();
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
    public boolean isClosed(String ip){
        Socket socket = mClients.get(ip);
        return socket == null || socket.isClosed();
    }

    /**
     * 添加一个Socket进缓存
     * @param ip
     * @param s
     */
    public void addSocket(String ip,Socket s){
        if (!mClients.containsKey(ip)){
            mClients.put(ip, s);
        }else {
            try {
                Socket socket1 = mClients.replace(ip,s);
                if (socket1 != null){
                    socket1.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
