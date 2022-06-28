package com.example.turistic.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.turistic.PostAdapter;
import com.example.turistic.R;
import com.example.turistic.models.Post;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import SwipeGestures.SwipeGestureManager;

public class FeedFragment extends Fragment {

    public static final String TAG = "FeedFragment";
    private SwipeRefreshLayout srFeedFragment;
    protected PostAdapter adapter;
    protected List<Post> allPosts;

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
        srFeedFragment = view.findViewById(R.id.srFeedFragment);

        allPosts = new ArrayList<>();
        adapter = new PostAdapter(getContext(), allPosts);

        rvFeedFragment.setAdapter(adapter);
        rvFeedFragment.setLayoutManager(new LinearLayoutManager(getContext()));

        getPosts();

        srFeedFragment.setOnRefreshListener(() -> {
            allPosts.clear();
            getPosts();
            srFeedFragment.setRefreshing(false);
        });

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvFeedFragment);

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


            allPosts.addAll(posts);
            adapter.notifyDataSetChanged();
        });
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Log.i(TAG, "SWIPED LEFT");
            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive);
        }
    };

}
