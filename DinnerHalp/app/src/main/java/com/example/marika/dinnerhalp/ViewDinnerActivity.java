package com.example.marika.dinnerhalp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
//import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class ViewDinnerActivity extends AppCompatActivity {

    private DinnersDbAdapter mDbHelper;

    //Data members for various text and image views
    private TextView mTitleText;
    private TextView mMethodText;
    private TextView mTimeText;
    private TextView mServingsText;
    private ImageView mDinnerImage;
    private TextView mRecipeText;
    private Long mRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_dinner);

        mDbHelper = new DinnersDbAdapter(this);

        mTitleText = (TextView) findViewById(R.id.section_label);
        mMethodText = (TextView) findViewById(R.id.textview_method);
        mTimeText = (TextView) findViewById(R.id.textview_time);
        mServingsText = (TextView) findViewById(R.id.textview_servings);
        mDinnerImage = (ImageView) findViewById(R.id.image_dinner_thumb);
        mRecipeText = (TextView) findViewById(R.id.textview_recipe);

        mRowId =
                (savedInstanceState == null) ?
                        null :
                        (Long) savedInstanceState.getSerializable(DinnersDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(DinnersDbAdapter.KEY_ROWID)
                    : null;
        }

        populateDinnerText();
        Log.d(ViewDinnerActivity.class.getSimpleName(), "RowID onCreate is " + mRowId);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_dinner, menu);

        /** Get the actionprovider associated with the menu item whose id is share */
        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getShareIntent());

//        return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Switch for different action bar item clicks
        switch (id) {

            case R.id.action_edit:
                Intent intent1 = new Intent(this, AddDinnerActivity.class);
                intent1.putExtra(DinnersDbAdapter.KEY_ROWID, mRowId);
                startActivity(intent1);
                finish();
                return true;

            case R.id.action_delete:
                showDeleteDialog();
                return true;

            case R.id.action_share:
                //Doesn't need anything because clicks are handled by ShareActionProvider
                return true;

            case R.id.action_search:
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.putExtra("FRAGMENT_TRACKER", 0);
                this.startActivity(intent2);
                return true;

            case R.id.action_manage:
                Intent intent3 = new Intent(this, MainActivity.class);
                intent3.putExtra("FRAGMENT_TRACKER", 1);
                this.startActivity(intent3);
                return true;

            case R.id.action_about:
                Intent intent4 = new Intent(this, AboutAppActivity.class);
                this.startActivity(intent4);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void populateDinnerText() {

        if (mRowId != null) {
            mDbHelper.open();
            Cursor dinner = mDbHelper.fetchDinner(mRowId);
            mDbHelper.close();
            startManagingCursor(dinner);

            mTitleText.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbAdapter.KEY_NAME)));

            mMethodText.setText(dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbAdapter.KEY_METHOD)));

            mTimeText.setText(dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbAdapter.KEY_TIME)));

            mServingsText.setText(dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbAdapter.KEY_SERVINGS)));

            //Show ImageView only if there is a uri value in the database
            String picPath = dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbAdapter.KEY_PICPATH));
            Log.d(ViewDinnerActivity.class.getSimpleName(), "picPath value is " + picPath);
            if (picPath == null || picPath.equalsIgnoreCase("")) {
                mDinnerImage.setVisibility(View.GONE);
            } else {
                //Turn picPath into Uri and put downsampled bitmap in ImageView
                Uri picUri = Uri.parse(picPath);
                try {
                    mDinnerImage.setImageBitmap(decodeUri(picUri, 192));
                } catch (FileNotFoundException e) {
                    Log.d(ViewDinnerActivity.class.getSimpleName(), Log.getStackTraceString(e));
                }
            }

            mRecipeText.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbAdapter.KEY_RECIPE)));
            stopManagingCursor(dinner);
            dinner.close();
        }
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

    //Method to build an intent to share dinner names/recipes
    private Intent getShareIntent() {
        String dinnerTitle = mTitleText.getText().toString();
        String dinnerRecipe = mRecipeText.getText().toString();

        //Get title for shareIntent; ShareActionProvider doesn't use this
//        CharSequence shareTitle = getResources().getString(R.string.intent_share_recipe);

        //Set up shareIntent and put dinner title and recipe in the intent as extras
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
//        shareIntent.setType("message/rfc822");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, dinnerTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, dinnerRecipe);

        return shareIntent;

    }

    public static class DeleteDialogFragment extends DialogFragment {

        public static DeleteDialogFragment newInstance(int title) {
            DeleteDialogFragment frag = new DeleteDialogFragment();
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
                    .setPositiveButton(R.string.alert_dialog_delete_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Log.d(ViewDinnerActivity.class.getSimpleName(), "Delete button clicked!");
                                    ((ViewDinnerActivity)getActivity()).doPositiveClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.button_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((ViewDinnerActivity)getActivity()).doNegativeClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showDeleteDialog() {
        DialogFragment newFragment = DeleteDialogFragment.newInstance(
                R.string.delete_alert_title);
        newFragment.show(getFragmentManager(), "dialog");

    }

    public void doPositiveClick() {

        //deleteDinner() will return true if successful
        mDbHelper.open();
        boolean deleteSuccess = mDbHelper.deleteDinner(mRowId);
        mDbHelper.close();
        if (deleteSuccess) {
            Context context = getApplicationContext();
            CharSequence text = mTitleText.getText() + " deleted";
            int duration = Toast.LENGTH_LONG;
            Toast.makeText(context, text, duration).show();

            Intent intent = new Intent(this, DinnerListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void doNegativeClick() {
        //Todo: Anything needed here? or just dismiss dialog within the click listener?
    }
}
