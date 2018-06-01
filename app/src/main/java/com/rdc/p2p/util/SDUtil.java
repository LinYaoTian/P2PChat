package com.rdc.p2p.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.MimeTypeFilter;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.rdc.p2p.activity.ChatDetailActivity;
import com.rdc.p2p.app.App;
public class SDUtil {

    private static final String TAG = "SDUtil";
    /**
     * 将图片存到本地
     * @param bm 图片
     * @param name 图片名不包含文件格式
     * @param type 文件格式，包含 .
     * @return 图片所在的Path
     */
    public static String saveBitmap(Bitmap bm, String name,String type) {
        try {
            File file = getMyAppFile(name,type);
            assert file != null;
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
     * 获取本App缓存目录下的文件对象
     * @param fileName 文件名 e.g: sun
     * @param fileType 文件类型 e.g: .jpg
     * @return
     */
    public static File getMyAppFile(String fileName, String fileType){
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
                file = new File(dirFile,fileName+fileType);
                if (file.exists()){
                    fileName = fileName+"&";
                }else {
                    file.createNewFile();
                    break;
                }
            }
            return file;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取本App缓存的文件路径
     * @param name 包含文件格式
     * @return
     */
    public static String getFilePath(String name){
        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/P2P/"+name;
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
     * @return 文件所在的Path
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
                file = new File(dirFile,name+fileType);
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
     * 获取文件的字节大小
     * @param filePath
     * @return
     */
    public static int getFileByteSize(String filePath){
        return (int) new File(filePath).length();
    }

    /**
     * 将字节按合适比例转换为B/KB/MB
     * @param bytesSize
     * @return
     */
    public static String  bytesTransform(int bytesSize){
        double fileSize = bytesSize;
        DecimalFormat df = new DecimalFormat(".0");
        if (fileSize > 1024*1024){
            return df.format(fileSize/(1024*1024))+" MB";
        }else if (fileSize > 1024){
            return df.format(fileSize/1024)+" KB";
        }else {
            return fileSize+" B";
        }
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

    /**
     * 根据文件本地路径获取文件类型
     * @param filePath
     * @return
     */
    public static String getMimeTypeFromFilePath(String filePath) {
        String type = null;
        String fName = new File(filePath).getName();
        String fileExtension = getFileExtension(fName);
        if (fileExtension != null){
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        }
        return type;
    }

    /**
     * 获取文件后缀名(不包含 . )
     * @param fileName
     * @return String fileExtension or null
     */
    public static String getFileExtension(String fileName){
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            //获取文件的后缀名
            return fileName.substring(dotIndex+1, fileName.length()).toLowerCase(Locale.getDefault());
        }
        return null;
    }

    /**
     * 获取文件的Uri
     * @param filePath
     * @return
     */
    public static Uri getFileUri(String filePath){
        File file = new File(filePath);
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24){
            //Android 7.0 适配
            uri = FileProvider.getUriForFile(App.getContxet(),"com.rdc.p2p.fileprovider",file);
            App.getContxet().grantUriPermission(App.getContxet().getPackageName(),uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    private static String renameFile(String filePath){
        String fileName = getFileName(filePath);
        String type;
        String name;
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            //获取文件的后缀名
            type = fileName.substring(dotIndex, fileName.length());
            name = fileName.substring(0,dotIndex);
        }else {
            name = fileName;
            type = "";
        }
        File oldFile = new File(filePath);
        File newFile = getMyAppFile(name,type);
        oldFile.renameTo(newFile);
        assert newFile != null;
        return newFile.getAbsolutePath();
    }

    /**
     * 检查是否在列表上已经存在该文件，若是，则修改文件名，直到不重名为止
     * @param fileNameList 在列表上的文件名集合
     * @return
     */
    public static String checkFileName(@NonNull List<String> fileNameList, String filePath){
        String fileName = getFileName(filePath);
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fileName.lastIndexOf(".");
        String type;
        String name;
        if (dotIndex > 0) {
            //获取文件的后缀名
            type = fileName.substring(dotIndex, fileName.length());
            name = fileName.substring(0,dotIndex);
        }else {
            name = fileName;
            type = "";
        }
        while (true){
            if (fileNameList.contains(fileName)){
                fileName = name + "*" +type;
            }else {
                break;
            }
        }
        return fileName;
    }


}

