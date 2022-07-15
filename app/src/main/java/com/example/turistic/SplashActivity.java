package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.parse.ParseUser;

public class SplashActivity extends AppCompatActivity {
    private static int TIME_OUT = 2000; //Time to launch the another activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                if(ParseUser.getCurrentUser() == null){
                    i = new Intent(SplashActivity.this, LoginActivity.class);
                }else{
                    i = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
    }
}