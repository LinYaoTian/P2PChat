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
     * @return 图片所在的Uri
     */
    public static Uri saveBitmap(Bitmap bm, String picName) {
        Uri uri;
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
            if (Build.VERSION.SDK_INT >= 24){
                uri = FileProvider.getUriForFile(App.getContxet(),"com.rdc.p2p.fileprovider",f);
            }else {
                uri = Uri.fromFile(f);
            }
            return uri;
        } catch (IOException e) {
            e.printStackTrace();    }
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

