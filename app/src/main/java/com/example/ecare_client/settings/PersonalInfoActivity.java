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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.bumptech.glide.request.target.Target;
import com.example.ecare_client.R;
import com.example.ecare_client.TitleLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.subinkrishna.widget.CircularImageView;

import java.io.IOException;
import java.util.UUID;

/**
 * display, save and update user information
 */

public class PersonalInfoActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Button btnSaveInfo;
    private TextInputEditText inputPhone,inputName, inputCarer, inputCarerPhone;
    private CheckBox inputIsCarer;
    private Button btnChoose;
    private CircularImageView imageView;

    private Uri filePath;
    private String picPath;

    private final int PICK_IMAGE_REQUEST = 71;
    //grab input from user_info page
    private  UserInfo getUserForm(String pic){
        String isCarer = "false";
        if(inputIsCarer.isChecked())
        {
            isCarer = "true";
        }
        return new UserInfo(inputPhone.getText().toString().trim(),
                inputName.getText().toString().trim(),
                pic,
                inputCarer.getText().toString().trim(),
                isCarer,
                inputCarerPhone.getText().toString().trim());


    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    // get image and load into imageview
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    // upload image into firebase storage
    private String uploadImage() {
        String path = picPath;
        if(filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            path = "images/"+ UUID.randomUUID().toString();
            StorageReference ref = storageReference.child(path);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(PersonalInfoActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(PersonalInfoActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }else{
            Toast.makeText(getApplicationContext(), "No new picture file",
                    Toast.LENGTH_LONG).show();
        }
        return path;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);
        TitleLayout titleLayout = (TitleLayout) findViewById(R.id.personalinfo_title);
        titleLayout.setTitleText("Personal Info");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
        //Initialize Views
        inputPhone = (TextInputEditText) findViewById(R.id.phone_input_et);
        inputName = (TextInputEditText) findViewById(R.id.full_name_et);
        inputCarer = (TextInputEditText) findViewById(R.id.inputCarer);
        inputCarerPhone = (TextInputEditText) findViewById(R.id.inputCarerPhone);
        inputIsCarer = (CheckBox) findViewById(R.id.carerCheckBox);

        btnChoose = (Button) findViewById(R.id.btnChoose);
        imageView = (CircularImageView) findViewById(R.id.profile_image);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final FirebaseUser currentUser = auth.getCurrentUser();

        final DatabaseReference userRef = database.getReference().child("Users").child(currentUser.getUid());
        picPath = "null";
        Context context = this;
        DatabaseReference infoRef = userRef.child("Info");
        // Attach a listener to read the data at our posts reference
        infoRef.addValueEventListener(new ValueEventListener() {
            @Override
            //load info from firebase node into user_info page
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String contactEmail = "Null";
                    String phoneNo = "Null";
                    String carerName = "Null";
                    String carerPhone = "Null";


                    for(DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getKey().equals("email")) {
                            contactEmail = child.getValue(String.class);
                            inputName.setText(contactEmail);

                        }else if (child.getKey().equals("phone")) {
                            phoneNo = child.getValue(String.class);
                            inputPhone.setText(phoneNo);
                        }else if (child.getKey().equals("carerName")) {
                            carerName = child.getValue(String.class);
                            inputCarer.setText(carerName);
                        }else if (child.getKey().equals("carerName")) {
                            carerName = child.getValue(String.class);
                            inputCarer.setText(carerName);
                        } else if (child.getKey().equals("carerPhone")) {
                            carerPhone = child.getValue(String.class);
                            inputCarerPhone.setText(carerPhone);
                        }else if (child.getKey().equals("picPath")) {
                            // load image with url
                            picPath = child.getValue(String.class);
                            GlideApp.with(getApplicationContext())
                                    .load(storageReference.child(picPath))
                                    .override(96, Target.SIZE_ORIGINAL)
                                    .into(imageView);


                        }else if (child.getKey().equals("isCarer")) {
                            if (child.getValue(String.class).equals("true")){
                                inputIsCarer.setChecked(true);
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing.
            }
        });

        btnSaveInfo = (Button) findViewById(R.id.btn_save);
        btnSaveInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        picPath = uploadImage();
//                        if (picPath.equals("null")){
//                            picPath =
//                        }
                        UserInfo userinfo = getUserForm(picPath);
                        Toast.makeText(getApplicationContext(), "Basic info saved successfully",
                                Toast.LENGTH_LONG).show();
                        userRef.child("Info").setValue(userinfo);

                    }
                }
        );


        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


    }

}