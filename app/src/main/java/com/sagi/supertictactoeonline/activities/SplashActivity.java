package com.sagi.supertictactoeonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.utilities.SharedPreferencesHelper;
import com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant;
import com.sagi.supertictactoeonline.R;

public class SplashActivity extends AppCompatActivity {

    private static final int TIME_SPLASH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showCurrentScreen();
            }
        }, TIME_SPLASH);
    }

    private void showCurrentScreen() {
        Intent intent;
        if (false) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, RegisterLoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private boolean isAlreadyLogin() {
        return SharedPreferencesHelper.getInstance(this).isAlreadyLogin();
    }
}
