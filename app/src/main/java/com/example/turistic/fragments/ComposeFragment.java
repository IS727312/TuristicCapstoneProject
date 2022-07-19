package com.example.turistic.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.turistic.MainActivity;
import com.example.turistic.R;
import com.example.turistic.models.Post;
import com.example.turistic.utility.UtilityMethods;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;

import es.dmoral.toasty.Toasty;

public class ComposeFragment extends Fragment {

    public static final String sTAG = "ComposeFragment";
    public static final int sCAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final int sSELECT_PICTURE_REQUEST_CODE = 200;
    private EditText mEtComposeTitle;
    private ImageView mIvComposePictureToPost;
    private EditText mEtComposeCaption;
    private File mPhotoFile;
    private RatingBar mRbComposeRating;
    public String mPhotoFileName = "photo.jpg";
    private float mStarRating;

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnComposeSubmitPicture = view.findViewById(R.id.btnComposeSubmitPicture);
        Button btnComposeSubmitPost = view.findViewById(R.id.btnComposeSubmitPost);
        Button btnComposeTakePicture = view.findViewById(R.id.btnComposeTakePicture);
        mEtComposeTitle = view.findViewById(R.id.etComposeTitle);
        mEtComposeCaption = view.findViewById(R.id.etComposeCaption);
        mIvComposePictureToPost = view.findViewById(R.id.ivComposePictureToPost);
        mRbComposeRating = view.findViewById(R.id.rbComposeRating);

        mRbComposeRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mStarRating = rating;
            }
        });

        btnComposeTakePicture.setOnClickListener(v -> launchCamera());

        btnComposeSubmitPost.setOnClickListener(v -> {
            String caption = mEtComposeCaption.getText().toString();
            String title = mEtComposeTitle.getText().toString();
            if(caption.isEmpty() || title.isEmpty()){
                Toasty.error(getContext(), "Empty fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if(mPhotoFile == null || mIvComposePictureToPost.getDrawable() == null){
                Toasty.error(getContext(), "There is no photo", Toast.LENGTH_SHORT).show();
            }
            ParseUser currentUser = ParseUser.getCurrentUser();
            savePosts(caption, title, currentUser, mPhotoFile, mStarRating);
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
        });

        btnComposeSubmitPicture.setOnClickListener(this::onPickPhoto);
    }

    private void savePosts(String caption, String title, ParseUser currentUser, File photoFile, float rating) {
            Post post = new Post();
            post.setTitle(title);
            post.setCaption(caption);
            post.setPicture(new ParseFile(photoFile));
            post.setRating(rating);
            post.setOwner(currentUser);
            post.saveInBackground(e -> {
                if(e != null){
                    Log.e(sTAG, "Error while saving the post: ", e);
                    return;
                }

                post.saveInBackground(e1 -> {
                    if(e1 != null){
                        Log.e(sTAG, "Error while saving the post image: ", e1);
                        return;
                    }
                    Log.i(sTAG, "PHOTO SAVED");
                });
                Log.i(sTAG, "Save successful");
                mEtComposeTitle.setText("");
                mEtComposeCaption.setText("");
                mIvComposePictureToPost.setImageResource(0);
            });
    }

    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, sSELECT_PICTURE_REQUEST_CODE);
    }

    private void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoFile = UtilityMethods.getPhotoFileUri(mPhotoFileName, sTAG, getContext());

        Uri fileProvider = FileProvider.getUriForFile(requireContext(), "com.codepath.fileprovider", mPhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(intent, sCAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == sCAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = rotateBitmapOrientation(mPhotoFile.getAbsolutePath());
                UtilityMethods.submitPictureFromCamera(mPhotoFileName, sTAG, getContext());
                mIvComposePictureToPost.setImageBitmap(takenImage);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == sSELECT_PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = data.getData();
                Bitmap selectedImage = UtilityMethods.loadFromUri(photoUri, getContext());
                mPhotoFile = UtilityMethods.submitPictureFromGallery(getContext(), mPhotoFileName, selectedImage);
                mIvComposePictureToPost.setImageBitmap(selectedImage);
            }
        }
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create two BitmapFactory.Options Bounds and opts
        // bounds will help to limit the bounds of the return Bitmap.createBitmap
        //opt is used to create a bitmap from the photoFilePath
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        //inJustDecodeBounds is true so the .decodeFile does not return a Bitmap
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data -> Get the orientation the camera is originally in, and rotate the image based on that
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90: rotationAngle = 90;
            break;
            case ExifInterface.ORIENTATION_ROTATE_180: rotationAngle = 180;
            break;
            case ExifInterface.ORIENTATION_ROTATE_270: rotationAngle = 270;
            break;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
    }

}