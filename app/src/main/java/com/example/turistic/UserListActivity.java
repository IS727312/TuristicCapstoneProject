package com.example.turistic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import com.example.turistic.fragments.UserListFragment;
import com.google.android.material.tabs.TabLayout;

public class UserListActivity extends AppCompatActivity {

    public static final String TAG = "UserListActivity";
    private boolean mIsFollowerList = true;
    private Bundle mBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mBundle = new Bundle();
        TabLayout tabLayout = findViewById(R.id.tlUserList);

        //Default selection
        mBundle.putBoolean("followers", mIsFollowerList);
        switchTab();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mIsFollowerList = tab.getPosition() == 0;
                mBundle.putBoolean("followers", mIsFollowerList);
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
        Fragment fragment = new UserListFragment();
        fragment.setArguments(mBundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        //Animations that fit the position of the tabs
        if(mIsFollowerList){
            fragmentManager.beginTransaction().setCustomAnimations( R.anim.slide_in_left, R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left).replace(R.id.flUserList, fragment).commit();
        }else {
            fragmentManager.beginTransaction().setCustomAnimations( R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right).replace(R.id.flUserList, fragment).commit();
        }

    }
}