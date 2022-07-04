package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.turistic.adapters.PostAdapter;
import com.example.turistic.adapters.UserAdapter;
import com.example.turistic.models.Post;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    public static final String TAG = "SearchActivity";
    protected PostAdapter adapter;
    protected UserAdapter userAdapter;
    protected List<Post> queryPosts;
    protected List<ParseUser> queryUsers;
    private EditText etTitleToSearch;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etTitleToSearch = findViewById(R.id.etTitleToSearch);
        RecyclerView rvSearchPosts = findViewById(R.id.rvSearchPosts);
        RecyclerView rvSearchUsers = findViewById(R.id.rvSearchUsers);

        queryPosts = new ArrayList<>();
        queryUsers = new ArrayList<>();
        adapter = new PostAdapter(this, queryPosts);
        userAdapter = new UserAdapter(this, queryUsers);

        rvSearchPosts.setAdapter(adapter);
        rvSearchPosts.setLayoutManager(new LinearLayoutManager(this));

        rvSearchUsers.setAdapter(userAdapter);
        rvSearchUsers.setLayoutManager(new LinearLayoutManager(this));

        etTitleToSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = etTitleToSearch.getText().toString().toLowerCase(Locale.ROOT);
                getPosts();
                getUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {
                queryPosts.clear();
                queryUsers.clear();
            }
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_OWNER);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((posts, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            for(Post post: posts){
                if(post.getTitle().toLowerCase(Locale.ROOT).equals(searchQuery)){
                    queryPosts.add(post);
                    Log.i(TAG, "" + queryPosts.size());
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getUsers(){
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((objects, e) -> {
            if(e != null){
                Log.e(TAG, "Issues with retrieving users");
                return;
            }
            Log.i(TAG, "GETUSERS()");
            Log.i(TAG, searchQuery);
            for(ParseUser parseUser: objects){
                if(parseUser.getUsername().toLowerCase(Locale.ROOT).equals(searchQuery) ){
                    queryUsers.add(parseUser);
                }
            }
            userAdapter.notifyDataSetChanged();
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
}
