package com.ifraag.arrested;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceGroup;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ifraag.settings.CustomPreference;
import com.ifraag.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SettingsActivity extends ActionBarActivity {

    /* String holds tha prefix for the Title's key of the preference that is created during run-time. Typically, the key prefix will be
    appended by a number*/
    public static final String KEY_PREFIX_TITLE = "pref_key_dynamic";

    /* String holds tha prefix for the Icon resource id key of the preference that is created during run-time. Typically, the key prefix will be
    appended by the Title's key*/
    private static final String KEY_PREFIX_ICON_RES_ID = "icon_resource_id";

    /* List holds new preferences that are created dynamically. */
    private List<CustomPreference> preferencesList;

    /* An instance of the preference group that holds dynamic preferences representing configured account types; facebook, twitter or GMail.*/
    private PreferenceGroup preferenceCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* Create new instance of dynamic-preferences List.
         * To keep the previously created preferences that will be used upon restarted of Settings Activity. */
        preferencesList = new ArrayList<CustomPreference>();

        /* Add Preference Fragment to the current Activity context. */
        /* Schedules a commit of this transaction.
         * The commit does not happen immediately; it will be scheduled as work on the main thread to be done
         * the next time that thread is ready. That's why you can't get a reference to preference category
         * after calling commit directly.*/
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();

        /* Check if Activity is created due to device orientation or app killing (first time of application running is
         * a special case of app killing).
         * In case Device orientation: given bundle is not null, Load preferences from Bundle in method: onRestoreInstanceState
         * In case Activity creation due to app killing/first time running: given bundle is null: You have to load preference from shared preferences
         * TODO: Is faster to load preferences from bundles or shared preferences? */
        if (null == savedInstanceState) {
            loadFromSharedPreference();
            /* Addition of preferences to the preference group will not applied here since commit transaction doesn't happen immediately.
            * But it will be applied inside Preference fragment to make sure that xml preferences has been loaded and initialized. */
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

        /* Check if Up button (Caret beside application logo) is pressed. It should move to Main Activity.*/
        if (id == android.R.id.home) {
            restartParentActivity();
        }

        if (id == R.id.action_help) {

            Toast.makeText(this, "Help TBD", Toast.LENGTH_SHORT).show();
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

            /* Note that preference key is saved implicitly since it is a bundle key.
            * Also preference title and icon resource id are saved into output bundle. All of these attributes
            * will be retrieved from bundle passed to method: onRestoreInstanceState*/
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

            /* Get the set of keys in the given bundle. Find preferences related-data keys (These keys that starts
            with special key prefix). Once a matched key is found,
            restore custom preference with same attributes: key, title , icon & click listener.
            Finally add restored custom preference to preference group and list of dynamic preferences. */

            for (String key : state.keySet()) {
                if (key.startsWith(KEY_PREFIX_TITLE)) {

                    /* Important note: If you use getApplicationContext instead of this, text color style of
                    * restored custom preferences will change from black color to white. So you have to use keyword this. */
                    CustomPreference preference = new CustomPreference(this);

                    /* Set preference key, title, icon and listener. */
                    preference.setKey(key);
                    preference.setTitle(state.getString(key));
                    preference.setIcon(state.getInt(KEY_PREFIX_ICON_RES_ID + preference.getKey()));
                    preference.setOnPreferenceClickListener(preference.getOnPreferenceClickListener());

                    /* Add preference to preference group and list of dynamic preferences. */
                    preferencesList.add(preference);
                    preferenceCategory.addPreference(preference);
                }
            }
        }
        super.onRestoreInstanceState(state);
    }

    private void saveToSharedPreferences() {
        /* Get reference to shared preferences file whose name is typical to your application package name.
        * It is shared among your application activities but not accessible to other application (private mode)*/
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        /* Get an instance of editor to the file of shared preferences. */
        SharedPreferences.Editor editor = sharedPreferences.edit();

        /* Save preference attributes; key, title, icon resource id into the shared preferences file. */
        for (CustomPreference pref : preferencesList) {
            editor.putString(pref.getKey(), pref.getTitle().toString());
            editor.putInt(KEY_PREFIX_ICON_RES_ID + pref.getKey(), pref.getIconResId());
        }

        /* Finally commit all changes that you have made to the shared preferences file. */
        editor.commit();
    }

    private void loadFromSharedPreference() {
        /* Get reference to shared preferences file whose name is typical to your application package name.
        * It is shared among your application activities but not accessible to other application (private mode)*/
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        /* Get an instance of editor to the file of shared preferences. */
        SharedPreferences.Editor editor = sharedPreferences.edit();

        /* To display all dynamic added preferences in the same order used during addition,
        * Sort the map collection according to used keys as explained here: http://goo.gl/nqN1cu
        * Otherwise, preferences are not always displayed in same order (of addition) as expected. */
        TreeMap<String, ?> sortedKeyValuePairs = new TreeMap<String, Object>(sharedPreferences.getAll());


        /* Get the set of keys in the given shared preference TreeMap.
        Find preferences related-data keys (These keys that starts with special key prefix).
        Once a matched key is found, restore custom preference with same attributes: key, title , icon & click listener.
        Finally add restored custom preference to preference group and list of dynamic preferences. */
        for (Map.Entry<String, ?> kv : sortedKeyValuePairs.entrySet()) {
            if (kv.getKey().startsWith(KEY_PREFIX_TITLE)) {

                /* Important note: If you use getApplicationContext instead of this, text color style of
                 * restored custom preferences will change from black color to white. So you have to use keyword this. */
                CustomPreference preference = new CustomPreference(this);

                /* Set preference key, title, icon and listener. */
                preference.setKey(new String(kv.getKey()));
                preference.setTitle(new String((String) kv.getValue()));
                preference.setIcon(sharedPreferences.getInt(KEY_PREFIX_ICON_RES_ID + kv.getKey(), -1));
                preference.setOnPreferenceClickListener(preference.getOnPreferenceClickListener());

                /* Add preference to preference group and list of dynamic preferences. */
                preferencesList.add(preference);

                /* Remove restored preference attributes from the shared preferences file. Because at run-time corresponding preference
                * that represents an account type, can be removed so it is meaningful to keep it inside shared preferences file.
                 * until user decides to remove this account type.*/
                /*editor.remove(kv.getKey());
                editor.remove(KEY_PREFIX_ICON_RES_ID + kv.getKey());*/

                /* Addition to preference group will not applied here since commit transaction doesn't happen immediately. */
            }
        }

        /* Commit all required changes. */
        editor.commit();
    }

    private void restartParentActivity() {

        /* Since onSaveInstanceState is not called if Back/Up button is pressed, you have to save dynamic preferences
        * in shared preferences explicitly. */
        saveToSharedPreferences();
        Intent intent = new Intent(this, MainActivity.class);

        /* With this flag, if the activity you're starting already exists in the current task,
         * then all activities on top of it are destroyed and it is brought to the front.
         * you should usually not create a new instance of the home activity. Otherwise,
         * you might end up with a long stack of activities in the current task with multiple
         * instances of the home activity.*/

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /* Getter for list of custom preferences. It will be referenced from:
        1- CustomListPreference upon adding new preference to the list.
        2- SettingsFragment to re-add preferences due to device orientation/app killing.
    **/
    public List<CustomPreference> getPreferencesList() {
        return preferencesList;
    }

    /* Setter for preference category that holds dynamically configured preferences. It will be invoked by SettingsFragment
    * after obtaining a reference to preference category overthere.*/
    public void setPreferenceCategory(PreferenceGroup preferenceCategory) {
        this.preferenceCategory = preferenceCategory;
    }

    /* Getter for preference category that hold dynamically configured preferences. It will be invoked by CustomListPreference
    * to add new chosen preferences to this preference category.*/
    public PreferenceGroup getPreferenceCategory() {
        return preferenceCategory;
    }
}
