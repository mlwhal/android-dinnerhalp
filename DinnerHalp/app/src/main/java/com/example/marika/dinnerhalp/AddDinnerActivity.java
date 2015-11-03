package com.example.marika.dinnerhalp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import java.io.FileNotFoundException;

public class AddDinnerActivity extends ActionBarActivity {

    public int mFragmentTracker;
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
    private String mRecipeText;
    private Long mRowId;

    //Track whether cancel or save button is clicked to finish Activity
    private Boolean mCancelledState = false;

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
                mSharedContent = true;
                mRecipeText = shareIntent.getExtras().getString(Intent.EXTRA_TEXT);
//                Log.d(AddDinnerActivity.class.getSimpleName(), "Text sent to app!");
//                Log.d(AddDinnerActivity.class.getSimpleName(), "Text sent is " + mRecipeText);
//                Log.d(AddDinnerActivity.class.getSimpleName(), "mRowId is " + mRowId);
            }
        }

        mDbHelper = new DinnersDbAdapter(this);
        mDbHelper.open();

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
                photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
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
                mCancelledState = true;
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_cancel:
                mCancelledState = true;
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
                    //After adding new dinner, go to DinnerListActivity
                    //If updating existing dinner, reload ViewDinnerActivity to show update
                    if (mRowId == null) {
                        Intent intent1 = new Intent(this, DinnerListActivity.class);
                        this.startActivity(intent1);
                        finish();
                    } else {
                        Intent intent2 = new Intent(this, ViewDinnerActivity.class);
                        intent2.putExtra(DinnersDbAdapter.KEY_ROWID, mRowId);
                        this.startActivity(intent2);
                        finish();
                    }
                }
                return true;

            case R.id.action_search:
                mCancelledState = true;
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("FRAGMENT_TRACKER", 0);
                this.startActivity(intent);
                return true;

            case R.id.action_manage:
                mCancelledState = true;
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.putExtra("FRAGMENT_TRACKER", 1);
                this.startActivity(intent2);
                return true;

            case R.id.action_about:
                mCancelledState = true;
                Intent intent3 = new Intent(this, AboutAppActivity.class);
                this.startActivity(intent3);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case PICK_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    mSelectedImageUri = imageReturnedIntent.getData();
                    Log.d(AddDinnerActivity.class.getSimpleName(), "Uri is " + mSelectedImageUri);
                    //Show the image in the ImageView so the user knows this worked.
                    try {
                        mSetPicPath.setImageBitmap(decodeUri(mSelectedImageUri, 192));
                    } catch (FileNotFoundException e) {
                        Log.d(AddDinnerActivity.class.getSimpleName(), Log.getStackTraceString(e));
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
//            mDbHelper.open();
            Cursor dinner = mDbHelper.fetchDinner(mRowId);
            startManagingCursor(dinner);

            //Change section label if dinner is being updated rather than created
            TextView sectionLabel = (TextView) findViewById(R.id.section_label);
            sectionLabel.setText("Update dinner");
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
                Log.d(AddDinnerActivity.class.getSimpleName(), "Uri from db is " + imageUri);
                try {
                    mSetPicPath.setImageBitmap(decodeUri(imageUri, 192));
                } catch (FileNotFoundException e) {
                    Log.d(AddDinnerActivity.class.getSimpleName(), Log.getStackTraceString(e));
                }
            }

            mEditRecipe.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbAdapter.KEY_RECIPE)));

        } else {
            //If another app sent in text via an intent, put that in the recipe EditText
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
    private Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) throws FileNotFoundException {

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
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage),
                null, o2);

    }

    //Lifecycle handling methods
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(DinnersDbAdapter.KEY_ROWID, mRowId);
    }

    //onPause() is run even when the activity is ended with finish(),
    //so the current dinner is always created/updated via saveState().
    //Todo: Does it make sense to save state onPause(), or onStop()?
    //You're not supposed to write to databases in onPause().
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
////        mDbHelper.close();
//        Log.d(AddDinnerActivity.class.getSimpleName(), "mDbHelper is now closed");
//    }

    private void saveState() {

        //Check mCancelledState to determine whether to save state or not
        if (!mCancelledState) {
            String name = mEditNameText.getText().toString();
            if (name.matches("")) {
                //Don't save the dinner
            } else {
                //Go ahead and save the dinner
                setResult(RESULT_OK);
                String method = mMethodSpinner.getSelectedItem().toString();
                String time = mTimeSpinner.getSelectedItem().toString();
                String servings = mServingsSpinner.getSelectedItem().toString();
                String picpath;

                //Set picpath depending on whether there is a selected image
                if (mSelectedImageUri != null) {
                    picpath = mSelectedImageUri.toString();
                    Log.d(AddDinnerActivity.class.getSimpleName(), "Picpath will be " + picpath);
                } else {
                    picpath = null;
                }

                String recipe = mEditRecipe.getText().toString();

                Log.d(AddDinnerActivity.class.getSimpleName(), "mRowId = " + mRowId);

                //Create dinner or update existing record depending on the value
                //returned by createDinner()
                if (mRowId == null) {
                    long id = mDbHelper.createDinner(name, method, time, servings, picpath, recipe);
                    //Todo: manually close the db? mDbHelper.close();
                    Log.d(AddDinnerActivity.class.getSimpleName(), "Dinner created");
                    if (id > 0) {
                        mRowId = id;
                    }
                } else {
                    mDbHelper.updateDinner(mRowId, name, method, time, servings, picpath, recipe);
                    //Todo: manually close the db? mDbHelper.close();
                }

                Context context = getApplicationContext();
                CharSequence text = name + " saved";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            }
        }
    }
}
