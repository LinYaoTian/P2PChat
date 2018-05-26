package com.rdc.p2p.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.rdc.p2p.app.App;
public class SDUtil {

    private static final String TAG = "SDUtil";
    /**
     * 将图片存到本地
     * @param bm 图片
     * @param name 图片名
     * @return 图片所在的Path
     */
    public static String saveBitmap(Bitmap bm, String name) {
        try {
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
            while (true){
                file = new File(dirFile,name+".jpg");
                if (file.exists()){
                    name = name+"&";
                }else {
                    file.createNewFile();
                    break;
                }
            }
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
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
            File dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/P2P");
            File file;
            if (dirFile.exists()){
                if (!dirFile.isDirectory()){
                    if (!dirFile.isDirectory()){
                        dirFile.delete();
                        dirFile.mkdirs();
                    }
                }
            }else {
                dirFile.mkdirs();
            }
            while (true){
                file = new File(dirFile,name+".m4a");
                if (file.exists()){
                    name = name+"&";
                }else {
                    file.createNewFile();
                    break;
                }
            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(bytes);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将文件存到本地
     * @param bytes 文件字节数组
     * @param name 文件名字
     * @return 图片所在的Path
     */
    public static String saveFile(byte[] bytes, String name,String fileType) {
        try {
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
            while (true){
                file = new File(dirFile,name+"."+fileType);
                if (file.exists()){
                    name = name+"&";
                }else {
                    file.createNewFile();
                    break;
                }
            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(bytes);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取文件的名字
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath){
        File file = new File(filePath);
        return file.getName();
    }

    /**
     * 获取文件的大小
     * @param fileSize
     * @return
     */
    public static long getFileSize(String fileSize){
        File file = new File(fileSize);
        return file.length();
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

    /**
     * 根据Uri获取文件的绝对路径
     * @param context
     * @param uri
     * @return
     */
    public static String getFilePathByUri(final Context context, final Uri uri) {
        if (Build.VERSION.SDK_INT >= 19){
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // DownloadsProvider
                else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
            return null;
        }else {
            return getDataColumn(context,uri,null,null);
        }
    }

    /**
     * 通过ContentProvider文件的绝对路径
     * @param context
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

//    public static String getMimeTypeFromFile(File file) {
//        String type = "*/*";
//        String fName = file.getName();
//        //获取后缀名前的分隔符"."在fName中的位置。
//        int dotIndex = fName.lastIndexOf(".");
//        if (dotIndex > 0) {
//            //获取文件的后缀名
//            String end = fName.substring(dotIndex, fName.length()).toLowerCase(Locale.getDefault());
//            //在MIME和文件类型的匹配表中找到对应的MIME类型。
//            HashMap<String, String> map = MyMimeMap.getMimeMap();
//            if (!TextUtils.isEmpty(end) && map.keySet().contains(end)) {
//                type = map.get(end);
//            }
//        }
//        return type;
//    }

    /** 使用系统API，根据url获得对应的MIME类型 */
    public static String getMimeTypeFromUrl(String url) {
        String type = null;
        //使用系统API，获取URL路径中文件的后缀名（扩展名）
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        Log.d("SDUtil", "getMimeTypeFromUrl: "+extension);
        if (extension != null) {
            //使用系统API，获取MimeTypeMap的单例实例，然后调用其内部方法获取文件后缀名（扩展名）所对应的MIME类型
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


}

