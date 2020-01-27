package com.example.mychat.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mychat.MessageActivity;
import com.example.mychat.Model.User;
import com.example.mychat.Model.UserAdapter;
import com.example.mychat.Model.UserChatlist;
import com.example.mychat.Notifications.Token;
import com.example.mychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {
    private ListView chatsListView;
    private UserAdapter userAdapter;
    private ArrayList<User> users;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

//    private ArrayList<String> usersIds;
    private ArrayList<UserChatlist> userChatsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        chatsListView = view.findViewById(R.id.chats_list);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        usersIds = new ArrayList<>();
        users = new ArrayList<>();
        userChatsList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), new ArrayList<User>(), true);

        /*
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message.getSender().equals(firebaseUser.getUid())) {
                        usersIds.add(message.getReceiver());
                    }
                    if (message.getReceiver().equals(firebaseUser.getUid())) {
                        usersIds.add(message.getSender());
                    }
                }
                readMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
         */

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userChatsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserChatlist currentChatList = snapshot.getValue(UserChatlist.class);
                    userChatsList.add(currentChatList);
                }

                showChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.v("ChatsFragment.java", "User clicked whose id is " + id);
                Intent intent = new Intent(getContext(), MessageActivity.class);
                intent.putExtra("UserId", users.get(position).getId());
                startActivity(intent);
            }
        });


        // updating token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("MessagingService", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String refreshToken = task.getResult().getToken();
                        Log.v("Chats Fragment", "get refreshed token : " + refreshToken);

                        updateToken(refreshToken);
                    }
                });

        return view;
    }

    private void showChatList() {

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                userAdapter.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (UserChatlist currentChatList : userChatsList) {
                        if (user.getId().equals(currentChatList.getId())) {
                            users.add(user);
                        }
                    }
                }
                if (users != null && !users.isEmpty()) {
                    userAdapter.addAll(users);
                    Log.v("ChatsFragment.java", "Users adapter updated in onDataChange");
                }
                chatsListView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
        Log.v("ChatsFragment.java", "token updated to : " + token);
    }


/*
    private void readMessages() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                userAdapter.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    // check if current user is a friend with mUser
                    for (String id : usersIds) {
                        if (user.getId().equals(id)) {
                            if (users.size() != 0) {
                                //check if that chat hasn't already added
                                for (User user1 : users) {
                                    if (!user.getId().equals(user1.getId())) {
                                        users.add(user);
                                    }
                                }

                            } else {
                                users.add(user);
                            }
                        }
                    }
                }
                if (users != null && !users.isEmpty()) {
                    userAdapter.addAll(users);
                    Log.v("ChatsFragment.java", "Users adapter updated in onDataChange");
                }
                chatsListView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
*/
}
