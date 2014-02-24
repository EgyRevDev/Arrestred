package com.ifraag.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifraag.arrested.R;
import com.ifraag.arrested.SettingsActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomListPreference extends ListPreference {

    private static final String TAG = "CustomListPreference";

    /* An instance for context within which custom preference is running.*/
    private Context mContext;

    /* An instance of text choices that appear on the dialog of custom preference. */
    private List<String> choicesList;

    /* An instance of icons that appear on the dialog of custom preference. */
    private List<Integer> drawablesResIDList;

    /* An instance of Android resources included within this application.*/
    private Resources resources ;

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        /* Initialize ArrayList of drawables IDs. */
        drawablesResIDList = new ArrayList<Integer>();

        /* Take a reference to resources and set choices/icons that appear on Dialog of custom preference. */
        resources = mContext.getResources();

        /* Set the internal Array List for string titles that will be displayed on the Dialog of Custom List Preference. */
        setChoicesArrayList();

        /* Set the internal Array List of Drawables that will be displayed on the Dialog of Custom List Preference. */
        setDrawableResIDArrList();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        /* Prepares the dialog builder to be shown when the custom list preference is clicked.*/
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
            public void onClick(DialogInterface dialogInterface, int index) {
                dialogInterface.dismiss();

                /* Here you add a preference for every new account type pressed in the Dialog
                * But if account type was already configured before, Just Toast warning message to the user .*/
                String title = myAdapter.getItem(index);

                if (null == findPreferenceInHierarchy(SettingsActivity.KEY_PREFIX_TITLE +index)) {

                    /* Prepare new custom preference to be added. Set title, key, icon and click listener. */
                    CustomPreference preference = new CustomPreference(mContext);
                    preference.setTitle(title);
                    preference.setKey(SettingsActivity.KEY_PREFIX_TITLE + index);
                    preference.setIcon(drawablesResIDList.get(index));

                    /* Note that without setting click listener, the preference will not respond to user clicks. */
                    preference.setOnPreferenceClickListener(preference.getOnPreferenceClickListener());

                    /* Get reference to parent preference category "Account Types" to add new preference in it. */
                    PreferenceGroup preferenceCategory = ((SettingsActivity) mContext).getPreferenceCategory();

                    /* Add new custom preference to preference category "Account Types" */
                    if (preferenceCategory != null) {
                        preferenceCategory.addPreference(preference);

                        /* Add preference to list of new preferences. It will be used in case Settings
                         * Activity is restarted due to device orientation ot app killing. */
                        ((SettingsActivity)mContext).getPreferencesList().add(preference);
                    }
                }else{/* Case pressed preference has a valid previous entry in parent Preference Category. */
                    String warnMsg = resources.getString(R.string.warn_msg_part_1)
                            +" " +title + " "
                            + resources.getString(R.string.warn_msg_part_2);

                    Toast.makeText(mContext,warnMsg,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class CustomArrayAdapter extends ArrayAdapter<String> {
        /* Custom Array Adapter for the list of items that are displayed on the Dialog of this custom list preference. */
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

    /* Convert array of strings into ArrayList. It is passed during Adapter instantiation. */
    private void setChoicesArrayList(){
        /* Get list of text entries that will be displayed on DialogPreference. */
        String choices [] = resources.getStringArray(R.array.pref_account_types_entries);
        choicesList = Arrays.asList(choices);
    }
}
