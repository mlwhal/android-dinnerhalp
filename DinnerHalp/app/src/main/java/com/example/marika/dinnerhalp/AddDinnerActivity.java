package com.example.marika.dinnerhalp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AddDinnerActivity extends AppCompatActivity {

    private DinnersDbAdapter mDbHelper;
    private EditText mEditNameText;
    private Spinner mMethodSpinner;
    private Spinner mTimeSpinner;
    private Spinner mServingsSpinner;
    private ImageButton mSetPicPath;
    private Uri mSelectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText mEditRecipe;
    //Tracker variable in case activity is getting content from another app
    private boolean mSharedContent = false;
    private String mNameText;
    private String mRecipeText;
    private Long mRowId;

    //TAG String used for logging
    private static final String TAG = AddDinnerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Handle cases where this activity is getting content from another app
        Intent shareIntent = getIntent();
        String action = shareIntent.getAction();
        String type = shareIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                //go ahead and prepare to create a new dinner using incoming text as recipe
                Log.d(TAG, "Text sent to app!");
//                Log.d(TAG, "mRowId is " + mRowId);
                mSharedContent = true;
                //Todo: Do I need to load mRecipeText with a value here, or can I just do it in
                //handleShareString?
                mRecipeText = shareIntent.getExtras().getString(Intent.EXTRA_TEXT);
                Log.d(TAG, "Text sent is " + mRecipeText);

                handleShareString(shareIntent);
            }
        }

        mDbHelper = new DinnersDbAdapter(this);

        //setContentView(R.layout.activity_add_dinner_noscroll);
        setContentView(R.layout.activity_add_dinner);

        //EditText for dinner name
        mEditNameText = (EditText) findViewById(R.id.edittext_name);
        mEditNameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        //Cooking method spinner
        mMethodSpinner = (Spinner) findViewById(R.id.spinner_method);
        ArrayAdapter<CharSequence> methodAdapter = ArrayAdapter.createFromResource(
                this, R.array.method_array, android.R.layout.simple_spinner_item);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMethodSpinner.setAdapter(methodAdapter);

        //Cooking time spinner
        mTimeSpinner = (Spinner) findViewById(R.id.spinner_time);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                this, R.array.time_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimeSpinner.setAdapter(timeAdapter);

        //Servings spinner
        mServingsSpinner = (Spinner) findViewById(R.id.spinner_servings);
        ArrayAdapter<CharSequence> servingsAdapter = ArrayAdapter.createFromResource(
                this, R.array.servings_array, android.R.layout.simple_spinner_item);
        servingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mServingsSpinner.setAdapter(servingsAdapter);

        //Initialize image button
        mSetPicPath = (ImageButton) findViewById(R.id.button_add_image);
        mSetPicPath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setType("image/*");
                photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                photoPickerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select picture"),
                        PICK_IMAGE_REQUEST);
            }
        });

        //EditText for recipe
        mEditRecipe = (EditText) findViewById(R.id.edittext_recipe);
        mEditRecipe.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        //Check savedInstanceState for a row ID to use, or use null if there is none
        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(DinnersDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(DinnersDbAdapter.KEY_ROWID)
                    : null;
        }

        //If coming from another app sharing content, override any mRowId value to create new dinner
        if (mSharedContent) {
            mRowId = null;
        }

        //Fill in text fields and set spinner values if we are editing a record.
        populateFields();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_dinner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Switch for different action bar item clicks
        switch (id) {

            case android.R.id.home:
//                mCancelledState = true;
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_cancel:
//                mCancelledState = true;
                finish();
                return true;

            case R.id.action_save:
                //Check whether name field is empty before allowing save
                String nameText = mEditNameText.getText().toString();
                if (nameText.matches("")) {
                    Toast.makeText(getApplicationContext(), "Dinners gotta have names!",
                            Toast.LENGTH_LONG).show();
                } else {
                    setResult(RESULT_OK);
                    saveDinner();
                }
                return true;

            case R.id.action_search:
//                mCancelledState = true;
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("FRAGMENT_TRACKER", 0);
                this.startActivity(intent);
                return true;

            case R.id.action_manage:
//                mCancelledState = true;
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.putExtra("FRAGMENT_TRACKER", 1);
                this.startActivity(intent2);
                return true;

            case R.id.action_settings:
//                mCancelledState = true;
                Intent intent3 = new Intent(this, SettingsActivity.class);
                this.startActivity(intent3);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    //Lifecycle handling methods
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DinnersDbAdapter.KEY_ROWID, mRowId);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case PICK_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    mSelectedImageUri = imageReturnedIntent.getData();
                    Log.d(TAG, "Uri is " + mSelectedImageUri);

                    //Take a persistent permission for the image file so the app doesn't lose it later
                    int takeFlags = imageReturnedIntent.getFlags();
                    takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    //Check for the freshest data
                    getContentResolver().takePersistableUriPermission(mSelectedImageUri, takeFlags);

                    //Show the image in the ImageView so the user knows this worked.
                    try {
                        mSetPicPath.setImageBitmap(processImage(mSelectedImageUri, 192));
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, Log.getStackTraceString(e));
                    }
                }

        }
    }

    //Todo: Is this method being called twice when loading an existing dinner?
    //The log is showing "Uri from db is " twice.
    //Method to fill in data if existing record is pulled from database.
    //Todo: Replace startManagingCursor with CursorLoader,
    //that will eliminate the deprecated stuff.
    private void populateFields() {

        if (mRowId != null) {
            mDbHelper.open();
            Cursor dinner = mDbHelper.fetchDinner(mRowId);
            mDbHelper.close();
            startManagingCursor(dinner);

            //Change section label if dinner is being updated rather than created
            TextView sectionLabel = (TextView) findViewById(R.id.section_label);
            sectionLabel.setText(getResources().getString(R.string.update_dinner_title));
            mEditNameText.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbAdapter.KEY_NAME)));
            //Set spinners to correct index for existing dinner
            mMethodSpinner.setSelection(getSpinnerIndex(mMethodSpinner,
                    dinner.getString(dinner.getColumnIndexOrThrow(DinnersDbAdapter.KEY_METHOD))));
            mTimeSpinner.setSelection(getSpinnerIndex(mTimeSpinner,
                    dinner.getString(dinner.getColumnIndexOrThrow(DinnersDbAdapter.KEY_TIME))));
            mServingsSpinner.setSelection(getSpinnerIndex(mServingsSpinner,
                    dinner.getString(dinner.getColumnIndexOrThrow(
                            DinnersDbAdapter.KEY_SERVINGS))));

            String imageString = dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbAdapter.KEY_PICPATH));
            //If there is a picpath in the database, downsample bitmap and display
            if (imageString != null) {
                Uri imageUri = Uri.parse(imageString);
                Log.d(TAG, "Uri from db is " + imageUri);
                try {
                    mSetPicPath.setImageBitmap(processImage(imageUri, 192));
                } catch (FileNotFoundException e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                }
            }

            mEditRecipe.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbAdapter.KEY_RECIPE)));

            //Close up cursor
            stopManagingCursor(dinner);
            dinner.close();

        } else {
            mEditNameText.requestFocus();

            //If another app sent in text via an intent, put that in the name and recipe EditTexts
            mEditNameText.setText(mNameText);
            mEditRecipe.setText(mRecipeText);
        }

     }

    //Method to get index of spinner when value is known (thanks StackOverflow)
    //http://stackoverflow.com/questions/29595478/set-spinner-value-based-on-database-record-in-android
    private int getSpinnerIndex(Spinner spinner, String myString) {

        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;

    }

    //Method to downsample large images before loading into ImageView
    //http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app
    private Bitmap processImage(Uri selectedImage, int REQUIRED_SIZE) throws FileNotFoundException {

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

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

        Bitmap scaledBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage),
                null, o2);

        //Correct the rotation of the bitmap if needed
        //Read rotation metadata from Uri stream
        InputStream inStream = getContentResolver().openInputStream(selectedImage);
        Bitmap rotatedBitmap = null;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(inStream);
            //Obtain the Exif directory
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            //Query the tag's value
            int rotation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            Log.d(TAG, "Rotation value of the image is " + rotation);

            Matrix matrix = new Matrix();
            switch(rotation) {
                case 0:
                    break;
                case 3:
                    matrix.postRotate(180);
                    break;
                case 6:
                    matrix.postRotate(90);
                    break;
                case 8:
                    matrix.postRotate(270);
                    break;
            }
            rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(),
                    scaledBitmap.getHeight(),
                    matrix, true);

        } catch (ImageProcessingException | IOException | MetadataException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return rotatedBitmap;
    }

    private void saveDinner() {

        //Collect values from input fields
        String name = mEditNameText.getText().toString();
        String method = mMethodSpinner.getSelectedItem().toString();
        String time = mTimeSpinner.getSelectedItem().toString();
        String servings = mServingsSpinner.getSelectedItem().toString();
        String picpath;

        //Set picpath depending on whether there is a selected image
        if (mSelectedImageUri != null) {
            picpath = mSelectedImageUri.toString();
            Log.d(TAG, "Picpath will be " + picpath);
        } else {
            picpath = null;
        }

        String recipe = mEditRecipe.getText().toString();

        Log.d(TAG, "mRowId = " + mRowId);

        //Create dinner or update existing record depending on the value of mRowId
//            mDbHelper.open();
        if (mRowId == null) {
            //Todo: Does it ever crash on a new dinner when name is not unique?
            mDbHelper.open();
            long id = mDbHelper.createDinner(name, method, time, servings, picpath, recipe);
            mDbHelper.close();
            Log.d(TAG, "id = " + id);
            //If id == -1 the dinner hasn't been saved; toast this and remain
            //in AddDinnerActivity.
            if (id == -1) {
                notUniqueName();
            } else if (id > 0) {
                Log.d(TAG, "Dinner created");
                mRowId = id;
                saveSuccessToast(name);

                //After dinner is saved, launch DinnerListActivity to display updated dinner list
                Intent intent1 = new Intent(this, DinnerListActivity.class);
                this.startActivity(intent1);
                finish();
            }
        } else {
            //updateDinner() returns a boolean indicating whether rows were updated.
            boolean updateSuccess = false;
            try {
                mDbHelper.open();
                updateSuccess = mDbHelper.updateDinner(
                        mRowId, name, method, time, servings, picpath, recipe);
                mDbHelper.close();
            } catch (SQLiteConstraintException e) {
                Log.d(TAG, "Exception caught: " + e.toString());
                notUniqueName();
            }
            if (updateSuccess) {
                saveSuccessToast(name);

                //Launch ViewDinnerActivity to see updated dinner
                Intent intent2 = new Intent(this, ViewDinnerActivity.class);
                intent2.putExtra(DinnersDbAdapter.KEY_ROWID, mRowId);
                this.startActivity(intent2);
                finish();
            }

        }

    }

    private void handleShareString(Intent shareIntent) {
        String shareString = shareIntent.getExtras().getString(Intent.EXTRA_TEXT);
        //Todo: Experiment with sharing from different apps to test behavior
        if (shareString != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 50; i++) {
                char c = shareString.charAt(i);
                //Build up a string of chars until a line break is hit
                if (c != '\n') {
                    builder.append(c);
                } else {
                    break;
                }
            }
            mNameText = builder.toString();
            Log.d(TAG, "Name text is " + mNameText);
//        Log.d(TAG, "Recipe text is " + mRecipeText);
        }

        //Remove mNameText and line break from front of shareString to make mRecipeText.
        mRecipeText = mRecipeText.replaceFirst(mNameText + "\n", "");
        Log.d(TAG, "Truncated recipe is " + mRecipeText);

    }

    public void saveSuccessToast(CharSequence name) {

        Context context = getApplicationContext();
        CharSequence text = name + " saved";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();

    }

    public void notUniqueName() {

        //Toast that name must be unique
        Context context = getApplicationContext();
        CharSequence text = getResources().getString(R.string.toast_not_unique);
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, text, duration).show();

        //Put user back into editText to rename dinner
        mEditNameText.setSelectAllOnFocus(true);
        mEditNameText.requestFocus();

    }
}
