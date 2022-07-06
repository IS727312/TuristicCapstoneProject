package com.example.turistic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
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
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditUserInformation extends AppCompatActivity {

    public static final String sTAG = "EditActivity";
    public static final int sSELECT_PICTURE_REQUEST_CODE = 200;
    private Spinner mSpnPrivacyMode;
    private EditText mEtEditUsername;
    private EditText mEtEditName;
    private EditText mEtEditLastName;
    private ImageView mIvEditProfilePicture;
    private String mSpnValue;
    private int mPrivacyModeValue;
    public String mPhotoFileName = "photo.jpg";
    private File mPhotoFile;
    private ParseUser mUser;
    private Boolean mPhotoChanged = false;

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


        mSpnPrivacyMode = findViewById(R.id.spnEditPrivacyMode);
        mPrivacyModeValue = 0;

        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.privacyMode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpnPrivacyMode.setAdapter(adapter);

        setUserInfo();

        mIvEditProfilePicture.setOnClickListener(v -> {
            onPickPhoto(v);
            mPhotoChanged = true;
        });

        btnEditSave.setOnClickListener(v -> {
            mSpnValue = mSpnPrivacyMode.getSelectedItem().toString();
            switch (mSpnValue){
                case "Choose a Privacy Mode":mPrivacyModeValue = -1;
                break;
                case "Public":mPrivacyModeValue = 0;
                break;
                case "Followers Only": mPrivacyModeValue = 1;
                break;
                case "Friends Only": mPrivacyModeValue = 2;
                break;
                case "Private":
                default: mPrivacyModeValue = 3;
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
                Toast.makeText(EditUserInformation.this, "Privacy Mode not selected", Toast.LENGTH_SHORT).show();
            }else {
                mUser.put("profileMode", mPrivacyModeValue);
                mUser.saveInBackground(e -> {
                    if (e != null) {
                        Log.e(sTAG, "Issue with updating data");
                    }
                    Toast.makeText(EditUserInformation.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(EditUserInformation.this, MainActivity.class);
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
        Glide.with(EditUserInformation.this).load(profilePicture.getUrl()).into(mIvEditProfilePicture);

        mSpnPrivacyMode.setSelection(userPrivacyMode);
    }

    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, sSELECT_PICTURE_REQUEST_CODE);

    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            // on newer versions of Android, use the new decodeBitmap method
            ImageDecoder.Source source = ImageDecoder.createSource(EditUserInformation.this.getContentResolver(), photoUri);
            image = ImageDecoder.decodeBitmap(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == sSELECT_PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                assert data != null;
                Uri photoUri = data.getData();

                // Load the image located at photoUri into selectedImage
                Bitmap selectedImage = loadFromUri(photoUri);

                //create a file to write bitmap data
                File f = new File(EditUserInformation.this.getCacheDir(), mPhotoFileName);
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = null;

                try {
                    fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mPhotoFile = f;

                mIvEditProfilePicture.setImageBitmap(selectedImage);
            }
        }
    }

}