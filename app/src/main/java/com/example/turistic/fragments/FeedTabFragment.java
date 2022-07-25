package com.example.turistic.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.turistic.R;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class FeedTabFragment extends Fragment {

    public static final String TAG = "UserListActivity";
    private boolean mTabIsFollowing = true;
    private Bundle mBundle;

    public FeedTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBundle = new Bundle();
        TabLayout tabLayout = view.findViewById(R.id.tlFeedPosts);

        //Default selection
        mBundle.putBoolean("tab", mTabIsFollowing);
        switchTab();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTabIsFollowing = tab.getPosition() == 0;
                mBundle.putBoolean("tab", mTabIsFollowing);
                switchTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void switchTab(){
        Fragment fragment = new FeedFragment();
        fragment.setArguments(mBundle);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        //Animations that fit the position of the tabs
        if(mTabIsFollowing){
            fragmentManager.beginTransaction().setCustomAnimations( R.anim.slide_in_left, R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left).replace(R.id.flFeedPosts, fragment).commit();
        }else {
            fragmentManager.beginTransaction().setCustomAnimations( R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right).replace(R.id.flFeedPosts, fragment).commit();
        }

    }
}