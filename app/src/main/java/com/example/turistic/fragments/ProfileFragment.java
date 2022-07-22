package com.example.turistic.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.turistic.EditUserInformationActivity;
import com.example.turistic.UserListActivity;
import com.example.turistic.adapters.PostAdapter;
import com.example.turistic.R;
import com.example.turistic.models.Post;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String sTAG = "ProfileFragment";
    private ParseUser mUser;
    private JSONArray mFollowers, mFollowing;
    private PostAdapter mAdapter;
    private List<Post> mUserPosts;

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

        mUser = ParseUser.getCurrentUser();
        mFollowers = mUser.getJSONArray("followers");
        mFollowing = mUser.getJSONArray("following");
        ParseFile profilePicture = ParseUser.getCurrentUser().getParseFile("profilePicture");
        RecyclerView rvProfilePosts = view.findViewById(R.id.rvUserDetailsPosts);

        ImageView ivProfileProfilePicture = view.findViewById(R.id.ivUserDetailsProfilePicture);
        TextView tvProfileFollowers = view.findViewById(R.id.tvUserDetailsFollowers);
        TextView tvProfileFollowing = view.findViewById(R.id.tvUserDetailsFollowing);
        TextView tvProfileUsername = view.findViewById(R.id.tvUserDetailsUsername);
        ImageButton iBtnEdit = view.findViewById(R.id.iBtnEdit);
        int totalFollowers;
        int totalFollowing;

        mUserPosts = new ArrayList<>();
        mAdapter = new PostAdapter(getContext(), mUserPosts);

        rvProfilePosts.setAdapter(mAdapter);
        rvProfilePosts.setLayoutManager(new LinearLayoutManager(getContext()));

        getPosts();

        assert profilePicture != null;
        Glide.with(requireContext())
                .load(profilePicture.getUrl())
                .into(ivProfileProfilePicture);
        if(mFollowers == null){
            totalFollowers = 0;
        }else{
            totalFollowers = mFollowers.length();
        }

        if(mFollowing == null){
            totalFollowing = 0;
        }else{
            totalFollowing = mFollowing.length();
        }
        tvProfileFollowing.setText(String.format("Following: %d", totalFollowing));
        tvProfileFollowers.setText(String.format("Followers: %d", totalFollowers));
        tvProfileUsername.setText(mUser.getUsername());

        iBtnEdit.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), EditUserInformationActivity.class);
            startActivity(i);
        });

        tvProfileFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listUsers();
            }
        });

        tvProfileFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listUsers();
            }
        });
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
            mAdapter.notifyDataSetChanged();
        });
    }

    private void listUsers(){
        Intent i = new Intent(getContext(), UserListActivity.class);
        startActivity(i);
    }
}