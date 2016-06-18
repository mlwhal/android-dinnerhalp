package com.example.marika.dinnerhalp;

import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceFragment;

//import com.gmail.mlwhal.dinnerhalp.R;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Display SettingsFragment as main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //Load preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

    }
}
