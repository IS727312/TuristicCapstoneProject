package com.example.turistic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;


import com.parse.ParseUser;

import java.util.Objects;

public class FeedActivity extends AppCompatActivity {

    public static final String TAG = "FeedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_feed_layout);

        setContentView(R.layout.activity_feed);

        ImageButton btnFeedLogOut = findViewById(R.id.btnFeedLogOut);

        btnFeedLogOut.setOnClickListener(v -> ParseUser.logOutInBackground(e -> {
            if (e != null){
                Log.e(TAG, "Issue with Logging Out: " + e);
                return;
            }
            //Check there is no current user
            ParseUser currentUser = ParseUser.getCurrentUser();
            if(currentUser == null){
                Intent i = new Intent(FeedActivity.this, LoginActivity.class);
                startActivity(i);
            }
        }));
    }
}