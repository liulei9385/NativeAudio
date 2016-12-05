package hello.leilei.utils;

import android.text.TextUtils;

/**
 * Created by liulei on 16-8-2.
 * TIME : 下午1:48
 * COMMECTS :常用数据类型转换工具类
 */
public class NumberUtils {

    public static int safeParseInteger(String source, int defaultVal) {
        int result;
        try {
            if (TextUtils.isEmpty(source))
                result = defaultVal;
            else
                result = Integer.parseInt(source);
        } catch (NumberFormatException ex) {
            result = defaultVal;
        }
        return result;
    }

    public static long safeParseLong(String source, long defaultVal) {
        long result;
        try {
            if (TextUtils.isEmpty(source))
                result = defaultVal;
            else
                result = Long.parseLong(source);
        } catch (NumberFormatException ex) {
            result = defaultVal;
        }
        return result;
    }


}
