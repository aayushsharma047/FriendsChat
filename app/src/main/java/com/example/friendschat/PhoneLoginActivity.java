package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private EditText phonenumber, verificationcode;
    private Button sendcode, verifycode;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        mtoolbar=findViewById(R.id.phoneverifypage_appbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Phone Login");

        initialiseFields();

        sendcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber= phonenumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Phone number is required..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait while we authenticate your phone.");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifycode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String vericode= verificationcode.getText().toString();
                if (TextUtils.isEmpty(vericode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter the code first ...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait while we verify the code.");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, vericode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid, Please Enter correct phone number with country code", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent successfully.", Toast.LENGTH_SHORT).show();
            }
        };

    }

    private void initialiseFields()
    {
        mAuth=FirebaseAuth.getInstance();
        phonenumber= findViewById(R.id.phonenumber);
        verificationcode=findViewById(R.id.verificationcode);
        sendcode=findViewById(R.id.sendcodebutton);
        verifycode=findViewById(R.id.verifycodebutton);
        loadingBar=new ProgressDialog(this);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations, you are logged in successfully ...", Toast.LENGTH_SHORT).show();
                            sendusertoMainActivity();
                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(PhoneLoginActivity.this, "An Error Occured: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendusertoMainActivity() {
        startActivity(new Intent(PhoneLoginActivity.this, MainActivity.class));
        finish();
    }

}
