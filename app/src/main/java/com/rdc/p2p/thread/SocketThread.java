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
import com.rdc.p2p.listener.OnSocketSendListener;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.util.GsonUtil;
import com.rdc.p2p.util.SDUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private AtomicBoolean mIsFileReceived;


    public SocketThread(Socket mSocket, PeerListContract.Presenter mPresenter) {
        mTargetIp = mSocket.getInetAddress().getHostAddress();
        mTimeOutNeedDestroy = true;
        this.mSocket = mSocket;
        this.mPresenter = mPresenter;
        mIsFileReceived = new AtomicBoolean(true);
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
                        }else {
                            mTimeOutNeedDestroy = true;
                            mHandler.sendMessageDelayed(getDestroyMsg(), DELAY_MILLIS);
                        }
                        break;
                    case DELAY_DESTROY:
                        mTimeOutNeedDestroy = false;
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
    public void sendMsg(MessageBean messageBean, OnSocketSendListener listener){
        while (!mIsFileReceived.get()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (listener != null){
                    if (messageBean.getMsgType() == Protocol.FILE){
                        messageBean.getFileBean().setStates(Constant.SEND_FILE_ERROR);
                        listener.fileSending(messageBean.getFileBean());
                    }else {
                        listener.sendMsgError();
                    }
                }
            }
        }
        try {
            DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
            dos.writeInt(messageBean.getMsgType());
            switch (messageBean.getMsgType()){
                case Protocol.TEXT:
                    byte[] textBytes = messageBean.getText().getBytes();
                    dos.writeInt(textBytes.length);
                    dos.write(textBytes);
                    if (listener != null){
                        listener.sendMsgSuccess();
                    }
                    break;
                case Protocol.IMAGE:
                    FileInputStream imageInputStream = new FileInputStream(messageBean.getImagePath());
                    int size = imageInputStream.available();
                    dos.writeInt(size);
                    byte[] bytes = new byte[size];
                    imageInputStream.read(bytes);
                    dos.write(bytes);
                    if (listener != null){
                        listener.sendMsgSuccess();
                    }
                    break;
                case Protocol.AUDIO:
                    FileInputStream audioInputStream = new FileInputStream(messageBean.getAudioPath());
                    int audioSize = audioInputStream.available();
                    dos.writeInt(audioSize);
                    byte[] audioBytes = new byte[audioSize];
                    audioInputStream.read(audioBytes);
                    dos.write(audioBytes);
                    dos.flush();
                    if (listener != null){
                        listener.sendMsgSuccess();
                    }
                    break;
                case Protocol.FILE:
                    mIsFileReceived.set(false);
                    FileBean fileBean = messageBean.getFileBean();
                    FileInputStream fileInputStream = new FileInputStream(fileBean.getFilePath());
                    int fileSize = fileBean.getFileSize();
                    dos.writeInt(fileSize);
                    dos.writeUTF(fileBean.getFileName());
                    byte[] fileBytes = new byte[fileSize];
                    fileInputStream.read(fileBytes);
                    int offset = 0;//每次写入长度
                    int count = 0;//次数
                    int denominator;//将总文件分为多少份传输
                    if (fileSize < (1024*300)){
                        denominator = 1;
                    }else {
                        denominator = fileSize / (1024*300);
                    }
                    fileBean.setStates(Constant.SEND_FILE_ING);
                    while (true){
                        count++;
                        dos.write(fileBytes,offset,fileSize / denominator);
                        offset += fileSize/denominator;
                        if (count == denominator){
                            dos.write(fileBytes,offset,fileSize % denominator);
                            dos.flush();
                            fileBean.setTransmittedSize(fileSize);
                            fileBean.setStates(Constant.SEND_FILE_FINISH);
                            if (listener != null){
                                listener.fileSending(fileBean);
                            }
                            break;
                        }
                        fileBean.setTransmittedSize(offset);
                        if (listener != null){
                            listener.fileSending(fileBean);
                        }
                    }
                    break;
            }
            mHandler.sendMessage(getDelayDestroyMsg());
        } catch (IOException e) {
            e.printStackTrace();
            mIsFileReceived.set(true);
            if (listener != null){
                if (messageBean.getMsgType() == Protocol.FILE){
                    messageBean.getFileBean().setStates(Constant.SEND_FILE_ERROR);
                    listener.fileSending(messageBean.getFileBean());
                }else {
                    listener.sendMsgError();
                }
            }
        }
    }

    /**
     * 发送连接或连接响应请求
     * @param userBean
     * @param msgType
     * @return
     */
    public boolean sendRequest(UserBean userBean, int msgType){
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
                case Protocol.FILE_RECEIVED:
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
                        sendRequest(App.getUserBean(),Protocol.CONNECT_RESPONSE);
                        break;
                    case Protocol.CONNECT_RESPONSE:
                        String u2 = dis.readUTF();
                        mPresenter.addPeer(getPeer(u2));
                        break;
                    case Protocol.FILE_RECEIVED:
                        mIsFileReceived.set(true);
                        break;
                    case Protocol.TEXT:
                        int textBytesLength = dis.readInt();
                        byte[] textBytes = new byte[textBytesLength];
                        dis.readFully(textBytes);
                        String text = new String(textBytes,"utf-8");
                        Log.d(TAG, "run: receive text:"+text);
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
                        Log.d(TAG, "run: image size = "+size);
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
                        byte[] fileBytes = new byte[1024*1024];
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
                            fileBean.setStates(Constant.RECEIVE_FILE_START);
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
                            int read;
                            while(true){
                                read = dis.read(fileBytes);
                                transLen += read;
                                countBytes += read;
                                fos.write(fileBytes,0, read);
                                if (transLen == fileSize || read == -1){
                                    fos.flush();
                                    fos.close();
                                    fileMsg = fileMsg.clone();
                                    fileMsg.getFileBean().setTransmittedSize(transLen);
                                    fileMsg.getFileBean().setStates(Constant.RECEIVE_FILE_FINISH);
                                    mPresenter.fileReceiving(fileMsg);
                                    sendRequest(App.getUserBean(),Protocol.FILE_RECEIVED);
                                    break;
                                }
                                if (countBytes >= 1024*100){
                                    //每接收到100KB数据就更新一次界面
                                    fileMsg = fileMsg.clone();
                                    fileMsg.getFileBean().setTransmittedSize(transLen);
                                    fileMsg.getFileBean().setStates(Constant.RECEIVE_FILE_ING);
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
