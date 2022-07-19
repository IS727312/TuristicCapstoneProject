package com.example.turistic.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.turistic.EditUserInformationActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UtilityMethods {

    public static File submitPictureFromGallery(Context context, String mPhotoFileName, Bitmap selectedImage){
        File f = new File(context.getCacheDir(), mPhotoFileName);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
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

        return f;
    }

    public static void submitPictureFromCamera(String mPhotoFileName, String TAG, Context context){
        Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(mPhotoFileName, TAG, context));
        // by this point we have the camera photo on disk
        Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
        // See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, 400);

        // Configure byte output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bitmapdata = bos.toByteArray();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);

        File resizedFile = getPhotoFileUri(mPhotoFileName + "_resized", TAG, context);
        try {
            resizedFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(resizedFile);
            fos.write(bitmapdata);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static File getPhotoFileUri(String fileName, String TAG, Context context) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }
    public static Bitmap loadFromUri(Uri photoUri, Context context) {
        Bitmap image = null;
        try {
            // check version of Android on device
            // on newer versions of Android, use the new decodeBitmap method
            ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), photoUri);
            image = ImageDecoder.decodeBitmap(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
