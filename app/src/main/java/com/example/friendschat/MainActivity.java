package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessAdapter tabsAccessAdapter;
    FirebaseAuth firebaseAuth;
    DatabaseReference rootref;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        firebaseAuth=FirebaseAuth.getInstance();
        rootref=FirebaseDatabase.getInstance().getReference();

        //Main Activity Toolbar
        setContentView(R.layout.activity_main);
        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends Chat");

        viewPager=findViewById(R.id.main_tabs_pager);
        tabsAccessAdapter=new TabsAccessAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessAdapter);

        tabLayout=findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if (firebaseUser==null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("Online");
            checkuserexistence();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if (firebaseUser!=null)
        {
            updateUserStatus("Offline");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if (firebaseUser!=null)
        {
            updateUserStatus("Offline");
        }

    }

    private void checkuserexistence() {
        String currentuserID= firebaseAuth.getCurrentUser().getUid();
        rootref.child("Users").child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists())
                {
                   // Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.logout_option)
        {
            updateUserStatus("Offline");
            firebaseAuth.signOut();
            sendUserToLoginActivity();
        }
        else if (item.getItemId()==R.id.settings_option)
        {
            sendUserToSettingsActivity();
        }
        else if (item.getItemId()==R.id.create_group_option)
        {
            requestnewgroup();
        }
        else if (item.getItemId()==R.id.find_friends_option)
        {
            sendUserToFindFriendsActivity();
        }
        else if (item.getItemId()==R.id.friends_request_option)
        {
            sendUserToRequestsActivity();
        }

        return true;
    }

    private void sendUserToRequestsActivity() {
        Intent i= new Intent(MainActivity.this, RequestsActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        //finish();
    }

    private void sendUserToSettingsActivity() {
        Intent i= new Intent(MainActivity.this, SettingsActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        //finish();
    }

    private void sendUserToFindFriendsActivity() {
        Intent i= new Intent(MainActivity.this, FindFriendsActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        //finish();
    }

    private void sendUserToLoginActivity() {
        Intent i= new Intent(MainActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void requestnewgroup() {
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter Group Name");
        final EditText groupnameField= new EditText(MainActivity.this);
        groupnameField.setHint("e.g. Friends Forever");
        builder.setView(groupnameField);

        builder.setPositiveButton("Create Group", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupname= groupnameField.getText().toString();
                if (TextUtils.isEmpty(groupname))
                {
                    Toast.makeText(MainActivity.this, "Enter a group name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    createGroup(groupname);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void createGroup(final String groupname) {
        rootref.child("Groups").child(groupname).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, groupname+ " is created successfully !", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Some error occured, kindly retry !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentdate.format(calendar.getTime());

        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currenttime.format(calendar.getTime());

        HashMap<String,Object> onlinestate = new HashMap<>();

        onlinestate.put("time",saveCurrentTime);
        onlinestate.put("date",saveCurrentDate);
        onlinestate.put("state",state);

        currentUserId=firebaseAuth.getCurrentUser().getUid();
        rootref.child("Users").child(currentUserId).child("userState")
                .updateChildren(onlinestate);
    }
}
