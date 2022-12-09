package com.project1.community;

import android.app.Application;
import android.util.Log;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.project1.community.Adapter.ChatAdapter;
import com.project1.community.Adapter.UsersAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AppController extends Application implements LifecycleObserver {
    private FirebaseAuth mAuth;
    public static DatabaseReference databaseReference;
    private static String TAG=AppController.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        try {
            mAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
            databaseReference.keepSynced(true);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        Log.e(TAG,"App is in foreground State");
        updateParticularField("currentStatus","Online");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        Log.e(TAG,"App is in Background State");
//        Date date = new Date();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a on dd-MM");
//        String strDate = simpleDateFormat.format(date);
        updateParticularField("currentStatus","Offline");
    }
    public void updateParticularField(String fieldName,String fieldValue){
        try {
            if (!mAuth.getUid().equals("null")) {
                databaseReference.child(mAuth.getUid()).child(fieldName).setValue(fieldValue);
            }
        }catch (Exception e){

            e.printStackTrace();
        }
    }

}
