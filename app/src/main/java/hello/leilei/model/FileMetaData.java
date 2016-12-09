package hello.leilei.model;

import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.Charset;
import java.util.UUID;

import es.dmoral.prefs.Prefs;
import hello.leilei.MainApplication;

/**
 * Created by liulei
 * DATE: 2016/12/2
 * TIME: 8:54
 */
public class FileMetaData {

    private String uri;  //本地文件路径

    public String phoneid; //设备唯一编号

    public String author;
    public String title;
    public String duration;

    public String album;
    public String artlist;

    public void setUri(String uri) {
        byte[] encode = Base64.encode(uri.getBytes(Charset.defaultCharset()), Base64.NO_WRAP);
        this.uri = new String(encode, 0, encode.length);
    }

    public String getUri() {
        if (!TextUtils.isEmpty(uri)) {
            byte[] bytes = Base64.decode(uri.getBytes(Charset.defaultCharset()), Base64.NO_WRAP);
            return new String(bytes, 0, bytes.length);
        }
        return null;
    }

    public static String getUuid() {
        Prefs prefs = Prefs.with(MainApplication.getApp());
        String phoneUuid = prefs.read("phoneUuid");
        if (TextUtils.isEmpty(phoneUuid)) {
            phoneUuid = UUID.randomUUID().toString();
            prefs.write("phoneUuid", phoneUuid);
        }
        return phoneUuid;
    }

}
