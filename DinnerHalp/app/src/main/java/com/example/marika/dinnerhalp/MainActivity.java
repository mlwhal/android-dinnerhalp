package com.example.marika.dinnerhalp;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

        //Todo: When launching other activities, track current fragment so that back/cancel
    //returns you to last fragment. Currently always returns to SearchFragment.

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    //Track which tab to show when navigating from other activities
    public int mFragmentTracker = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Todo: Get info from extras to determine which tab to show
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mFragmentTracker = 0;
            } else {
                mFragmentTracker = extras.getInt("FRAGMENT_TRACKER");
            }
        } else {
            mFragmentTracker = savedInstanceState.getInt("FRAGMENT_TRACKER");
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.action_add_dinner:
                Intent intent = new Intent(this, AddDinnerActivity.class);
                //intent.putExtra("FRAGMENT_TRACKER", 1);
                //Log.d(MainActivity.class.getSimpleName(), "fragTracker is " + mFragmentTracker);
                Log.d(MainActivity.class.getSimpleName(), "getSelNavIndx is " + getSupportActionBar().getSelectedNavigationIndex());
                this.startActivity(intent);
                return true;

            case R.id.action_about:
                Intent intent2 = new Intent(this, AboutAppActivity.class);
                this.startActivity(intent2);
                return true;

            default:
                return super.onOptionsItemSelected(item);

            }

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        mFragmentTracker = tab.getPosition();
        Log.d(MainActivity.class.getSimpleName(), "Tab position is " + tab.getPosition());
        Log.d(MainActivity.class.getSimpleName(), "mFragmentTracker is " + mFragmentTracker);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return either SearchFragment or ManageDBFragment (defined as a static inner class below).
            if (position == 0) {
                return SearchFragment.newInstance(position + 1);
            } else {
                return ManageDBFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    //Todo: Add an onSaveInstanceState() method to save currently selected tab?
    @Override
    public void onSaveInstanceState(Bundle outstate) {
        super.onSaveInstanceState(outstate);

        outstate.putInt("FRAGMENT_TRACKER", mFragmentTracker);

    }

    //Method to track which tab to load when navigating from other activities
    @Override
    public void onResume() {
        super.onResume();

        //Todo: This next line doesn't seem to be working as hoped.
        mViewPager.setCurrentItem(mFragmentTracker);
    }


    /**
     * A fragment containing the search screen view.
     */
    public static class SearchFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        //Custom click listener for ListView items
        public static class SearchOnItemClickListener implements AdapterView.OnItemClickListener {

            public SearchOnItemClickListener(MainActivity activity, SearchFragment frag) {
                theActivity = activity;
                theFragment = frag;
            }

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                Log.d(MainActivity.class.getSimpleName(), "Position clicked " + position);
                switch (position) {
                    case 0:
                        theActivity.confirmKeyword();
                        break;

                    case 1:
                        theActivity.confirmMethod();
                        break;

                    case 2:
                        theActivity.confirmTime();
                        break;

                    case 3:
                        theActivity.confirmServings();
                        break;

                    case 4:
                        Intent intent2 = new Intent(theActivity, DinnerListActivity.class);
                        theFragment.startActivity(intent2);
                        break;
                }

            }

            private MainActivity theActivity;
            private SearchFragment theFragment;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SearchFragment newInstance(int sectionNumber) {
            SearchFragment fragment = new SearchFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SearchFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);

            //Create ListView and add ArrayAdapter to display search options
            final ListView listView = (ListView)rootView.findViewById(R.id.list);

            //Get list item names and image ids from array resources
            Resources res = getResources();
            String[] itemName = res.getStringArray(R.array.search_array);
            TypedArray imageResArray = res.obtainTypedArray(R.array.search_icon_array);
            int lgth = imageResArray.length();
            Integer[] imageId = new Integer[lgth];
            for (int i = 0; i < lgth; i++) {
                imageId[i] = imageResArray.getResourceId(i, 0);
            }
            imageResArray.recycle();

            //Create custom adapter and pass in context, image, and name info
            CustomListAdapter adapter = new CustomListAdapter(this.getActivity(), itemName,
                    imageId);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(
                    new SearchOnItemClickListener((MainActivity)getActivity(), this));
            return rootView;
        }

    }

    public static class KeywordDialogFragment extends DialogFragment {
        public static KeywordDialogFragment newInstance(int title) {
            KeywordDialogFragment frag = new KeywordDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            //Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            //Cast the Dialog as a View so I can get text out of the EditText
            //http://stackoverflow.com/questions/12799751/android-how-do-i-retrieve-edittext-gettext-in-custom-alertdialog
            //Inflate and set the layout
            //Pass null as the parent view because it's going in the dialog layout
            final View dialogView = inflater.inflate(R.layout.dialog_keywordsearch, null);
            builder.setTitle(title)
                    .setView(dialogView)
                    //Add action buttons
                    .setPositiveButton(R.string.button_search,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    //Pull user input from EditText to construct db query
                                    EditText keywordEditText = (EditText) dialogView.findViewById(
                                            R.id.dialog_edittext_keyword);
                                    String keywordInput = keywordEditText.getText().toString();
                                    String whereClause = "name LIKE ? OR recipe LIKE ?";
                                    Log.d(MainActivity.class.getSimpleName(), "Search column is " +
                                            whereClause);
                                    Log.d(MainActivity.class.getSimpleName(), "Search string is " +
                                            "%" + keywordInput + "%");
                                    Intent intent = new Intent(getActivity(), DinnerListActivity.class);
                                    /*Flag this as a keyword search so that two WhereArgs are used
                                    * when the database is searched
                                    * */
                                    intent.putExtra("KEYWORD_SEARCH", true);
                                    intent.putExtra("SEARCH_COLUMN", whereClause);
                                    intent.putExtra("SEARCH_STRING", "%" + keywordInput + "%");
                                    startActivity(intent);

                                }

                            })
                    .setNegativeButton(R.string.button_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    KeywordDialogFragment.this.getDialog().cancel();
                                }
                            });
            return builder.create();
        }
    }

    //Method to invoke keyword search dialog fragment; called in click listener
    void confirmKeyword() {
        DialogFragment newFragment = KeywordDialogFragment.newInstance(R.string.keyword_alert_title);
        newFragment.show(getFragmentManager(), "keywordConfirm");
    }


    //Custom class for cooking method Alert Dialog
    public static class CookMethodDialogFragment extends DialogFragment {
        public static CookMethodDialogFragment newInstance(int title) {
            CookMethodDialogFragment frag = new CookMethodDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            //Create a string array to hold method values; these will be the db query terms.
            final String[] methods = getResources().getStringArray(R.array.method_array);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title)
                   .setItems(R.array.method_array, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int position) {
                           Log.d(MainActivity.class.getSimpleName(), "Position clicked " + position);
                           //Query DB for selected method and display a list of dinners
                           Intent intent = new Intent(getActivity(), DinnerListActivity.class);
                           intent.putExtra("SEARCH_COLUMN", "method LIKE ?");
                           intent.putExtra("SEARCH_STRING", methods[position]);
                           //intent.putExtra("SEARCH_STRING", "Oven");
                           startActivity(intent);
                       }

                   });
            return builder.create();
        }
    }

    //Method to invoke cooking method dialog fragment; called in click listener
    void confirmMethod() {
        DialogFragment newFragment = CookMethodDialogFragment.newInstance(R.string.method_alert_title);
        newFragment.show(getFragmentManager(), "methodConfirm");
    }

    //Custom class for cooking time Alert Dialog
    public static class CookTimeDialogFragment extends DialogFragment {
        public static CookTimeDialogFragment newInstance(int title) {
            CookTimeDialogFragment frag = new CookTimeDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            //Create a string array to hold time values; these will be the db query terms.
            final String[] times = getResources().getStringArray(R.array.time_array);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title)
                    .setItems(R.array.time_array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            Log.d(MainActivity.class.getSimpleName(), "Position clicked " + position);
                            Log.d(MainActivity.class.getSimpleName(), "Item clicked " +
                                    times[position]);
                            //Query DB for selected cook time and display a list of dinners
                            //Also pass in "time" as column to search
                            Intent intent = new Intent(getActivity(), DinnerListActivity.class);
                            intent.putExtra("SEARCH_COLUMN", "time LIKE ?");
                            intent.putExtra("SEARCH_STRING", times[position]);
                            startActivity(intent);
                        }

                    });
            return builder.create();
        }
    }

    //Method to invoke cooking time dialog fragment; called in click listener
    void confirmTime() {
        DialogFragment newFragment = CookTimeDialogFragment.newInstance(R.string.time_alert_title);
        newFragment.show(getFragmentManager(), "timeConfirm");
    }

    //Custom class for servings Alert Dialog
    public static class ServingsDialogFragment extends DialogFragment {
        public static ServingsDialogFragment newInstance(int title) {
            ServingsDialogFragment frag = new ServingsDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            //Create a string array to hold serving values; these will be the db query terms.
            final String[] servings = getResources().getStringArray(R.array.servings_array);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title)
                   .setItems(R.array.servings_array, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int position) {
                           Log.d(TAG, "Position clicked " + position);
                           //Query DB for selected no. of servings and display a list of dinners
                           Intent intent = new Intent(getActivity(), DinnerListActivity.class);
                           intent.putExtra("SEARCH_COLUMN", "servings LIKE ?");
                           intent.putExtra("SEARCH_STRING", servings[position]);
                           startActivity(intent);
                       }
                   });
            return builder.create();
        }

        private static final String TAG = "ServingsDialogFrag";
    }

    //Method to invoke servings dialog fragment; called in click listener
    void confirmServings() {
        DialogFragment newFragment = ServingsDialogFragment.newInstance(R.string.servings_alert_title);
        newFragment.show(getFragmentManager(), "servingsConfirm");
        Log.d(MainActivity.class.getSimpleName(), "Servings clicked!");
    }

    /**
     * A fragment containing the manage DB screen view.
     */
    public static class ManageDBFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static class ManageOnItemClickListener implements AdapterView.OnItemClickListener {

            //Todo: If I end up with one ListView, then the listName string is unneeded.
            ManageOnItemClickListener(MainActivity activity,
                                      ManageDBFragment frag,
                                      String listName) {
                theActivity = activity;
                theFragment = frag;
                mListName = listName;
            }

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                Log.d(MainActivity.class.getSimpleName(), "Position clicked " + position);
                Log.d(MainActivity.class.getSimpleName(), "mListName = " + mListName);
                if (mListName.equalsIgnoreCase("MANAGE_DINNERS")) {
                    switch (position) {
                        //Add dinner
                        case 0:
                            Intent intent1 = new Intent(theActivity, AddDinnerActivity.class);
                            //Let next activity know we came from ManageFragment
                            //intent1.putExtra("FRAGMENT_TRACKER", 1);
                            theFragment.startActivity(intent1);
                            break;
                        //Get dinner list in order to edit or delete
                        case 1:
                            Intent intent2 = new Intent(theActivity, DinnerListActivity.class);
                            theFragment.startActivity(intent2);
                            break;
                        //Delete all records in database
                        case 2:
                            theActivity.showDeleteDialog();
                            Log.d(MainActivity.class.getSimpleName(), "Manage db position 0 clicked");
                            Log.d(MainActivity.class.getSimpleName(), "mListName = " + mListName);
                            break;
                    }
                } else {
                    switch (position) {
                        //Add dinner
                        case 0:
                            //delete database file, or whatever option 1 is
                            theActivity.showDeleteDialog();
                            Log.d(MainActivity.class.getSimpleName(), "Manage db position 0 clicked");
                            Log.d(MainActivity.class.getSimpleName(), "mListName = " + mListName);
                            break;
                    }
                }

            }
            private MainActivity theActivity;
            private ManageDBFragment theFragment;
            private String mListName;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ManageDBFragment newInstance(int sectionNumber) {
            ManageDBFragment fragment = new ManageDBFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ManageDBFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_manage, container, false);

            //Create ListViews and add CustomListAdapters to display manage dinner and DB options
            //First is the Manage dinners listView
            ListView listView1 = (ListView)rootView.findViewById(R.id.list);
//            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
//                    R.array.manage_dinners_array, android.R.layout.simple_list_item_1);

            Resources res = getResources();
            String[] itemName = res.getStringArray(R.array.manage_dinners_array);
            TypedArray imageResArray = res.obtainTypedArray(R.array.manage_dinner_icon_array);
            int lgth = imageResArray.length();
            Integer[] imageId = new Integer[lgth];
            for (int i = 0; i < lgth; i++) {
                imageId[i] = imageResArray.getResourceId(i, 0);
            }
            imageResArray.recycle();

            //Create custom adapter and pass in context, image, and name info
            CustomListAdapter adapter1 = new CustomListAdapter(this.getActivity(), itemName,
                    imageId);

            listView1.setAdapter(adapter1);
            listView1.setOnItemClickListener(
                    new ManageOnItemClickListener((MainActivity) getActivity(),
                            this, "MANAGE_DINNERS"));

            //Second is the manage database listView
//            ListView listView2 = (ListView)rootView.findViewById(R.id.list2);
//            itemName = res.getStringArray(R.array.manage_db_array);
//            imageResArray = res.obtainTypedArray(R.array.manage_db_icon_array);
//            lgth = imageResArray.length();
//            Integer[] dbImageId = new Integer[lgth];
//            for (int i = 0; i < lgth; i++) {
//                dbImageId[i] = imageResArray.getResourceId(i, 0);
//            }
//            imageResArray.recycle();
//
//            CustomListAdapter adapter2 = new CustomListAdapter(this.getActivity(), itemName,
//                    dbImageId);
//
//            listView2.setAdapter(adapter2);
//            listView2.setOnItemClickListener(
//                    new ManageOnItemClickListener((MainActivity) getActivity(),
//                            this, "MANAGE_DB"));

            return rootView;
        }

    }

    //Dialog fragment to handle confirming that the user wants to delete the DB file
    public static class DeleteDBDialogFragment extends DialogFragment {

        public static DeleteDBDialogFragment newInstance(int title) {
            DeleteDBDialogFragment frag = new DeleteDBDialogFragment();
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
                                    ((MainActivity)getActivity()).doPositiveClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.button_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((MainActivity)getActivity()).doNegativeClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showDeleteDialog() {
        DialogFragment newFragment = DeleteDBDialogFragment.newInstance(
                R.string.delete_db_alert_title);
        newFragment.show(getFragmentManager(), "dialog");

    }

    public void doPositiveClick() {
        //Open a database object and delete all rows
        Log.d(MainActivity.class.getSimpleName(), "Positive button clicked");
        DinnersDbAdapter mDbHelper;
        mDbHelper = new DinnersDbAdapter(this);
        mDbHelper.open();

        //Method clearAllDinners() returns an int giving the number of rows deleted
        int rowsDeleted = mDbHelper.clearAllDinners();
        if (rowsDeleted == 0) {
            Toast.makeText(getApplicationContext(), "Nothing was deleted",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "All " + rowsDeleted + " dinners deleted",
                    Toast.LENGTH_LONG).show();
        }
        mDbHelper.close();

    }

    public void doNegativeClick() {
        //Go back to manage fragment
        Log.d(MainActivity.class.getSimpleName(), "Cancel button clicked");
        Toast.makeText(getApplicationContext(), "Nothing was deleted",
                Toast.LENGTH_LONG).show();
    }

}
