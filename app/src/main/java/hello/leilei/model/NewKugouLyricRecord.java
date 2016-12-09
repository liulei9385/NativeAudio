package hello.leilei.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by liulei
 * DATE: 2016/12/9
 * TIME: 11:13
 */

public class NewKugouLyricRecord {

    public int status;
    public String error;
    public DataBean data;
    public int errcode;

    public static class DataBean {

        public String tab;
        public int correctiontype;
        public int timestamp;
        public int total;
        public int istag;
        public int istagresult;
        public int forcecorrection;
        public String correctiontip;
        public List<AggregationBean> aggregation;
        public List<InfoBean> info;


        public static class AggregationBean {
            /**
             * key : DJ
             * count : 0
             */

            public String key;
            public int count;
        }

        public static class InfoBean {
            public String filename;
            public String songname;
            public int m4afilesize;
            @SerializedName("320hash")
            public String value320hash;
            public String mvhash;
            public int privilege;
            public int filesize;
            public String source;
            public int bitrate;
            public int ownercount;
            public String album_name;
            public String topic;
            @SerializedName("320filesize")
            public int value320filesize;
            public int isnew;
            public int duration;
            public String album_id;
            public int Accompany;
            public String singername;
            public String extname;
            @SerializedName("320privilege")
            public int value320privilege;
            public int sourceid;
            public int srctype;
            public int feetype;
            public int sqfilesize;
            public String hash;
            public int sqprivilege;
            public String sqhash;
            public String othername;
            public List<GroupBean> group;


            public static class GroupBean {

                public String filename;
                public String songname;
                public int m4afilesize;
                @SerializedName("320hash")
                public String value320hash;
                public String mvhash;
                public int privilege;
                public int filesize;
                public String source;
                public int bitrate;
                public int ownercount;
                public String album_name;
                public String topic;
                @SerializedName("320filesize")
                public int value320filesize;
                public int isnew;
                public int duration;
                public String album_id;
                public int Accompany;
                public String singername;
                public String extname;
                @SerializedName("320privilege")
                public int value320privilege;
                public int sourceid;
                public int srctype;
                public int feetype;
                public int sqfilesize;
                public String hash;
                public int sqprivilege;
                public String sqhash;
                public String othername;

            }
        }
    }
}
