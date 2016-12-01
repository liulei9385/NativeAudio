package hello.leilei.lyric;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import hello.leilei.model.LyricBean;
import okio.BufferedSource;
import okio.Okio;

/**
 * 描述：
 * <p>
 * Created by zhaodecang on 2016-11-22下午7:02:32
 * <p>
 * 邮箱：zhaodecang@gmail.com
 */
public class LyricsParser {
    /**
     * 从歌词文件解析出歌词数据列表
     */
    public static ArrayList<LyricBean> parserFromFile(File lyricsFile) {
        ArrayList<LyricBean> lyricsList = new ArrayList<>();
        // 数据可用性检查
        if (lyricsFile == null || !lyricsFile.exists()) {
            lyricsList.add(new LyricBean(0, "没有找到歌词文件。"));
            return lyricsList;
        }

        BufferedSource bufferedSource = null;

        try {
            bufferedSource = Okio.buffer(Okio.source(lyricsFile));
            String line;
            while ((line = bufferedSource.readUtf8Line()) != null) {
                LyricBean lyricBean = parserLine(line);
                if (lyricBean != null)
                    lyricsList.add(lyricBean);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedSource != null)
                    bufferedSource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(lyricsList);// 排序
        return lyricsList;
    }

    /**
     * 解析一行歌词 [01:45.51][02:58.62]整理好心情再出发
     */
    private static LyricBean parserLine(String line) {
        int start = line.indexOf("[");
        int end = line.indexOf("]");
        LyricBean mLyricBean = new LyricBean();

        if (start < 0 || end < 0)
            return new LyricBean(0, "");

        String timeStr = line.substring(start + 1, end);
        if (timeStr.matches("\\d{2}[:]\\d{2}[.]\\d{2}")) {
            long time = parserStartPoint(timeStr);
            String lyric = line.substring(end + 1);
            int index = lyric.indexOf("/");
            if (index >= 0) {
                String[] split = lyric.split("/");
                mLyricBean.chineseLyric = split[1];
                mLyricBean.lyric = split[0];
            } else
                mLyricBean.lyric = lyric;
            mLyricBean.time = time;
        } else {
            return null;
        }

        return mLyricBean;
    }

    /**
     * 解析出一行歌词的起始时间 01:45.51
     */
    private static long parserStartPoint(String startPoint) {
        int time;
        String[] arr = startPoint.split(":");
        // [01 45.51
        String minStr = arr[0].substring(1);
        // 45.51
        arr = arr[1].split("\\.");
        // 45 51
        String secStr = arr[0];
        String mSecStr = arr[1];
        time = parseInt(minStr) * 60 * 1000 + parseInt(secStr) * 1000 + parseInt(mSecStr) * 10;
        return time;
    }

    private static int parseInt(String str) {
        return Integer.parseInt(str);
    }

}