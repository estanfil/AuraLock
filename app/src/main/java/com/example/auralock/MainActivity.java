package com.example.auralock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.TextUtilsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Button btnUnlock, btnAddFace, btnSettings, btnFixed, btnHistory, btnCancel;
    private Button btnLive;

    private DatabaseReference mDatabaseRefUnlock, mDatabaseRefDelay, mDatabaseRefTime;
    private DatabaseReference mDatabaseRefCancel;

    private DatabaseReference mDatabaseRefRTime, mLiveUpdate;

    private String unlock, delayUnlock, strTxtTime, cancel;

    private TextView txtTime, txtTRemain, txtRemain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUnlock = findViewById(R.id.btnUnlock);
        btnAddFace = findViewById(R.id.btnAddFace);
        btnHistory = findViewById(R.id.btnHistory);
        btnSettings = findViewById(R.id.btnSettings);
        btnFixed = findViewById(R.id.btnFixed);
        txtTime = findViewById(R.id.txtTime);
        txtTRemain = findViewById(R.id.txtTRemain);
        btnCancel =  findViewById(R.id.btnCancel);
        btnLive = findViewById(R.id.btnLive);

        mDatabaseRefUnlock = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("Unlock");
        mDatabaseRefDelay = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("DelayedUnlock").child("DelayUnlock");
        mDatabaseRefTime = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("DelayedUnlock").child("delayTime");
        mDatabaseRefRTime = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("DelayedUnlock").child("timeRemaining");
        mDatabaseRefCancel = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("DelayedUnlock").child("cancelDelayUnlock");
        mLiveUpdate = FirebaseDatabase.getInstance().getReference("AuraLock Data")
                .child("LiveCapture").child("newCapture");

        mDatabaseRefRTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String rTime = snapshot.getValue().toString();
                    txtTRemain.setText("Time Remaining: " + rTime + " minutes");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseRefCancel.setValue("True");
            }
        });


        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unlock = "True";
                mDatabaseRefUnlock.setValue(unlock);
            }
        });


        btnFixed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty((txtTime.getText().toString()))){
                    int time = Integer.parseInt(txtTime.getText().toString());
                    delayUnlock = "True";
                    mDatabaseRefDelay.setValue(delayUnlock);
                    mDatabaseRefTime.setValue(time);
                }else{
                    int time = 0;
                    mDatabaseRefTime.setValue(time);
                }
            }
        });

        btnAddFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),NewFace.class));
                finish();
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),LastUnlocked.class));
                finish();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Settings.class));
                finish();
            }
        });

        btnLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),RealTimeImage.class));
                String newCapture = "True";
                mLiveUpdate.setValue(newCapture);
                finish();
            }
        });


    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }


}