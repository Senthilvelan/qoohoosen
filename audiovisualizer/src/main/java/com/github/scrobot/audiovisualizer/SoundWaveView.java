package com.github.scrobot.audiovisualizer;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.scrobot.audiovisualizer.player.DefaultSoundViewPlayer;
import com.github.scrobot.audiovisualizer.player.SoundViewPlayer;
import com.github.scrobot.audiovisualizer.player.SoundViewPlayerOnCompleteListener;
import com.github.scrobot.audiovisualizer.player.SoundViewPlayerOnDurationListener;
import com.github.scrobot.audiovisualizer.player.SoundViewPlayerOnPauseListener;
import com.github.scrobot.audiovisualizer.player.SoundViewPlayerOnPlayListener;
import com.github.scrobot.audiovisualizer.player.SoundViewPlayerOnPreparedListener;
import com.github.scrobot.audiovisualizer.utils.ConverterUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class SoundWaveView extends FrameLayout implements SoundViewPlayerOnPlayListener,
        SoundViewPlayerOnDurationListener,
        SoundViewPlayerOnPauseListener,
        SoundViewPlayerOnPreparedListener,
        SoundViewPlayerOnCompleteListener {

    private SoundWaveOnCompleteListener soundWaveOnCompleteListener;
    protected final Context context;
    protected SoundViewPlayer player = new DefaultSoundViewPlayer();
    protected int layout = R.layout.sounwave_view;

    private SoundVisualizerBarView visualizerBar;
    private TextView timer;
    private ImageView actionButton;
    private AtomicInteger duration = new AtomicInteger();

    private final String TAG = SoundWaveView.class.getCanonicalName();

    public SoundWaveView(Context context) {
        super(context);
        this.context = context;

        init(context);
    }

    public SoundWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init(context);
    }

    public SoundWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        init(context);
    }

//    public void setSoundWaveOnCompleteListener(){
//        soundWaveOnCompleteListener
//    }

    public void setPlayer(SoundViewPlayer player) {
        this.player = player;
    }

    public void addAudioFileUri(final Uri audioFileUri) throws IOException {
//        player.preparePlayer();
        player.setAudioSource(context, audioFileUri);
        visualizerBar.updateVisualizer(audioFileUri);
    }

    public void clearMediaplayer() throws IOException {
        actionButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play));
//        visualizerBar.updatePlayerPercent(0);
//        visualizerBar.clearAnimation();
//        player.clearMediaplayer();
    }


//    public void addAudioFileUrl(String audioFileUrl) throws IOException {
//        player.setAudioSource(audioFileUrl);
//
//        visualizerBar.updateVisualizer(audioFileUrl);
//    }

    protected void init(final Context context) {
        View view = LayoutInflater.from(context).inflate(layout, this);

        player.setOnCompleteListener(this)
                .setOnDurationListener(this)
                .setOnPauseListener(this)
                .setOnPlayListener(this)
                .setOnPrepariedListener(this);

        visualizerBar = view.findViewById(R.id.vSoundBar);
        timer = view.findViewById(R.id.vTimer);
        actionButton = view.findViewById(R.id.vActionButton);
        actionButton.setVisibility(VISIBLE);
        actionButton.setOnClickListener(v -> player.toggle());
    }

    public void toggle() {
        if (player != null)
            player.toggle();
    }


    @Override
    public void onDurationProgress(SoundViewPlayer player, Long duration, Long currentTimestamp) {
        visualizerBar.updatePlayerPercent(currentTimestamp / (float) duration);
        timer.setText(ConverterUtil.convertMillsToTime(duration - currentTimestamp));
    }

    @Override
    public void onPause(SoundViewPlayer player) {
        actionButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play));
    }

    @Override
    public void onPlay(SoundViewPlayer player) {
        actionButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause));
    }

    @Override
    public void onPrepared(SoundViewPlayer player) {
        timer.setText(ConverterUtil.convertMillsToTime(player.getDuration()));
    }

    @Override
    public void onComplete(SoundViewPlayer player) {
        actionButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play));
        visualizerBar.updatePlayerPercent(0);

    }

    public SoundViewPlayer getPlayer() {
        return player;
    }

    public SoundVisualizerBarView getVisualizerBar() {
        return visualizerBar;
    }

    public void setVisualizerBar(SoundVisualizerBarView visualizerBar) {
        this.visualizerBar = visualizerBar;
    }

    public TextView getTimer() {
        return timer;
    }

    public void setTimer(TextView timer) {
        this.timer = timer;
    }

    public ImageView getActionButton() {
        return actionButton;
    }

    public void setActionButton(ImageView actionButton) {
        this.actionButton = actionButton;
    }

    public AtomicInteger getDuration() {
        return duration;
    }

    public void setDuration(AtomicInteger duration) {
        this.duration = duration;
    }

    public interface SoundWaveOnCompleteListener {

        public void onComplete(boolean isDone);

    }

}
