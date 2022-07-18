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

import com.bumptech.glide.Glide;
import com.example.turistic.adapters.PostAdapter;
import com.example.turistic.models.Post;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsActivity extends AppCompatActivity {

    public static final String sTAG = "UserDetailsActivity";
    private ParseUser mUser;
    private List<Post> mUserPosts;
    private PostAdapter mPostAdapter;
    private ParseUser mCurrentUser;
    private int mUserPrivacyMode;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        int totalFollowers;
        int totalFollowing;
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
        mPostAdapter = new PostAdapter(this, mUserPosts);
        rvUserDetailsPosts.setAdapter(mPostAdapter);
        rvUserDetailsPosts.setLayoutManager(new LinearLayoutManager(this));

        getPosts();

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
        }else{
            btnUserDetailsFollowStatus.setText(R.string.FollowStatus2);
        }

        if(mUserPosts.size() == 0){
            tvUserDetailsPostStatus.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFollowing(ParseUser owner) {
        ArrayList<ParseUser> followingList = (ArrayList) mCurrentUser.get("following");
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
}