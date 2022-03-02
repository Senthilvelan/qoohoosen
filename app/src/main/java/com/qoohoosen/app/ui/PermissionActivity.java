package com.qoohoosen.app.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.scrobot.audiovisualizer.SoundWaveView;
import com.qoohoosen.app.R;

import java.io.File;
import java.io.IOException;

public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        final SoundWaveView view = findViewById(R.id.vWaveView);

//        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/raw/record_error");

//        Uri uri = Uri.parse("/data/user/0/com.qoohoosen.app/files/15e6afa5-01bc-4916-96a0-38795a2c258f.wav");

        File file = new File(getFilesDir(), "15e6afa5-01bc-4916-96a0-38795a2c258f.wav");

//        Uri uri = Uri.parse("/data/user/0/com.qoohoosen.app/files/15e6afa5-01bc-4916-96a0-38795a2c258f.wav");
//        Uri uri = Uri.parse(file.getAbsolutePath());
        Uri  uri = Uri.fromFile(file);

        try {
//            view.addAudioFileUrl(file.getAbsolutePath());
            view.addAudioFileUri(uri,false);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
