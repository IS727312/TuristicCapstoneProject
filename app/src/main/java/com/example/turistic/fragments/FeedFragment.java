package com.example.turistic.fragments;

import static com.example.turistic.enumerations.PrivacyMode.FOLLOWERS_ONLY;
import static com.example.turistic.enumerations.PrivacyMode.FRIENDS_ONLY;
import static com.example.turistic.enumerations.PrivacyMode.PRIVATE;
import static com.example.turistic.enumerations.PrivacyMode.PUBLIC;

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
import com.example.turistic.models.FollowersRequestedFollowing;
import com.example.turistic.models.Post;
import com.example.turistic.models.Unfollow;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class FeedFragment extends Fragment {

    public static final String sTAG = "FeedFragment";
    private SwipeRefreshLayout mSrFeedFragment;
    private ParseUser mCurrentUser;
    private  int mUserPrivacyMode;
    private PostAdapter mAdapter;
    private List<FollowersRequestedFollowing> mQueryRequests;
    private List<Post> mAllPosts;
    private List<ParseUser> followingUsers = new ArrayList<>();
    private boolean mIsFollowingTab;

    public FeedFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mIsFollowingTab = getArguments().getBoolean("tab");

        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvFeedFragment = view.findViewById(R.id.rvFeedFragment);
        mSrFeedFragment = view.findViewById(R.id.srFeedFragment);

        mAllPosts = new ArrayList<>();
        mQueryRequests = new ArrayList<>();
        Log.i(sTAG, getActivity().toString());
        mAdapter = new PostAdapter(getActivity(), mAllPosts);
        mCurrentUser = ParseUser.getCurrentUser();

        rvFeedFragment.setAdapter(mAdapter);
        rvFeedFragment.setLayoutManager(new LinearLayoutManager(getContext()));
        Toasty.success(getActivity(), "" + mIsFollowingTab, Toast.LENGTH_SHORT).show();
        getPosts();
        getRequests();
        mSrFeedFragment.setOnRefreshListener(() -> {
            mAllPosts.clear();
            mQueryRequests.clear();
            getPosts();
            getRequests();
            mSrFeedFragment.setRefreshing(false);
        });

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvFeedFragment);
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

    @SuppressLint("NotifyDataSetChanged")
    private void getPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.sKEY_OWNER);
        query.setLimit(30);
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground((posts, e) -> {
            if (e != null) {
                Log.e(sTAG, "Issue with getting posts", e);
                return;
            }
            for (Post post: posts){
                if(mIsFollowingTab){
                    if(isFollowing(post.getOwner())){
                        mUserPrivacyMode = post.getOwner().getInt("profileMode");
                        switch (mUserPrivacyMode){
                            case PUBLIC:
                                onUserIsPublic(post);
                                break;
                            case FOLLOWERS_ONLY:
                                onUserIsFollowersOnly(post);
                                break;
                            case FRIENDS_ONLY:
                                onUserIsFriendsOnly(post);
                                break;
                            case PRIVATE: default:
                                onUserIsPrivate();
                                break;
                        }
                    }
                }else {
                    if(!isFollowing(post.getOwner())) {
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

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
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
                switch (direction){
                    case ItemTouchHelper.LEFT:
                        followUser(postOwner);
                        break;
                    case ItemTouchHelper.RIGHT:
                        unfollowUser(postOwner);
                        break;
                }
            }
            mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            //Limit the distance of the swipe
            super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive);
        }
    };

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
                    Toasty.success(requireContext(), "Following user: " + postOwner.getUsername(), Toast.LENGTH_SHORT).show();
                }else{
                    Toasty.success(requireContext(), "Request sent", Toast.LENGTH_SHORT).show();
                }
            }else {
                if(alreadyRequested(postOwner)){
                    Toasty.error(requireContext(), "Request already sent", Toast.LENGTH_SHORT).show();
                }else {
                    Toasty.error(requireContext(), "User already followed", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            Toasty.error(requireContext(), "Can not follow yourself", Toast.LENGTH_SHORT).show();
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
                    Toasty.warning(requireContext(), "Unfollowed user: "+ postOwner.getUsername(), Toast.LENGTH_SHORT).show();
                }
            }else {
                Toasty.error(requireContext(), "Can not unfollow a user not followed", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toasty.error(requireContext(), "Can not unfollow yourself", Toast.LENGTH_SHORT).show();
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
}
