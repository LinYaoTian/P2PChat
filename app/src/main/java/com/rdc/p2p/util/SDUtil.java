package com.rdc.p2p.util;

/**
 * 1/判断SD卡是否存在(挂载);<isMounted()>.
 * 2/获取SD卡的根目录(物理绝对路径);<getAbsolutePath()>.
 * 3/获取外部公共储存文件夹的路径;<getPublicExternalSDpath()>.
 * 4/获取应用内部储存目录(跟随应用自身的文件路径);<getPrivateExternalSDpath()>.
 * 5/获取SD卡的总空间大小;<getSDSize()>.
 * 6/获取SD卡的可用(剩余)空间;<getAvailabeSize()>.
 * 7/将文件储存到SD卡上的目录里;<saveFileIntoSDCard()>.
 * 8/将文件储存到SD卡上的公共文件目录里;<saveFileIntoPublicSDCard()>.
 * 9/将文件储存到应用本身的某些类型的目录里面(私人的);<saveFileIntoPrivateSDCard()>.
 * 10/ 获取存储在SDCard中的文件;<getFileFromSDCard()>.
 * 11/在SD卡上创建新文件;<createFileInSDCard()>.
 * 12/ 在SD卡上创建新目录;<creatSDDir()>.
 * 13/判断SD卡上的文件夹是否存在;<isFileExist()>.
 * 14/将一个InputStream里面的数据写入到SD卡中 ,如:从网络上读取图片;<writeToSDFromInput()>.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
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


}

