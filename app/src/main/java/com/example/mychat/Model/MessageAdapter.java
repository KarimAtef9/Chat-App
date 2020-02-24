package com.example.mychat.Model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.mychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends ArrayAdapter<Message> {

    private ArrayList<Message> messages;
    // required for getting image
//    private Context mContext;
    private FirebaseUser user;
    private int lastSeenIndex;
    private int lastUnseenIndex;


    public MessageAdapter(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
//        this.mContext = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView;
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getUid().equals(messages.get(position).getSender())) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.right_message, parent, false);
        } else {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.left_message, parent, false);
        }

        TextView messageTextView = listItemView.findViewById(R.id.message);
        messageTextView.setText(messages.get(position).getMessage());
        // if image only ,, remove text place
        if (messages.get(position).getMessage().equals("")){
            messageTextView.setVisibility(View.GONE);
        }

        ImageView imageView = listItemView.findViewById(R.id.message_image);
        // display image if found
        if (!messages.get(position).getImageUrl().equals("null")) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(imageView.getContext())
                    .load(messages.get(position).getImageUrl())
                    .into(imageView);
        }



        // set time or time and date next to message
        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        TextView time = listItemView.findViewById(R.id.time);
        if (messages.get(position).getDate().equals(currentDate)) {
            // same day
            time.setText(messages.get(position).getTime());
        } else {
            time.setText(messages.get(position).getTime() + " " + messages.get(position).getDate());
        }

        ImageView seen = listItemView.findViewById(R.id.message_seen);

        // check if last message
/**        if (position < messages.size()-1) {
            // next message seen
            if (messages.get(position+1).getSeen()) {
                seen.setImageResource(0);
            } else { // next message not seen
                if (messages.get(position).getSeen()) {
                    seen.setImageResource(R.drawable.ic_seen);
                } else {
                    seen.setImageResource(0);
                }
            }

        } else { // this is the last message
            if (messages.get(position).getSeen()) {
                seen.setImageResource(R.drawable.ic_seen);
            } else {
                seen.setImageResource(R.drawable.ic_not_seen);
            }
        }
**/
        Log.v("MessageAdapter.java", "Position : "+position+" Last Seen : "+lastSeenIndex+" Last Unseen : "+lastUnseenIndex);
        if (position == lastSeenIndex) {
            seen.setImageResource(R.drawable.ic_seen);
        } else if (position == lastUnseenIndex) {
            seen.setImageResource(R.drawable.ic_not_seen);
        } else {
            seen.setImageResource(0);
        }


        return listItemView;
    }

    public void setLastSeenIndex(int lastSeen) {
        this.lastSeenIndex = lastSeen;
    }

    public void setLastUnseenIndex(int lastUnseen) {
        this.lastUnseenIndex = lastUnseen;
    }

}
