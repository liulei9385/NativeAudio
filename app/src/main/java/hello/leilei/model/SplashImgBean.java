package hello.leilei.model;

import cn.bmob.v3.BmobObject;

import java.io.File;

/**
 * Created by liulei on 2016/11/30.
 */
public class SplashImgBean extends BmobObject {

    private File imgData;

    public File getImgData() {
        return imgData;
    }

    public void setImgData(File imgData) {
        this.imgData = imgData;
    }
}
