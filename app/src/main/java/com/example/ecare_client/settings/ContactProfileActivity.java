/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecare_client.BaseActivity;
import com.example.ecare_client.ChatActivity;
import com.example.ecare_client.R;
import com.example.ecare_client.SinchService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sinch.android.rtc.SinchError;

import java.io.File;
import java.io.IOException;

import com.subinkrishna.widget.CircularImageView;

public class ContactProfileActivity extends BaseActivity implements SinchService.StartFailedListener {

    private CircularImageView contactPicture;

    private TextView contactEmail;
    private TextView contactNickname;
    private Button messageButton;
    private Button updateNicknameButton;
    private TextView isCarerText;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private StorageReference mStorageRef;

    private ProgressDialog mSpinner;

    public boolean isChatReady;



    private String selectedContactName;
    private String selectedContactKey;
    private String selectedContactNickname;
    private Boolean selectedContactIsCarer;

    private ValueEventListener onlineListener;



    protected void onCreate(Bundle savedInstanceState) {

        isChatReady = true;

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();


        super.onCreate(savedInstanceState);

        selectedContactName = getIntent().getStringExtra("ContactName");
        selectedContactKey = getIntent().getStringExtra("ContactKey");
        selectedContactNickname = getIntent().getStringExtra("ContactNickname");

        DatabaseReference contactIDRef =
                database.getReference().child("Users").child(selectedContactKey);

        setContentView(R.layout.activity_contact_profile);

        contactPicture = (CircularImageView) findViewById(R.id.contact_picture);
        contactEmail = (TextView) findViewById(R.id.contact_email);
        contactNickname = (TextView) findViewById(R.id.contact_nickname);
        messageButton = (Button) findViewById(R.id.message_button);
        updateNicknameButton = (Button) findViewById(R.id.update_nickname);
        isCarerText = (TextView) findViewById(R.id.is_carer_text);

        contactEmail.setText(selectedContactName);

        contactNickname.setText(selectedContactNickname);

        getProfilePicture();

        DatabaseReference contactIsCarer =
                database.getReference().child("Users")
                        .child(selectedContactKey).child("Info").child("isCarer");


        Query isCarerQuery = contactIsCarer;

        isCarerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Boolean isCarer = Boolean.parseBoolean(dataSnapshot.getValue(String.class));
                    if (isCarer) {
                        isCarerText.setText("This contact is a carer.");
                    }

                    else {
                        // Just set to nothing.
                        isCarerText.setText("");

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





        updateNicknameButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                String newText = contactNickname.getText().toString().trim();

                String userID = auth.getCurrentUser().getUid();

                
                database.getReference().child("Users").child(userID).
                        child("Contacts").child(selectedContactKey).setValue(newText);

                Toast.makeText(getApplicationContext(),
                        "Nickname updated.",
                        Toast.LENGTH_SHORT).show();

                // FINISH THIS


            }

        });


        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                beginChat(selectedContactName);


            }
        });


        onlineListener = new ValueEventListener() {
            @Override
            // THE DATA SNAPSHOT IS AT THE CHILD!! NOT THE ROOT NODE!!!!
            public void onDataChange(DataSnapshot dataSnapshot) {

                Boolean contactOnline = false;

                for(DataSnapshot child : dataSnapshot.getChildren()) {

                    if (child.getKey().equals("Online")) {
                        contactOnline = Boolean.parseBoolean(
                                child.getValue(String.class));
                    }

                }

                messageButton.setEnabled(contactOnline);
                messageButton.setText(contactOnline ? "Message" : "Offline");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing.
            }
        };

        contactIDRef.addValueEventListener(onlineListener);

    }


    private void getProfilePicture() {

        DatabaseReference contactProfilePicture =
                database.getReference().child("Users")
                        .child(selectedContactKey).child("Info").child("picPath");


        Query queryNew = contactProfilePicture;


        queryNew.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())  {

                    String profilePicturePath = dataSnapshot.getValue(String.class);
                    StorageReference imageRef = mStorageRef.child(profilePicturePath);

                    try {
                        final File imageFile = File.createTempFile("images", "jpg");

                        imageRef.getFile(imageFile)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Successfully downloaded data to local file
                                        Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                                        contactPicture.setImageBitmap(myBitmap);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle failed download
                                // Do nothing
                            }
                        });

                    }

                    catch (IOException e) {
                        // Do nothing
                    }



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isChatReady = false;

    }

    public void beginChat(String contactName) {

        selectedContactName = contactName;

        auth = FirebaseAuth.getInstance();
        String email = auth.getCurrentUser().getEmail();

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(email);
            showSpinner();

        } else {

            Intent chatActivity = new Intent(this, ChatActivity.class);

            Bundle options = new Bundle();
            options.putString("ContactName", contactName);

            chatActivity.putExtras(options);

            startActivity(chatActivity);
        }

    }

    private void showSpinner() {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Connecting");
        mSpinner.setMessage("Please wait...");
        mSpinner.show();
    }




    @Override
    protected void onPause() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        super.onPause();
    }


    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
    }

    @Override
    public void onStarted() {

        if (isChatReady) {
            Intent chatActivity = new Intent(this, ChatActivity.class);

            Bundle options = new Bundle();
            options.putString("ContactName", selectedContactName);

            chatActivity.putExtras(options);

            startActivity(chatActivity);
        }

    }

    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
    }

}
