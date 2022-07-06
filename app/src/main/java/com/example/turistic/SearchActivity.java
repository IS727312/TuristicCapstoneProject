package com.example.turistic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.turistic.adapters.PostAdapter;
import com.example.turistic.adapters.UserAdapter;
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
    private EditText mEtTitleToSearch;
    private String mSearchQuery;
    private ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mCurrentUser = ParseUser.getCurrentUser();
        mEtTitleToSearch = findViewById(R.id.etTitleToSearch);
        RecyclerView rvSearchPosts = findViewById(R.id.rvSearchPosts);
        RecyclerView rvSearchUsers = findViewById(R.id.rvSearchUsers);

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
            }

            @Override
            public void afterTextChanged(Editable s) {
                mQueryPosts.clear();
                mQueryUsers.clear();
            }
        });

        //new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSearchUsers);
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
                if(post.getTitle().toLowerCase(Locale.ROOT).equals(mSearchQuery)){
                    mQueryPosts.add(post);
                    Log.i(sTAG, "" + mQueryPosts.size());
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
        query.findInBackground((objects, e) -> {
            if(e != null){
                Log.e(sTAG, "Issues with retrieving users");
                return;
            }
            for(ParseUser parseUser: objects){
                if(parseUser.getUsername().toLowerCase(Locale.ROOT).equals(mSearchQuery) ){
                    mQueryUsers.add(parseUser);
                }
            }
            mUserAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Expand the search view and request focus
        searchItem.expandActionView();
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
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
                Post post = mQueryPosts.get(position);
                ParseUser postOwner = post.getOwner();

                if(!postOwner.getObjectId().equals(mCurrentUser.getObjectId())){
                    if(!isAlreadyFollowed(post)){
                        Toast.makeText(SearchActivity.this, "Following user"+ postOwner.getUsername(), Toast.LENGTH_SHORT).show();
                        mCurrentUser.add("following", postOwner);
                        mCurrentUser.saveInBackground(e -> {
                            if(e != null){
                                Log.e(sTAG,"Could not add user", e);
                                return;
                            }
                            Log.i(sTAG, "Follower added successfully");
                        });
                    }else {
                        Toast.makeText(SearchActivity.this, "User already followed", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(SearchActivity.this, "Can not follow yourself", Toast.LENGTH_SHORT).show();
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
