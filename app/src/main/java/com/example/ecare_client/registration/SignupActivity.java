/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client.registration;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ecare_client.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.ecare_client.R;
import com.google.firebase.auth.FirebaseUser;

import com.example.ecare_client.MainActivity;

public class SignupActivity extends BaseActivity {

    private EditText inputEmail, inputPassword;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private LinearLayout btnGoogleSignIn;
    private LinearLayout btnFacebookSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseApp.initializeApp(this);

        //asking for permissions here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE},100);
        }

        //Get Firebase auth instance
        // Remember that this is a singleton class.
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnGoogleSignIn = (LinearLayout) findViewById(R.id.google_sign_in_btn);
        btnFacebookSignIn = (LinearLayout) findViewById(R.id.facebook_sign_in_btn);

        //btnResetPassword  = (Button) findViewById(R.id.btn_reset_password);

        /*
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, ResetPasswordActivity.class);


                startActivity(intent);
            }
        });
        */

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // PERHAPS REFACTOR THIS!!!!
                final String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    setOnline(true);
                                    loginClicked(email);

                                    Intent intent = new Intent(SignupActivity.this,
                                            //-------------------------------
                                            MainActivity.class);
                                    //-------------------------------

                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(SignupActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {

                                    createNewUser();

                                    setOnline(true);
                                    loginClicked(email);

                                    startActivity(new Intent(SignupActivity.this,
                                            //-------------------------------
                                            MainActivity.class));
                                    //-------------------------------
                                    //finish();
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onResume() {
        setOnline(false);
        Log.d("ERROR", "set online false OnResume");
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        setOnline(false);
        Log.d("ERROR", "set online false OnDestroy");
        super.onDestroy();
    }

    protected void createNewUser() {
        FirebaseUser user = auth.getCurrentUser();


        DatabaseReference userRef =
                database.
                        getReference().
                        child("Users").
                        child(user.getUid());

        userRef.child("Contacts").child("Null").setValue("Null");
        userRef.child("Info").child("Null").setValue("Null");
        userRef.child("Email").setValue(user.getEmail());

        userRef.child("Online").setValue("true");
        Log.d("SUCCESS", "set online true");
    }

    protected void setOnline(boolean value) {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {

            return;
        }

        DatabaseReference userRef =
                database.
                        getReference().
                        child("Users").
                        child(user.getUid());

        if (value) {

            userRef.child("Online").setValue("true");
        }

        else {

            userRef.child("Online").setValue("false");

        }
    }

    private void loginClicked(String userName) {
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(userName);
            //showSpinner();
        }
        //openContactListActivity();
    }
}