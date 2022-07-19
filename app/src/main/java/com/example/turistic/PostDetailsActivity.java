package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.turistic.models.Post;
import com.parse.ParseFile;

import org.parceler.Parcels;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Post post = Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));

        ParseFile postPicture = post.getPicture();
        TextView tvPostDetailsTitle = findViewById(R.id.tvPostDetailsTitle);
        TextView tvPostDetailsCaption = findViewById(R.id.tvPostDetailsCaption);
        TextView tvPostDetailsUsername = findViewById(R.id.tvPostDetailsUsername);
        ImageView ivPostDetailsPicture = findViewById(R.id.ivPostDetailsPicture);
        RatingBar rbPostDetailsRating = findViewById(R.id.rbPostDetailsRating);
        String username = "@" + post.getOwner().getUsername();

        Glide.with(this).load(postPicture.getUrl()).into(ivPostDetailsPicture);
        tvPostDetailsCaption.setText(post.getCaption());
        tvPostDetailsTitle.setText(post.getTitle());
        tvPostDetailsUsername.setText(username);
        rbPostDetailsRating.setRating(post.getRating());
    }
}