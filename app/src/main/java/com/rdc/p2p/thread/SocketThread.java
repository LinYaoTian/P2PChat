package com.rdc.p2p.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.util.GsonUtil;
import com.rdc.p2p.util.SDUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Lin Yaotian on 2018/5/20.
 */
public class SocketThread extends Thread {

    public static final int DESTROY = 0;
    public static final int DELAY_DESTROY = 1;
    private static final String TAG = "SocketThread";
    private static final int DELAY_MILLIS = 1000*60*5;
    private Socket socket;
    private DataInputStream dis = null;//读取消息
    private DataOutputStream dos = null;//发送消息
    private PeerListContract.Presenter presenter;
    private String targetIp;
    private boolean needDestroy;
    private Handler mHandler;
    private HandlerThread mHandlerThread;


    public SocketThread(Socket socket, PeerListContract.Presenter presenter) {
        targetIp = socket.getInetAddress().getHostAddress();
        needDestroy = true;
        this.socket = socket;
        this.presenter = presenter;
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case DESTROY:
                        if (needDestroy){
                            String ip = (String) msg.obj;
                            SocketManager.getInstance().removeSocketByIp(ip);
                            SocketManager.getInstance().removeSocketThreadByIp(ip);
                            mHandlerThread.quitSafely();
                            Log.d(TAG, "handleMessage: destroy");
                        }else {
                            needDestroy = true;
                            mHandler.sendMessageDelayed(getDestroyMsg(), DELAY_MILLIS);
                        }
                        break;
                    case DELAY_DESTROY:
                        needDestroy = false;
                        Log.d(TAG, "handleMessage: delay_destroy");
                        break;
                }
            }
        };
    }

    /**
     * 发送消息
     * @param messageBean
     * @return
     */
    public boolean sendMsg(MessageBean messageBean){
        UserBean userBean = messageBean.transformToUserBean();
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(messageBean.getMsgType());
            dos.writeUTF(GsonUtil.gsonToJson(userBean));
            switch (messageBean.getMsgType()){
                case Protocol.TEXT:
                    dos.writeUTF(messageBean.getText());
                    break;
                case Protocol.IMAGE:
                    FileInputStream ImageInputStream = new FileInputStream(messageBean.getImageUrl());
                    int size = ImageInputStream.available();
                    dos.writeInt(size);
                    byte[] bytes = new byte[size];
                    ImageInputStream.read(bytes);
                    dos.write(bytes);
                    break;
                case Protocol.AUDIO:
                    FileInputStream AudioInputStream = new FileInputStream(messageBean.getAudioUrl());
                    int audioSize = AudioInputStream.available();
                    dos.writeInt(audioSize);
                    byte[] audioBytes = new byte[audioSize];
                    AudioInputStream.read(audioBytes);
                    dos.write(audioBytes);
                    break;
            }
            mHandler.sendMessage(getDelayDestroyMsg());
        } catch (IOException e) {
            e.printStackTrace();
            return false ;
        }
        return true;
    }

    @Override
    public void run() {
        mHandler.sendMessageDelayed(getDestroyMsg(), DELAY_MILLIS);
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            //循环读取消息
            while (true){
                Log.d(TAG, "ip="+targetIp+" start read Protocol !");
                int type = dis.readInt();
                Log.d(TAG, "ip="+targetIp+",type: "+type);
                if (type == Protocol.KEEP_LIVE){
                    continue;
                }
                String u = dis.readUTF();
//                Log.d(TAG, "type:"+type+",user:"+u);
                UserBean userBean = GsonUtil.gsonToBean(u, UserBean.class);
                PeerBean peer = new PeerBean();
                peer.setUserIp(targetIp);
                peer.setUserImageId(userBean.getUserImageId());
                peer.setNickName(userBean.getNickName());
                peer.setRecentMessage("");
                mHandler.sendMessage(getDelayDestroyMsg());
                switch (type) {
                    case Protocol.DISCONNECT:
                        Log.d(TAG, "Protocol disconnect ! ip="+targetIp);
                        SocketManager.getInstance().removeSocketByIp(targetIp);
                        presenter.removePeer(targetIp);
                        break;
                    case Protocol.CONNECT:
                        presenter.addPeer(peer);
                        //回复连接响应
                        String userGson = GsonUtil.gsonToJson(App.getUserBean());
                        dos.writeInt(Protocol.CONNECT_RESPONSE);
                        dos.writeUTF(userGson);
                        break;
                    case Protocol.CONNECT_RESPONSE:
                        Log.d(TAG, "CONNECT_RESPONSE");
                        presenter.addPeer(peer);
                        break;
                    case Protocol.TEXT:
                        peer.setRecentMessage(dis.readUTF());
                        MessageBean messageBean = peer.transformToMessageBean(Protocol.TEXT,false);
                        presenter.messageReceived(messageBean);
                        break;
                    case Protocol.IMAGE:
                        int size = dis.readInt();
                        byte[] bytes = new byte[size];
                        dis.readFully(bytes);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,size);
                        MessageBean messageBean1 = peer.transformToMessageBean(Protocol.IMAGE,false);
                        messageBean1.setImageUrl(SDUtil.saveBitmap(bitmap,System.currentTimeMillis()+""));
                        presenter.messageReceived(messageBean1);
                        break;
                    case Protocol.AUDIO:
                        int audioSize = dis.readInt();
                        byte[] audioByte = new byte[audioSize];
                        dis.readFully(audioByte);
                        MessageBean audioMessage = peer.transformToMessageBean(Protocol.AUDIO,false);
                        audioMessage.setAudioUrl(SDUtil.saveAudio(audioByte,System.currentTimeMillis()+""));
                        presenter.messageReceived(audioMessage);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            SocketManager.getInstance().removeSocketByIp(targetIp);
            SocketManager.getInstance().removeSocketThreadByIp(targetIp);
            presenter.removePeer(targetIp);
            mHandlerThread.quitSafely();
        }
    }

    private Message getDestroyMsg(){
        Message destroyMsg = new Message();
        destroyMsg.obj = targetIp;
        destroyMsg.what = DESTROY;
        return destroyMsg;
    }

    private Message getDelayDestroyMsg(){
        Message delayDestroyMsg = new Message();
        delayDestroyMsg.obj = targetIp;
        delayDestroyMsg.what = DELAY_DESTROY;
        return delayDestroyMsg;
    }
}
