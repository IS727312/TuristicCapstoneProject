package com.example.turistic.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("FollowersRequestedFollowing")
public class FollowersRequestedFollowing extends ParseObject {
    public static final String sTAG = "FollowersRequestedFollowing";
    public static final String sKEY_FOLLOWER = "follower";
    public static final String sKEY_REQUESTED_FOLLOWING = "requestedFollowing";

    public void setFollower(ParseUser follower){put(sKEY_FOLLOWER, follower);}
    public ParseUser getFollower(){return getParseUser(sKEY_FOLLOWER);}
    public void setRequestedFollowing(ParseUser following){put(sKEY_REQUESTED_FOLLOWING, following);}
    public ParseUser getRequestedFollowing(){return getParseUser(sKEY_REQUESTED_FOLLOWING);}
}
