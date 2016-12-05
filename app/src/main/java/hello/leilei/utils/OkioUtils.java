package hello.leilei.utils;

import java.io.File;
import java.io.IOException;

import hello.leilei.MainApplication;
import hello.leilei.model.FileMetaData;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by liulei
 * DATE: 2016/12/5
 * TIME: 16:24
 */
public class OkioUtils {

    public static void writeByteToFile(File mFile, byte[] bytes) {
        if (bytes == null) return;
        BufferedSink bufferedSink = null;
        try {
            bufferedSink = Okio.buffer(Okio.sink(mFile));
            bufferedSink.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedSink != null)
                    bufferedSink.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
