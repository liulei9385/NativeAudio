package hello.leilei.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by liulei
 * DATE: 2016/12/5
 * TIME: 14:15
 */
public class Md5Utils {

    public static String md5(String str) {
        StringBuilder mess = new StringBuilder();
        try {

            MessageDigest md = MessageDigest.getInstance("MD5");//获取加密器，方式是Md5
            byte[] bytes = str.getBytes();
            byte[] digest = md.digest(bytes);

            for (byte b : digest) {
                int d = b & 0xff;//把每个字节转成16进制数
                String hexString = Integer.toHexString(d);
                if (hexString.length() == 1) {//字节的高4位为0
                    hexString = "0" + hexString;
                }
                mess.append(hexString);//把每个字节对应的2位十六进制数当成字符串拼接一起

            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return mess.toString();
    }

}
