package com.example.turistic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.turistic.models.Post;
import com.parse.ParseFile;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public static final String TAG = "PostAdapter";
    private Context context;
    private List<Post> posts;

    public PostAdapter(Context context, List<Post> posts){
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivProfilePicture;
        private ImageView ivPostPicture;
        private TextView tvUsername;
        private TextView tvCaption;
        private TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivPostPicture = itemView.findViewById(R.id.ivPostPicture);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "Post Clicked");
        }


        public void bind(Post post) {
            tvCaption.setText(post.getCaption());
            tvTitle.setText(post.getTitle());
            tvUsername.setText(post.getOwner().getUsername());
            ParseFile postImage = post.getPicture();
            ParseFile profileImage = post.getOwner().getParseFile("profilePicture");
            if(postImage != null){
                Glide.with(context).load(postImage.getUrl()).into(ivPostPicture);
            }
            if(profileImage != null){
                Glide.with(context).load(profileImage.getUrl()).into(ivProfilePicture);
            }
        }
    }
}
