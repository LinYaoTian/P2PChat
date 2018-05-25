package com.rdc.p2p.widget;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * Created by Lin Yaotian on 2018/5/24.
 */
public class MsgImageTarget extends BitmapImageViewTarget
{
    // 长图，宽图比例阈值
    public static final int RATIO_OF_LARGE = 3;
    // 长图截取后的高宽比（宽图截取后的宽高比）
    public static int HW_RATIO = 3;

    public MsgImageTarget(ImageView view)
    {
        super(view);
    }

    @Override
    public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        super.onResourceReady(resolveBitmap(resource), transition);
    }

    private Bitmap resolveBitmap(Bitmap resource)
    {
        int srcWidth = resource.getWidth();
        int srcHeight = resource.getHeight();

        if (srcWidth > srcHeight)
        {
            float srcWHRatio = (float) srcWidth / srcHeight;
            // 宽图
            if (srcWHRatio > RATIO_OF_LARGE)
            {
                return Bitmap.createBitmap(resource, 0, 0, srcHeight * HW_RATIO, srcHeight);
            }
        }
        else
        {
            float srcHWRatio = (float) srcHeight / srcWidth;
            // 长图
            if (srcHWRatio > RATIO_OF_LARGE)
            {
                return Bitmap.createBitmap(resource, 0, 0, srcWidth, srcWidth * HW_RATIO);
            }
        }
        return resource;
    }
}

