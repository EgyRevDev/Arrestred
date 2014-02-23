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
 * I have to add target API annotation telling Lint that I am sure that I will use this class only when API level greater than or equal to 11*/
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Since it can't be called from PreferenceActivity class.
        * Be careful to call getActivity() only when the fragment is attached to an activity.
        * When the fragment is not yet attached, or was detached during the end of its lifecycle, getActivity() will return null.*/
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        if(null != actionBar) {
           actionBar.setDisplayHomeAsUpEnabled(true);

        /* Since all fragments of Sub-screens are displayed on same Settings Activity so you have to change Activity title according
        * to the given fragment. */
           actionBar.setTitle(R.string.title_activity_settings);
       }
        /*Load the preferences from an XML resource*/
        addPreferencesFromResource(R.xml.preferences);

        PreferenceGroup pg = (PreferenceGroup) getPreferenceManager().findPreference("pref_category_accounts");
        ((SettingsActivity)getActivity()).setPreferenceCategory(pg);

        addPreferencesToPreferenceCategory(pg);

    }

    private void addPreferencesToPreferenceCategory( PreferenceGroup a_preferenceGroup){
        Context context = getActivity();
        List<CustomPreference> listOfPreferences = ((SettingsActivity) context).getPreferencesList();

        for (CustomPreference preference: listOfPreferences)
            a_preferenceGroup.addPreference(preference);
    }
}
