package com.example.turistic.adapters;

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
import com.example.turistic.R;
import com.example.turistic.models.Post;
import com.parse.ParseFile;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public static final String sTAG = "PostAdapter";
    private Context mContext;
    private List<Post> mPosts;

    public PostAdapter(Context context, List<Post> posts){
        this.mContext = context;
        this.mPosts = posts;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIvProfilePicture;
        private ImageView mIvPostPicture;
        private TextView mTvUsername;
        private TextView mTvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvUsername = itemView.findViewById(R.id.tvUsername);
            mTvTitle = itemView.findViewById(R.id.tvTitle);
            mIvPostPicture = itemView.findViewById(R.id.ivPostPicture);
            mIvProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(sTAG, "Post Clicked");
        }


        public void bind(Post post) {
            //tvCaption.setText(post.getCaption());
            mTvTitle.setText(post.getTitle());
            mTvUsername.setText(post.getOwner().getUsername());
            ParseFile postImage = post.getPicture();
            ParseFile profileImage = post.getOwner().getParseFile("profilePicture");
            if(postImage != null){
                Glide.with(mContext).load(postImage.getUrl()).into(mIvPostPicture);
            }
            if(profileImage != null){
                Glide.with(mContext).load(profileImage.getUrl()).into(mIvProfilePicture);
            }
        }
    }
}
