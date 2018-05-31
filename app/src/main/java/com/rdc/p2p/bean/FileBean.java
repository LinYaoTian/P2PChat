package com.rdc.p2p.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by Lin Yaotian on 2018/5/27.
 */
public class FileBean implements Cloneable{
    private String filePath;
    private String fileName;
    private int fileSize;
    private int states;//传输状态
    private int transmittedSize;//已经传输的字节

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public FileBean copy(int states, int transmittedSize){
        FileBean fileBean = new FileBean();
        fileBean.setStates(states);
        fileBean.setTransmittedSize(transmittedSize);
        fileBean.setFileSize(fileSize);
        fileBean.setFileName(fileName);
        fileBean.setFilePath(filePath);
        return fileBean;
    }

    public String getFilePath() {
        return filePath == null ? "" : filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? "" : filePath;
    }

    public String getFileName() {
        return fileName == null ? "" : fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? "" : fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getStates() {
        return states;
    }

    public void setStates(int states) {
        this.states = states;
    }

    public int getTransmittedSize() {
        return transmittedSize;
    }

    public void setTransmittedSize(int transmittedSize) {
        this.transmittedSize = transmittedSize;
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", states=" + states +
                ", transmittedSize=" + transmittedSize +
                '}';
    }
}
