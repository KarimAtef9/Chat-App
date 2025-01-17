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

import com.bumptech.glide.Glide;
import com.example.mychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserAdapter extends ArrayAdapter<User> {
    private ArrayList<User> users;
    // required for getting image
    private Context mContext;
    private boolean isChat;
    private Message lastMessage;

    public UserAdapter(Context context, ArrayList<User> userList, boolean isChat) {
        super(context, 0, userList);
        this.mContext = context;
        this.users = userList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.user_item, parent, false);
        }

        TextView username = listItemView.findViewById(R.id.username);
        ImageView profileImage = listItemView.findViewById(R.id.profile_image);
        ImageView status;
        TextView last_message = listItemView.findViewById(R.id.last_message);
        TextView unread_messages = listItemView.findViewById(R.id.unread_number);
        TextView time = listItemView.findViewById(R.id.time);
        ImageView seen = listItemView.findViewById(R.id.seen);

        User currentUser = users.get(position);
        username.setText(currentUser.getUsername());
        if (currentUser.getImageUrl().equals("default")) {
            profileImage.setImageResource(R.drawable.profile_image);
        } else {
            Glide.with(mContext).load(currentUser.getImageUrl()).into(profileImage);
        }

        if (isChat) {
            if (currentUser.getStatus().equals("online")) {
                status = listItemView.findViewById(R.id.img_online);
                status.setVisibility(View.VISIBLE);
                status = listItemView.findViewById(R.id.img_offline);
                status.setVisibility(View.GONE);
            } else {
                status = listItemView.findViewById(R.id.img_online);
                status.setVisibility(View.GONE);
                status = listItemView.findViewById(R.id.img_offline);
                status.setVisibility(View.VISIBLE);
            }
            lastMessage(currentUser, last_message, username, unread_messages, time, seen);
        } else {
            status = listItemView.findViewById(R.id.img_online);
            status.setVisibility(View.GONE);
            status = listItemView.findViewById(R.id.img_offline);
            status.setVisibility(View.GONE);
            last_message.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            seen.setVisibility(View.GONE);
        }

        return listItemView;
    }

    // displaying last message & number of unread messages
    private void lastMessage(User lastUser, final TextView last_message, final TextView username,
                             final TextView unread_messages, final TextView timeTextView, final ImageView seenView) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null) return;
        String chatId = "";
        if (currentUser.getUid().compareTo(lastUser.getId()) > 0) {
            chatId += currentUser.getUid();
            chatId += lastUser.getId();
        } else {
            chatId += lastUser.getId();
            chatId += currentUser.getUid();
        }

        // getting last message from the chat and its time
        final DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Message message = data.getValue(Message.class);
                    lastMessage = message;
                    String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
                    String date = message.getDate();

                    if (date.equals(currentDate)) {
                        // message today
                        timeTextView.setText(message.getTime());
                    } else {
                        // message not today ,, give date
                        timeTextView.setText(message.getTime() + " " + date);
                    }

                    if (message.getReceiver().equals(currentUser.getUid()) && !lastMessage.getSeen()) {
                        unread++;
                    }
                }
                last_message.setText(lastMessage.getMessage());
                if (lastMessage.getMessage().equals("")) {
                    last_message.setText("Image");
                }
                if (lastMessage.getReceiver().equals(currentUser.getUid()) && !lastMessage.getSeen()) {
                    last_message.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                    username.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                    timeTextView.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));

                } else {
                    last_message.setTextColor(getContext().getResources().
                            getColor(R.color.common_google_signin_btn_text_light));
                    username.setTextColor(getContext().getResources().
                            getColor(R.color.common_google_signin_btn_text_light));
                }

                if (lastMessage.getSender().equals(currentUser.getUid()) && !lastMessage.getSeen()) {
                    seenView.setImageResource(R.drawable.ic_not_seen);
                } else if (lastMessage.getSender().equals(currentUser.getUid()) && lastMessage.getSeen()){
                    seenView.setImageResource(R.drawable.ic_seen);
                } else {
                    seenView.setVisibility(View.GONE);
                }
                last_message.setVisibility(View.VISIBLE);

                if (unread != 0) {
                    unread_messages.setText(String.valueOf(unread));
                    unread_messages.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
