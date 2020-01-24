package com.example.mychat.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {

    private ArrayList<Message> messages;
    // required for getting image
//    private Context mContext;
    FirebaseUser user;


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

        TextView message = listItemView.findViewById(R.id.message);
        message.setText(messages.get(position).getMessage());

        ImageView seen = listItemView.findViewById(R.id.message_seen);

        // check if last message
        if (messages.get(position).getSeen()) {
            seen.setImageResource(R.drawable.ic_seen);
        } else {
            seen.setImageResource(R.drawable.ic_not_seen);
        }


        return listItemView;
    }
}
