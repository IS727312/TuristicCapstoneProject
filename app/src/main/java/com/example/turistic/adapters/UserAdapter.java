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
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class UserAdapter  extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    public static final String TAG = "UserAdapter";
    private Context context;
    private List<ParseUser> users;

    public UserAdapter(Context context, List<ParseUser> users){
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_card_profile, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivProfilePicture;
        private TextView tvName;
        private TextView tvLastName;
        private TextView tvUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvCardUsername);
            ivProfilePicture = itemView.findViewById(R.id.ivCardProfilePicture);
            tvName = itemView.findViewById(R.id.tvCardName);
            tvLastName = itemView.findViewById(R.id.tvCardLastName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "Post Clicked");
        }


        public void bind(ParseUser user) {
            String username = "@" + user.getUsername();
            tvUsername.setText(username);
            tvName.setText(user.getString("name"));
            tvLastName.setText(user.getString("lastName"));
            ParseFile profileImage = user.getParseFile("profilePicture");
            if(profileImage != null){
                Glide.with(context).load(profileImage.getUrl()).into(ivProfilePicture);
            }
        }
    }
}
