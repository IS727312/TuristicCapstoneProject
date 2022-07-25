package com.example.turistic;

import static com.example.turistic.enumerations.PrivacyMode.FOLLOWERS_ONLY;
import static com.example.turistic.enumerations.PrivacyMode.FRIENDS_ONLY;
import static com.example.turistic.enumerations.PrivacyMode.PRIVATE;
import static com.example.turistic.enumerations.PrivacyMode.PUBLIC;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.turistic.adapters.PostAdapter;
import com.example.turistic.models.FollowersRequestedFollowing;
import com.example.turistic.models.Post;
import com.example.turistic.models.Unfollow;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class UserDetailsActivity extends AppCompatActivity {

    public static final String sTAG = "UserDetailsActivity";
    private ParseUser mUser;
    private List<Post> mUserPosts;
    private PostAdapter mPostAdapter;
    private ParseUser mCurrentUser;
    private int mUserPrivacyMode;
    private List<ParseUser> followingUsers = new ArrayList<>();
    private List<FollowersRequestedFollowing> mQueryRequests;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        int totalFollowers;
        ParseFile profilePicture;

        mCurrentUser = ParseUser.getCurrentUser();
        mUser = Parcels.unwrap(getIntent().getParcelableExtra(ParseUser.class.getSimpleName()));

        profilePicture = mUser.getParseFile("profilePicture") ;
        RecyclerView rvUserDetailsPosts = findViewById(R.id.rvUserDetailsPosts);
        ImageView ivUserDetailsProfilePicture = findViewById(R.id.ivUserDetailsProfilePicture);
        TextView tvUserDetailsFollowers = findViewById(R.id.tvUserDetailsFollowers);
        TextView tvUserDetailsPostStatus = findViewById(R.id.tvUserDetailsPostStatus);
        TextView tvUserDetailsUsername = findViewById(R.id.tvUserDetailsUsername);
        Button btnUserDetailsFollowStatus = findViewById(R.id.btnUserDetailsFollowStatus);

        mUserPosts = new ArrayList<>();
        mQueryRequests = new ArrayList<>();
        mPostAdapter = new PostAdapter(this, mUserPosts);
        rvUserDetailsPosts.setAdapter(mPostAdapter);
        rvUserDetailsPosts.setLayoutManager(new LinearLayoutManager(this));

        getPosts();
        getRequests();

        if(mUser.getJSONArray("followers") == null){
            totalFollowers = 0;
        }else{
            totalFollowers = mUser.getJSONArray("followers").length();
        }

        assert profilePicture != null;
        Glide.with(this)
                .load(profilePicture.getUrl())
                .into(ivUserDetailsProfilePicture);

        tvUserDetailsUsername.setText(mUser.getUsername());
        tvUserDetailsFollowers.setText(String.format("Followers: %d", totalFollowers));

        if(isFollowing(mUser)){
            btnUserDetailsFollowStatus.setText(R.string.FollowStatus1);
            btnUserDetailsFollowStatus.setBackgroundColor(Color.WHITE);
            btnUserDetailsFollowStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unfollowUser(mUser);
                }
            });
        }else{
            btnUserDetailsFollowStatus.setText(R.string.FollowStatus2);
            btnUserDetailsFollowStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followUser(mUser);
                }
            });
        }

        if(mUserPosts.size() == 0){
            tvUserDetailsPostStatus.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFollowing(ParseUser owner) {
        List<ParseUser> followingList;
        followingList = new ArrayList<>();
        followingList =  mCurrentUser.getList("following");
        if(followingList != null){
            for(ParseUser user: followingList){
                if(user.getObjectId().equals(owner.getObjectId())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFollowedBy(ParseUser owner) {
        ArrayList<ParseUser> postOwnerFollowingList = (ArrayList) owner.get("following");
        if(postOwnerFollowingList != null){
            for(ParseUser user: postOwnerFollowingList){
                if(mCurrentUser.getObjectId().equals(user.getObjectId())){
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getPosts() {

        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.sKEY_OWNER);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground((posts, e) -> {
            if (e != null) {
                Log.e(sTAG, "Issue with getting posts", e);
                return;
            }
            for (Post post: posts){
                if(post.getOwner().getObjectId().equals(mUser.getObjectId())) {
                    mUserPrivacyMode = post.getOwner().getInt("profileMode");
                    switch (mUserPrivacyMode) {
                        case PUBLIC:
                            onUserIsPublic(post);
                            break;
                        case FOLLOWERS_ONLY:
                            onUserIsFollowersOnly(post);
                            break;
                        case FRIENDS_ONLY:
                            onUserIsFriendsOnly(post);
                            break;
                        case PRIVATE:
                        default:
                            onUserIsPrivate();
                            break;
                    }
                }
            }
            mPostAdapter.notifyDataSetChanged();
        });
    }

    private void getRequests() {
        ParseQuery<FollowersRequestedFollowing> queryRequests = ParseQuery.getQuery(FollowersRequestedFollowing.class);
        queryRequests.include(FollowersRequestedFollowing.sKEY_FOLLOWER);
        queryRequests.addDescendingOrder("createdAt");
        queryRequests.findInBackground((objects, e) -> {
            if(e != null){
                Log.e(sTAG, "Issue with getting Requests");
                return;
            }
            mQueryRequests.addAll(objects);
        });
    }

    private void onUserIsPrivate() {
        Log.i(sTAG, "PRIVATE");
    }

    private void onUserIsFriendsOnly(Post post) {
        if(isFollowing(post.getOwner()) && isFollowedBy(post.getOwner())){
            mUserPosts.add(post);
        }
    }

    private void onUserIsFollowersOnly(Post post) {
        if(isFollowing(post.getOwner())){
            mUserPosts.add(post);
        }
    }

    private void onUserIsPublic(Post post) {
        mUserPosts.add(post);
    }

    private void followUser(ParseUser postOwner){
        if(!postOwner.getObjectId().equals(mCurrentUser.getObjectId())){
            if(!isAlreadyFollowed(postOwner) && !alreadyRequested(postOwner)){
                FollowersRequestedFollowing frf = new FollowersRequestedFollowing();
                frf.setFollower(mCurrentUser);
                frf.setRequestedFollowing(postOwner);
                frf.saveInBackground(e -> {
                    if (e != null) {
                        Log.e(sTAG, "Could not add user", e);
                    }
                });
                if(postOwner.getBoolean("anyoneCanFollow")) {
                    mCurrentUser.add("following", postOwner);
                    mCurrentUser.saveInBackground(e -> {
                        if (e != null) {
                            Log.e(sTAG, "Could not add user", e);
                            return;
                        }
                        Log.i(sTAG, "Follower added successfully");
                    });
                    Toasty.success(UserDetailsActivity.this, "Following user: " + postOwner.getUsername(), Toast.LENGTH_SHORT).show();
                }else{
                    Toasty.success(UserDetailsActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                }
            }else {
                if(alreadyRequested(postOwner)){
                    Toasty.error(UserDetailsActivity.this, "Request already sent", Toast.LENGTH_SHORT).show();
                }else {
                    Toasty.error(UserDetailsActivity.this, "User already followed", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            Toasty.error(UserDetailsActivity.this, "Can not follow yourself", Toast.LENGTH_SHORT).show();
        }
    }

    private void unfollowUser(ParseUser postOwner) {
        if(!postOwner.getObjectId().equals(mCurrentUser.getObjectId())){
            if(isAlreadyFollowed(postOwner) && !alreadyUnfollowed(postOwner)){
                Unfollow unfollow = new Unfollow();
                unfollow.setUserToUnfollow(postOwner);
                unfollow.setUserWhoUnfollows(mCurrentUser);
                unfollow.saveInBackground();
                if(mCurrentUser.getList("following") != null){
                    followingUsers = mCurrentUser.getList("following");
                    assert followingUsers != null;
                    for(ParseUser u : followingUsers){
                        if(u.getObjectId().equals(postOwner.getObjectId())){
                            followingUsers.remove(u);
                            break;
                        }
                    }
                    mCurrentUser.put("following", followingUsers);
                    mCurrentUser.saveInBackground();
                    Toasty.warning(UserDetailsActivity.this, "Unfollowed user: "+ postOwner.getUsername(), Toast.LENGTH_SHORT).show();
                }
            }else {
                Toasty.error(UserDetailsActivity.this, "Can not unfollow a user not followed", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toasty.error(UserDetailsActivity.this, "Can not unfollow yourself", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean alreadyUnfollowed(ParseUser parseUser){
        ArrayList<Unfollow> unfollowsList = new ArrayList<>();
        ParseQuery<Unfollow> queryUnfollows = ParseQuery.getQuery(Unfollow.class);
        queryUnfollows.addDescendingOrder("createdAt");
        queryUnfollows.findInBackground((objects, e) -> unfollowsList.addAll(objects));
        for (Unfollow uf: unfollowsList){
            if (uf.getUserWhoUnfollows().getObjectId().equals(mCurrentUser.getObjectId())
                    && uf.getUserToUnfollow().getObjectId().equals(parseUser.getObjectId())){
                return true;
            }
        }
        return false;
    }

    private boolean isAlreadyFollowed(ParseUser postOwner){
        ArrayList<ParseUser> followingList = (ArrayList) mCurrentUser.get("following");
        if(followingList != null) {
            for (ParseUser user : followingList) {
                if (postOwner.getObjectId().equals(user.getObjectId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean alreadyRequested(ParseUser owner) {
        for (FollowersRequestedFollowing request: mQueryRequests){
            if(request.getFollower().getObjectId().equals(mCurrentUser.getObjectId())
                    && request.getRequestedFollowing().getObjectId().equals(owner.getObjectId())){
                return true;
            }
        }
        return false;
    }
}