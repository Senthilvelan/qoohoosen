package com.qoohoosen.app.ui;

import static com.qoohoosen.utils.Constable.FILE_EXT;
import static com.qoohoosen.utils.Constable.FREQUENCY;
import static com.qoohoosen.utils.Constable.MIN_RECORD_TIME_THRESHOLD;
import static com.qoohoosen.utils.Constable.RECORD_CANCEL;
import static com.qoohoosen.utils.Constable.RECORD_COMPLETED;
import static com.qoohoosen.utils.Constable.RECORD_START;
import static com.qoohoosen.utils.Constable.TIMER_1000;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.qoohoosen.app.R;
import com.qoohoosen.app.ui.adapter.MsgBubbleAdapter;
import com.qoohoosen.app.ui.adapter.pojo.MsgBubble;
import com.qoohoosen.recorder.AudioRecordConfig;
import com.qoohoosen.recorder.OmRecorder;
import com.qoohoosen.recorder.PullTransport;
import com.qoohoosen.recorder.PullableSource;
import com.qoohoosen.recorder.Recorder;
import com.qoohoosen.utils.AudioTinyPlayer;
import com.qoohoosen.utils.Utilities;
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
    private AppCompatButton buttonClean;

    //Adapter
    private MsgBubbleAdapter msgBubbleAdapter;

    //vars
    private long time, stopTime;
    private Recorder recorder;
    private static String onGoingFile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbarHome = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbarHome);
        permissionChecking();
        startInitRecorder();

        bottomTextRecordView = new BottomTextRecordView();
        bottomTextRecordView.initView(findViewById(R.id.viewRootMain));

        //Container - bottom record view
        View containerView = bottomTextRecordView
                .setContainerView(R.layout.activity_main_layout_recycle);
        recyclerViewMsgBubble = containerView.findViewById(R.id.recyclerViewMsgBubble);
        buttonClean = containerView.findViewById(R.id.buttonClean);
        ShimmerLayout shimmerLayoutHeader = containerView.findViewById(R.id.shimmer_layout_header);
        shimmerLayoutHeader.startShimmerAnimation();

        setUpRecycler();

        bottomTextRecordView.setRecordingListener(this);
        bottomTextRecordView.setRecordPermissionHandler(this);

        getListOfFiles();
        buttonClean.setOnClickListener(v -> {
            try {
                clearFiles();
                checkRecyclerItems();
            } catch (Exception e) {
                debug(e.toString());
            }
        });

        checkRecyclerItems();
    }

    private void checkRecyclerItems() {
        buttonClean.setText("Clean");

        if (msgBubbleAdapter != null) {
            if (msgBubbleAdapter.getItemCount() <= 0)
                buttonClean.setText("Long press the mic to record audio!");
        } else {
            buttonClean.setText("Long press the mic to record audio!");
        }

    }

    private void permissionChecking() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied())
                            showSettingsDialog();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private void setUpRecycler() {
        recyclerViewMsgBubble.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerViewMsgBubble.setHasFixedSize(false);
        recyclerViewMsgBubble.setItemAnimator(new DefaultItemAnimator());
        attachAdapter();
    }


    private void attachAdapter() {
        msgBubbleAdapter = new MsgBubbleAdapter(MainActivity.this);
        recyclerViewMsgBubble.setAdapter(msgBubbleAdapter);
    }

    @Override
    public boolean isPermissionGranted() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        int checkVal = getApplicationContext()
                .checkCallingOrSelfPermission(android.Manifest.permission.RECORD_AUDIO);

        if (checkVal == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            permissionChecking();
            return false;
        }
    }

    @Override
    public void onRecordingStarted() {
        long currentTime = System.currentTimeMillis();

        if (time != 0) {
            int timeDiff = (int) (currentTime - time);
            if (timeDiff <= 900)
                return;
        }

        if (recorder == null)
            startInitRecorder();

        playRecordStatus(RECORD_START);

        time = currentTime;
        recorder.startRecording();

    }


    @Override
    public void onRecordingLocked() {
//        debug("locked");
    }

    @Override
    public void onRecordingCompleted() {
        long currentTime = System.currentTimeMillis();
        if (stopTime != 0) {
            int timeDiff = (int) (currentTime - stopTime);
            if (timeDiff <= 900)
                return;
        }

//        if (!isPermissionGranted()) {
//            return;
//        }
//        debug("onRecordingCompleted");

        stopRecord();
        int recordTime = (int) ((currentTime / (TIMER_1000)) - (time / (TIMER_1000)));

        if (recordTime > MIN_RECORD_TIME_THRESHOLD) {

            int size = msgBubbleAdapter.getItemCount();

            msgBubbleAdapter.add(new MsgBubble(size + 1,
                    recordTime, onGoingFile));

            if (recyclerViewMsgBubble != null)
                recyclerViewMsgBubble.smoothScrollToPosition(size);
            checkRecyclerItems();
            playRecordStatus(RECORD_COMPLETED);

        } else
            Utilities.showSnackBar(MainActivity.this, recyclerViewMsgBubble,
                    "Long press and hold the mic, to record valid audio !");

        stopTime = currentTime;

    }

    private void stopRecord() {

        new Thread(() -> {
            try {
                recorder.stopRecording();
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
            startInitRecorder();
        }).start();

    }


    @Override
    public void onRecordingCanceled() {
        playRecordStatus(RECORD_CANCEL);
    }

    private void startInitRecorder() {
        if (!isPermissionGranted())
            return;

        new Thread(() -> recorder
                = OmRecorder.wav(new PullTransport.Default(mic()), file())).start();

//        new Thread(() -> recorder = OmRecorder.wav(
//                new PullTransport.Default(mic(),
//                        audioChunk -> animateVoice((float) (audioChunk.maxAmplitude() / 200.0))),
//                file())).start();

    }


//    private void animateVoice(final float maxPeak) {
////        if (bottomTextRecordView != null)
////            bottomTextRecordView.animateRecordButton(maxPeak);
//    }//eof animateVoice

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, FREQUENCY
                )
        );
    }//eof mic

    @NonNull
    private File file() {
        File file = new File(getFilesDir(), UUID.randomUUID().toString() + FILE_EXT);
//        return new File(Environment.getExternalStorageDirectory()
//                + "/qoohoo",
//                onGoingFile);
        onGoingFile = file.getAbsolutePath().toString();
        return file;
    }


    private void getListOfFiles() {
        File[] dirFiles = getFilesDir().listFiles();
        if (dirFiles != null && dirFiles.length != 0) {
            int index = 0;
            for (File dirFile : dirFiles) {
                String fileOutput = dirFile.toString();
                if (!fileOutput.isEmpty()) {
                    if (dirFile.length() > 102400) {
                        MsgBubble msgBubble = new MsgBubble(index, dirFiles.length, fileOutput);
                        msgBubbleAdapter.add(msgBubble);
//                        debug(fileOutput);
                        index++;
                    } else {
                        dirFile.delete();
                    }
                }//eof if
            }//eof for
        }//eof if


    }//eof getListOfFiles


    private void clearFiles() throws Exception {
        File[] dirFiles = getFilesDir().listFiles();
        if (dirFiles != null && dirFiles.length != 0) {
            for (File dirFile : dirFiles) {
                if (dirFile != null)
                    dirFile.delete();
            }//eof for
        }//eof if
        msgBubbleAdapter.clean();

    }//eof clearFiles

    private void debug(String log) {
        Log.d(TAG, log);
    }


    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("QooHoo needs permission to record voice ! " +
                " You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();

    }

    private void openSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            permissionActivityResultLauncher.launch(intent);
        } catch (Exception e) {
            debug(e.toString());
        }

    }

    ActivityResultLauncher<Intent> permissionActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                }
            });

    private void playRecordStatus(int statusMusic) {

        new Thread(() -> {
            AudioTinyPlayer.getAudioTinyPlayerInstance()
                    .playTinyMusic(MainActivity.this, statusMusic);
//            if (recorder == null) {
//                startInitRecorder();
//            } else {
//
//                recorder.startRecording();
//            }
        }).start();
    }


    //avoid to record silent
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
//                            }
//                        }, 200
//                ), file()
//        );
//    }


    //IntentService : Not in use, used Thread
/*    public static class PlayTinyIntentService extends IntentService {


        public PlayTinyIntentService() {
            super(PlayTinyIntentService.class.getSimpleName().toString());
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {

        }
    }*/
}
