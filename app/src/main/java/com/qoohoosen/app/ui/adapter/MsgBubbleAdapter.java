package com.qoohoosen.app.ui.adapter;

import static com.qoohoosen.utils.Constable.INTENT_PATH_AUDIO;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qoohoosen.app.R;
import com.qoohoosen.app.ui.adapter.pojo.MsgBubble;
import com.qoohoosen.service.ForgroundAudioPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;


public class MsgBubbleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<MsgBubble> msgBubbleArrayList = new ArrayList<>();
    private static SimpleDateFormat timeFormatter;
    private Context context;
    private static String contentPlaying = "";


    public MsgBubbleAdapter(Context context) {
        this.context = context;
        timeFormatter = new SimpleDateFormat("m:ss", Locale.getDefault());
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
        ((MessageViewHolder) holder).bind(msgBubbleArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        if (msgBubbleArrayList == null)
            return 0;
        return msgBubbleArrayList.size();
    }

    public static String getAudioTime(long time) {
        time *= 1000;
        timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return timeFormatter.format(new Date(time));
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTitle;
        public TextView textViewDescription;
        //        public AudioWaveView wave;
        ImageView audio_button_play;


        public MessageViewHolder(View view) {
            super(view);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
//            wave = itemView.findViewById(R.id.audio_wave);
            audio_button_play = itemView.findViewById(R.id.audio_button_play);

        }

        public void bind(final MsgBubble message) {

            if (message.type == MsgBubble.TYPE_AUDIO) {
//                textViewTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mic_small,
//                        0, 0, 0);
                textViewTitle.setText(String.valueOf(message.index));
                textViewDescription.setText(message.path);

            } else if (message.type == MsgBubble.TYPE_TEXT) {
//                textViewDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                        0, 0);
                textViewDescription.setText(message.text);

            } else {
//                textViewDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                        0, 0);
                textViewDescription.setText("");
            }

            audio_button_play.setOnClickListener(v -> {

                if (contentPlaying.length() <= 0) {
                    contentPlaying = message.path;
                    audio_button_play.setImageResource(android.R.drawable.ic_media_pause);
                    context.startService(new Intent(context, ForgroundAudioPlayer.class)
                            .putExtra(INTENT_PATH_AUDIO, contentPlaying));
                } else {
                    contentPlaying = "";
                    audio_button_play.setImageResource(android.R.drawable.ic_media_play);
                    context.stopService(new Intent(context, ForgroundAudioPlayer.class));
                }


            });

//            wave.setScaledData(new byte[0]);
//            wave.setProgress(0);
//
//            final Random rnd = new Random();
//            wave.setRawData(generateDummyItem(1024 * 200, rnd));
        }
    }

    private static byte[] generateDummyItem(int length, @NonNull Random rnd) {
        final byte[] raw = new byte[length];
        rnd.nextBytes(raw);

        return raw;
    }
}