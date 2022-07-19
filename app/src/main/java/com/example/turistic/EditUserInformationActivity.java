package com.example.turistic;

import static com.example.turistic.enumerations.fromString.CHOOSE_FOLLOWING_MODE;
import static com.example.turistic.enumerations.fromString.CHOOSE_PRIVACY_MODE;
import static com.example.turistic.enumerations.fromString.FOLLOWERS_ONLY;
import static com.example.turistic.enumerations.fromString.FRIENDS_ONLY;
import static com.example.turistic.enumerations.fromString.NO;
import static com.example.turistic.enumerations.fromString.PRIVATE;
import static com.example.turistic.enumerations.fromString.PUBLIC;
import static com.example.turistic.enumerations.fromString.YES;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.turistic.utility.UtilityMethods;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;

import es.dmoral.toasty.Toasty;

public class EditUserInformationActivity extends AppCompatActivity {

    public static final String sTAG = "EditActivity";
    public static final int sSELECT_PICTURE_REQUEST_CODE = 200;
    private Spinner mSpnEditPrivacyMode;
    private Spinner mSpnEditFollowingMode;
    private EditText mEtEditUsername;
    private EditText mEtEditName;
    private EditText mEtEditLastName;
    private ImageView mIvEditProfilePicture;
    private String mSpnPrivacyModeValue;
    private int mPrivacyModeValue;
    public String mPhotoFileName = "photo.jpg";
    private File mPhotoFile;
    private ParseUser mUser;
    private boolean mPhotoChanged = false;
    private String mSpnFollowingModeValue;
    private boolean mFollowingMode = true;
    private int mFollowingModeValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_information);

        mEtEditUsername = findViewById(R.id.etEditUsername);
        mEtEditName = findViewById(R.id.etEditName);
        mEtEditLastName = findViewById(R.id.etEditLastName);
        mIvEditProfilePicture = findViewById(R.id.ivEditProfilePicture);
        mUser = ParseUser.getCurrentUser();
        Button btnEditSave = findViewById(R.id.btnEditSave);

        //mUtilityMethods = new UtilityMethods(mPhotoFile, mPhotoFileName, mIvEditProfilePicture);

        mSpnEditPrivacyMode = findViewById(R.id.spnEditPrivacyMode);
        mPrivacyModeValue = 0;

        mSpnEditFollowingMode = findViewById(R.id.spnEditFollowingMode);

        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.privacyMode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpnEditPrivacyMode.setAdapter(adapter);

        ArrayAdapter<CharSequence> followingAdapter = ArrayAdapter.createFromResource(this, R.array.anyoneCanFollow, android.R.layout.simple_spinner_item);
        followingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpnEditFollowingMode.setAdapter(followingAdapter);

        setUserInfo();

        mIvEditProfilePicture.setOnClickListener(v -> {
            onPickPhoto(v);
            mPhotoChanged = true;
        });

        btnEditSave.setOnClickListener(v -> {
            mSpnPrivacyModeValue = mSpnEditPrivacyMode.getSelectedItem().toString();
            switch (mSpnPrivacyModeValue){
                case CHOOSE_PRIVACY_MODE:mPrivacyModeValue = -1;
                break;
                case PUBLIC:mPrivacyModeValue = 0;
                break;
                case FOLLOWERS_ONLY: mPrivacyModeValue = 1;
                break;
                case FRIENDS_ONLY: mPrivacyModeValue = 2;
                break;
                case PRIVATE:
                default: mPrivacyModeValue = 3;
                    break;
            }

            mSpnFollowingModeValue = mSpnEditFollowingMode.getSelectedItem().toString();
            switch (mSpnFollowingModeValue){
                case CHOOSE_FOLLOWING_MODE:
                    mFollowingModeValues = -1;
                    break;
                case YES:
                    mFollowingModeValues = 0;
                    mFollowingMode = true;
                    break;
                case NO:
                default:
                    mFollowingModeValues = 1;
                    mFollowingMode = false;
                    break;
            }
            if(!mEtEditName.getText().toString().isEmpty()) {
                mUser.put("name", mEtEditName.getText().toString());
            }
            if(!mEtEditLastName.getText().toString().isEmpty()){
                mUser.put("lastName", mEtEditLastName.getText().toString());
            }
            if(!mEtEditUsername.getText().toString().isEmpty()){
                mUser.setUsername(mEtEditUsername.getText().toString());
            }
            if(mPhotoChanged){
                mUser.put("profilePicture",new ParseFile(mPhotoFile));
            }
            if(mPrivacyModeValue == -1){
                Toasty.error(EditUserInformationActivity.this, "Privacy Mode not selected", Toast.LENGTH_SHORT).show();
            }else if(mFollowingModeValues == -1){
                Toasty.error(EditUserInformationActivity.this, "Following Mode not selected", Toast.LENGTH_SHORT).show();
            } else {
                mUser.put("profileMode", mPrivacyModeValue);
                mUser.put("anyoneCanFollow", mFollowingMode);
                mUser.saveInBackground(e -> {
                    if (e != null) {
                        Log.e(sTAG, "Issue with updating data");
                    }
                    Toasty.success(EditUserInformationActivity.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(EditUserInformationActivity.this, MainActivity.class);
                    startActivity(i);
                });
            }
        });
    }

    private void setUserInfo(){
        ParseFile profilePicture = mUser.getParseFile("profilePicture");
        int userPrivacyMode =  mUser.getInt("profileMode");

        mEtEditName.setHint(mUser.getString("name"));
        mEtEditLastName.setHint(mUser.getString("lastName"));
        mEtEditUsername.setHint(mUser.getUsername());
        assert profilePicture != null;
        Glide.with(EditUserInformationActivity.this).load(profilePicture.getUrl()).into(mIvEditProfilePicture);

        mSpnEditPrivacyMode.setSelection(userPrivacyMode);
    }

    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, sSELECT_PICTURE_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == sSELECT_PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                Uri photoUri = data.getData();
                Bitmap selectedImage = UtilityMethods.loadFromUri(photoUri, EditUserInformationActivity.this);

                mPhotoFile = UtilityMethods.submitPictureFromGallery(EditUserInformationActivity.this, mPhotoFileName, selectedImage);

                mIvEditProfilePicture.setImageBitmap(selectedImage);
            }
        }
    }
}