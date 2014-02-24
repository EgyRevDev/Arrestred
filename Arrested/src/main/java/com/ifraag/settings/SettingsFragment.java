package com.ifraag.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceGroup;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.ifraag.arrested.R;
import com.ifraag.arrested.SettingsActivity;

import java.util.List;

/* Note that PreferenceFragment class requires API level 11 but current minimum SDK is 7 so
 * I have to use unofficial android-support-v4-PreferenceFragment library project to be able to use Preference Fragment
 * on any Android API whose level is 4+ */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Be careful to call getActivity() only when the fragment is attached to an activity.
        * When the fragment is not yet attached, or was detached during the end of its lifecycle or device orientation changes,
        * getActivity() will return null.*/
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        if(null != actionBar) {
           actionBar.setDisplayHomeAsUpEnabled(true);

            /* Since all fragments of Sub-screens are displayed on same Settings Activity so you have to change Activity title according
            * to the given fragment. */
           actionBar.setTitle(R.string.title_activity_settings);
       }

        /*Load the preferences from an XML resource*/
        addPreferencesFromResource(R.xml.preferences);

        /* Obtain a reference to PreferenceCategory that collects different account types. */
        PreferenceGroup pg = (PreferenceGroup) getPreferenceManager().findPreference("pref_category_accounts");
        ((SettingsActivity)getActivity()).setPreferenceCategory(pg);

        /* Add any run-time created preferences to the preference group. */
        addPreferencesToPreferenceCategory(pg);

    }

    /* Method to add new created preference to the given preference group. This may occur during device orientation change or
    * app restoring after killing. */
    private void addPreferencesToPreferenceCategory( PreferenceGroup a_preferenceGroup){

        /* Obtain an instance for list of preferences that are created within SettingsActivity. */
        Context context = getActivity();
        List<CustomPreference> listOfPreferences = ((SettingsActivity) context).getPreferencesList();

        /* Add all preferences that are in list to the given preference group. */
        if(null != listOfPreferences) {
            for (CustomPreference preference : listOfPreferences)
                a_preferenceGroup.addPreference(preference);
        }
    }
}
