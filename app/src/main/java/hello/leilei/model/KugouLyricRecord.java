package hello.leilei.model;

import java.util.List;

/**
 * Created by liulei
 * DATE: 2016/12/5
 * TIME: 16:04
 */

public class KugouLyricRecord {

    public String info;
    public int status;
    public String proposal;
    public String keyword;
    public List<CandidatesBean> candidates;

    public static class CandidatesBean {

        public  String soundname;
        public int krctype;
        public String nickname;
        public String originame;
        public String accesskey;
        public String origiuid;
        public int score;
        public int hitlayer;
        public int duration;
        public String sounduid;
        public String transname;
        public String uid;
        public String transuid;
        public String song;
        public String id;
        public int adjust;
        public String singer;
        public String language;
    }
}
