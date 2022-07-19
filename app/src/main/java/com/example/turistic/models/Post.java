package com.example.turistic.models;

import android.content.Intent;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String sTAG = "Post";
    public static final String sKEY_CAPTION =  "caption";
    public static final String sKEY_PICTURE = "picture";
    public static final String sKEY_OWNER = "owner";
    public static final String sKEY_TITLE = "title";
    public static final String sKEY_RATING = "rating";

    public String getCaption() {return getString(sKEY_CAPTION); }
    public void setCaption(String caption) {put(sKEY_CAPTION, caption); }
    public ParseFile getPicture() {return getParseFile(sKEY_PICTURE); }
    public void  setPicture(ParseFile picture) {put(sKEY_PICTURE, picture); }
    public ParseUser getOwner() {return  getParseUser(sKEY_OWNER); }
    public void setOwner(ParseUser owner) {put(sKEY_OWNER, owner); }
    public String getTitle() {return getString(sKEY_TITLE); }
    public void setTitle(String title) {put(sKEY_TITLE, title); }
    public float getRating() {return getNumber(sKEY_RATING).floatValue();}
    public void setRating(float rating) {put(sKEY_RATING, rating);}

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
            Log.i(sTAG, "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }


}
