package com.example.mychat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.mychat.Fragments.ChatsFragment;
import com.example.mychat.Fragments.UsersFragment;
import com.example.mychat.Model.Message;
import com.example.mychat.Model.User;
import com.example.mychat.Model.UserChatlist;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    private int unopenedChats = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);


        // to count number of friends with unread messages
        // first enter friends list of current user
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserChatlist currentChat = snapshot.getValue(UserChatlist.class);
                    String uniqueChatId = "";
                    // creating one unique chat id
                    if (firebaseUser.getUid().compareTo(currentChat.getId()) > 0) {
                        uniqueChatId += firebaseUser.getUid();
                        uniqueChatId += currentChat.getId();
                    } else {
                        uniqueChatId += currentChat.getId();
                        uniqueChatId += firebaseUser.getUid();
                    }

                    // second search in messages of each friend for unseen messages
                    // search in current user chat in chat messages
                    DatabaseReference messagesReference = FirebaseDatabase.getInstance().getReference("Chats").child(uniqueChatId);
                    messagesReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                                Message message = snapshot1.getValue(Message.class);
                                if (message.getReceiver().equals(firebaseUser.getUid()) && !message.getSeen()) {
                                    unopenedChats++;
//                                    Log.d("MainActivity", "check el message : " + unopenedChats);
                                    break;
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });


                }
                Log.d("MainActivity", "check el message : " + unopenedChats);
                if (unopenedChats == 0) {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                } else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "("+unopenedChats+") Chats");
                }
                viewPagerAdapter.addFragment(new UsersFragment(), "Users");
                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });







    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                startActivity(intent);
                return true;
            case R.id.profile:
                intent = new Intent(MainActivity.this, ProfileActivity.class).
                        setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return false;
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        databaseReference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
