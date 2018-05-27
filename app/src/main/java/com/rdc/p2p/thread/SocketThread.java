package com.rdc.p2p.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
                    imageInputStream.read(bytes);
                    dos.write(bytes);
                    dos.flush();
                    Log.d(TAG, "sendImageMsg: "+size);
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
                    fileInputStream.read(fileBytes);
                    dos.write(fileBytes);
                    dos.flush();
                    break;
                case Protocol.CONNECT:
                    dos.writeUTF(GsonUtil.gsonToJson(messageBean.transformToUserBean()));
                    break;
                case Protocol.CONNECT_RESPONSE:
                    dos.writeUTF(GsonUtil.gsonToJson(messageBean.transformToUserBean()));
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
            DataInputStream dis = new DataInputStream(mSocket.getInputStream());
            //循环读取消息
            while (true){
                Log.d(TAG, "ip="+ mTargetIp +" start read Protocol !");
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
                        MessageBean responseMsg = new MessageBean();
                        responseMsg.setUserImageId(App.getUserBean().getUserImageId());
                        responseMsg.setNickName(App.getUserBean().getNickName());
                        responseMsg.setMsgType(Protocol.CONNECT_RESPONSE);
                        sendMsg(responseMsg);
                        break;
                    case Protocol.CONNECT_RESPONSE:
                        Log.d(TAG, "CONNECT_RESPONSE");
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
                        Log.d(TAG, "getImageMsg: "+size);
                        byte[] bytes = new byte[size];

                        File dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/P2P");
                        File file;
                        if (dirFile.exists()){
                            if (!dirFile.isDirectory()){
                                dirFile.delete();
                                dirFile.mkdirs();
                            }
                        }else {
                            dirFile.mkdirs();
                        }
                        file = new File(dirFile,"林耀填.jpg");
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        int transLen = 0;
                        while(true){
                            int read = 0;
                            read = dis.read(bytes);
                            if(read == -1)
                                break;
                            transLen += read;
                            Log.d(TAG, "run: 接收文件进度"+100 * transLen/size +"%...");
                            fos.write(bytes,0, read);
                            fos.flush();
                        }
                        Log.d(TAG, "run: 接收文件成功！transLen="+transLen+",size="+size);
//                        dis.readFully(bytes);
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,size);
//                        MessageBean imageMsg = new MessageBean();
//                        imageMsg.setUserIp(mTargetIp);
//                        imageMsg.setMine(false);
//                        imageMsg.setMsgType(Protocol.IMAGE);
//                        imageMsg.setImagePath(SDUtil.saveBitmap(bitmap,System.currentTimeMillis()+""));
//                        Log.d(TAG, "run: "+imageMsg.getImagePath());
//                        mPresenter.messageReceived(imageMsg);
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
                        byte[] fileBytes = new byte[fileSize];
                        dis.readFully(fileBytes);
                        dis.read(fileBytes);
                        dis.readFully(fileBytes,0,fileSize);
                        MessageBean fileMsg = new MessageBean();
                        fileMsg.setUserIp(mTargetIp);
                        fileMsg.setMine(false);
                        fileMsg.setMsgType(Protocol.FILE);
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

                        Log.d(TAG, "run: fileName="+fileName+"fileType="+fileType+",name="+name);
                        FileBean fileBean = new FileBean();
                        fileBean.setFilePath(SDUtil.saveFile(fileBytes,name,fileType));
                        fileBean.setFileName(SDUtil.getFileName(fileBean.getFilePath()));
                        fileBean.setFileSize(SDUtil.getFileByteSize(fileBean.getFilePath()));
                        fileBean.setStates(Constant.RECEIVE_ING);
                        fileBean.setTransmittedSize(0);
                        mPresenter.messageReceived(fileMsg);
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
