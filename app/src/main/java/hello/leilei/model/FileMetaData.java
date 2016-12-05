package hello.leilei.model;

import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.Charset;

import cn.bmob.v3.BmobObject;
import hello.leilei.base.audioplayer.NativePlayer;

/**
 * Created by liulei
 * DATE: 2016/12/2
 * TIME: 8:54
 */
public class FileMetaData extends BmobObject {

    private String uri;  //本地文件路径

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

}
