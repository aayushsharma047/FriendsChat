package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private Button signup;
    private EditText newemail, newpwd, cnfpwd;
    private TextView logintext;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();

        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        logintext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });
    }

    private void createUser() {
        String email= newemail.getText().toString().trim();
        String password= newpwd.getText().toString().trim();
        String confirm=cnfpwd.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm))
        {
            Toast.makeText(this, "Fill up all the fields", Toast.LENGTH_SHORT).show();
        }
        else if (!password.matches(confirm))
        {
            Toast.makeText(this, "Password fields does not match", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Creating new Account");
            loadingbar.setMessage("Please wait while we create your account");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        String currentuserId= firebaseAuth.getCurrentUser().getUid();
                        databaseReference.child("Users").child(currentuserId).setValue("");

                        sendusertoMainActivity();
                        Toast.makeText(RegisterActivity.this, "New user registered successfully", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                    else
                    {
                        String error= task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error Occured :"+error, Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }

    private void sendusertoMainActivity() {
        Intent i= new Intent(RegisterActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(i);
        finish();
    }

    private void initializeFields() {
        signup=findViewById(R.id.signupbutton);
        newemail=findViewById(R.id.newemail);
        newpwd=findViewById(R.id.newpassword);
        cnfpwd=findViewById(R.id.confirmpassword);
        logintext=findViewById(R.id.loginlink);
        loadingbar=new ProgressDialog(this);
    }
}
