package com.rdc.p2p.util;

import com.rdc.p2p.app.App;

/**
 * Created by Lin Yaotian on 2018/5/1.
 */
public class ImageUtil {

    public static int getImageResId(int imageId){
        return App.getContxet().getResources().getIdentifier(
                "iv_"+imageId,
                "drawable",
                App.getContxet().getPackageName());
    }
}
