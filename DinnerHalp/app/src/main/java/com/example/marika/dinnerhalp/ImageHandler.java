package com.example.marika.dinnerhalp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;

/**
 * Created by marika on 1/19/17.
 * Written to hold static methods for downsampling and rotating image files picked by the
 * user and stored in the DinnerHalp user database.
 */

public class ImageHandler {

    private static final String TAG = ImageHandler.class.getSimpleName();

    public static Bitmap resizeImage(Context ctx, Uri selectedImage, int REQUIRED_SIZE)
            throws FileNotFoundException {

        Bitmap scaledBitmap = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(selectedImage), null, o);

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            scaledBitmap = BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(selectedImage),
                    null, o2);

        } catch (FileNotFoundException e) {
            Log.d(TAG, Log.getStackTraceString(e));
            Toast.makeText(ctx, ctx.getResources().getString(R.string.toast_filenotfound),
                    Toast.LENGTH_LONG).show();
        }

        return scaledBitmap;

    }

}
