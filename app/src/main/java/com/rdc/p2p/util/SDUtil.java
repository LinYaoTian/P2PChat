package com.rdc.p2p.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.rdc.p2p.app.App;
public class SDUtil {
    /**
     * 将图片存到本地
     * @param bm 图片
     * @param picName 图片名
     * @return 图片所在的Path
     */
    public static String saveBitmap(Bitmap bm, String picName) {
        try {
            String dir= Environment.getExternalStorageDirectory().getAbsolutePath()+"/P2P/"+picName+".jpg";
            File f = new File(dir);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.flush();
            out.close();
            return f.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();    }
        return null;
    }

    /**
     * 将音频存到本地
     * @param bytes 文件字节数组
     * @param name 音频名字
     * @return 图片所在的Path
     */
    public static String saveAudio(byte[] bytes, String name) {
        try {
            String dir= Environment.getExternalStorageDirectory()+"/P2P/"+name+".m4a";
            File f = new File(dir);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            out.write(bytes);
            out.flush();
            out.close();
            return f.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除文件
     * @param path
     */
    public static void deleteFile(String path){
        File file = new File(path);
        if (file.exists() && file.isFile()){
            file.delete();
        }
    }


}

