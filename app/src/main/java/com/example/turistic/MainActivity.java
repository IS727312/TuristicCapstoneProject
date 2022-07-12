package com.example.turistic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.PointOfInterest;
import com.example.turistic.fragments.FeedFragment;
import com.example.turistic.fragments.ProfileFragment;
import com.example.turistic.fragments.ComposeFragment;
import com.example.turistic.models.FollowersRequestedFollowing;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String sTAG = "MainActivity";
    public static final int LOCATION_REQUEST_CODE = 1;
    public static final int RESOLUTION_REQUEST_CODE = 10;
    public static final String CHANNEL_ID = "100";
    public static final String CHANNEL_NAME = "Touristic Notification";
    final FragmentManager mFragmentManager = getSupportFragmentManager();
    private List<ParseUser> mAllUsers;
    private List<FollowersRequestedFollowing> mAllRequests;
    private ParseUser mCurrentUser;
    private int prevFragment = 0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    public static final String CHANNEL_ID_1 = "CustomServiceChannel1";
    private Amadeus amadeus;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_feed_layout);

        setContentView(R.layout.activity_main);
        createNotificationChannel();

         amadeus = Amadeus
                .builder("FceczHVR3ECcAdSOF3oh0ZP2GSYGhuw2", "8FqmCRs6YrrhxnFW")
                .build();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        mAllRequests = new ArrayList<>();
        mAllUsers = new ArrayList<>();
        mCurrentUser = ParseUser.getCurrentUser();

        ImageButton btnFeedLogOut = findViewById(R.id.btnFeedLogOut);
        ImageButton btnFeedSearchPost = findViewById(R.id.btnFeedSearchPost);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        btnFeedLogOut.setOnClickListener(v -> {
            //LogInManager is used for logging out of Facebook
            LoginManager.getInstance().logOut();
            ParseUser.logOutInBackground(e -> {
                if (e != null) {
                    Log.e(sTAG, "Issue with Logging Out: " + e);
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser == null) {
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            });
        });

        btnFeedSearchPost.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(i);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int slideInAnim, slideOutAnim, popInAnim, popOutAnim;
            switch (item.getItemId()) {
                case R.id.action_profile:
                    fragment = new ProfileFragment();
                    prevFragment = 1;
                    slideInAnim = R.anim.slide_in_right;
                    slideOutAnim = R.anim.slide_out_left;
                    popInAnim = R.anim.slide_in_left;
                    popOutAnim = R.anim.slide_out_right;
                    break;
                case R.id.action_compose:
                    fragment = new ComposeFragment();
                    prevFragment = 2;
                    slideInAnim = R.anim.slide_in_left;
                    slideOutAnim = R.anim.slide_out_right;
                    popInAnim = R.anim.slide_in_right;
                    popOutAnim = R.anim.slide_out_left;
                    break;
                case R.id.action_feed:
                default:
                    fragment = new FeedFragment();
                    if (prevFragment == 1) {
                        slideInAnim = R.anim.slide_in_left;
                        slideOutAnim = R.anim.slide_out_right;
                        popInAnim = R.anim.slide_in_right;
                        popOutAnim = R.anim.slide_out_left;
                    } else {
                        slideInAnim = R.anim.slide_in_right;
                        slideOutAnim = R.anim.slide_out_left;
                        popInAnim = R.anim.slide_in_left;
                        popOutAnim = R.anim.slide_out_right;
                    }
                    break;
            }
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(slideInAnim, slideOutAnim,
                            popInAnim, popOutAnim)
                    .replace(R.id.flMainActivity, fragment).commit();
            return true;
        });
        try {
            addNewFollowers();
            checkPastsRequests();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Default selection for the BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.action_feed);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkLocationPermissions()) {
            requestLocationPermission();
        } else {
            checkSettingsAndStartLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void getLastLocation() {
        @SuppressLint("MissingPermission")
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(location -> {
            if (location != null) {
                Log.i(sTAG, "onSuccess: " + location);
                Log.i(sTAG, "Longitude: " + location.getLongitude());
                Log.i(sTAG, "Latitude: " + location.getLatitude());
            } else {
                Log.i(sTAG, "onSuccess: location was null");
            }
        });

        locationTask.addOnFailureListener(e -> Log.e(sTAG, "onFailure: ", e));
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> locationSettingsResponseTask = settingsClient.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> {
            //Setting in the device are correct and can get location updates
            startLocationUpdates();
        });
        locationSettingsResponseTask.addOnFailureListener(e -> {
            //Settings not correct, ask the user for permission
            if (e instanceof ResolvableApiException) {
                ResolvableApiException apiException = (ResolvableApiException) e;
                try {
                    apiException.startResolutionForResult(MainActivity.this, RESOLUTION_REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location location: locationResult.getLocations()){
                Log.i(sTAG, "onLocationResult: " + location.toString());
                //pushNotification(location.toString());
                new POITask().execute();
            }

        }
    };

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void addNewFollowers() throws JSONException {
        if(mCurrentUser.getBoolean("anyoneCanFollow")) {
            ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
            query.addDescendingOrder("createdAt");
            query.findInBackground((objects, e) -> {
                if (e != null) {
                    Log.e(sTAG, "Issue with getting new followers", e);
                    return;
                }
                mAllUsers.addAll(objects);
                for (ParseUser user : mAllUsers) {
                    ArrayList<ParseUser> userFollowingList = (ArrayList) user.get("following");
                    if (userFollowingList != null) {
                        for (int i = 0; i < userFollowingList.size(); i++) {
                            if (userFollowingList.get(i).getObjectId().equals(mCurrentUser.getObjectId()) &&
                                    !isAlreadyFollowed(user)) {
                                mCurrentUser.add("followers", user);
                                mCurrentUser.saveInBackground();
                            }
                        }
                    }

                }
            });
        }else{
            ParseQuery<FollowersRequestedFollowing> queryRequests = ParseQuery.getQuery(FollowersRequestedFollowing.class);
            queryRequests.include(FollowersRequestedFollowing.sKEY_FOLLOWER);
            queryRequests.addDescendingOrder("createdAt");
            queryRequests.findInBackground((objects, e) -> {
                if (e != null) {
                    Log.e(sTAG, "Issue with getting requests", e);
                    return;
                }
                mAllRequests.addAll(objects);

                for (FollowersRequestedFollowing request : mAllRequests) {
                    if (request.getRequestedFollowing().getObjectId().equals(mCurrentUser.getObjectId())) {
                        ParseUser newFollower = request.getFollower();
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainActivity.this).
                                        setMessage("@" + newFollower.getUsername() + " wants to follow you\"").
                                        setPositiveButton("Accept", (dialog, which) -> {
                                            dialog.dismiss();
                                            request.setStatus(true);
                                            request.saveInBackground();
                                            mCurrentUser.add("followers", newFollower);
                                        }).
                                        setNegativeButton("Decline", (dialog, which) -> {
                                            dialog.dismiss();
                                            request.deleteInBackground(e1 -> Log.i(sTAG, "Request Declined"));
                                        });
                        builder.create().show();
                    }
            }
            });
        }
    }

    private boolean isAlreadyFollowed(ParseUser user){
        ArrayList<ParseUser> followersList = (ArrayList) mCurrentUser.get("followers");
        if(followersList != null) {
            for (ParseUser follower : followersList) {
                if (user.getObjectId().equals(follower.getObjectId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkPastsRequests(){
        //Checks for past requests sent by the current user
        //To see their status (accepted, or not responded)
        mAllRequests.clear();
        ParseQuery<FollowersRequestedFollowing> queryRequests = ParseQuery.getQuery(FollowersRequestedFollowing.class);
        queryRequests.include(FollowersRequestedFollowing.sKEY_FOLLOWER);
        queryRequests.addDescendingOrder("createdAt");
        queryRequests.findInBackground((objects, e) -> {
            if (e != null) {
                Log.e(sTAG, "Issue with getting requests", e);
                return;
            }
            mAllRequests.addAll(objects);
        for (FollowersRequestedFollowing request: mAllRequests){
            if(request.getFollower().getObjectId().equals(mCurrentUser.getObjectId())){
                if(request.getStatus()){
                    mCurrentUser.add("following", request.getRequestedFollowing());
                    request.deleteInBackground(e1 -> Log.i(sTAG, "Your request was accepted"));
                }
            }
        }
        });
    }

    private void getRequests(){
        ParseQuery<FollowersRequestedFollowing> queryRequests = ParseQuery.getQuery(FollowersRequestedFollowing.class);
        queryRequests.include(FollowersRequestedFollowing.sKEY_FOLLOWER);
        queryRequests.addDescendingOrder("createdAt");
        queryRequests.findInBackground((objects, e) -> {
            if (e != null) {
                Log.e(sTAG, "Issue with getting requests", e);
                return;
            }
            mAllRequests.addAll(objects);
        });
    }

    private boolean checkLocationPermissions(){
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        String[] arr = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        // If it is no the first time the users goes into the app
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
            Log.d(sTAG, "DIALOG ASKING AGAIN");
        }
        ActivityCompat.requestPermissions(this, arr, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission Granted
                checkSettingsAndStartLocationUpdates();
            }
        }
    }

    private void pushNotification(String location){
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_home_24)
                .setContentText(location)
                .setContentTitle("Turistic");
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(24 , notification);
    }

    private void createNotificationChannel(){
        NotificationChannel channel1 = new NotificationChannel(
                CHANNEL_ID_1,
                "Channel 1",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel1.setDescription("This is Channel 1");
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.createNotificationChannel(channel1);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        bundle.getString("DATA");
    }

    private class POITask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                PointOfInterest[] pointsOfInterest = amadeus.referenceData.locations.pointsOfInterest.bySquare.get(Params
                        .with("north", "41.397158")
                        .and("west", "2.160873")
                        .and("south", "41.394582")
                        .and("east", "2.177181"));
                pushNotification(pointsOfInterest[0].getName());
                Log.i(sTAG, pointsOfInterest[0].getName());
            } catch (ResponseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}