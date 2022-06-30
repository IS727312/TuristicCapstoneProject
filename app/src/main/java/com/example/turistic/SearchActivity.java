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
import com.example.turistic.models.Post;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    public static final String TAG = "SearchActivity";
    protected PostAdapter adapter;
    protected List<Post> queryPosts;
    private EditText etTitleToSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etTitleToSearch = findViewById(R.id.etTitleToSearch);
        RecyclerView rvSearch = findViewById(R.id.rvSearch);

        queryPosts = new ArrayList<>();
        adapter = new PostAdapter(this, queryPosts);

        rvSearch.setAdapter(adapter);
        rvSearch.setLayoutManager(new LinearLayoutManager(this));

        etTitleToSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPosts();
            }

            @Override
            public void afterTextChanged(Editable s) {
                queryPosts.clear();
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
        // start an asynchronous call for posts

        query.findInBackground((posts, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            for(Post post: posts){
                if(post.getTitle().toLowerCase(Locale.ROOT).equals(etTitleToSearch.getText().toString().toLowerCase(Locale.ROOT))){
                    queryPosts.add(post);
                }
            }
            adapter.notifyDataSetChanged();
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
