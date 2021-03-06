package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {


    private String receive_user_id, sender_user_id, current_state;

    private CircleImageView userprofileimage;
    private TextView userprofilename, userprofilestatus;
    private Button sendChatRequestButton, declineChatRequestButton;
    private DatabaseReference usersRef, chatreqRef, contactsref;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatreqRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsref= FirebaseDatabase.getInstance().getReference().child("Contacts");

        receive_user_id= getIntent().getExtras().get("visit_user_id").toString();
        sender_user_id=mAuth.getCurrentUser().getUid();
        //Toast.makeText(this, "User ID: "+receive_user_id, Toast.LENGTH_SHORT).show();

        userprofileimage=findViewById(R.id.user_profile_photo);
        userprofilename=findViewById(R.id.profile_name);
        userprofilestatus=findViewById(R.id.profile_status);
        sendChatRequestButton=findViewById(R.id.sendmessagebtn);
        declineChatRequestButton=findViewById(R.id.declinechatbtn);
        current_state="new";

        Retrieveuserinfo();
    }

    private void Retrieveuserinfo() {

        usersRef.child(receive_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image"))
                {
                    String userimage= dataSnapshot.child("image").getValue().toString();
                    String username= dataSnapshot.child("name").getValue().toString();
                    String userstatus= dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(userprofileimage);
                    userprofilename.setText(username);
                    userprofilestatus.setText(userstatus);

                    ManageChatRequest();
                }
                else
                {
                    String username= dataSnapshot.child("name").getValue().toString();
                    String userstatus= dataSnapshot.child("status").getValue().toString();

                    userprofilename.setText(username);
                    userprofilestatus.setText(userstatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ManageChatRequest()
    {
        chatreqRef.child(sender_user_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receive_user_id))
                        {
                            String requesttype = dataSnapshot.child(receive_user_id).child("request_type").getValue().toString();
                            if (requesttype.equals("sent"))
                            {
                                current_state="request_sent";
                                sendChatRequestButton.setText("Cancel Request");
                            }
                            else if (requesttype.equals("received"))
                            {
                                current_state="request_received";
                                sendChatRequestButton.setText("Accept Chat Request");

                                declineChatRequestButton.setVisibility(View.VISIBLE);
                                declineChatRequestButton.setEnabled(true);
                                declineChatRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactsref.child(sender_user_id)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receive_user_id))
                                            {
                                                current_state="friends";
                                                sendChatRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        if (!sender_user_id.equals(receive_user_id))
        {
            sendChatRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    sendChatRequestButton.setEnabled(false);
                    if (current_state.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(current_state.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (current_state.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (current_state.equals("friends"))
                    {
                        removeContact();
                    }

                }
            });
        }
        else
        {
            sendChatRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void removeContact()
    {
        contactsref.child(sender_user_id).child(receive_user_id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    contactsref.child(receive_user_id).child(sender_user_id)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                sendChatRequestButton.setEnabled(true);
                                current_state="new";
                                sendChatRequestButton.setText("Send Chat Request");

                                declineChatRequestButton.setVisibility(View.INVISIBLE);
                                declineChatRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest()
    {
        contactsref.child(sender_user_id).child(receive_user_id)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsref.child(receive_user_id).child(sender_user_id)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                chatreqRef.child(sender_user_id).child(receive_user_id)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                chatreqRef.child(receive_user_id).child(sender_user_id)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                sendChatRequestButton.setEnabled(true);
                                                                                current_state="friends";
                                                                                sendChatRequestButton.setText("Remove Contact");

                                                                                declineChatRequestButton.setVisibility(View.INVISIBLE);
                                                                                declineChatRequestButton.setEnabled(false);
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelChatRequest()
    {
        chatreqRef.child(sender_user_id).child(receive_user_id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    chatreqRef.child(receive_user_id).child(sender_user_id)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                sendChatRequestButton.setEnabled(true);
                                current_state="new";
                                sendChatRequestButton.setText("Send Chat Request");

                                declineChatRequestButton.setVisibility(View.INVISIBLE);
                                declineChatRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest()
    {
        chatreqRef.child(sender_user_id).child(receive_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatreqRef.child(receive_user_id).child(sender_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendChatRequestButton.setEnabled(true);
                                                current_state="request_sent";
                                                sendChatRequestButton.setText("Cancel Request");
                                            }
                                        }
                                    });
                        }

                    }
                });
    }
}
