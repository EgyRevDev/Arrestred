package com.ifraag.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifraag.arrested.R;
import com.ifraag.arrested.SettingsActivityFacebook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomPreference extends ListPreference {

    private static final String TAG = "CustomPreference";

    /* An instance for context within which custom preference is running.*/
    private Context mContext;

    /* An instance of text choices that appear on the dialog of custom preference. */
    private List<String> choicesList;

    /* An instance of icons that appear on the dialog of custom preference. */
    private List<Integer> drawablesResIDList;

    /* An instance of Android resources included within this application.*/
    private Resources resources ;

    /* An instance for click listener for any preference created dynamically*/
    private MyPreferenceClickListener preferenceClickListener;

    /* List holds new preferences that are created dynamically. */
    public static List<Preference> preferencesList ;

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        drawablesResIDList = new ArrayList<Integer>();

        /* Take a reference to resources and set choices/icons that appear on Dialog of custom preference. */
        resources = mContext.getResources();
        setChoicesArrayList();
        setDrawableResIDArrList();

        /* */
        preferenceClickListener = new MyPreferenceClickListener();

        /* Create new instance of dynamic-preferences List, just in first time. Otherwise you needn't do so to keep
        * the previously created preferences that will be used upon restarted of Settings Activity. */
        if (null == preferencesList)
            preferencesList = new ArrayList<Preference>();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        /* Default method, creates list of single items in the Dialog so you have to override this by
        * providing your own adapter. Check this post answer: http://goo.gl/blVv3V */

        /* Create an instance of custom adapter. */
         final CustomArrayAdapter myAdapter = new CustomArrayAdapter(
                mContext,
                R.layout.pref_list_item_row,
                R.id.text1,
                choicesList);

        builder.setAdapter(myAdapter,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                /* Here you add a preference for every new account type pressed in the Dialog
                * But if account type was already configured before, Just Toast warning message to the user .*/
                String str = myAdapter.getItem(i);
                String warnMsg = resources.getString(R.string.warn_msg_part_1)
                        +" " +str + " "
                        + resources.getString(R.string.warn_msg_part_2);
                Log.d(TAG,"Item pressed postion: "+ i + "\nItem text is: "+str);

                if (null == findPreferenceInHierarchy("pref"+i)) {

                    /* Prepare new preference to be added. */
                    Preference preference = new Preference(mContext);
                    preference.setTitle(str);
                    preference.setKey("pref" + i);
                    preference.setIcon(drawablesResIDList.get(i));
                    preference.setOnPreferenceClickListener(preferenceClickListener);

                    /* Get reference to parent preference category "Account Types" to add new preference in it. */
                    PreferenceGroup preferenceCategory =
                            (PreferenceGroup) findPreferenceInHierarchy("pref_category_accounts");
                    /*preferenceCategory.setOrderingAsAdded(false);*/ /* TODO: Check which is better? */

                    /* Add new created preference to preference category "Account Types" */
                    if (preferenceCategory != null) {
                        preferenceCategory.addPreference(preference);

                        /* Add preference to list of new preferences. It will be used in case Settings
                         * Activity is restarted. */
                        preferencesList.add(preference);
                    }
                }else /* Case pressed preference has a valid previous entry in parent Preference Category. */
                    Toast.makeText(mContext,warnMsg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class CustomArrayAdapter extends ArrayAdapter<String> {

        public CustomArrayAdapter(Context context, int resource,
                                  int textViewResourceId, List<String> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            /* Inflate a row entry in custom List. */
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.pref_list_item_row, parent, false);
            }

            if(row != null) {

                /* Set the text beside the radio button and set its state whether it is checked or not. */
                TextView txtView = (TextView) row.findViewById(R.id.text1);
                txtView.setText(choicesList.get(position));

                /* Set the corresponding image for each icon in the row of the list view. */
                ImageView icon = (ImageView) row.findViewById(R.id.icon);
                icon.setImageResource(drawablesResIDList.get(position));
            }

            return row;
        }
    }

    /* Set Drawable IDs of corresponding to icons of account types; facebook, twitter & gmail.*/
    private void setDrawableResIDArrList() {
        for(String txt: choicesList){
            if(txt.equals(resources.getString(R.string.pref_facebook_title))){
                drawablesResIDList.add(R.drawable.ic_facebook_logo);
            }else if(txt.equals(resources.getString(R.string.pref_twitter_title))){
                drawablesResIDList.add(R.drawable.ic_twitter_logo); /* TODO: Cant u find logo in Twitter API Lib?*/
            }else if(txt.equals(resources.getString(R.string.pref_gmail_title))){
                drawablesResIDList.add(R.drawable.gmail_logo); /*TODO: Cant u find logo in Android?*/
            }
        }
    }

    /* Convert array of strings into ArrayList. It is passed to Adapter creation. */
    private void setChoicesArrayList(){
        /* Get list of text entries that will be displayed on DialogPreference. */
        String choices [] = resources.getStringArray(R.array.pref_account_types_entries);
        choicesList = Arrays.asList(choices);
    }

    /* Generic class to implement Preference Listener*/
    private class MyPreferenceClickListener implements OnPreferenceClickListener{

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent;

            /* Check what is the title of the give preference and start the corresponding activity.*/
            if (preference.getTitle().equals(resources.getString(R.string.pref_facebook_title))){
                intent = new Intent(mContext, SettingsActivityFacebook.class);
                intent.setAction("com.ifraag.arrested.action.VIEW");
                preference.setIntent(intent);
                mContext.startActivity(intent);
            }else if(preference.getTitle().equals(resources.getString(R.string.pref_twitter_title))){

            }else if (preference.getTitle().equals(resources.getString(R.string.pref_gmail_title))){

            }


            return false;
        }
    }


}
