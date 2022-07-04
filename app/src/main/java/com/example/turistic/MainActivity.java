package com.example.turistic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.example.turistic.fragments.FeedFragment;
import com.example.turistic.fragments.ProfileFragment;
import com.example.turistic.fragments.ComposeFragment;
import com.example.turistic.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "FeedActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private FrameLayout frameLayout;
    private List<ParseUser> allUsers;
    private ParseUser currentUser;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_feed_layout);

        setContentView(R.layout.activity_main);

        allUsers = new ArrayList<>();
        currentUser = ParseUser.getCurrentUser();

        frameLayout = findViewById(R.id.flMainActivity);
        ImageButton btnFeedLogOut = findViewById(R.id.btnFeedLogOut);
        ImageButton btnFeedSearchPost = findViewById(R.id.btnFeedSearchPost);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        btnFeedLogOut.setOnClickListener(v -> ParseUser.logOutInBackground(e -> {
            if (e != null){
                Log.e(TAG, "Issue with Logging Out: " + e);
                return;
            }
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
                    break;
                case R.id.action_compose:
                    fragment = new ComposeFragment();
                    break;
                case R.id.action_feed:
                default:
                    fragment = new FeedFragment();
                    break;
            }
            fragmentManager.beginTransaction().replace(R.id.flMainActivity, fragment).commit();
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
                Log.e(TAG, "Issue with getting users", e);
                return;
            }
            allUsers.addAll(objects);
            Log.i(TAG, "FOLLOWING");
            ArrayList<ParseUser> currentUserFollowerList = (ArrayList) currentUser.get("followers");
            for(ParseUser user: allUsers){
                ArrayList<ParseUser> userFollowingList = (ArrayList) user.get("following");
                if(userFollowingList != null){
                    for(int i = 0; i < userFollowingList.size(); i++){
                        if(userFollowingList.get(i).getObjectId().equals(currentUser.getObjectId()) &&
                            !isAlreadyFollowed(user)){
                            currentUser.add("followers", user);
                            currentUser.saveInBackground();
                        }
                    }
                }

            }
        });
    }

    private boolean isAlreadyFollowed(ParseUser user){
        ArrayList<ParseUser> followersList = (ArrayList) currentUser.get("followers");
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