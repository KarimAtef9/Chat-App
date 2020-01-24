package com.example.mychat.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mychat.MessageActivity;
import com.example.mychat.Model.User;
import com.example.mychat.Model.UserAdapter;
import com.example.mychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class UsersFragment extends Fragment {
    private ListView usersListView;
    private UserAdapter userAdapter;
    private ArrayList<User> users;
    EditText search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        usersListView = view.findViewById(R.id.users_list);
        search = view.findViewById(R.id.search);

        users = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), new ArrayList<User>(), false);
//        readUsers();

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.v("UsersFragment.java", "User clicked whose id is " + id);
                Intent intent = new Intent(getContext(), MessageActivity.class);
                intent.putExtra("UserId", users.get(position).getId());
                startActivity(intent);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals("")) {
                    userAdapter.clear();
                    usersListView.setAdapter(userAdapter);
                } else {
                    searchUsers(charSequence.toString().toLowerCase().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                userAdapter.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (! user.getId().equals(firebaseUser.getUid())) {
                        users.add(user);
                    }
                }

                if (users != null && !users.isEmpty()) {
                    userAdapter.addAll(users);
                    Log.v("UsersFragment.java", "Users adapter updated in onDataChange");
                }
                usersListView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void searchUsers(String username) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").
                orderByChild("searchName").startAt(username).endAt(username + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    users.clear();
                    userAdapter.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (! user.getId().equals(currentUser.getUid())) {
                            users.add(user);
                        }
                    }

                    if (users != null && !users.isEmpty()) {
                        userAdapter.addAll(users);
                        Log.v("UsersFragment.java", "Users adapter updated in onDataChange in Search");
                    }
                    usersListView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
