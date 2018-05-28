package com.rdc.p2p.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.rdc.p2p.app.App;
import com.rdc.p2p.bean.FileBean;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.bean.UserBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.PeerListContract;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.util.GsonUtil;
import com.rdc.p2p.util.SDUtil;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Lin Yaotian on 2018/5/20.
 */
public class SocketThread extends Thread {

    private static final int DESTROY = 0;
    private static final int DELAY_DESTROY = 1;
    private static final String TAG = "SocketThread";
    private static final int DELAY_MILLIS = 1000*60*5;
    private Socket mSocket;
    private PeerListContract.Presenter mPresenter;
    private String mTargetIp;
    private boolean mTimeOutNeedDestroy;//超过 DELAY_MILLIS 时间没有进行通信，则把Socket连接关闭，但界面上仍然显示
    private Handler mHandler;
    private HandlerThread mHandlerThread;


    public SocketThread(Socket mSocket, PeerListContract.Presenter mPresenter) {
        mTargetIp = mSocket.getInetAddress().getHostAddress();
        mTimeOutNeedDestroy = true;
        this.mSocket = mSocket;
        this.mPresenter = mPresenter;
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case DESTROY:
                        if (mTimeOutNeedDestroy){
                            String ip = (String) msg.obj;
                            SocketManager.getInstance().removeSocketByIp(ip);
                            SocketManager.getInstance().removeSocketThreadByIp(ip);
                            mHandlerThread.quitSafely();
                            Log.d(TAG, "handleMessage: destroy");
                        }else {
                            mTimeOutNeedDestroy = true;
                            mHandler.sendMessageDelayed(getDestroyMsg(), DELAY_MILLIS);
                        }
                        break;
                    case DELAY_DESTROY:
                        mTimeOutNeedDestroy = false;
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
        try {
            DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
            dos.writeInt(messageBean.getMsgType());
            switch (messageBean.getMsgType()){
                case Protocol.TEXT:
                    dos.writeUTF(messageBean.getText());
                    break;
                case Protocol.IMAGE:
                    FileInputStream imageInputStream = new FileInputStream(messageBean.getImagePath());
                    int size = imageInputStream.available();
                    dos.writeInt(size);
                    byte[] bytes = new byte[size];
                    dos.write(bytes);
                    break;
                case Protocol.AUDIO:
                    FileInputStream audioInputStream = new FileInputStream(messageBean.getAudioPath());
                    int audioSize = audioInputStream.available();
                    dos.writeInt(audioSize);
                    byte[] audioBytes = new byte[audioSize];
                    audioInputStream.read(audioBytes);
                    dos.write(audioBytes);
                    dos.flush();
                case Protocol.FILE:
                    FileInputStream fileInputStream = new FileInputStream(messageBean.getFileBean().getFilePath());
                    int fileSize = fileInputStream.available();
                    dos.writeInt(fileSize);
                    dos.writeUTF(messageBean.getFileBean().getFileName());
                    byte[] fileBytes = new byte[fileSize];
                    Log.d(TAG, "sendMsg: File start");
                    fileInputStream.read(fileBytes);
                    int offset = 0;//每次写入长度
                    int count = 0;//次数
                    int denominator;//将总文件分为多少份传输
                    if (fileSize < 1024*20){
                        denominator = 1;
                    }else {
                        denominator = fileSize / 1024*10;
                    }
                    FileBean fileBean = messageBean.getFileBean();
                    fileBean.setStates(Constant.SEND_ING);
                    while (true){
                        count++;
                        dos.write(fileBytes,offset,fileSize / denominator);
                        offset += fileSize/denominator;
                        if (count == denominator){
                            dos.write(fileBytes,offset,fileSize % denominator);
                            dos.flush();
                            fileBean.setStates(Constant.SEND_FINISH);
                            messageBean.setFileBean(fileBean);
                            mPresenter.fileSending(messageBean);
                            break;
                        }
                        fileBean.setTransmittedSize(offset);
                        messageBean.setFileBean(fileBean);
                        mPresenter.fileSending(messageBean);
                    }
                    break;
                case Protocol.CONNECT:
                    dos.writeUTF(GsonUtil.gsonToJson(App.getUserBean()));
                    break;
                case Protocol.CONNECT_RESPONSE:
                    dos.writeUTF(GsonUtil.gsonToJson(App.getUserBean()));
                    break;
            }
            mHandler.sendMessage(getDelayDestroyMsg());
        } catch (IOException e) {
            e.printStackTrace();
            return false ;
        }
        return true;
    }

    public boolean sendMsg(UserBean userBean,int msgType){
        try {
            DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
            dos.writeInt(msgType);
            switch (msgType){
                case Protocol.CONNECT:
                    dos.writeUTF(GsonUtil.gsonToJson(userBean));
                    break;
                case Protocol.CONNECT_RESPONSE:
                    dos.writeUTF(GsonUtil.gsonToJson(userBean));
                    break;
            }
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
            DataInputStream dis = new DataInputStream(mSocket.getInputStream());
            //循环读取消息
            while (true){
                int type = dis.readInt();
                mHandler.sendMessage(getDelayDestroyMsg());
                switch (type) {
                    case Protocol.DISCONNECT:
                        Log.d(TAG, "Protocol disconnect ! ip="+ mTargetIp);
                        SocketManager.getInstance().removeSocketByIp(mTargetIp);
                        mPresenter.removePeer(mTargetIp);
                        break;
                    case Protocol.CONNECT:
                        String u1 = dis.readUTF();
                        mPresenter.addPeer(getPeer(u1));
                        //回复连接响应
                        sendMsg(App.getUserBean(),Protocol.CONNECT_RESPONSE);
                        break;
                    case Protocol.CONNECT_RESPONSE:
                        String u2 = dis.readUTF();
                        mPresenter.addPeer(getPeer(u2));
                        break;
                    case Protocol.TEXT:
                        String text = dis.readUTF();
                        MessageBean textMsg = new MessageBean();
                        textMsg.setUserIp(mTargetIp);
                        textMsg.setMsgType(Protocol.TEXT);
                        textMsg.setMine(false);
                        textMsg.setText(text);
                        mPresenter.messageReceived(textMsg);
                        break;
                    case Protocol.IMAGE:
                        int size = dis.readInt();
                        byte[] bytes = new byte[size];
                        dis.readFully(bytes);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,size);
                        MessageBean imageMsg = new MessageBean();
                        imageMsg.setUserIp(mTargetIp);
                        imageMsg.setMine(false);
                        imageMsg.setMsgType(Protocol.IMAGE);
                        imageMsg.setImagePath(SDUtil.saveBitmap(bitmap,System.currentTimeMillis()+"",".jpg"));
                        mPresenter.messageReceived(imageMsg);
                        break;
                    case Protocol.AUDIO:
                        int audioSize = dis.readInt();
                        byte[] audioBytes = new byte[audioSize];
                        dis.readFully(audioBytes);
                        MessageBean audioMsg = new MessageBean();
                        audioMsg.setUserIp(mTargetIp);
                        audioMsg.setMine(false);
                        audioMsg.setMsgType(Protocol.AUDIO);
                        audioMsg.setAudioPath(SDUtil.saveAudio(audioBytes,System.currentTimeMillis()+""));
                        mPresenter.messageReceived(audioMsg);
                        break;
                    case Protocol.FILE:
                        int fileSize = dis.readInt();
                        String fileName = dis.readUTF();//文件名，包括了文件类型(e.g: pic.jpg)
                        Log.d(TAG, "run: 接收到一个文件="+fileName);
                        byte[] fileBytes = new byte[fileSize];
                        //解析出文件名和类型
                        int dotIndex = fileName.lastIndexOf(".");
                        String fileType;//文件类型(e.g: .jpg)
                        String name;//文件名(e.g: pic)
                        if (dotIndex != -1){
                            fileType = fileName.substring(dotIndex,fileName.length());
                            name = fileName.substring(0,dotIndex);
                        }else {
                            //解析不到文件类型
                            fileType = "";
                            name = fileName;
                        }
                        File file = SDUtil.getMyAppFile(name,fileType);
                        if (file != null){
                            FileBean fileBean = new FileBean();
                            fileBean.setFilePath(file.getAbsolutePath());
                            fileBean.setFileName(file.getName());
                            fileBean.setFileSize(fileSize);
                            fileBean.setStates(Constant.RECEIVE_ING);
                            fileBean.setTransmittedSize(0);
                            MessageBean fileMsg = new MessageBean();
                            fileMsg.setUserIp(mTargetIp);
                            fileMsg.setMine(false);
                            fileMsg.setMsgType(Protocol.FILE);
                            fileMsg.setFileBean(fileBean);
                            mPresenter.fileReceiving(fileMsg);
                            FileOutputStream fos = new FileOutputStream(file);
                            int transLen = 0;
                            int countBytes = 0;//计算传输的字节，达到一定数量才更新界面
                            while(true){
                                int read;
                                read = dis.read(fileBytes);
                                transLen += read;
                                countBytes += read;
                                fileBean.setTransmittedSize(transLen);
                                fos.write(fileBytes,0, read);
                                if (transLen == fileSize || read == -1){
                                    fos.flush();
                                    fos.close();
                                    fileBean.setStates(Constant.RECEIVE_FINISH);
                                    mPresenter.fileReceiving(fileMsg);
                                    break;
                                }
                                if (countBytes >= 1024*100){
                                    //每接收到100KB数据就更新一次界面
                                    mPresenter.fileReceiving(fileMsg);
                                    countBytes = 0;
                                }
                            }
                        }else {
                            mSocket.close();
                        }
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (!mTimeOutNeedDestroy){
                mPresenter.removePeer(mTargetIp);
            }
            SocketManager.getInstance().removeSocketByIp(mTargetIp);
            SocketManager.getInstance().removeSocketThreadByIp(mTargetIp);
            mHandlerThread.quitSafely();
        }
    }

    /**
     * 根据UserGson构造PeerBean
     * @param userGson User实体类对应的gson
     * @return PeerBean
     */
    @NonNull
    private PeerBean getPeer(String userGson) {
        UserBean userBean = GsonUtil.gsonToBean(userGson, UserBean.class);
        PeerBean peer = new PeerBean();
        peer.setUserIp(mTargetIp);
        peer.setUserImageId(userBean.getUserImageId());
        peer.setNickName(userBean.getNickName());
        return peer;
    }

    private Message getDestroyMsg(){
        Message destroyMsg = new Message();
        destroyMsg.obj = mTargetIp;
        destroyMsg.what = DESTROY;
        return destroyMsg;
    }

    private Message getDelayDestroyMsg(){
        Message delayDestroyMsg = new Message();
        delayDestroyMsg.obj = mTargetIp;
        delayDestroyMsg.what = DELAY_DESTROY;
        return delayDestroyMsg;
    }
}
