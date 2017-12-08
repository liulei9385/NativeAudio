package hello.leilei.nativeaudio;

import java.util.List;

/**
 * Created by liulei on 16-3-19.
 * TIME : 下午3:19
 * COMMECTS :
 */
public class FileFind {

    static {
        System.loadLibrary("native-audio-jni");
    }

    public static native void scanDir(String dirPath);

    public static native List<String> searchMp3File(String dirPath);

    public static synchronized List<String> getMp3FileFromPath(String dirPath) {
        return searchMp3File(dirPath);
    }

   /* public static String newStringUtf8(byte[] input) {
        try {
            return new String(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return ""; //JSTRING_CONVERT_FAIL
        }
    }*/
}