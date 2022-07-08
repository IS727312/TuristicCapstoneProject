package com.example.turistic.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("FollowersRequestedFollowing")
public class FollowersRequestedFollowing extends ParseObject {
    public static final String sTAG = "FollowersRequestedFollowing";
    public static final String sKEY_FOLLOWER = "follower";
    public static final String sKEY_REQUESTED_FOLLOWING = "requestedFollowing";
    public static final String sKEY_ACCEPTED_REQUEST = "acceptedRequest";

    public void setFollower(ParseUser follower){put(sKEY_FOLLOWER, follower);}
    public ParseUser getFollower(){return getParseUser(sKEY_FOLLOWER);}
    public void setRequestedFollowing(ParseUser following){put(sKEY_REQUESTED_FOLLOWING, following);}
    public ParseUser getRequestedFollowing(){return getParseUser(sKEY_REQUESTED_FOLLOWING);}
    public void setStatus(boolean choice){put(sKEY_ACCEPTED_REQUEST, choice);}
    public boolean getStatus(){return getBoolean(sKEY_ACCEPTED_REQUEST);}
}
