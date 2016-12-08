package hello.leilei.base.audioplayer;

/**
 * Created by liulei
 * DATE: 2016/12/8
 * TIME: 16:55
 */

public class PlayerLoader {

    public enum PlayerType {
        NATIVE, EXOPLAYER
    }

    public BasePlayer getPlayer(PlayerType type) {

        if (type == PlayerType.EXOPLAYER)
            return AudioPlayer.getInstance();
        else if (type == PlayerType.NATIVE)
            return NativePlayer.getInstance();

        return NativePlayer.getInstance();
    }
}
