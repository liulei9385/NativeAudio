package hello.leilei.base.audioplayer;

/**
 * Created by liulei
 * DATE: 2016/12/2
 * TIME: 14:52
 */

public interface IPlayerCallback {

    void onPlayState(int state);

    void onProgressChanged(NativePlayer.ProgressItem mProgressItem);

    void onLoadResourceComplete();

    class SimpleCallback implements IPlayerCallback {

        @Override
        public void onPlayState(int state) {
        }

        @Override
        public void onProgressChanged(NativePlayer.ProgressItem mProgressItem) {
        }

        @Override
        public void onLoadResourceComplete() {
        }
    }

}
