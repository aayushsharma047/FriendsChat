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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Button login, loginwithphone;
    private TextView forgotpassword, signuptext;
    private EditText email, pwd;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initiateFields();
        firebaseAuth= FirebaseAuth.getInstance();

        signuptext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginuser();
            }
        });
        loginwithphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, PhoneLoginActivity.class ));
            }
        });

    }

    private void loginuser() {
        String mail = email.getText().toString().trim();
        String password= pwd.getText().toString().trim();
        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Fill up all the fields", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Logging In");
            loadingbar.setMessage("Please wait while we log in to your account");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            firebaseAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        sendusertoMainActivity();
                        Toast.makeText(LoginActivity.this, "Welcome to FriendsChat", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                    else
                    {
                        //String error= task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Some Error Occured", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }

    private void initiateFields() {
        login=findViewById(R.id.loginbutton);
        loginwithphone=findViewById(R.id.phonebutton);
        forgotpassword=findViewById(R.id.forgotpwd);
        signuptext=findViewById(R.id.signuplink);
        email=findViewById(R.id.email);
        pwd=findViewById(R.id.password);
        loadingbar=new ProgressDialog(this);
    }

    private void sendusertoMainActivity() {
        Intent i= new Intent(LoginActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(i);
        finish();
    }
}
