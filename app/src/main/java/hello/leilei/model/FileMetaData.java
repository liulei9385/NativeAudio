package hello.leilei.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import hello.leilei.utils.RxUiUtils;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by liulei
 * DATE: 2016/12/2
 * TIME: 8:54
 */

public class FileMetaData {

    public String uri;  //本地文件路径

    public byte[] art;
    public String author;
    public String title;
    public String duration;

    public String album;
    public String artlist;

    private Bitmap mBitmap;

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
