/*
 * Copyright (c) 2016. Hefei Royalstar Electronic Appliance Group Co., Ltd. All rights reserved.
 */

package hello.leilei.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * USER: liulei
 * DATA: 2015/1/16
 * TIME: 16:59
 */
@SuppressWarnings({"UnusedAssignment", "ResultOfMethodCallIgnored"})
public class FileUtils {

    private FileUtils() {
    }

    private static File createTempFile(File parent) {
        File tempFile = new File(parent, ".img.temp");
        try {
            if (!tempFile.exists())
                tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    /**
     * @param bitmap       源文件
     * @param destFilePath 保存的路径
     * @return 是否保存成功
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, String destFilePath) {
        FileOutputStream fos = null;
        try {
            File file = new File(destFilePath);
            if (!file.exists())
                file.createNewFile();
            File tempFile = createTempFile(file.getParentFile());
            if (tempFile == null || !file.exists()) return false;
            fos = new FileOutputStream(tempFile);
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            if (compressed) {
                ByteBuffer byteBuffer = readFile(tempFile.getAbsolutePath());
                if (byteBuffer != null)
                    return saveFile(byteBuffer, destFilePath);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException ior) {
                ior.printStackTrace();
            }
        }
        return false;
    }

    /**
     * @param bitmap       源文件
     * @param destFilePath 保存的路径
     * @return 是否保存成功
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, Bitmap.CompressFormat format,
                                           String destFilePath) {
        FileOutputStream fos = null;
        try {
            File file = new File(destFilePath);
            if (!file.exists())
                file.createNewFile();
            File tempFile = createTempFile(file.getParentFile());
            if (tempFile == null || !file.exists()) return false;
            fos = new FileOutputStream(tempFile);

            if (format == null) format = Bitmap.CompressFormat.JPEG;
            boolean compressed = bitmap.compress(format, 95, fos);
            if (compressed) {
                ByteBuffer byteBuffer = readFile(tempFile.getAbsolutePath());
                if (byteBuffer != null)
                    return saveFile(byteBuffer, destFilePath);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException ior) {
                ior.printStackTrace();
            }
        }
        return false;
    }

    public static File getInternalCacheStorage(Context context) {
        return context.getCacheDir();
    }

    public static boolean checkFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static File getExternalSdDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }

    public static File getExternalCacheDir(Context context) {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //跟路径
            File file = Environment.getExternalStorageDirectory();
            if (file != null) {
                File path = buildPath(file, "Android", "data", context.getPackageName(), "cache");
                //noinspection ResultOfMethodCallIgnored
                path.mkdirs();
                return path;
            } else return Environment.getExternalStorageDirectory();
        }
        return getInternalCacheStorage(context);
    }

    /**
     * Append path segments to given base path, returning result.
     */
    public static File buildPath(File base, String... segments) {
        File cur = base;
        for (String segment : segments) {
            if (cur == null) {
                cur = new File(segment);
            } else {
                cur = new File(cur, segment);
            }
        }
        return cur;
    }

    /**
     * 读取文件
     */
    public static ByteBuffer readFile(String filename) {
        FileChannel fiChannel;
        MappedByteBuffer mBuf = null;
        try {
            fiChannel = new FileInputStream(filename).getChannel();
            mBuf = fiChannel.map(FileChannel.MapMode.READ_ONLY, 0, fiChannel.size());
            fiChannel.close();
            fiChannel = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mBuf;
    }

    /**
     * 保存文件
     */
    public static boolean saveFile(ByteBuffer src, String filename) {
        boolean result = false;
        FileChannel foChannel = null;
        try {
            foChannel = new FileOutputStream(filename).getChannel();
            foChannel.write(src);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (foChannel != null) {
                    foChannel.close();
                    foChannel = null;
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return result;
    }

    //noinspection ResultOfMethodCallIgnored
    public static File createFile(File dir, String fileName) {
        if (fileName == null || fileName.equals("")) return null;
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName);
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 建立sd卡的文件，如果没有将在ram中建立
     *
     * @param context
     * @param fileName
     * @return
     */
    public static File createSdFile(Context context, String fileName) {
        return createFile(getExternalCacheDir(context), fileName);
    }

    /**
     * 建立程序sd卡的缓存目录，如果没有将在ram中cache建立
     *
     * @param context
     * @param fileName
     * @return
     */
    public static File createSdCacheFile(Context context, String fileName) {
        if (TextUtils.isEmpty(fileName)) return null;
        File dir = getExternalCacheDir(context);
        if (fileName.matches("\\S*[/]\\S*")) {
            int index = fileName.lastIndexOf("/");
            String dirName = fileName.substring(0, index);
            createDir(dir, dirName);
        }
        return createFile(getExternalCacheDir(context), fileName);
    }

    public static File createDir(File dir, String fileName) {
        if (fileName == null || fileName.equals("")) return null;
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取文件的大小
     *
     * @param filePath 文件路径
     * @return
     */
    public static long getFileSize(String filePath) {
        long s = 0;
        if (TextUtils.isEmpty(filePath))
            return 0;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                s = fis.available();
                fis.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return s;
    }

    /**
     * 获取文件的大小
     *
     * @param file
     * @return
     */
    public static long getFileSize(File file) {
        long s = 0;
        FileChannel fc = null;
        try {
            if (file != null && file.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                //TODO  int 类型，只能读取1.99G
                fc = fis.getChannel();
                s = fc.size();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (fc != null)
                    fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    /**
     * 复制文件
     *
     * @param sourceFilePath
     * @param destFilePath
     */
    public static void copyFile(String sourceFilePath, String destFilePath) {
        File sourceFile = new File(sourceFilePath);
        File sdFile = new File(destFilePath);
        if (sourceFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(sourceFile);
                if (!sdFile.exists())
                    sdFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(sdFile, false);
                int length;
                byte[] data = new byte[1024];
                while ((length = fis.read(data)) != -1) {
                    fos.write(data, 0, length);
                }
                fos.flush();
                fos.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;
        try {
            File mFile = new File(filePath);
            if (mFile.isFile() && mFile.exists()) {
                mFile.delete();
            }
        } catch (Exception ignored) {
        }
    }

}