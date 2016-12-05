package hello.leilei.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.Charset;

import cn.bmob.v3.BmobObject;
import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by liulei
 * DATE: 2016/12/2
 * TIME: 8:54
 */
public class FileMetaData extends BmobObject {

    private String uri;  //本地文件路径

    public byte[] art;
    public String author;
    public String title;
    public String duration;

    public String album;
    public String artlist;

    private Bitmap mBitmap;

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

    public Observable<Bitmap> getBitmapObservable() {
        return Observable.fromCallable((Func0<Bitmap>) this::getArtBitmap)
                .compose(RxUiUtils.applySchedulers());
    }


    private Bitmap getArtBitmap() {
        if (art != null && art.length > 0) {
            if (mBitmap == null || mBitmap.isRecycled())
                mBitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            return mBitmap;
        }
        return null;
    }

}
