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
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class UserAdapter  extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    public static final String sTAG = "UserAdapter";
    private Context mContext;
    private List<ParseUser> mUsers;

    public UserAdapter(Context context, List<ParseUser> users){
        this.mContext = context;
        this.mUsers = users;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.content_card_profile, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = mUsers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIvProfilePicture;
        private TextView mTvName;
        private TextView mTvLastName;
        private TextView mTvUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvUsername = itemView.findViewById(R.id.tvCardUsername);
            mIvProfilePicture = itemView.findViewById(R.id.ivCardProfilePicture);
            mTvName = itemView.findViewById(R.id.tvCardName);
            mTvLastName = itemView.findViewById(R.id.tvCardLastName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(sTAG, "Post Clicked");
        }


        public void bind(ParseUser user) {
            String username = "@" + user.getUsername();
            mTvUsername.setText(username);
            mTvName.setText(user.getString("name"));
            mTvLastName.setText(user.getString("lastName"));
            ParseFile profileImage = user.getParseFile("profilePicture");
            if(profileImage != null){
                Glide.with(mContext).load(profileImage.getUrl()).into(mIvProfilePicture);
            }
        }
    }
}
