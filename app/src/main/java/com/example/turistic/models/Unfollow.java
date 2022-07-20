package com.example.turistic.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Unfollow")
public class Unfollow extends ParseObject {
    public static final String TAG = "UnfollowClass";
    public static final String KEY_USER_TO_UNFOLLOW = "userToUnfollow";
    public static final String KEY_USER_WHO_UNFOLLOWS = "userWhoUnfollows";

    public void setUserToUnfollow(ParseUser user){put(KEY_USER_TO_UNFOLLOW, user);}
    public ParseUser getUserToUnfollow(){return getParseUser(KEY_USER_TO_UNFOLLOW);}
    public void setUserWhoUnfollows(ParseUser user){put(KEY_USER_WHO_UNFOLLOWS, user);}
    public ParseUser getUserWhoUnfollows(){return getParseUser(KEY_USER_WHO_UNFOLLOWS);}
}
