package com.example.mychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mychat.Fragments.APIService;
import com.example.mychat.Model.Message;
import com.example.mychat.Model.MessageAdapter;
import com.example.mychat.Model.User;
import com.example.mychat.Notifications.Data;
import com.example.mychat.Notifications.MyResponse;
import com.example.mychat.Notifications.RetrofitClient;
import com.example.mychat.Notifications.Sender;
import com.example.mychat.Notifications.Token;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    private static final int RC_PHOTO_PICKER =  2;

    private CircleImageView profileImage;
    private TextView username;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Button sendButton;
    private ImageButton photoPickerButton;
    private EditText messageEditText;

    private ListView messagesListView;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private String chatId = "";
    String otherUserId;

    private Uri selectedImageUri = null;

    ValueEventListener seenListener;

    APIService apiService;
    boolean notify = false;

    private int lastSeenIndex = -1;
    private int lastUnseenIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        // notification
        apiService = RetrofitClient.getClient("https://fcm.googleapis.com/").create(APIService.class);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        sendButton = findViewById(R.id.sendButton);
        photoPickerButton = findViewById(R.id.photoPickerButton);
        messageEditText = findViewById(R.id.messageEditText);

        Intent intent= getIntent();
        otherUserId = intent.getStringExtra("UserId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId);

        // creating one unique chat id
        if (firebaseUser.getUid().compareTo(otherUserId) > 0) {
            chatId += firebaseUser.getUid();
            chatId += otherUserId;
        } else {
            chatId += otherUserId;
            chatId += firebaseUser.getUid();
        }

        // Enable Send button when there's text to send or selected image
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0 || selectedImageUri != null) {
                    sendButton.setEnabled(true);
                    sendButton.setBackgroundResource(R.drawable.ic_send_btn);
                } else {
                    sendButton.setEnabled(false);
                    sendButton.setBackgroundResource(R.drawable.ic_send_btn_off);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String message = messageEditText.getText().toString().trim();
                Log.v("MessageActivity.java", "message sent to user with id : "+ otherUserId);
                sendMessage(firebaseUser.getUid(), otherUserId, message);
                messageEditText.setText("");
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference().child("ChatsPhotos");
        // to open image picker
        photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageUrl().equals("default")) {
                    profileImage.setImageResource(R.drawable.profile_image);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profileImage);
                }
                readMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messagesListView = findViewById(R.id.messageListView);
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, new ArrayList<Message>());

        seenMessage(otherUserId);

    }

    private void seenMessage(final String userId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message.getSender().equals(userId)) {
                        message.setSeen(true);
                    }
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("seen", message.getSeen());
                    snapshot.getRef().updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, final String message) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        final HashMap<String, Object> map = new HashMap<>();
        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("message", message);
        map.put("seen", false);
        map.put("date", currentDate);
        map.put("time", currentTime);
        if (selectedImageUri != null) {
            final StorageReference photoRef = storageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.v("MessageActivity.class", "Upload a new Image with url : " + uri.toString());
                            String imageUrl = uri.toString();
                            map.put("imageUrl", imageUrl);
                            reference.child("Chats").child(chatId).push().setValue(map);
                        }
                    });
                }
            });
            selectedImageUri = null;
            findViewById(R.id.added_image_container).setVisibility(View.GONE);
        } else {
            map.put("imageUrl", "null");
            reference.child("Chats").child(chatId).push().setValue(map);
        }

        //reference.child("Chats").child(chatId).push().setValue(map);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("ChatList").child(firebaseUser.getUid()).child(receiver);
        // adding user2 as a friend in the chats list (if not exists)
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chat2Ref = FirebaseDatabase.getInstance()
                .getReference("ChatList").child(receiver).child(firebaseUser.getUid());
        // adding user1 as a friend of user2 in the chats list (if not exists)
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chat2Ref.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // to get current username & send notification to other user
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (notify) {
                    //Log.d("Message Activity", "Send Notification !!!!!!!!!!!!!!!!!!!!!!!!!!!!! -----------------------------------");
                    sendNotification(receiver, currentUser.getUsername(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readMessages() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                messageAdapter.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                    if (message.getSender().equals(firebaseUser.getUid()) && message.getSeen()) {
                        lastSeenIndex = messages.size()-1;
                    } else if (message.getSender().equals(firebaseUser.getUid()) && !message.getSeen()){
                        lastUnseenIndex = messages.size()-1;
                    }
                }
                if (messages != null && !messages.isEmpty()) {
                    messageAdapter.setLastSeenIndex(lastSeenIndex);
                    if (messages.size() != lastSeenIndex+1)
                        messageAdapter.setLastUnseenIndex(lastUnseenIndex);
                    else
                        messageAdapter.setLastUnseenIndex(-1);
                    messageAdapter.addAll(messages);
                    Log.v("MessageActivity.java", "Messages adapter updated in onDataChange");
                }
                messagesListView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokensReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokensReference.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_logo,
                            username + " : " + message, "New Message!", otherUserId);
                    Sender sender = new Sender(data, token.getToken());
                    //Log.d("Messaging Activity", "onDataChange SendNotification MessageActivity !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! -----------------------------------");
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void otherUser(String otherUserId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("otherUserId", otherUserId);
        editor.apply();
    }

    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        databaseReference.updateChildren(map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            String imageUrl = selectedImageUri.toString();
            final RelativeLayout container = findViewById(R.id.added_image_container);
            container.setVisibility(View.VISIBLE);
            final ImageView selectedImage = findViewById(R.id.selected_image);
            Picasso.with(this).load(imageUrl).into(selectedImage);

            sendButton.setEnabled(true);
            sendButton.setBackgroundResource(R.drawable.ic_send_btn);

            // if close ,, empty the imageview & layout is gone
            Button close = findViewById(R.id.close_image_button);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedImage.setImageResource(0);
                    container.setVisibility(View.GONE);
                    selectedImageUri = null;
                    if (messageEditText.getText().toString().equals("")) {
                        sendButton.setEnabled(false);
                        sendButton.setBackgroundResource(R.drawable.ic_send_btn_off);
                    }
                }
            });


            /*
            final StorageReference photoRef = storageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.v("MessageActivity.class", "Upload a new Image with url : " + uri.toString());
                            String imageUrl = uri.toString();
                        }
                    });
                }
            });

             */
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        otherUser(otherUserId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenListener);
        status("offline");
        otherUser("none");
    }

}
