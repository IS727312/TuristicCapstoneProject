package com.example.turistic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.example.turistic.fragments.FeedFragment;
import com.example.turistic.fragments.ProfileFragment;
import com.example.turistic.fragments.ComposeFragment;
import com.example.turistic.models.Post;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String sTAG = "FeedActivity";
    final FragmentManager mFragmentManager = getSupportFragmentManager();
    private FrameLayout mFrameLayout;
    private List<ParseUser> mAllUsers;
    private ParseUser mCurrentUser;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_feed_layout);

        setContentView(R.layout.activity_main);

        mAllUsers = new ArrayList<>();
        mCurrentUser = ParseUser.getCurrentUser();

        mFrameLayout = findViewById(R.id.flMainActivity);
        ImageButton btnFeedLogOut = findViewById(R.id.btnFeedLogOut);
        ImageButton btnFeedSearchPost = findViewById(R.id.btnFeedSearchPost);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        btnFeedLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null){
                            Log.e(sTAG, "Issue with Logging Out: " + e);
                            return;
                        }
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        if(currentUser == null){
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i);
                        }
                    }
                });
            }
        });
/*
        btnFeedLogOut.setOnClickListener(v ->
                ParseUser.logOutInBackground(e -> {
            if (e != null){
                Log.e(sTAG, "Issue with Logging Out: " + e);
                return;
            }
            ParseUser currentUser = ParseUser.getCurrentUser();
            if(currentUser == null){
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        }));

 */

        btnFeedSearchPost.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(i);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()){
                case R.id.action_profile:
                    fragment = new ProfileFragment();
                    break;
                case R.id.action_compose:
                    fragment = new ComposeFragment();
                    break;
                case R.id.action_feed:
                default:
                    fragment = new FeedFragment();
                    break;
            }
            mFragmentManager.beginTransaction().replace(R.id.flMainActivity, fragment).commit();
            return true;
        });
        try {
            addNewFollowers();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Default selection for the BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.action_feed);
    }

    private void addNewFollowers() throws JSONException {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.addDescendingOrder("createdAt");
        query.findInBackground((objects, e) -> {
            if (e != null) {
                Log.e(sTAG, "Issue with getting users", e);
                return;
            }
            mAllUsers.addAll(objects);
            ArrayList<ParseUser> currentUserFollowerList = (ArrayList) mCurrentUser.get("followers");
            for(ParseUser user: mAllUsers){
                ArrayList<ParseUser> userFollowingList = (ArrayList) user.get("following");
                if(userFollowingList != null){
                    for(int i = 0; i < userFollowingList.size(); i++){
                        if(userFollowingList.get(i).getObjectId().equals(mCurrentUser.getObjectId()) &&
                            !isAlreadyFollowed(user)){
                            mCurrentUser.add("followers", user);
                            mCurrentUser.saveInBackground();
                        }
                    }
                }

            }
        });
    }

    private boolean isAlreadyFollowed(ParseUser user){
        ArrayList<ParseUser> followersList = (ArrayList) mCurrentUser.get("followers");
        if(followersList != null) {
            for (ParseUser follower : followersList) {
                if (user.getObjectId().equals(follower.getObjectId())) {
                    return true;
                }
            }
        }
        return false;
    }
}