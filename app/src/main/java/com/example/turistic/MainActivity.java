package com.example.turistic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SearchView;


import com.example.turistic.fragments.FeedFragment;
import com.example.turistic.fragments.ProfileFragment;
import com.example.turistic.fragments.ComposeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "FeedActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private FrameLayout frameLayout;
    private int currentFragmentId = R.id.action_feed;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_feed_layout);

        setContentView(R.layout.activity_main);

        frameLayout = findViewById(R.id.flMainActivity);
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
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        }));

        btnFeedSearchPost.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(i);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()){
                case R.id.action_profile:
                    fragment = new ProfileFragment();
                    currentFragmentId = R.id.action_profile;
                    break;
                case R.id.action_compose:
                    fragment = new ComposeFragment();
                    currentFragmentId = R.id.action_compose;
                    break;
                case R.id.action_feed:
                default:
                    fragment = new FeedFragment();
                    currentFragmentId = R.id.action_feed;
                    break;
            }
            fragmentManager.beginTransaction().replace(R.id.flMainActivity, fragment).commit();
            return true;
        });
        //Default selection for the BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.action_feed);
    }

}