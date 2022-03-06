package com.qoohoosen.app.ui;

import static com.qoohoosen.utils.Constable.FILE_EXT;
import static com.qoohoosen.utils.Constable.FREQUENCY;
import static com.qoohoosen.utils.Constable.MIN_RECORD_TIME_THRESHOLD;
import static com.qoohoosen.utils.Constable.RECORD_CANCEL;
import static com.qoohoosen.utils.Constable.RECORD_COMPLETED;
import static com.qoohoosen.utils.Constable.RECORD_START;
import static com.qoohoosen.utils.Constable.TIMER_1000;

import android.Manifest;
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
    private long time;
    private Recorder recorder;
    private static String onGoingFile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbarHome = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbarHome);

        permissionChecking();

//        setupNoiseRecorder();
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

        //focus on mic
//        bottomTextRecordView.getMessageView().requestFocus();
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
                        // permission is granted,start the process
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                            showSettingsDialog();

                        }
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


//        boolean recordPermissionAvailable = false;
//        try {
//            recordPermissionAvailable = ContextCompat
//                    .checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
//                    == PERMISSION_GRANTED;
//        } catch (Exception e) {
//            debug(e.toString());
//        }
//
//        if (recordPermissionAvailable)
//            return true;
//
//        ActivityCompat.
//                requestPermissions(MainActivity.this,
//                        new String[]{Manifest.permission.RECORD_AUDIO},
//                        0);
//
//
//        return false;
    }

    @Override
    public void onRecordingStarted() {

        if (!isPermissionGranted()) {
            return;
        }
        debug("started");
        if (recorder == null) {
            startInitRecorder();
        } else {
            AudioTinyPlayer.getAudioTinyPlayerInstance()
                    .playTinyMusic(MainActivity.this, RECORD_START);
            recorder.startRecording();
        }

        time = System.currentTimeMillis() / (1000);
    }

    @Override
    public void onRecordingLocked() {
        debug("locked");
    }

    @Override
    public void onRecordingCompleted() {
        if (!isPermissionGranted()) {
            return;
        }
        debug("completed");

        int recordTime = (int) ((System.currentTimeMillis() / (TIMER_1000)) - time);

        if (recordTime > MIN_RECORD_TIME_THRESHOLD && msgBubbleAdapter != null) {

            int size = msgBubbleAdapter.getItemCount();

            msgBubbleAdapter.add(new MsgBubble(size + 1,
                    recordTime, onGoingFile));
            msgBubbleAdapter.setSelectionLast();

            if (recyclerViewMsgBubble != null)
                recyclerViewMsgBubble.smoothScrollToPosition(size);
            checkRecyclerItems();

        } else
            Utilities.showSnackBar(recyclerViewMsgBubble, "Not a valid audio !");


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
        if (!isPermissionGranted())
            return;

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
        if (bottomTextRecordView != null)
            bottomTextRecordView.animateRecordButton(maxPeak);
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
                    MsgBubble msgBubble = new MsgBubble(index, dirFiles.length, fileOutput);
                    msgBubbleAdapter.add(msgBubble);
                    debug(fileOutput);
                    index++;
                }//eof if
            }//eof for
        }//eof if


    }//eof getListOfFiles


    private void clearFiles() throws Exception {
//        getFilesDir().delete();
        File[] dirFiles = getFilesDir().listFiles();
        if (dirFiles != null && dirFiles.length != 0) {
            for (File dirFile : dirFiles) {
                if (dirFile != null)
                    dirFile.delete();
            }//eof for
        }//eof if

        msgBubbleAdapter.clean();


    }//eof clearFiles


//    private synchronized void showToast(String message) {
//        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
//    }

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

    // navigating user to app settings
    private void openSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        } catch (Exception e) {
            debug(e.toString());
        }

    }

}
