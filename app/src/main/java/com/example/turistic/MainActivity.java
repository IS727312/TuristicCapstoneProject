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
import android.app.PendingIntent;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final String sTAG = "MainActivity";
    public static final int LOCATION_REQUEST_CODE = 1;
    public static final int RESOLUTION_REQUEST_CODE = 10;
    public static final String CHANNEL_ID = "100";
    public static final String CHANNEL_NAME = "Touristic Notification";
    final FragmentManager mFragmentManager = getSupportFragmentManager();
    private List<FollowersRequestedFollowing> mAllRequests;
    private ParseUser mCurrentUser;
    private int mPrevFragment = 0;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private Amadeus mAmadeus;
    private List<PointOfInterest> mPointsOfInterestInUserRadius;
    private Location mLocation;
    private boolean mSwitchLocation;
    private String mLongitude;
    private String mLatitude;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_feed_layout);

        setContentView(R.layout.activity_main);
        createNotificationChannel();

         mAmadeus = Amadeus
                .builder("8B5cs7xeAYWhj0YouANbHaXqt62Aodb5", "9zogRFmi4rx9VYCA")
                .build();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();
        mAllRequests = new ArrayList<>();
        mPointsOfInterestInUserRadius = new ArrayList<>();
        mCurrentUser = ParseUser.getCurrentUser();
        mSwitchLocation = false;
        mLatitude = "41.3874";
        mLongitude = "2.1686";

        ImageButton btnFeedLogOut = findViewById(R.id.btnFeedLogOut);
        ImageButton btnFeedSearchPost = findViewById(R.id.btnFeedSearchPost);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(6000);
        mLocationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

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
                    mPrevFragment = 1;
                    slideInAnim = R.anim.slide_in_right;
                    slideOutAnim = R.anim.slide_out_left;
                    popInAnim = R.anim.slide_in_left;
                    popOutAnim = R.anim.slide_out_right;
                    break;
                case R.id.action_compose:
                    fragment = new ComposeFragment();
                    mPrevFragment = 2;
                    slideInAnim = R.anim.slide_in_left;
                    slideOutAnim = R.anim.slide_out_right;
                    popInAnim = R.anim.slide_in_right;
                    popOutAnim = R.anim.slide_out_left;
                    break;
                case R.id.action_feed:
                default:
                    fragment = new FeedFragment();
                    if (mPrevFragment == 1) {
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
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest).build();
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
                Log.i(sTAG, "onLocationResult - Lat: " + mLatitude + " Lon:" + mLongitude);
                mLocation = location;
                //pushNotification(location.toString());
                if(mSwitchLocation){
                    mLatitude = "41.3874";
                    mLongitude = "2.1686";
                }else{
                    mLatitude = "41.3884";
                    mLongitude = "2.1673";
                }
                mSwitchLocation = !mSwitchLocation;
                //Needed for getting the info out of the API,
                // because it can not be done in the Main Thread
                //new POITask().execute();
            }

        }
    };

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates(){
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void addNewFollowers() throws JSONException {
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
                        if(!mCurrentUser.getBoolean("anyoneCanFollow")) {
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
                        }else{
                            mCurrentUser.add("followers", newFollower);
                            mCurrentUser.saveInBackground();
                            request.deleteInBackground();
                        }
                    }
            }
            });
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

    private void pushNotification(String location, PendingIntent pendingIntent, int m){
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_home_24)
                .setContentText(location)
                .setContentTitle("Turistic")
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        notificationManagerCompat.notify(m , notification);
    }

    private void createNotificationChannel(){
        NotificationChannel channel1 = new NotificationChannel(
                CHANNEL_ID,
                "Channel 1",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel1.setDescription("This is Channel for notifications");
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
                PointOfInterest[] pointsOfInterest = mAmadeus.referenceData.locations.pointsOfInterest.get(Params
                        .with("latitude", mLatitude)
                        .and("longitude", mLongitude)
                        .and("radius", 1));
                for(PointOfInterest point : pointsOfInterest){
                    Log.i(sTAG, point.getName() + " AlreadyInRadius:  " + !alreadyInRadius(point));
                    if(!alreadyInRadius(point)){
                        // Create an explicit intent for an Activity in your app
                        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                        intent.putExtra("query", point.getName());
                        intent.setFlags(   Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        //Random is used to pass a different id for each notification
                        //Pass the correct information to the query
                        Random random = new Random();
                        int m = random.nextInt(9999 - 1000) + 1000;
                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, m , intent, PendingIntent.FLAG_IMMUTABLE);
                        mPointsOfInterestInUserRadius.add(point);
                        pushNotification(point.getName(), pendingIntent, m);
                    }
                }
                mPointsOfInterestInUserRadius.removeIf(point -> noLongerInRadius(point, pointsOfInterest));
                Log.i(sTAG, "ARRAYLIST POIS");
                for(PointOfInterest p: mPointsOfInterestInUserRadius){
                    Log.i(sTAG, p.getName());
                }
                Log.i(sTAG,"POIS: "+pointsOfInterest.length);

            } catch (ResponseException e) {
                e.printStackTrace();
            }
            return null;
        }

        private boolean noLongerInRadius(PointOfInterest point, PointOfInterest[] pointsOfInterest) {
            for(PointOfInterest poi: pointsOfInterest){
                if(point.getName().equals(poi.getName())){
                    return false;
                }
            }
            return true;
        }

        private boolean alreadyInRadius(PointOfInterest point) {
            if(mPointsOfInterestInUserRadius != null){
                for(PointOfInterest poi: mPointsOfInterestInUserRadius){
                    if(poi.getName().equals(point.getName())){
                        return true;
                    }
                }
            }
            return false;
        }
    }
}