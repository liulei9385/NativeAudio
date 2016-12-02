package hello.leilei.model;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

import java.io.File;

/**
 * Created by liulei
 * DATE: 2016/11/30
 * TIME: 15:43
 */
public class SplashImgBean extends BmobObject {

    private BmobFile imgData;

    public BmobFile getImgData() {
        return imgData;
    }

    public void setImgData(BmobFile imgData) {
        this.imgData = imgData;
    }
}
