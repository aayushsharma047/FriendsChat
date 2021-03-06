package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ImageButton sendmessagebutton;
    private EditText usermsginput;
    private ScrollView scrollView;
    private TextView displayTextMessages;
    private FirebaseAuth mAuth;
    private DatabaseReference userReference, groupnameRef, groupmessagekeyref;
    private String currentgroup, currentUserId, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentgroup=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, currentgroup, Toast.LENGTH_SHORT).show();

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        userReference= FirebaseDatabase.getInstance().getReference().child("Users");
        groupnameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentgroup);

        initializeFields();
        getUserInfo();
        sendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                savemessagetodatabase();
                usermsginput.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupnameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists())
                {
                    Displaymessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists())
                {
                    Displaymessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initializeFields() {

        mtoolbar=findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentgroup);

        sendmessagebutton=findViewById(R.id.send_message_button);
        usermsginput=findViewById(R.id.messagetext);
        scrollView=findViewById(R.id.messagescrollview);
        displayTextMessages=findViewById(R.id.group_chat_text_display);

    }

    private void getUserInfo() {
        userReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    currentUserName= dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void savemessagetodatabase() {
        String message= usermsginput.getText().toString();
        String messageKey= groupnameRef.push().getKey();
        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Write your message first ...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calfordate= Calendar.getInstance();
            SimpleDateFormat currentdateformat= new SimpleDateFormat("MMM dd, yyyy");
            currentDate= currentdateformat.format(calfordate.getTime());

            Calendar calfortime= Calendar.getInstance();
            SimpleDateFormat currenttimeformat= new SimpleDateFormat("hh:mm:ss a");
            currentTime= currenttimeformat.format(calfortime.getTime());

            HashMap<String, Object> groupmessagekey= new HashMap<>();
            groupnameRef.updateChildren(groupmessagekey);

            groupmessagekeyref= groupnameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name:", currentUserName);
            messageInfoMap.put("message:", message);
            messageInfoMap.put("date:", currentDate);
            messageInfoMap.put("time:", currentTime);
            groupmessagekeyref.updateChildren(messageInfoMap);

        }
    }

    private void Displaymessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext())
        {
            String chatdate= (String)((DataSnapshot)iterator.next()).getValue();
            String chatmessage= (String)((DataSnapshot)iterator.next()).getValue();
            String chatname= (String)((DataSnapshot)iterator.next()).getValue();
            String chattime= (String)((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatname+ "\n"+chatmessage+"\n"+chattime+"    "+chatdate+"\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
