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
import com.qoohoosen.widget.DebounceClickListener;

import java.util.ArrayList;
import java.util.List;


public class MsgBubbleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTitle;
        public TextView textViewDescription;
        public ImageView audio_button_play;


        public MessageViewHolder(View view) {
            super(view);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            audio_button_play = itemView.findViewById(R.id.audio_button_play);
        }

        @Override
        public String toString() {
            return super.toString();
        }

        public void bind(final MsgBubble message, int adapterPosition) {

            if (message.type == MsgBubble.TYPE_AUDIO) {
                textViewTitle.setText(String.valueOf(message.index));
                textViewDescription.setText(message.path);
            } else if (message.type == MsgBubble.TYPE_TEXT) {
                textViewDescription.setText(message.text);
            } else
                textViewDescription.setText("");


            if (adapterPosition == selectedItemPos)
                selectedItem(message.path, audio_button_play);
            else
                defaultItem(audio_button_play);


            audio_button_play.setOnClickListener(new DebounceClickListener(v -> {
                selectedItemPos = adapterPosition;
                if (contentPlaying.length() <= 0)
                    selectedItem(message.path, v);
                else
                    defaultItem(v);

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
            context.startService(intent
                    .putExtra(INTENT_PATH_AUDIO, contentPlaying));
        }//eof if
    }//eof selectedItem

}