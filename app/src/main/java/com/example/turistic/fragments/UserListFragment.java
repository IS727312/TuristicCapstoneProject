package com.example.turistic.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.example.turistic.R;
import com.example.turistic.adapters.UserAdapter;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class UserListFragment extends Fragment {

    public static final String TAG = "UserListFragment";
    private ParseUser mCurrentUser;
    private List<ParseUser> mUsersList;
    private List<ParseUser> mUsersIds;
    private UserAdapter mUserAdapter;
    private boolean isFollowerList;
    private int mUserPrivacyMode;

    public UserListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments() != null){
            isFollowerList = getArguments().getBoolean("followers");
        }else{
            isFollowerList = false;
        }

        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCurrentUser = ParseUser.getCurrentUser();
        mUsersList = new ArrayList<>();
        mUserAdapter = new UserAdapter(getContext(), mUsersList);

        RecyclerView recyclerView = view.findViewById(R.id.rvUserListRecycleView);
        recyclerView.setAdapter(mUserAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getUsers();
    }

    private void getUsers(){
        if (isFollowerList){
            mUsersIds = mCurrentUser.getList("followers");
        }else {
            mUsersIds = mCurrentUser.getList("following");
        }
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((users, e) -> {
            if(e != null){
                Log.e(TAG, "Issues with retrieving users");
                return;
            }
            for(ParseUser user: users){
                if(userInList(user)){
                    mUsersList.add(user);
                }
            }
            mUserAdapter.notifyDataSetChanged();
        });
    }

    private boolean userInList(ParseUser user){
        for(ParseUser u:mUsersIds){
            if(u.getObjectId().equals(user.getObjectId())){
                return true;
            }
        }
        return false;
    }
}