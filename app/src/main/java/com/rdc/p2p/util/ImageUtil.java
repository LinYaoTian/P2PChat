package com.rdc.p2p.util;

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
