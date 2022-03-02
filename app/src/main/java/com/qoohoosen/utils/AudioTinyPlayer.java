package com.qoohoosen.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

public class AudioTinyPlayer {

    public static AudioTinyPlayer audioTinyPlayerInstance;

    private MediaPlayer player;

    public static AudioTinyPlayer getAudioTinyPlayerInstance() {
        if (audioTinyPlayerInstance == null)
            audioTinyPlayerInstance = new AudioTinyPlayer();
        return audioTinyPlayerInstance;
    }

    private AudioTinyPlayer() {

    }

    public synchronized void playTinyMusic(Context context, int soundRes) {

        if (soundRes == 0)
            return;

        try {
            player = new MediaPlayer();
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(soundRes);
            if (afd == null) return;
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            player.prepare();
            player.start();
            player.setOnCompletionListener(mp -> {
                mp.release();
                mp = null;
                player = null;
            });

            player.setOnErrorListener((mp, what, extra) -> {
                mp.release();
                mp = null;
                player = null;
                return false;
            });

            player.setLooping(false);
        } catch (IOException e) {
            e.printStackTrace();
        }//eof try...catch


    }//eof playTinyMusic


}
