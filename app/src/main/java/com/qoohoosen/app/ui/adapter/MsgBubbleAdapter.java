package com.qoohoosen.app.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.scrobot.audiovisualizer.SoundWaveView;
import com.qoohoosen.app.R;
import com.qoohoosen.app.ui.adapter.pojo.MsgBubble;
import com.qoohoosen.service.ForgroundAudioPlayer;
import com.qoohoosen.widget.DebounceClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MsgBubbleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SoundWaveView.SoundWaveOnCompleteListener {

    private final List<MsgBubble> msgBubbleArrayList = new ArrayList<>();
    private Context context;
    private static String contentPlaying = "";
    private int selectedItemPos = -1;
    private int lastItemSelectedPos = -1;
    private Intent intent;

    public MsgBubbleAdapter(Context context) {
        this.context = context;
        intent = new Intent(context, ForgroundAudioPlayer.class);
    }

    public void add(MsgBubble message) {
        msgBubbleArrayList.add(message);
        notifyItemInserted(msgBubbleArrayList.lastIndexOf(message));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflater_msg_bubble,
                null);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ((MessageViewHolder) holder).bind(msgBubbleArrayList.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (msgBubbleArrayList == null)
            return 0;
        return msgBubbleArrayList.size();
    }

    @Override
    public void onComplete(boolean isDone) {

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTitle;
        public TextView textViewDescription;
        public ImageView audio_button_play;
        public FrameLayout frameWaves;
        //        public SoundVisualizerBarView vWaveView;
        public SoundWaveView soundWaveView;


        public MessageViewHolder(View view) {
            super(view);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            audio_button_play = itemView.findViewById(R.id.audio_button_play);
            soundWaveView = itemView.findViewById(R.id.vWaveView);
            frameWaves = itemView.findViewById(R.id.frameWaves);

        }

//        public synchronized void updateDynamicWave(String path) {
//            frameWaves.setVisibility(View.VISIBLE);
////            frameWaves.removeAllViews();
//            SoundWaveView vWaveView = new SoundWaveView(context);
////            vWaveView.getLayoutParams().height = 20;
//            vWaveView.setLayoutParams(new FrameLayout
//                    .LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT));
//
//            vWaveView.setBackgroundColor(context.getResources().getColor(R.color.black_smoke));
//
//            frameWaves.addView(vWaveView);
//
//            try {
//                Uri uri = Uri.fromFile(new File(path));
////            vWaveView.addAudioFileUrl(file.getAbsolutePath());
//                if (vWaveView != null) {
////                    vWaveView.setVisibility(View.VISIBLE);
//                    vWaveView.addAudioFileUri(uri);
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        public synchronized void removeUpdateWave(String path) {
//            frameWaves.removeAllViews();
//            frameWaves.setVisibility(View.GONE);
            try {
                soundWaveView.clearMediaplayer();
            } catch (IOException e) {
                e.printStackTrace();
            }

            soundWaveView.setVisibility(View.GONE);
//            SoundWaveView vWaveView = new SoundWaveView(context);
//            try {
//                Uri uri = Uri.fromFile(new File(path));
////            vWaveView.addAudioFileUrl(file.getAbsolutePath());
//                if (vWaveView != null) {
//                    vWaveView.setVisibility(View.VISIBLE);
//                    vWaveView.addAudioFileUri(uri, true);
//                    frameWaves.addView(vWaveView);
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }


        public void updateWave(String path) {
            try {
                Uri uri = Uri.fromFile(new File(path));
//            vWaveView.addAudioFileUrl(file.getAbsolutePath());
                if (soundWaveView != null) {
                    soundWaveView.setVisibility(View.VISIBLE);
                    soundWaveView.addAudioFileUri(uri);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


//        public void updateWaveBar(String path) {
//            try {
//                Uri uri = Uri.fromFile(new File(path));
////            vWaveView.addAudioFileUrl(file.getAbsolutePath());
//                if (vWaveView != null) {
//                    vWaveView.setVisibility(View.VISIBLE);
////                    vWaveView.addAudioFileUri(uri, true);
//                    vWaveView.updateVisualizer(uri);
//                    vWaveView.updatePlayerPercent(50);
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        @Override
        public String toString() {
            return super.toString();
        }

        public void bind(final MsgBubble message, int adapterPosition) {
            if (message.type == MsgBubble.TYPE_AUDIO) {
                textViewTitle.setText(String.format("Recording #%s", String.valueOf(message.index)));
                textViewDescription.setText(message.path);
            } else if (message.type == MsgBubble.TYPE_TEXT) {
                textViewDescription.setText(message.text);
            } else
                textViewDescription.setText("");


            if (adapterPosition == selectedItemPos) {
                selectedItem(message.path, audio_button_play);
                //updateWave(message.path);
                updateWave(message.path);
            } else {
                defaultItem(audio_button_play);
                removeUpdateWave(message.path);
            }

//            audio_button_play.setVisibility(View.INVISIBLE);
            textViewDescription.setVisibility(View.GONE);
            audio_button_play.setOnClickListener(new DebounceClickListener(v -> {
                selectedItemPos = adapterPosition;
                if (contentPlaying.length() <= 0) {
                    selectedItem(message.path, v);
                    //  updateWave(message.path);
                } else {
                    defaultItem(v);
                    //  removeUpdateWave(message.path);
                }

                if (lastItemSelectedPos == selectedItemPos)
                    return;

                if (lastItemSelectedPos == -1)
                    lastItemSelectedPos = selectedItemPos;
                else {
                    notifyItemChanged(lastItemSelectedPos);
                    lastItemSelectedPos = selectedItemPos;
                }
                notifyItemChanged(selectedItemPos);

            }));


        }
    }//eof bind

    private void defaultItem(View v) {
        contentPlaying = "";
        ((ImageView) v).setImageResource(android.R.drawable.ic_media_play);
        context.stopService(intent);
    }//eof defaultItem

    private void selectedItem(String path, View v) {
        if (path != null && path.length() > 0) {
            contentPlaying = path;
            ((ImageView) v).setImageResource(android.R.drawable.ic_media_pause);
//            context.startService(intent
//                    .putExtra(INTENT_PATH_AUDIO, contentPlaying));


        }//eof if
    }//eof selectedItem


}