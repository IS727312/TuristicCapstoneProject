package com.example.turistic.fragments;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.turistic.adapters.PostAdapter;
import com.example.turistic.R;
import com.example.turistic.adapters.UserAdapter;
import com.example.turistic.models.FollowersRequestedFollowing;
import com.example.turistic.models.Post;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    public static final String sTAG = "FeedFragment";
    private SwipeRefreshLayout mSrFeedFragment;
    private ParseUser mCurrentUser;
    private  int mUserPrivacyMode;
    private PostAdapter mAdapter;
    private UserAdapter mUserAdapter;
    private List<ParseUser> mAllUsers;
    private List<Post> mAllPosts;

    public FeedFragment(){
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvFeedFragment = view.findViewById(R.id.rvFeedFragment);
        mSrFeedFragment = view.findViewById(R.id.srFeedFragment);

        mAllPosts = new ArrayList<>();
        mAllUsers = new ArrayList<>();
        mUserAdapter = new UserAdapter(getContext(), mAllUsers);
        mAdapter = new PostAdapter(getContext(), mAllPosts);
        mCurrentUser = ParseUser.getCurrentUser();

        rvFeedFragment.setAdapter(mAdapter);
        rvFeedFragment.setLayoutManager(new LinearLayoutManager(getContext()));

        getPosts();
        mSrFeedFragment.setOnRefreshListener(() -> {
            mAllPosts.clear();
            getPosts();
            mSrFeedFragment.setRefreshing(false);
        });

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvFeedFragment);

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
                mUserPrivacyMode = post.getOwner().getInt("profileMode");
                switch (mUserPrivacyMode){
                    case 0:
                        onUserIsPublic(post);
                        break;
                    case 1:
                        onUserIsFollowersOnly(post);
                        break;
                    case 2:
                        onUserIsFriendsOnly(post);
                        break;
                    case 3: default:
                        onUserIsPrivate();
                        break;
                }
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    private void onUserIsPrivate() {
        Log.i(sTAG, "PRIVATE");
    }

    private void onUserIsFriendsOnly(Post post) {
        if(isFollowing(post.getOwner()) && isFollowedBy(post.getOwner())){
            Log.i(sTAG, "YES");
            mAllPosts.add(post);
        }
    }

    private void onUserIsFollowersOnly(Post post) {
       if(isFollowing(post.getOwner())){
           mAllPosts.add(post);
       }
    }

    private void onUserIsPublic(Post post) {
        mAllPosts.add(post);
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

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                Post post = mAllPosts.get(position);
                ParseUser postOwner = post.getOwner();

                if(!postOwner.getObjectId().equals(mCurrentUser.getObjectId())){
                    if(!isAlreadyFollowed(post)){
                        if(postOwner.getBoolean("anyoneCanFollow")) {
                            Toast.makeText(getContext(), "Following user: " + postOwner.getUsername(), Toast.LENGTH_SHORT).show();
                            mCurrentUser.add("following", postOwner);
                            mCurrentUser.saveInBackground(e -> {
                                if (e != null) {
                                    Log.e(sTAG, "Could not add user", e);
                                    return;
                                }
                                Log.i(sTAG, "Follower added successfully");
                            });
                        }else {
                            FollowersRequestedFollowing frf = new FollowersRequestedFollowing();
                            frf.setFollower(mCurrentUser);
                            frf.setRequestedFollowing(postOwner);
                            frf.saveInBackground(e -> {
                                if (e != null) {
                                    Log.e(sTAG, "Could not add user", e);
                                    return;
                                }
                                Toast.makeText(getContext(), "Request sent", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }else {
                        Toast.makeText(getContext(), "User already followed", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "Can not follow yourself", Toast.LENGTH_SHORT).show();
                }
            }
            mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            //Limit the distance of the swipe
            super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive);
        }
    };

    private boolean isAlreadyFollowed(Post post){
        ParseUser postOwner = post.getOwner();
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
}
