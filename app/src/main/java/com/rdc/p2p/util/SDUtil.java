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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

/**
 * * 方法：getDataDirectory() 解释：返回 File ，获取 Android 数据目录。
 *
 * 方法：getDownloadCacheDirectory() 解释：返回 File ，获取 Android 下载/缓存内容目录。
 *
 * 方法：getExternalStorageDirectory() 解释：返回 File ，获取外部存储目录即 SDCard
 *
 * 方法：getExternalStoragePublicDirectory(String type) 解释：返回 File，公用的外部存储器目录
 *
 * 方法：getExternalStorageState() 解释：返回 File,获取外部存储设备的当前状态
 *
 * 方法：getRootDirectory() 解释：返回 File ，获取 Android 的根目录
 *
 * 方法:getAbsolutePath() 解释:返回一个字符串,得到当前文件的绝对路径
 *
 * @author wen
 *
 */
public class SDUtil {
    /**
     * 判断SD卡是否存在(挂载)
     *
     * @return true:存在 flase:不存在
     */
    public Boolean isMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡的根目录(物理绝对路径)
     *
     * @return sd卡的路径:storage/sdcard
     */
    public String getAbsolutePath() {
        if (isMounted()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }

    /**
     * 获取外部公共储存文件夹的路径
     *
     * @param type
     * 调用示例: (Environment.DIRECTORY_MUSIC)/(文件名)--->String
     *
     * DIRECTORY_MUSIC, DIRECTORY_PODCASTS, DIRECTORY_RINGTONES,
     * DIRECTORY_ALARMS, DIRECTORY_NOTIFICATIONS, DIRECTORY_PICTURES,
     * DIRECTORY_MOVIES, DIRECTORY_DOWNLOADS, or DIRECTORY_DCIM.
     *
     * @return 返回sd卡公共储存路径:storage/sdcard/Music
     */
    public String getPublicExternalSDpath(String type) {
        if (isMounted()) {
            return Environment.getExternalStoragePublicDirectory(type)
                    .getAbsolutePath();
        }
        return null;
    }

    /**
     * 应用内部储存目录(跟随应用自身的文件路径)
     *
     * @param context
     * 上下文
     * @param type
     * 调用示例: (Environment.DIRECTORY_PICTURES)/(文件名)--->String
     *
     * DIRECTORY_MUSIC, DIRECTORY_PODCASTS, DIRECTORY_RINGTONES,
     * DIRECTORY_ALARMS, DIRECTORY_NOTIFICATIONS, DIRECTORY_PICTURES,
     * DIRECTORY_MOVIES, DIRECTORY_DOWNLOADS, or DIRECTORY_DCIM.
     * @return: /storage/sdcard/Android/data/应用的包名/files/Pictures(文件名)
     */
    public String getPrivateExternalSDpath(Context context, String type) {
        if (isMounted()) {
            return context.getExternalFilesDir(type).getAbsolutePath();
        }
        return null;
    }

    /**
     * 计算SD卡的总空间大小*注解*:StatFs这个类用于检索查询文件系统存储空间的相关信息
     *
     * @return SD卡的总空间大小(long),单位为MB,
     */
    public Long getSDSize() {
        if (isMounted()) {
            StatFs statFs = new StatFs(getAbsolutePath());
            // 计算出来SD一共分了多少块储存区域.
            Long blockCount = statFs.getBlockCountLong();
            // 计算每块储存区域的大小
            Long blockSize = statFs.getBlockSizeLong();
            return (blockCount * blockSize) / (1024 * 1024);
        }
        return (long) 0;
    }

    /**
     * 获取SD卡的可用(剩余)空间
     *
     * @return 计算SD卡的可用(剩余)空间是多少,单位为MB
     */
    public Long getAvailabeSize() {
        if (isMounted()) {
            StatFs statFs = new StatFs(getAbsolutePath());
            Long availableBlocks = statFs.getAvailableBlocksLong();
            Long blockSize = statFs.getBlockSizeLong();
            return (Long) ((availableBlocks * blockSize) / (1024 * 1024));
        }
        return (long) 0;
    }

    /**
     * 将文件储存到SD卡上的目录里
     *
     * @param data
     * :要保存的数据byte[].
     * @param dir
     * :保存到的路径.
     * @param fileName
     * :保存的文件名.
     * @return boolean:保存的状态,是否保存成功
     */
    public boolean saveFileIntoSDCard(byte[] data, String dir, String fileName) {
        File file = new File(getAbsolutePath() + File.separator + dir);
        if (!file.exists()) {
            // 创建文件的所有完整路径
            file.mkdirs();
        }
        BufferedOutputStream bos = null;
        try {
            // 使用管道流,对接到要生成的文件上面,效率更高.
            bos = new BufferedOutputStream(new FileOutputStream(new File(file,
                    fileName)));
            // 将数据写出到文件中.
            bos.write(data, 0, data.length);
            bos.flush();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 将文件储存到SD卡上的公共文件目录里
     *
     * @param data
     * :要保存的数据Byte[]
     * @param type
     * :保存到的路径
     * @param fileName
     * :保存的文件名
     * @return boolean:保存的状态,是否保存成功
     */
    public boolean saveFileIntoPublicSDCard(byte[] data, String type,
                                            String fileName) {
        String publicSDPath = getPublicExternalSDpath(type);
        File file = new File(publicSDPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(new File(file,
                    fileName)));
            bos.write(data, 0, data.length);
            bos.flush();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 将文件储存到应用本身的某些类型的目录里面(私人的)
     *
     * @param data
     * :要保存的数据
     * @param context
     * :上下文
     * @param type
     * :保存到的路径
     * @param fileName
     * :保存的文件名
     * @return
     */
    public boolean saveFileIntoPrivateSDCard(byte[] data, Context context,
                                             String type, String fileName) {
        String privateSDPath = getPrivateExternalSDpath(context, type);
        File file = new File(privateSDPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(new File(file,
                    fileName)));
            bos.write(data, 0, data.length);
            bos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 获取存储在SDCard中的文件
     *
     * @param filePath
     * :文件的路径(String path = SDCardUtil.getSDPath() + File.separator +
     * "syc" + File.separator + "p1.jpg";)
     * @return
     */
    public byte[] getFileFromSDCard(String filePath) {

        File file = new File(filePath);
        // 判读文件是否存在, 如果文件存在的时候,才进行操作.
        if (file.exists()) {
            BufferedInputStream bis = null;
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                bis = new BufferedInputStream(new FileInputStream(file));
                int len = 0;
                byte[] buffer = new byte[1024 * 8];
                while ((len = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }

                return baos.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                        baos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 在SD卡上创建新文件
     *
     * @param dir
     * :目录的路径
     * @param fileName
     * :要创建文件的名字
     * @return 文件
     */
    public File createFileInSDCard(String dir, String fileName)
            throws IOException {
        File file = new File(getAbsolutePath() + File.separator + dir
                + File.separator + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上创建新目录
     *
     * @param dir
     * 创建目录的路径
     * @return 文件
     */

    public File creatSDDir(String dir) {
        File dirFile = new File(getAbsolutePath() + File.separator + dir
                + File.separator);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return dirFile;
    }

    /**
     * 判断SD卡上的文件夹是否存在
     *
     * @param dir
     * :目录路径
     * @param fileName
     * :文件夹名称
     * @return 是否存在
     */

    public boolean isFileExist(String dir, String fileName) {
        File file = new File(getAbsolutePath() + File.separator + dir
                + File.separator + fileName);
        return file.exists();
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中 ,从网络上读取图片
     *
     * @param dir
     * :目录的路径
     * @param fileName
     * :文件名
     * @param input
     * :InputStream流
     * @return:文件
     */
    public File writeToSDFromInput(String dir, String fileName,
                                   InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            int size = input.available();
            // 拥有可读可写权限，并且有足够的容量
            if (isMounted() && size < getAvailabeSize()) {
                creatSDDir(dir);
                file = createFileInSDCard(dir, fileName);
                output = new BufferedOutputStream(new FileOutputStream(file));
                byte buffer[] = new byte[4 * 1024];
                int temp;
                while ((temp = input.read(buffer)) != -1) {
                    output.write(buffer, 0, temp);
                }
                output.flush();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}

