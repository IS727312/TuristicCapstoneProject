package com.example.turistic.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.turistic.PostAdapter;
import com.example.turistic.R;
import com.example.turistic.models.Post;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    private ParseUser user;
    private JSONArray followers, following;
    protected PostAdapter adapter;
    protected List<Post> userPosts;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = ParseUser.getCurrentUser();
        followers = user.getJSONArray("followers");
        following = user.getJSONArray("following");
        ParseFile profilePicture = ParseUser.getCurrentUser().getParseFile("profilePicture");
        RecyclerView rvProfilePosts = view.findViewById(R.id.rvProfilePosts);

        ImageView ivProfileProfilePicture = view.findViewById(R.id.ivProfileProfilePicture);
        TextView tvProfileFollowers = view.findViewById(R.id.tvProfileFollowers);
        TextView tvProfileFollowing = view.findViewById(R.id.tvProfileFollowing);
        TextView tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        int totalFollowers;
        int totalFollowing;

        userPosts = new ArrayList<>();
        adapter = new PostAdapter(getContext(), userPosts);

        rvProfilePosts.setAdapter(adapter);
        rvProfilePosts.setLayoutManager(new LinearLayoutManager(getContext()));

        getPosts();

        assert profilePicture != null;
        Glide.with(requireContext())
                .load(profilePicture.getUrl())
                .into(ivProfileProfilePicture);
        if(followers == null){
            totalFollowers = 0;
        }else{
            totalFollowers = followers.length();
        }

        if(following == null){
            totalFollowing = 0;
        }else{
            totalFollowing = following.length();
        }
        tvProfileFollowing.setText(String.format("Following: %d", totalFollowing));
        tvProfileFollowers.setText(String.format("Followers: %d", totalFollowers));
        tvProfileUsername.setText(user.getUsername());

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_OWNER);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((objects, e) -> {
            for(Post post: objects){
                if(user.getObjectId().equals(post.getOwner().getObjectId())){
                    userPosts.add(post);
                }
            }
            adapter.notifyDataSetChanged();
        });


    }
}