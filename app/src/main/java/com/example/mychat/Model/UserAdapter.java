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

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User> {
    private ArrayList<User> users;
    // required for getting image
    private Context mContext;
    boolean isChat;
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
            lastMessage(currentUser, last_message, username);

        } else {
            status = listItemView.findViewById(R.id.img_online);
            status.setVisibility(View.GONE);
            status = listItemView.findViewById(R.id.img_offline);
            status.setVisibility(View.GONE);
            last_message.setVisibility(View.GONE);
        }

        return listItemView;
    }

    public void lastMessage(User lastUser, final TextView last_message, final TextView username) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String chatId = "";
        if (currentUser.getUid().compareTo(lastUser.getId()) > 0) {
            chatId += currentUser.getUid();
            chatId += lastUser.getId();
        } else {
            chatId += lastUser.getId();
            chatId += currentUser.getUid();
        }
        final DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Message message = data.getValue(Message.class);
                    lastMessage = message;
                }
                last_message.setText(lastMessage.getMessage());
                if (lastMessage.getReceiver().equals(currentUser.getUid()) && !lastMessage.getSeen()) {
                    last_message.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                    username.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                } else {
                    last_message.setTextColor(getContext().getResources().
                            getColor(R.color.common_google_signin_btn_text_light));
                    username.setTextColor(getContext().getResources().
                            getColor(R.color.common_google_signin_btn_text_light));
                }
                last_message.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
