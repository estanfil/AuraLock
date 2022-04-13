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

/*
MainActivity is the homepage of the mobile application.
 */

public class MainActivity extends AppCompatActivity {

    //Declaring all variables, including the database references
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

        //Initializing (activating) all the fields that will be changed through the user
        //Essentially connects the XML file to the activity
        btnUnlock = findViewById(R.id.btnUnlock);
        btnAddFace = findViewById(R.id.btnAddFace);
        btnHistory = findViewById(R.id.btnHistory);
        btnSettings = findViewById(R.id.btnSettings);
        btnFixed = findViewById(R.id.btnFixed);
        txtTime = findViewById(R.id.txtTime);
        txtTRemain = findViewById(R.id.txtTRemain);
        btnCancel =  findViewById(R.id.btnCancel);
        btnLive = findViewById(R.id.btnLive);

        //This is what connects the Firebase variables to the app
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

        /*
        ValueEventListeners wait until the database variable change and then take action after
        that.  For example, this one waits for "timeRemaining" (the countdown timer for fixed
        unlock) to change and then displays that time.  It displays the time by using the snapshot
        it collected and setting that value to rTime and then setting that to txtTRemain.
         */
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

        /*
        OnClickListeners are the listeners for buttons when action needs to be taken.  For example,
        this OnClickListener waits until the Cancel Fixed Unlock button is pressed and sets the
        "cancelDelayUnlock" to "True" in order to cancel the Fixed unlock.
         */
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

        /*
        startActivity is what occurs when you want a button to take you from one app page to another.
        Here, the Add Face (as shown to the user) button, when pressed, will take the user to the
        NewFace activity which is where they are able to add faces to the recognized face
        library on the Raspberry Pi.
         */
        btnAddFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),NewFace.class));
                finish();
            }
        });

        /*
        This History button takes the user to a page that displays an image of the last person
        who unlocked the door, as well as the name of the person and the date and time when the
        door was unlocked.
         */
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),LastUnlocked.class));
                finish();
            }
        });

        /*
        The Settings button takes the user to the About page that gives the user more information
        about the functionality of the AuraLock mobile app and allows the user to logout of the app.
         */
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