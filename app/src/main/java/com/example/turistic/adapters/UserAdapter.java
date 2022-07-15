package com.example.turistic.adapters;

import static com.example.turistic.enumerations.PrivacyMode.FOLLOWERS_ONLY;
import static com.example.turistic.enumerations.PrivacyMode.FRIENDS_ONLY;
import static com.example.turistic.enumerations.PrivacyMode.PRIVATE;
import static com.example.turistic.enumerations.PrivacyMode.PUBLIC;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.turistic.R;
import com.example.turistic.UserDetailsActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class UserAdapter  extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    public static final String sTAG = "UserAdapter";
    private Context mContext;
    private List<ParseUser> mUsers;
    private ParseUser mCurrentUser;

    public UserAdapter(Context context, List<ParseUser> users){
        this.mContext = context;
        this.mUsers = users;
        this.mCurrentUser = ParseUser.getCurrentUser();
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
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                ParseUser user = mUsers.get(position);
                switch (user.getInt("profileMode")){
                    case PUBLIC:
                        goToUserDetails(user);
                        break;
                    case FOLLOWERS_ONLY:
                        if(isFollowing(user)){
                            goToUserDetails(user);
                        }else{
                            Toasty.error(itemView.getContext(), "You are not following this user", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case FRIENDS_ONLY:
                        if(isFollowing(user) && isFollowedBy(user)){
                            goToUserDetails(user);
                        }else {
                            Toasty.error(itemView.getContext(), "You are not friends with this user", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

            }
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

        public void goToUserDetails(ParseUser user){
            Intent intent = new Intent(mContext, UserDetailsActivity.class);
            // serialize the movie using parceler, use its short name as a key
            intent.putExtra(ParseUser.class.getSimpleName(), Parcels.wrap(user));
            mContext.startActivity(intent);
        }

        private boolean isFollowing(ParseUser owner) {
            ArrayList<ParseUser> followingList = (ArrayList) mCurrentUser.get("following");
            if(followingList != null){
                for(ParseUser user: followingList){
                    if(user.getObjectId().equals(owner.getObjectId())){
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isFollowedBy(ParseUser owner) {
            ArrayList<ParseUser> postOwnerFollowingList = (ArrayList) owner.get("following");
            if(postOwnerFollowingList != null){
                for(ParseUser user: postOwnerFollowingList){
                    if(mCurrentUser.getObjectId().equals(user.getObjectId())){
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
