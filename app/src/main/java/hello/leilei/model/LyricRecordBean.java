package hello.leilei.model;

import java.util.List;

import hello.leilei.utils.CollectionUtils;

/**
 * Created by liulei
 * DATE: 2016/12/1
 * TIME: 15:02
 */
public class LyricRecordBean {

    /**
     * count : 15
     * code : 0
     * result : [{"aid":2848529,"lrc":"http://s.gecimi.com/lrc/344/34435/3443588.lrc","sid":3443588,"artist_id":2,"song":"海阔天空"},{"aid":2346662,"lrc":"http://s.gecimi.com/lrc/274/27442/2744281.lrc","sid":2744281,"artist_id":2396,"song":"海阔天空"},{"aid":1889264,"lrc":"http://s.gecimi.com/lrc/210/21070/2107014.lrc","sid":2107014,"artist_id":8715,"song":"海阔天空"},{"aid":2075717,"lrc":"http://s.gecimi.com/lrc/236/23651/2365157.lrc","sid":2365157,"artist_id":8715,"song":"海阔天空"},{"aid":1563419,"lrc":"http://s.gecimi.com/lrc/166/16685/1668536.lrc","sid":1668536,"artist_id":9208,"song":"海阔天空"},{"aid":1567586,"lrc":"http://s.gecimi.com/lrc/167/16739/1673997.lrc","sid":1673997,"artist_id":9208,"song":"海阔天空"},{"aid":1571906,"lrc":"http://s.gecimi.com/lrc/167/16796/1679605.lrc","sid":1679605,"artist_id":9208,"song":"海阔天空"},{"aid":1573814,"lrc":"http://s.gecimi.com/lrc/168/16819/1681961.lrc","sid":1681961,"artist_id":9208,"song":"海阔天空"},{"aid":1656038,"lrc":"http://s.gecimi.com/lrc/179/17907/1790768.lrc","sid":1790768,"artist_id":9208,"song":"海阔天空"},{"aid":1718741,"lrc":"http://s.gecimi.com/lrc/187/18757/1875769.lrc","sid":1875769,"artist_id":9208,"song":"海阔天空"},{"aid":2003267,"lrc":"http://s.gecimi.com/lrc/226/22642/2264296.lrc","sid":2264296,"artist_id":9208,"song":"海阔天空"},{"aid":2020610,"lrc":"http://s.gecimi.com/lrc/228/22889/2288967.lrc","sid":2288967,"artist_id":9208,"song":"海阔天空"},{"aid":2051678,"lrc":"http://s.gecimi.com/lrc/233/23323/2332322.lrc","sid":2332322,"artist_id":9208,"song":"海阔天空"},{"aid":2412704,"lrc":"http://s.gecimi.com/lrc/283/28376/2837689.lrc","sid":2837689,"artist_id":9208,"song":"海阔天空"},{"aid":2607041,"lrc":"http://s.gecimi.com/lrc/311/31116/3111659.lrc","sid":3111659,"artist_id":9208,"song":"海阔天空"}]
     */

    public int count;
    public int code;
    public List<Result> result;

    public static class Result {

        /**
         * aid : 2848529
         * lrc : http://s.gecimi.com/lrc/344/34435/3443588.lrc
         * sid : 3443588
         * artist_id : 2
         * song : 海阔天空
         */

        public int aid;
        public String lrc; // 歌词路径
        public int sid;
        public int artist_id;
        public String song;
    }

    public String getFirstDownloadPath() {
        if (count > 0 && CollectionUtils.isNotEmpty(result)) {
            return result.get(0).lrc;
        }
        return null;
    }
}
