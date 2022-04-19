package com.example.auralock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NewFace extends AppCompatActivity {

    private ImageView imageView;
    private Button btnUpload, btnHome, btnChooseImage;
    private EditText mEditTextFileName;
    private TextView txtRpi;
    private final int PICK_IMAGE_CODE = 172;
    private DatabaseReference AddingFace;
    private DatabaseReference mDatabaseRefAddFace, mDatabaseRefName;
    private String addFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_face);

        imageView = findViewById(R.id.imageView);
        btnUpload = findViewById(R.id.btnUpload);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnHome = findViewById(R.id.btnHome4);
        mEditTextFileName = findViewById(R.id.editTextFileName2);
        txtRpi = findViewById(R.id.txtRPi);

        AddingFace = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("Adding Face");
        mDatabaseRefAddFace = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("addFace");
        mDatabaseRefName = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("name");

        txtRpi.setText("The Raspberry Pi is ready for a face to be added");

        AddingFace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String rPiState = snapshot.getValue().toString();
                    if(rPiState.equals("working")){
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        txtRpi.setText("The Raspberry Pi is Processing..");

                    }
                    if(rPiState.equals("complete")) {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        txtRpi.setText("Your image has been Processed!");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });

        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(mEditTextFileName.getText().toString())){checkP();}
                else{
                    Toast.makeText(NewFace.this, "Please Enter a Name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkP(){
        Dexter.withActivity(NewFace.this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent,PICK_IMAGE_CODE);
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
                if (data != null) {
                    Uri imageUri = data.getData();
                    try {
                        Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver()
                                , imageUri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        original.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        imageView.setImageBitmap(original);
                        byte[] imageByte = stream.toByteArray();
                        uploadImage(imageByte);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    private void uploadImage(byte[] imageByte) {
            ProgressDialog progressDialog = new ProgressDialog(NewFace.this);
            progressDialog.setMessage("Image uploading...");
            progressDialog.show();
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference("AuraLock Data").child("Images to RPi").child(mEditTextFileName.getText().toString().trim()
                            + ".jpg");
            storageReference.putBytes(imageByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDatabaseRefName.setValue(mEditTextFileName.getText().toString());
                    progressDialog.dismiss();
                    addFace = "True";
                    mDatabaseRefAddFace.setValue(addFace);
                    Toast.makeText(NewFace.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    mEditTextFileName.setText("");
                    imageView.setImageBitmap(null);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(NewFace.this, "Image uploading failed", Toast.LENGTH_SHORT).show();
                }
            });
    }

}