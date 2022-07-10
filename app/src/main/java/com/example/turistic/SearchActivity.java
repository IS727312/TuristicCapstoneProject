package com.example.turistic;

import static com.example.turistic.enumerations.PrivacyMode.FOLLOWERS_ONLY;
import static com.example.turistic.enumerations.PrivacyMode.FRIENDS_ONLY;
import static com.example.turistic.enumerations.PrivacyMode.PUBLIC;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.turistic.adapters.PostAdapter;
import com.example.turistic.adapters.UserAdapter;
import com.example.turistic.models.FollowersRequestedFollowing;
import com.example.turistic.models.Post;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    public static final String sTAG = "SearchActivity";
    private PostAdapter mAdapter;
    private UserAdapter mUserAdapter;
    private List<Post> mQueryPosts;
    private List<ParseUser> mQueryUsers;
    private List<FollowersRequestedFollowing> mQueryRequests;
    private EditText mEtTitleToSearch;
    private String mSearchQuery;
    private ParseUser mCurrentUser;
    private int mUserPrivacyMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mCurrentUser = ParseUser.getCurrentUser();
        mEtTitleToSearch = findViewById(R.id.etTitleToSearch);
        RecyclerView rvSearchPosts = findViewById(R.id.rvSearchPosts);
        RecyclerView rvSearchUsers = findViewById(R.id.rvSearchUsers);

        mQueryRequests = new ArrayList<>();
        mQueryPosts = new ArrayList<>();
        mQueryUsers = new ArrayList<>();
        mAdapter = new PostAdapter(this, mQueryPosts);
        mUserAdapter = new UserAdapter(this, mQueryUsers);

        rvSearchPosts.setAdapter(mAdapter);
        rvSearchPosts.setLayoutManager(new LinearLayoutManager(this));

        rvSearchUsers.setAdapter(mUserAdapter);
        rvSearchUsers.setLayoutManager(new LinearLayoutManager(this));

        mEtTitleToSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchQuery = mEtTitleToSearch.getText().toString().toLowerCase(Locale.ROOT);
                getPosts();
                getUsers();
                getRequests();
            }

            @Override
            public void afterTextChanged(Editable s) {
                mQueryPosts.clear();
                mQueryUsers.clear();
                mQueryRequests.clear();
            }
        });

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSearchUsers);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSearchPosts);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.sKEY_OWNER);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((posts, e) -> {
            // check for errors
            if (e != null) {
                Log.e(sTAG, "Issue with getting posts", e);
                return;
            }
            for(Post post: posts){
                if(post.getTitle().toLowerCase(Locale.ROOT).equals(mSearchQuery)
                    || post.getOwner().getUsername().toLowerCase(Locale.ROOT).equals(mSearchQuery)){
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
                       default:
                            break;
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getUsers(){
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((users, e) -> {
            if(e != null){
                Log.e(sTAG, "Issues with retrieving users");
                return;
            }
            for(ParseUser user: users){
                if(user.getUsername().toLowerCase(Locale.ROOT).equals(mSearchQuery)){
                    mUserPrivacyMode = user.getInt("profileMode");
                    if(mUserPrivacyMode < 3){
                        mQueryUsers.add(user);
                    }
                }
            }
            mUserAdapter.notifyDataSetChanged();
        });
    }

    private void getRequests(){
        ParseQuery<FollowersRequestedFollowing> queryRequests = ParseQuery.getQuery(FollowersRequestedFollowing.class);
        queryRequests.include(FollowersRequestedFollowing.sKEY_FOLLOWER);
        queryRequests.addDescendingOrder("createdAt");
        queryRequests.findInBackground((objects, e) -> {
            mQueryRequests.addAll(objects);
        });
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Log.e(sTAG, "" +viewHolder.getItemId());
            int position = viewHolder.getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                Post post = mQueryPosts.get(position);
                ParseUser postOwner = post.getOwner();

                if(!postOwner.getObjectId().equals(mCurrentUser.getObjectId())){
                    if(!isAlreadyFollowed(postOwner) && !alreadyRequested(postOwner)){
                        if(postOwner.getBoolean("anyoneCanFollow")) {
                            Toast.makeText(SearchActivity.this, "Following user" + postOwner.getUsername(), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(SearchActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }else {
                        if(alreadyRequested(postOwner)){
                            Toast.makeText(SearchActivity.this, "Request already sent", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(SearchActivity.this, "User already followed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Toast.makeText(SearchActivity.this, "Can not follow yourself", Toast.LENGTH_SHORT).show();
                }
            }
            mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
            mUserAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            //Limit the distance of the swipe
            super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive);
        }
    };

    private boolean alreadyRequested(ParseUser owner) {
        for (FollowersRequestedFollowing request: mQueryRequests){
            if(request.getFollower().getObjectId().equals(mCurrentUser.getObjectId())
                    && request.getRequestedFollowing().getObjectId().equals(owner.getObjectId())){
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


    private void onUserIsFriendsOnly(Post post) {
        if(isFollowing(post.getOwner()) && isFollowedBy(post.getOwner())){
            mQueryPosts.add(post);
        }
    }

    private void onUserIsFollowersOnly(Post post) {
        if(isFollowing(post.getOwner())){
            mQueryPosts.add(post);
        }
    }

    private void onUserIsPublic(Post post) {
        mQueryPosts.add(post);
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
}