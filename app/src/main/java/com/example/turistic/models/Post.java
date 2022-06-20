package com.example.turistic.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String TAG = "Post";
    public static final String KEY_CAPTION =  "caption";
    public static final String KEY_PICTURE = "picture";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_LIKES = "likes";

}
