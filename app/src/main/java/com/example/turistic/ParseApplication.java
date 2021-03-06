package com.example.turistic;

import android.app.Application;

import com.example.turistic.models.FollowersRequestedFollowing;
import com.example.turistic.models.Post;
import com.example.turistic.models.Unfollow;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.facebook.ParseFacebookUtils;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //Register your parse models
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(FollowersRequestedFollowing.class);
        ParseObject.registerSubclass(Unfollow.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());

        ParseFacebookUtils.initialize(this);
    }
}
