package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText name, statusofuser;
    private CircleImageView userimage;
    private Button updatebutton;
    private String currentuserid;
    private ProgressDialog progressDialog;
    private static final int gallerycode=1;
    private StorageReference userProfileImagesRef;

    FirebaseAuth firebaseAuth;
    DatabaseReference rootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar=findViewById(R.id.settingspage_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Profile Settings");

        initializefields();
        firebaseAuth=FirebaseAuth.getInstance();
        currentuserid=firebaseAuth.getCurrentUser().getUid();
        rootref= FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef= FirebaseStorage.getInstance().getReference().child("Profile Images");


        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateusersettings();
            }
        });

        retrieveuserinfo();

        userimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent= new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallerycode);
            }
        });

    }

    private void initializefields() {
        name=findViewById(R.id.username);
        statusofuser=findViewById(R.id.userstatus);
        userimage=findViewById(R.id.profileImageView);
        updatebutton=findViewById(R.id.update);
        progressDialog=new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==gallerycode && resultCode==RESULT_OK && data!=null)
        {
            Uri imageuri= data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                progressDialog.setTitle("Image Upload");
                progressDialog.setMessage("Please wait while we upload your profile image.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final Uri resultUri = result.getUri();
                final StorageReference filePath = userProfileImagesRef.child(currentuserid+".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                rootref.child("Users").child(currentuserid).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingsActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                }
        });
    }}}

    private void updateusersettings()
        {

        String username= name.getText().toString();
        String status= statusofuser.getText().toString();
        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Kindly enter your username", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Update your status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, String> profilemap= new HashMap<>();
            profilemap.put("uid", currentuserid);
            profilemap.put("name", username);
            profilemap.put("status", status);
            rootref.child("Users").child(currentuserid).setValue(profilemap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        sendusertoMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(SettingsActivity.this, "An error occured, kindly retry", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void sendusertoMainActivity() {
        Intent i= new Intent(SettingsActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(i);
        finish();
    }


    private void retrieveuserinfo() {
        rootref.child("Users").child(currentuserid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("image") && dataSnapshot.hasChild("name")
                                && dataSnapshot.hasChild("status"))
                        {
                            String retrieveusername= dataSnapshot.child("name").getValue().toString();
                            String retrieveuserstatus= dataSnapshot.child("status").getValue().toString();
                            String retrieveuserimage= dataSnapshot.child("image").getValue().toString();

                            name.setText(retrieveusername);
                            statusofuser.setText(retrieveuserstatus);
                            Picasso.get().load(retrieveuserimage).into(userimage);
                        }
                        else if(dataSnapshot.exists() && dataSnapshot.hasChild("name")
                                && dataSnapshot.hasChild("status"))
                        {
                            String retrieveusername= dataSnapshot.child("name").getValue().toString();
                            String retrieveuserstatus= dataSnapshot.child("status").getValue().toString();

                            name.setText(retrieveusername);
                            statusofuser.setText(retrieveuserstatus);
                        }
                        else if (dataSnapshot.exists() && dataSnapshot.hasChild("name"))
                        {
                            String retrieveusername= dataSnapshot.child("name").getValue().toString();
                            name.setText(retrieveusername);
                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "Kindly update your profile ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
