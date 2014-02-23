package com.ifraag.arrested;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ifraag.settings.CustomPreference;
import com.ifraag.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SettingsActivity extends PreferenceActivity {

    public static final String KEY_PREFIX = "pref_key_dynamic";
    private static final String KEY_PREFIX_ICON_RES_ID = "icon_resource_id";

    /* List holds new preferences that are created dynamically. */
    private List<CustomPreference> preferencesList;

    private PreferenceGroup preferenceCategory;

    @SuppressWarnings("deprecation") /* To remove Lint warning for using deprecated method addPreferencesFromResource. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /* Create new instance of dynamic-preferences List, just in first time. Otherwise you needn't do so to keep
        * the previously created preferences that will be used upon restarted of Settings Activity. */
        preferencesList = new ArrayList<CustomPreference>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* Check running Android API version. If it is less than API Level 11, use deprecated methods to respective Settings */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preferences);
            preferenceCategory = (PreferenceGroup) getPreferenceScreen().findPreference("pref_category_accounts");
            /*preferenceCategory.setOrderingAsAdded(false);*/ /* TODO: Check which is better? */
        } else {
            PreferenceFragment pf = new SettingsFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.container,pf )
                    .commit();
                    /*Schedules a commit of this transaction.
                    The commit does not happen immediately; it will be scheduled as work on the main thread to be done
                    the next time that thread is ready. That's why you can't get a reference to preference category
                    after calling commit directly.*/
        }

        if (null == savedInstanceState) {
            loadFromSharedPreference();
            /* Addition to preference group will not applied here since commit transaction doesn't happen immediately. */
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

        /* Check if Up button (Caret beside application logo) is pressed. If Settings Activity contains a sub-screen fragment
        * then it should move to Parent Settings Activity. Otherwise, if Settings Activity contains Parent Settings Activity,
        * then it should move to Main Activity.
        * All of this must be handled in both cases pre/post Android API 11. */
        if (id == android.R.id.home) {
            restartParentActivity();
        }

        if (id == R.id.action_help) {

            Toast.makeText(this, "Help is pressed", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        /* It seems that either i still don't understand fragments well or there is a bug in Android so I have to workaround
        Back button pressed behaviour since, by above code if pressed Up button from Facebook sub-screen then
        back from settings screen, You will have another settings screen not Main Activity. */
        restartParentActivity();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /* In case of device rotation, preferences attributes should be saved into Bundles. */
        for (CustomPreference pref : preferencesList) {
            outState.putString(pref.getKey(), pref.getTitle().toString());
            outState.putInt(KEY_PREFIX_ICON_RES_ID + pref.getKey(), pref.getIconResId());
        }

        /* In case of killing application, preferences attributes should be saved permanently in shared preferences. */
        saveToSharedPreferences();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onRestoreInstanceState(Bundle state) {

        /* In case of device rotation, preference attributes are saved in bundles. */
        if (state != null) {
            for (String key : state.keySet()) {
                if (key.startsWith(KEY_PREFIX)) {
                    CustomPreference preference = new CustomPreference(this);
                    preference.setKey(key);
                    preference.setTitle(state.getString(key));
                    preference.setIcon(state.getInt(KEY_PREFIX_ICON_RES_ID + preference.getKey()));
                    preference.setOnPreferenceClickListener(preference.getOnPreferenceClickListener());
                    preferencesList.add(preference);
                    preferenceCategory.addPreference(preference);
                }
            }
        }
        super.onRestoreInstanceState(state);
    }

    private void saveToSharedPreferences(){
        String str = getPackageName();
        SharedPreferences sharedPreferences = getSharedPreferences(str,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (CustomPreference pref : preferencesList) {
            editor.putString(pref.getKey(), pref.getTitle().toString());
            editor.putInt(KEY_PREFIX_ICON_RES_ID + pref.getKey(), pref.getIconResId());
        }

        editor.commit();
    }

    private void loadFromSharedPreference(){
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        /* To display all dynamic added preferences in the same order used during addition,
        * Sort the map collection according to used keys as explained here:
        * http://goo.gl/nqN1cu Otherwise, preferences are not always displayed in same order as expected. */
        TreeMap<String, ?> sortedKeyValuePairs = new TreeMap<String, Object>(sharedPreferences.getAll());
        for (Map.Entry<String, ?> kv : sortedKeyValuePairs.entrySet()) {
            if (kv.getKey().startsWith(KEY_PREFIX)) {
                CustomPreference preference = new CustomPreference(this);
                preference.setKey(new String(kv.getKey()));
                preference.setTitle(new String((String) kv.getValue()));
                preference.setIcon(sharedPreferences.getInt(KEY_PREFIX_ICON_RES_ID + kv.getKey(), -1));
                preference.setOnPreferenceClickListener(preference.getOnPreferenceClickListener());
                preferencesList.add(preference);

                editor.remove(kv.getKey());
                editor.remove(KEY_PREFIX_ICON_RES_ID + kv.getKey());

                /* Addition to preference group will not applied here since commit transaction doesn't happen immediately. */
            }

        }
        /* Commit all required changes. */
        editor.commit();
    }

    private void restartParentActivity() {

        Intent intent = new Intent(this, MainActivity.class);

        /* With this flag, if the activity you're starting already exists in the current task,
         * then all activities on top of it are destroyed and it is brought to the front.
         * you should usually not create a new instance of the home activity. Otherwise,
         * you might end up with a long stack of activities in the current task with multiple
         * instances of the home activity.*/

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public List<CustomPreference> getPreferencesList() {
        return preferencesList;
    }

    public void setPreferenceCategory(PreferenceGroup preferenceCategory) {
        this.preferenceCategory = preferenceCategory;
    }

    public PreferenceGroup getPreferenceCategory() {
        return preferenceCategory;
    }
}
