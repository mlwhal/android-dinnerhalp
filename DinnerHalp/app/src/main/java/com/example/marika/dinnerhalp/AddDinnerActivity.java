package com.example.marika.dinnerhalp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
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

import java.io.FileNotFoundException;

public class AddDinnerActivity extends AppCompatActivity {

    private DinnersDbAdapter mDbHelper;
    private EditText mEditNameText;
    private Spinner mMethodSpinner;
    private Spinner mTimeSpinner;
    private Spinner mServingsSpinner;
    private ImageButton mSetPicPath;
    private ImageButton mChangePicPath;
    private ImageButton mRemovePicPath;
    private Uri mSelectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText mEditRecipe;
    //Tracker variable in case activity is getting content from another app
    private boolean mSharedContent = false;
    private String mNameText;
    private String mRecipeText;
    private Long mRowId;
    private int mImageScalePref;

    //TAG String used for logging
    private static final String TAG = AddDinnerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check SharedPreferences to determine what size images to display
        checkSharedPrefs();

        //Handle cases where this activity is getting content from another app
        Intent shareIntent = getIntent();
        String action = shareIntent.getAction();
        String type = shareIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                //go ahead and prepare to create a new dinner using incoming text as recipe
//                Log.d(TAG, "Text sent to app!");
//                Log.d(TAG, "mRowId is " + mRowId);
                mSharedContent = true;
//                Log.d(TAG, "Text sent is " + mRecipeText);
                //Show dialog to let user decide whether incoming text has a title to use as the
                //dinner name
                showShareDialog();
            }
        }

        mDbHelper = new DinnersDbAdapter(this);

        //setContentView(R.layout.activity_add_dinner_noscroll);
        setContentView(R.layout.activity_add_dinner);

        //EditText for dinner name
        mEditNameText = findViewById(R.id.edittext_name);
        mEditNameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        //Cooking method spinner
        mMethodSpinner = findViewById(R.id.spinner_method);
        ArrayAdapter<CharSequence> methodAdapter = ArrayAdapter.createFromResource(
                this, R.array.method_array, android.R.layout.simple_spinner_item);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMethodSpinner.setAdapter(methodAdapter);

        //Cooking time spinner
        mTimeSpinner = findViewById(R.id.spinner_time);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                this, R.array.time_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimeSpinner.setAdapter(timeAdapter);

        //Servings spinner
        mServingsSpinner = findViewById(R.id.spinner_servings);
        ArrayAdapter<CharSequence> servingsAdapter = ArrayAdapter.createFromResource(
                this, R.array.servings_array, android.R.layout.simple_spinner_item);
        servingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mServingsSpinner.setAdapter(servingsAdapter);

        //Initialize image button
        mSetPicPath = findViewById(R.id.button_add_image);
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

        //Initialize change image button but hide unless needed
        mChangePicPath = findViewById(R.id.button_change_image);
        mChangePicPath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setType("image/*");
                photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                photoPickerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Change picture"),
                        PICK_IMAGE_REQUEST);
            }
        });
        mChangePicPath.setVisibility(View.GONE);

        //Initialize remove image button but hide unless needed
        mRemovePicPath = findViewById(R.id.button_remove_image);
        mRemovePicPath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Create dialog to confirm the removal of the image
                showRemoveImgDialog();
            }
        });
        mRemovePicPath.setVisibility(View.GONE);

        //EditText for recipe
        mEditRecipe = findViewById(R.id.edittext_recipe);
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
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_cancel:
                finish();
                return true;

            case R.id.action_save:
                //Check whether name field is empty before allowing save
                String nameText = mEditNameText.getText().toString();
                if (nameText.matches("")) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.toast_no_dinner_name),
                            Toast.LENGTH_LONG).show();
                } else {
                    setResult(RESULT_OK);
                    saveDinner();
                }
                return true;

            case R.id.action_search:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("FRAGMENT_TRACKER", 0);
                this.startActivity(intent);
                return true;

            case R.id.action_manage:
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.putExtra("FRAGMENT_TRACKER", 1);
                this.startActivity(intent2);
                return true;

            case R.id.action_settings:
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
//                    Log.d(TAG, "Uri is " + mSelectedImageUri);

                    //Take a persistent permission for the image file so the app doesn't lose it later
                    int takeFlags = imageReturnedIntent.getFlags();
                    takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    //Check for the freshest data
                    getContentResolver().takePersistableUriPermission(mSelectedImageUri, takeFlags);

                    //Get preferred size for image
                    long imageSizePref = ImageHandler.getImageWidthPref(getApplicationContext(),
                            mImageScalePref);
                    //Show the image in the ImageView so the user knows this worked.
                    try {
                        Bitmap dinnerBitmap = ImageHandler.resizeImage(getApplicationContext(),
                                mSelectedImageUri, imageSizePref);
                        dinnerBitmap = ImageHandler.rotateImage(getApplicationContext(),
                                mSelectedImageUri, dinnerBitmap);
                        mSetPicPath.setImageBitmap(dinnerBitmap);
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
            TextView sectionLabel = findViewById(R.id.section_label);
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
            //If there is a picpath in the database, do a couple of things
            if (imageString != null) {

                //Display change and remove image buttons if there is an imageString
                mChangePicPath.setVisibility(View.VISIBLE);
                mRemovePicPath.setVisibility(View.VISIBLE);

                //Downsample bitmap and display
                Uri imageUri = Uri.parse(imageString);
//                Log.d(TAG, "Uri from db is " + imageUri);
                try {
                    long imageSizePref = ImageHandler.getImageWidthPref(getApplicationContext(),
                            mImageScalePref);
                    Bitmap dinnerBitmap = ImageHandler.resizeImage(getApplicationContext(),
                            imageUri, imageSizePref);
                    dinnerBitmap = ImageHandler.rotateImage(getApplicationContext(), imageUri,
                            dinnerBitmap);
                    mSetPicPath.setImageBitmap(dinnerBitmap);
                } catch (FileNotFoundException | SecurityException e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                    //Hide the add image button if the picPath is bad
                    mSetPicPath.setVisibility(View.GONE);
                }
                //Todo: Also, picpath is not being remembered consistently; becomes null unexpectedly
            } else {
                mChangePicPath.setVisibility(View.GONE);
                mRemovePicPath.setVisibility(View.GONE);
            }

            mEditRecipe.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbAdapter.KEY_RECIPE)));

            //Close up cursor
            stopManagingCursor(dinner);
            dinner.close();

        } else {
            //If mRowId is null, hide change/remove image buttons since they're not relevant
            mChangePicPath.setVisibility(View.GONE);
            mRemovePicPath.setVisibility(View.GONE);

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
//            Log.d(TAG, "Picpath will be " + picpath);
        } else {
            picpath = null;
        }

        String recipe = mEditRecipe.getText().toString();

        Log.d(TAG, "mRowId = " + mRowId);

        //Create dinner or update existing record depending on the value of mRowId
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
//                Log.d(TAG, "Dinner created");
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

    //Method to handle when user wants first line of shared text to become dinner name
    private void handleShareString(Intent shareIntent) {
        //Todo: shareString might be redundant with mRecipeText; or does scope matter?
        String shareString = shareIntent.getExtras().getString(Intent.EXTRA_TEXT);
        //Starting value for mRecipeText is the entire shareString
        mRecipeText = shareString;

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
//            Log.d(TAG, "Name text is " + mNameText);

            //Remove mNameText and line break from front of shareString to update mRecipeText.
            //This is inside the if statement so that replaceFirst() isn't attempted if
            //shareString is null.
            mRecipeText = mRecipeText.replaceFirst(mNameText + "\n", "");
//            Log.d(TAG, "Truncated recipe is " + mRecipeText);
        }

    }

    public void saveSuccessToast(CharSequence name) {

        Context context = getApplicationContext();
        CharSequence text = name + getResources().getString(R.string.toast_dinner_saved);
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

    //Method to check SharedPreferences to handle image size preference
    private void checkSharedPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //Check preference for displaying the dinner image
        String imageScalePrefString = sharedPref.getString(getResources()
                .getString(R.string.pref_image_size_key), "192");
        mImageScalePref = Integer.parseInt(imageScalePrefString);

    }

    //Class and methods for an alert dialog to let user decide whether shared text has a title
    public static class ShareDialogFragment extends DialogFragment {
        public static ShareDialogFragment newInstance(int title) {
            ShareDialogFragment frag = new ShareDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog (Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_share_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AddDinnerActivity)getActivity()).doPositiveShareClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.alert_dialog_share_no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AddDinnerActivity)getActivity()).doNegativeShareClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showShareDialog() {
        DialogFragment newFragment = ShareDialogFragment.newInstance(R.string.share_alert_title);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void doPositiveShareClick() {
        Intent shareIntent = getIntent();
        //User wants the first line of the shared text to go into the dinner name field
        handleShareString(shareIntent);
        //Refresh the fields to show updated name and recipe
        populateFields();
    }

    public void doNegativeShareClick() {
        //Grab shared text and put all of it into recipe field
        Intent shareIntent = getIntent();
        mRecipeText = shareIntent.getExtras().getString(Intent.EXTRA_TEXT);
        //Refresh the fields to show updated recipe
        populateFields();
    }

    //Class and methods for an alert dialog to let user remove an image from a dinner
    public static class RemoveImgDialogFragment extends DialogFragment {
        public static RemoveImgDialogFragment newInstance(int title, int message) {
            RemoveImgDialogFragment frag = new RemoveImgDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putInt("message", message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            int message = getArguments().getInt("message");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.alert_dialog_remove_img_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AddDinnerActivity) getActivity()).doPositiveImgClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.button_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AddDinnerActivity) getActivity()).doNegativeImgClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showRemoveImgDialog() {
        DialogFragment newFragment = RemoveImgDialogFragment.newInstance(R.string.image_remove_alert_title,
                R.string.image_remove_alert_message);
        newFragment.show(getFragmentManager(), "dialog");
    }

    //Remove path to image and reset image buttons
    void doPositiveImgClick() {
        mSelectedImageUri = null;
        Log.d(TAG, "mSelectedImageUri now null");
        Toast.makeText(getApplicationContext(), R.string.toast_image_removed, Toast.LENGTH_SHORT).show();
        mSetPicPath.setImageResource(R.drawable.ic_new_picture);
        mSetPicPath.setVisibility(View.VISIBLE);
        mChangePicPath.setVisibility(View.GONE);
        mRemovePicPath.setVisibility(View.GONE);
    }

    void doNegativeImgClick() {
        //Dialog dismisses and nothing is changed
    }
}
