package hello.leilei.base.listener;

import android.widget.SeekBar;

/**
 * Created by liulei
 * DATE: 2016/12/2
 * TIME: 15:02
 */

public class SimpleSeekbarChangeListener implements SeekBar.OnSeekBarChangeListener {

    public boolean fromUser;
    public int sysProgress;
    public int userProgress;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.fromUser = fromUser;
        if (this.fromUser)
            this.userProgress = progress;
        else this.sysProgress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
