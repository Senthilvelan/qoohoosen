package com.qoohoosen.app.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.scrobot.audiovisualizer.SoundWaveView;
import com.qoohoosen.app.R;
import com.qoohoosen.app.ui.adapter.pojo.MsgBubble;
import com.qoohoosen.utils.UtilsAnimation;
import com.qoohoosen.widget.DebounceClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;


public class MsgBubbleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SoundWaveView.SoundWaveOnCompleteListener {

    private final List<MsgBubble> msgBubbleArrayList = new ArrayList<>();
    private Context context;
    private static String contentPlaying = "";
    private int selectedItemPos = -1;
    private int lastItemSelectedPos = -1;
    private SoundWaveView lastSeletedView = null;

//    private Intent intent;

    public MsgBubbleAdapter(Context context) {
        this.context = context;
        //Enable when use bg audio service
//        intent = new Intent(context, ForgroundAudioPlayer.class);
    }

    public synchronized void add(MsgBubble message) {
        msgBubbleArrayList.add(message);
        notifyItemInserted(msgBubbleArrayList.lastIndexOf(message));
        setSelectionLast();
    }

    public void clean() {
        msgBubbleArrayList.clear();
        notifyDataSetChanged();
    }

    public void setSelectionLast() {
        selectedItemPos = getItemCount();
        notifyItemInserted(selectedItemPos);
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
        public SoundWaveView soundWaveView;
        public RelativeLayout relativeLayoutMsgInflater;
        public ShimmerLayout shimmer_layout_inflater;


        public MessageViewHolder(View view) {
            super(view);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            audio_button_play = itemView.findViewById(R.id.audio_button_play);
            soundWaveView = itemView.findViewById(R.id.vWaveView);
            frameWaves = itemView.findViewById(R.id.frameWaves);
            relativeLayoutMsgInflater = itemView.findViewById(R.id.relativeLayoutMsgInflater);
            shimmer_layout_inflater = itemView.findViewById(R.id.shimmer_layout_inflater);
        }


        @Override
        public String toString() {
            return super.toString();
        }

        public void bind(MsgBubble msgBubble, final int adapterPosition) {

            if (msgBubble == null)
                return;

            if (msgBubble.type == MsgBubble.TYPE_AUDIO) {
                textViewTitle.setText(String.format("Recording #%s",
                        String.valueOf(msgBubble.index + 1)));
            } else if (msgBubble.type == MsgBubble.TYPE_TEXT) {
                textViewDescription.setText(msgBubble.text);
            } else
                textViewDescription.setText("");

            if (adapterPosition == selectedItemPos) {
                selectedItem(frameWaves, soundWaveView, audio_button_play,
                        msgBubble.path, adapterPosition);
                shimmer_layout_inflater.startShimmerAnimation();

            } else {
                defaultItem(frameWaves, soundWaveView, audio_button_play,
                        msgBubble.path, adapterPosition);
                shimmer_layout_inflater.stopShimmerAnimation();
            }


            relativeLayoutMsgInflater.setOnClickListener(new DebounceClickListener(v -> {
                selectedItemPos = adapterPosition;
                if (lastItemSelectedPos == selectedItemPos)
                    return;

                if (lastItemSelectedPos == -1) {
                    lastItemSelectedPos = selectedItemPos;
                    lastSeletedView = soundWaveView;
                } else {
                    removeUpdateWave(lastSeletedView);
//                    notifyItemChanged(lastItemSelectedPos);
                    lastItemSelectedPos = selectedItemPos;
                    lastSeletedView = soundWaveView;
                }

                updateWave(soundWaveView, msgBubble.path);
//                notifyItemChanged(selectedItemPos);

            }, 50L));

        }
    }//eof bind

    private void defaultItem(ViewGroup viewGroup, SoundWaveView soundWaveView,
                             View view, String path, int adapterPosition) {
        contentPlaying = "";
        ((ImageView) view).setImageResource(com.github.scrobot.audiovisualizer.R.drawable.ic_play);
        removeUpdateWave(soundWaveView);
        /*
        Enable when service needs
        //Notification foreground
                context.stopService(intent);*/


    }//eof defaultItem

    public synchronized void removeUpdateWave(SoundWaveView soundWaveView) {

        try {
            soundWaveView.clearMediaplayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        UtilsAnimation.slideDown(soundWaveView, 500L);

    }//eof removeUpdateWave

    private void selectedItem(ViewGroup viewGroup, SoundWaveView soundWaveView,
                              View view, String path, int adapterPosition) {
        if (path != null && path.length() > 0) {
            contentPlaying = path;
            ((ImageView) view)
                    .setImageResource(com.github.scrobot.audiovisualizer.R.drawable.ic_pause);
            updateWave(soundWaveView, path);
            /*//Enable when service needs
                        context.startService(intent
                                .putExtra(INTENT_PATH_AUDIO, contentPlaying));*/
        }//eof if
    }//eof selectedItem


    public void updateWave(SoundWaveView view, String path) {
        try {
            Uri uri = Uri.fromFile(new File(path));
            if (view != null) {
                UtilsAnimation.slideUp(view, 500L);
                view.addAudioFileUri(uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//eof updateWave


}