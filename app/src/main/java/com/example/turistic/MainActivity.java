package com.example.turistic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.example.turistic.fragments.FeedFragment;
import com.example.turistic.fragments.ProfileFragment;
import com.example.turistic.fragments.ComposeFragment;
import com.example.turistic.models.FollowersRequestedFollowing;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String sTAG = "MainActivity";
    final FragmentManager mFragmentManager = getSupportFragmentManager();
    private List<ParseUser> mAllUsers;
    private List<FollowersRequestedFollowing> mAllRequests;
    private ParseUser mCurrentUser;
    private int prevFragment = 0;
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_feed_layout);

        setContentView(R.layout.activity_main);

        mAllRequests = new ArrayList<>();
        mAllUsers = new ArrayList<>();
        mCurrentUser = ParseUser.getCurrentUser();

        ImageButton btnFeedLogOut = findViewById(R.id.btnFeedLogOut);
        ImageButton btnFeedSearchPost = findViewById(R.id.btnFeedSearchPost);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        btnFeedLogOut.setOnClickListener(v -> {
            //LogInManager is used for logging out of Facebook
            LoginManager.getInstance().logOut();
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
            });
        });

        btnFeedSearchPost.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(i);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int slideInAnim, slideOutAnim, popInAnim, popOutAnim;
            switch (item.getItemId()){
                case R.id.action_profile:
                    fragment = new ProfileFragment();
                    prevFragment = 1;
                    slideInAnim = R.anim.slide_in_right;
                    slideOutAnim = R.anim.slide_out_left;
                    popInAnim = R.anim.slide_in_left;
                    popOutAnim = R.anim.slide_out_right;
                    break;
                case R.id.action_compose:
                    fragment = new ComposeFragment();
                    prevFragment = 2;
                    slideInAnim = R.anim.slide_in_left;
                    slideOutAnim = R.anim.slide_out_right;
                    popInAnim = R.anim.slide_in_right;
                    popOutAnim = R.anim.slide_out_left;
                    break;
                case R.id.action_feed:
                default:
                    fragment = new FeedFragment();
                    if(prevFragment == 1){
                        slideInAnim = R.anim.slide_in_left;
                        slideOutAnim = R.anim.slide_out_right;
                        popInAnim = R.anim.slide_in_right;
                        popOutAnim = R.anim.slide_out_left;
                    } else {
                        slideInAnim = R.anim.slide_in_right;
                        slideOutAnim = R.anim.slide_out_left;
                        popInAnim = R.anim.slide_in_left;
                        popOutAnim = R.anim.slide_out_right;
                    }
                    break;
            }
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(slideInAnim,slideOutAnim,
                            popInAnim, popOutAnim)
                    .replace(R.id.flMainActivity, fragment).commit();
            return true;
        });
        try {
            addNewFollowers();
            checkRequests();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Default selection for the BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.action_feed);
    }

    private void addNewFollowers() throws JSONException {
        if(mCurrentUser.getBoolean("anyoneCanFollow")) {
            ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
            query.addDescendingOrder("createdAt");
            query.findInBackground((objects, e) -> {
                if (e != null) {
                    Log.e(sTAG, "Issue with getting new followers", e);
                    return;
                }
                mAllUsers.addAll(objects);
                for (ParseUser user : mAllUsers) {
                    ArrayList<ParseUser> userFollowingList = (ArrayList) user.get("following");
                    if (userFollowingList != null) {
                        for (int i = 0; i < userFollowingList.size(); i++) {
                            if (userFollowingList.get(i).getObjectId().equals(mCurrentUser.getObjectId()) &&
                                    !isAlreadyFollowed(user)) {
                                mCurrentUser.add("followers", user);
                                mCurrentUser.saveInBackground();
                            }
                        }
                    }

                }
            });
        }else{
            ParseQuery<FollowersRequestedFollowing> queryRequests = ParseQuery.getQuery(FollowersRequestedFollowing.class);
            queryRequests.include(FollowersRequestedFollowing.sKEY_FOLLOWER);
            queryRequests.addDescendingOrder("createdAt");
            queryRequests.findInBackground((objects, e) -> {
                if (e != null) {
                    Log.e(sTAG, "Issue with getting requests", e);
                    return;
                }
                mAllRequests.addAll(objects);

                for (FollowersRequestedFollowing request : mAllRequests) {
                    if (request.getRequestedFollowing().getObjectId().equals(mCurrentUser.getObjectId())) {
                        ParseUser newFollower = request.getFollower();
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainActivity.this).
                                        setMessage("@" + newFollower.getUsername()).
                                        setPositiveButton("Accept", (dialog, which) -> {
                                            dialog.dismiss();
                                            request.setStatus(true);
                                            request.saveInBackground();
                                            mCurrentUser.add("followers", newFollower);
                                        }).
                                        setNegativeButton("Decline", (dialog, which) -> {
                                            dialog.dismiss();
                                            request.deleteInBackground(e1 -> Log.i(sTAG, "Request Declined"));
                                        });
                        builder.create().show();
                    }
            }
            });
        }
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

    private void checkRequests(){
        mAllRequests.clear();
        ParseQuery<FollowersRequestedFollowing> queryRequests = ParseQuery.getQuery(FollowersRequestedFollowing.class);
        queryRequests.include(FollowersRequestedFollowing.sKEY_FOLLOWER);
        queryRequests.addDescendingOrder("createdAt");
        queryRequests.findInBackground((objects, e) -> {
            if (e != null) {
                Log.e(sTAG, "Issue with getting requests", e);
                return;
            }
            mAllRequests.addAll(objects);
        for (FollowersRequestedFollowing request: mAllRequests){
            if(request.getFollower().getObjectId().equals(mCurrentUser.getObjectId())){
                if(request.getStatus()){
                    mCurrentUser.add("following", request.getRequestedFollowing());
                    request.deleteInBackground(e1 -> Log.i(sTAG, "Your request was accepted"));
                }
            }
        }
        });
    }

    private void getRequests(){
        ParseQuery<FollowersRequestedFollowing> queryRequests = ParseQuery.getQuery(FollowersRequestedFollowing.class);
        queryRequests.include(FollowersRequestedFollowing.sKEY_FOLLOWER);
        queryRequests.addDescendingOrder("createdAt");
        queryRequests.findInBackground((objects, e) -> {
            if (e != null) {
                Log.e(sTAG, "Issue with getting requests", e);
                return;
            }
            mAllRequests.addAll(objects);
        });
    }
}