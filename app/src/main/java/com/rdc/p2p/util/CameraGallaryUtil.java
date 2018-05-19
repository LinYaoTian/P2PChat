package com.rdc.p2p.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

/**
 * Created by Lin Yaotian on 2018/5/18.
 */
public class CameraGallaryUtil {
    private static final int RESULT_OK = -1;
    public static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    public static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    public static final int PHOTO_REQUEST_CUT = 3;// 裁剪结果

    public static final Uri fileUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()
            .getPath() + File.separator + "temp.jpg"));//临时储存的Uri

    public static Bitmap getBitmapFromCG(Activity activity, int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        switch (requestCode) {
            // 如果是拍照
            case PHOTO_REQUEST_TAKEPHOTO:
                if (resultCode == RESULT_OK) {
                    // 没有指定特定存储路径的时候，data不为null
                    if (data != null) {
                        if (data.getData() != null) {
                            startPhotoZoom(activity, data.getData());
                        }
                    } else {
                        startPhotoZoom(activity, fileUri);
                    }
                }
                break;
            // 如果是从相册选取
            case PHOTO_REQUEST_GALLERY:
                if (data != null) {
                    if (data.getData() != null) {
                        startPhotoZoom(activity, data.getData());
                    }
                }
                break;
            //如果是裁剪完成
            case PHOTO_REQUEST_CUT:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        bitmap = bundle.getParcelable("data");
                    }
                }
                break;
        }
        return bitmap;
    }

    private static void startPhotoZoom(Activity activity, Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        //aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 300);
        intent.putExtra("aspectY", 400);
        //outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 400);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }
}
