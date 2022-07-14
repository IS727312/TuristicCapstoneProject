package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

    private ParseUser mUser;
    private List<Post> mUserPosts;
    private PostAdapter mPostAdapter;


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        int totalFollowers;
        int totalFollowing;
        ParseFile profilePicture;

        mUser = Parcels.unwrap(getIntent().getParcelableExtra(ParseUser.class.getSimpleName()));

        profilePicture = mUser.getParseFile("profilePicture") ;
        RecyclerView rvUserDetailsPosts = findViewById(R.id.rvUserDetailsPosts);
        ImageView ivUserDetailsProfilePicture = findViewById(R.id.ivUserDetailsProfilePicture);
        TextView tvUserDetailsFollowers = findViewById(R.id.tvUserDetailsFollowers);
        TextView tvUserDetailsFollowing = findViewById(R.id.tvUserDetailsFollowing);
        TextView tvUserDetailsUsername = findViewById(R.id.tvUserDetailsUsername);

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

        if(mUser.getJSONArray("followers") == null){
            totalFollowing = 0;
        }else{
            totalFollowing = mUser.getJSONArray("followers").length();
        }

        assert profilePicture != null;
        Glide.with(this)
                .load(profilePicture.getUrl())
                .into(ivUserDetailsProfilePicture);

        tvUserDetailsUsername.setText(mUser.getUsername());
        tvUserDetailsFollowers.setText(String.format("Followers: %d", totalFollowers));
        tvUserDetailsFollowing.setText(String.format("Following: %d", totalFollowing));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.sKEY_OWNER);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((objects, e) -> {
            for(Post post: objects){
                if(mUser.getObjectId().equals(post.getOwner().getObjectId())){
                    mUserPosts.add(post);
                }
            }
            mPostAdapter.notifyDataSetChanged();
        });
    }
}