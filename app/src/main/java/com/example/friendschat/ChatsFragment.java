package com.example.friendschat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privatechatsview;
    private RecyclerView chatlist;
    private DatabaseReference chatsref, usersref;
    private FirebaseAuth mAuth;
    private String currentuserid;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth=FirebaseAuth.getInstance();
        currentuserid=mAuth.getCurrentUser().getUid();
        chatsref= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);
        usersref=FirebaseDatabase.getInstance().getReference().child("Users");
        privatechatsview = inflater.inflate(R.layout.fragment_chats, container, false);
        chatlist=privatechatsview.findViewById(R.id.chat_list);
        chatlist.setLayoutManager(new LinearLayoutManager(getContext()));
        return privatechatsview;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsref, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,chatsviewholder> adapter
                = new FirebaseRecyclerAdapter<Contacts, chatsviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatsviewholder chatsviewholder, int i, @NonNull Contacts contacts)
            {
                final String userids = getRef(i).getKey();
                final String[] usrimage = {"default_image"};
                usersref.child(userids).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild("image"))
                            {
                                usrimage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(usrimage[0]).into(chatsviewholder.profileimage);
                            }

                            final String usrname = dataSnapshot.child("name").getValue().toString();
                            final String usrstatus = dataSnapshot.child("status").getValue().toString();

                            chatsviewholder.username.setText(usrname);

                            if (dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("Online"))
                                {
                                    chatsviewholder.userstatus.setText("Online");
                                }
                                else if (state.equals("Offline"))
                                {
                                    chatsviewholder.userstatus.setText("Last Seen: "+date+" "+time);
                                }
                            }
                            else
                            {
                                chatsviewholder.userstatus.setText("Offline");
                            }


                            chatsviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("Visit_UID",userids);
                                    chatIntent.putExtra("Visit_UIMAGE", usrimage[0]);
                                    chatIntent.putExtra("Visit_UNAME",usrname);
                                    startActivity(chatIntent);

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public chatsviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new chatsviewholder(view);
            }
        };
        chatlist.setAdapter(adapter);
        adapter.startListening();
    }

    public static class chatsviewholder extends RecyclerView.ViewHolder
    {
        CircleImageView profileimage;
        TextView username, userstatus;

        public chatsviewholder(@NonNull View itemView)
        {
            super(itemView);
            profileimage= itemView.findViewById(R.id.users_profile_image);
            username= itemView.findViewById(R.id.user_profile_name);
            userstatus= itemView.findViewById(R.id.user_profile_status);
        }
    }
}
