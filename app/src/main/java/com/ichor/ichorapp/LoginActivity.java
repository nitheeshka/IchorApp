
package com.ichor.ichorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText mPhoneNumberET;
    private Button mSignInBtn;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean otpsent;
    private String phoneNumber;
    private boolean cancel;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthCredential credential;
    private DatabaseReference mRef;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPhoneNumberET=findViewById(R.id.phoneNumberET);
        mSignInBtn=findViewById(R.id.signInBtn);
        sharedPreferences=getSharedPreferences(Utils.pref, MODE_PRIVATE);

        phoneNumber=mPhoneNumberET.getText().toString();

        mAuth=FirebaseAuth.getInstance();
        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                phoneNumber=mPhoneNumberET.getText().toString();
                attemptLogin();
            }
        });

    }
    private void attemptLogin()
    {
        cancel=false;
        if(phoneNumber.isEmpty())
        {
            mPhoneNumberET.setError(getString(R.string.invalid_phone_number));
            cancel=true;
        }
        if(phoneNumber.length()!=10)
        {
            mPhoneNumberET.setError(getString(R.string.invalid_phone_number));
            
        }
        if(cancel)
        {
           mPhoneNumberET.requestFocus();
        }
        else
        {
                callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                        LoginActivity.this.credential=phoneAuthCredential;
                        Log.v("Login", credential.toString());
                        otpsent=true;
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                    }
                };
                PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, callbacks);

        }

    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Log.v("Login", "signInWithPhoneAuthCreds:Success");
                    final FirebaseUser user=task.getResult().getUser();
                    mRef= FirebaseDatabase.getInstance().getReference();
                    mRef.child("Phone Reference").child(user.getPhoneNumber()).setValue(user.getUid(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            editor=sharedPreferences.edit();
                            editor.putString("phone", user.getPhoneNumber());
                            editor.putString("uid", user.getUid());
                            editor.apply();
                            startActivity(new Intent(LoginActivity.this, Dummy.class));
                            
                        }
                    });
                }
                else
                {
                    Log.w(">>", "signInWithCredential:failure", task.getException());
                }
            }
        });
    }
}
