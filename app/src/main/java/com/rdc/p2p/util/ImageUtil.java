package com.rdc.p2p.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.rdc.p2p.app.App;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import java.io.File;

/**
 * Created by Lin Yaotian on 2018/5/1.
 */
public class ImageUtil {

    private static File mFile;

    /**
     * 获取Drawable包下的图片Id
     * @param imageNumber
     * @return
     */
    public static int getImageResId(int imageNumber){
        return App.getContxet().getResources().getIdentifier(
                "iv_"+imageNumber,
                "drawable",
                App.getContxet().getPackageName());
    }

    /**
     * 获取图片的高:宽比例
     * @param path 图片的存储路径
     * @return
     */
    public static float getBitmapSize(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//这个参数设置为true才有效，
        BitmapFactory.decodeFile(path, options);//这里的bitmap是个空
        Log.d("util", "getBitmapSize: width="+options.outWidth+",height="+options.outHeight);
        return options.outHeight*1f/options.outWidth;
    }


    /**
     * 图片的压缩
     * @param filePath
     * @param callback
     */
    public static void compressImage(String filePath, FileCallback callback) {
        mFile = new File(filePath);
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        Tiny.getInstance().source(new File(filePath))
                .asFile()
                .withOptions(options)
                .compress(callback);
    }

    /**
     * 清除压缩图片的缓存
     */
    public static void clearCompressImageDirectory(){
        Tiny.getInstance().clearCompressDirectory();
    }
}
