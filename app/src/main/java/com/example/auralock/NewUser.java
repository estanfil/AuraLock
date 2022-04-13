package com.example.auralock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;


public class NewUser extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button mBtnChooseImage, mBtnUpload, btnHome;
    //private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRefImage;
    private DatabaseReference mDatabaseRefName;
    private DatabaseReference mDatabaseRefAddFace;

    private StorageTask mUploadTask;

    private String addFace;
    private String unlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        mBtnChooseImage = findViewById(R.id.btnChooseImage);
        mBtnUpload = findViewById(R.id.btnUpload);
        //mTextViewShowUploads = findViewById(R.id.textViewShowUploads);
        mEditTextFileName = findViewById(R.id.editTextFileName);
        mImageView = findViewById(R.id.imageView);
        mProgressBar = findViewById(R.id.progressBar);
        btnHome = findViewById(R.id.btnHome2);

        mStorageRef = FirebaseStorage.getInstance().getReference("AuraLock Data")
                .child("Images to RPi");
        mDatabaseRefImage = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("Images to RPi");
        mDatabaseRefName = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("name");
        mDatabaseRefAddFace = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("addFace");

        mDatabaseRefAddFace.setValue(addFace);

        mBtnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });

        mBtnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(NewUser.this,"Upload in progress", Toast.LENGTH_LONG).show();
                } else {
                    String name = mEditTextFileName.getText().toString();
                    mDatabaseRefName.setValue(name);
                    addFace = "True";
                    mDatabaseRefAddFace.setValue(addFace);
                    mImageView.setImageDrawable(null);
                    uploadFile();
                }
            }
        });

      /*  mTextViewShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagesActivity();

            }
        });
       */
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null){
            mImageUri = data.getData();
            mImageView.setImageURI(mImageUri);
           
        }
    }
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
            if (mImageUri != null) {
                StorageReference fileReference = mStorageRef.child(mEditTextFileName.getText().toString().trim() + "." +
                        getFileExtension(mImageUri));

                mUploadTask = fileReference.putFile(mImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(0);
                                    }
                                }, 500);
                                Toast.makeText(NewUser.this, "Upload successful", Toast.LENGTH_LONG).show();
                                Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                        taskSnapshot.getUploadSessionUri().toString());
                                String uploadId = mDatabaseRefImage.push().getKey();
                                mDatabaseRefImage.child(uploadId).setValue(upload);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NewUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                mProgressBar.setProgress((int) progress);
                            }
                        });
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
            mEditTextFileName.setText("");
    }


  /*  private void openImagesActivity(){
        Intent intent = new Intent(this, ImagesActivity.class);
        startActivity(intent);
    }
   */
}