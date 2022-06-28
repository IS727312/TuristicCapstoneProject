package com.example.turistic.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String TAG = "Post";
    public static final String KEY_CAPTION =  "caption";
    public static final String KEY_PICTURE = "picture";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_LIKES = "likes";
    public static final String KEY_TITLE = "title";

    public String getCaption() {return getString(KEY_CAPTION); }
    public void setCaption(String caption) {put(KEY_CAPTION, caption); }
    public ParseFile getPicture() {return getParseFile(KEY_PICTURE); }
    public void  setPicture(ParseFile picture) {put(KEY_PICTURE, picture); }
    public ParseUser getOwner() {return  getParseUser(KEY_OWNER); }
    public void setOwner(ParseUser owner) {put(KEY_OWNER, owner); }
    public String getTitle() {return getString(KEY_TITLE); }
    public void setTitle(String title) {put(KEY_TITLE, title); }
    public int getLikes() {return getInt(KEY_LIKES); }
    public void increaseLikes() {put(KEY_LIKES, getLikes() + 1);}
    public void decreaseLikes() {put(KEY_LIKES, getLikes() - 1);}

    public String getRelativeTimeAgo(Date createdAt) {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (Exception e) {
            Log.i(TAG, "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }


}
