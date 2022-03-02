package com.qoohoosen.app.ui;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.qoohoosen.utils.Constable.FREQUENCY;
import static com.qoohoosen.utils.Constable.INTENT_PATH_AUDIO;
import static com.qoohoosen.utils.Constable.MIN_RECORD_TIME_THRESHOLD;
import static com.qoohoosen.utils.Constable.RECORD_CANCEL;
import static com.qoohoosen.utils.Constable.RECORD_COMPLETED;
import static com.qoohoosen.utils.Constable.RECORD_START;
import static com.qoohoosen.utils.Constable.TIMER_1000;

import android.Manifest;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qoohoosen.app.R;
import com.qoohoosen.app.ui.adapter.MsgBubbleAdapter;
import com.qoohoosen.app.ui.adapter.pojo.MsgBubble;
import com.qoohoosen.recorder.AudioRecordConfig;
import com.qoohoosen.recorder.OmRecorder;
import com.qoohoosen.recorder.PullTransport;
import com.qoohoosen.recorder.PullableSource;
import com.qoohoosen.recorder.Recorder;
import com.qoohoosen.service.ForgroundAudioPlayer;
import com.qoohoosen.utils.AudioTinyPlayer;
import com.qoohoosen.widget.BottomTextRecordView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class MainActivity extends AppCompatActivity implements
        BottomTextRecordView.RecordingListener, BottomTextRecordView.RecordPermissionHandler {
    private String TAG = MainActivity.this.getClass().getSimpleName();

    //Widgets
    private BottomTextRecordView bottomTextRecordView;
    private RecyclerView recyclerViewMsgBubble;

    //Adapter
    private MsgBubbleAdapter msgBubbleAdapter;

    //vars
    private long time;
    private Recorder recorder;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbarHome = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbarHome);

        startService(new Intent(this, ForgroundAudioPlayer.class)
                .putExtra(INTENT_PATH_AUDIO, ""));

//        setupNoiseRecorder();
        startInitRecorder();

        bottomTextRecordView = new BottomTextRecordView();
        bottomTextRecordView.initView(findViewById(R.id.viewRootMain));

        //Container - bottom record view
        View containerView = bottomTextRecordView
                .setContainerView(R.layout.activity_main_layout_recycle);
        recyclerViewMsgBubble = containerView.findViewById(R.id.recyclerViewMsgBubble);
        ShimmerLayout shimmerLayoutHeader = containerView.findViewById(R.id.shimmer_layout_header);
        shimmerLayoutHeader.startShimmerAnimation();

        setUpRecycler();

        // bottomTextRecordView.getMessageView().requestFocus();
        bottomTextRecordView.setRecordingListener(this);
        bottomTextRecordView.setRecordPermissionHandler(this);

    }


    private void setUpRecycler() {
        recyclerViewMsgBubble.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerViewMsgBubble.setHasFixedSize(false);
        recyclerViewMsgBubble.setItemAnimator(new DefaultItemAnimator());
        attachAdapter();
    }


    private void attachAdapter() {
        msgBubbleAdapter = new MsgBubbleAdapter();
        recyclerViewMsgBubble.setAdapter(msgBubbleAdapter);
    }

    @Override
    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        boolean recordPermissionAvailable = ContextCompat
                .checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                == PERMISSION_GRANTED;

        if (recordPermissionAvailable)
            return true;

        ActivityCompat.
                requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        0);

        return false;
    }

    @Override
    public void onRecordingStarted() {
        showToast("started");
        debug("started");

        if (recorder != null) {
            AudioTinyPlayer.getAudioTinyPlayerInstance()
                    .playTinyMusic(MainActivity.this, RECORD_START);
            recorder.startRecording();
        }

        time = System.currentTimeMillis() / (1000);
    }

    @Override
    public void onRecordingLocked() {
        showToast("locked");
        debug("locked");
    }

    @Override
    public void onRecordingCompleted() {
        debug("completed");

        int recordTime = (int) ((System.currentTimeMillis() / (TIMER_1000)) - time);

        if (recordTime > MIN_RECORD_TIME_THRESHOLD)
            msgBubbleAdapter.add(new MsgBubble(recordTime));


        try {
            if (recorder != null) {
                AudioTinyPlayer.getAudioTinyPlayerInstance()
                        .playTinyMusic(MainActivity.this, RECORD_COMPLETED);
                recorder.stopRecording();
                startInitRecorder();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRecordingCanceled() {
        debug("canceled");
        try {
            if (recorder != null) {
                AudioTinyPlayer.getAudioTinyPlayerInstance()
                        .playTinyMusic(MainActivity.this, RECORD_CANCEL);
                recorder.stopRecording();
                startInitRecorder();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startInitRecorder() {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(),
                        audioChunk -> animateVoice((float) (audioChunk.maxAmplitude() / 200.0))),
                file());
    }

//    private void setupNoiseRecorder() {
//        recorder = OmRecorder.wav(
//                new PullTransport.Noise(mic(),
//                        new PullTransport.OnAudioChunkPulledListener() {
//                            @Override
//                            public void onAudioChunkPulled(AudioChunk audioChunk) {
//                                animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
//                            }
//                        },
//                        new WriteAction.Default(),
//                        new Recorder.OnSilenceListener() {
//                            @Override
//                            public void onSilence(long silenceTime) {
//                                Log.e("silenceTime", String.valueOf(silenceTime));
//                                Toast.makeText(MainActivity.this, "silence of " + silenceTime + " detected",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }, 200
//                ), file()
//        );
//    }

    private void animateVoice(final float maxPeak) {
//        recordButton.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, FREQUENCY
                )
        );
    }

    @NonNull
    private File file() {
//        return new File(Environment.getExternalStorageDirectory()
//                + "/qoohoo",
//                UUID.randomUUID().toString()
//                        + ".wav");
        return new File(getFilesDir(),
                UUID.randomUUID().toString() + ".wav");

    }

    private synchronized void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void debug(String log) {
        Log.d(TAG, log);
    }


}
