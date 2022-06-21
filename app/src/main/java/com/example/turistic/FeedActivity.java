package com.example.turistic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;


import com.example.turistic.fragments.FeedFragment;
import com.example.turistic.fragments.ProfileFragment;
import com.example.turistic.fragments.ComposeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.util.Objects;

public class FeedActivity extends AppCompatActivity {

    public static final String TAG = "FeedActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_feed_layout);

        setContentView(R.layout.activity_feed);

        ImageButton btnFeedLogOut = findViewById(R.id.btnFeedLogOut);
        ImageButton btnFeedSearchPost = findViewById(R.id.btnFeedSearchPost);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

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

        btnFeedSearchPost.setOnClickListener(v -> {
            Intent i = new Intent(FeedActivity.this, SearchActivity.class);
            startActivity(i);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()){
                case R.id.action_profile:
                    Toast.makeText(FeedActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                    fragment = new ProfileFragment();
                    break;
                case R.id.action_compose:
                    Toast.makeText(FeedActivity.this, "Search", Toast.LENGTH_SHORT).show();
                    fragment = new ComposeFragment();
                    break;
                case R.id.action_feed:
                default:
                    Toast.makeText(FeedActivity.this, "Home", Toast.LENGTH_SHORT).show();
                    fragment = new FeedFragment();
                    break;
            }
            fragmentManager.beginTransaction().replace(R.id.flFeedContainer, fragment).commit();
            return true;
        });
        //Default selection for the BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.action_feed);
    }
}