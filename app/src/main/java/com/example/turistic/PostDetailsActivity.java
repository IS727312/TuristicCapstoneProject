package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.turistic.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Post post = Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));

        ParseFile postPicture = post.getPicture();
        ParseFile userPicture = post.getOwner().getParseFile("profilePicture");
        TextView tvPostDetailsTitle = findViewById(R.id.tvPostDetailsTitle);
        TextView tvPostDetailsCaption = findViewById(R.id.tvPostDetailsCaption);
        TextView tvPostDetailsUsername = findViewById(R.id.tvPostDetailsUsername);
        ImageView ivPostDetailsPicture = findViewById(R.id.ivPostDetailsPicture);
        ImageView ivPostDetailsProfilePicture = findViewById(R.id.ivPostDetailsProfilePicture);
        RatingBar rbPostDetailsRating = findViewById(R.id.rbPostDetailsRating);
        String username = "@" + post.getOwner().getUsername();

        Glide.with(this).load(postPicture.getUrl()).into(ivPostDetailsPicture);
        Glide.with(this).load(userPicture.getUrl()).into(ivPostDetailsProfilePicture);
        tvPostDetailsCaption.setText(post.getCaption());
        tvPostDetailsTitle.setText(post.getTitle());
        tvPostDetailsUsername.setText(username);
        rbPostDetailsRating.setRating(post.getRating());

        ivPostDetailsProfilePicture.setOnClickListener(v -> goToUserDetails(post.getOwner()));
    }
    public void goToUserDetails(ParseUser user){
        Intent intent = new Intent(PostDetailsActivity.this, UserDetailsActivity.class);
        // serialize the movie using parceler, use its short name as a key
        intent.putExtra(ParseUser.class.getSimpleName(), Parcels.wrap(user));
        this.startActivity(intent);
    }
}