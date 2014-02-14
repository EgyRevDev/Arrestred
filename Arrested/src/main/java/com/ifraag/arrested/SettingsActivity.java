package com.ifraag.arrested;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preferences);
        }else{

            if (savedInstanceState == null) {

            /* Too important note; PreferenceFragment has been introduced in API 11 which equals to minimum SDK version
             * of my current project so I did not face any compilation error when using an instance of PreferenceFragment.
              * Otherwise I have got a compilation error since PreferenceFragment is not back-ported into support library v4.*/
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new SettingsFragment())
                        .commit();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_help) {

            Toast.makeText(this, "Help is pressed", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);

        /* Too important note; I did not handle R.id.home since I have already added in the Manifest file:
        * 1- parentActivityName attribute (starting from API 16)
        * 2- Meta data to support older devices. */
    }
}
