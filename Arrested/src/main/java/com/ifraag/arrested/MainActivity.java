package com.ifraag.arrested;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ListFragment;
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
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
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
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /* Case Send action button is pressed. */
        if (id == R.id.action_send){
            Toast.makeText(this,"Send is Pressed",Toast.LENGTH_SHORT).show();


        }

        /* Case Settings button is pressed. */
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            /* Since Settings Activity has an intent filter for "preferences" scheme in AndroidManifest file, then you have to
            * set Uri before starting activity otherwise Settings Activity will not be started and an Exception is triggered. */
            intent.setData(Uri.parse("preferences://activity1"));
            startActivity(intent);
            return true;
        }

        /* Case Help button is pressed. */
        if (id == R.id.action_help){
            Toast.makeText(this, "Help is pressed", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
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
            Fragment f = null;
            switch (position){
                case 0:
                case 1:
                    // getItem is called to instantiate the fragment for the given page.
                    // Return a PlaceholderFragment (defined as a static inner class below).
                    f =  PlaceholderFragment.newInstance(position);
                    break;
                case 2:
                    f = EditTextFragment.newInstance(position);
                    break;
            }

            return f;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
                case 2:
                    return getString(R.string.title_section3);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private List<String> choicesArrayList;
        private List<Integer> drawablesResIDArryList;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 0:
                    rootView = inflater.inflate(R.layout.fragment_loc, container, false);
                    break;
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_authority, container, false);
                    break;
            }

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            String listOfChoices [] = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 0:
                    listOfChoices = getResources().getStringArray(R.array.location_strings);
                    drawablesResIDArryList = new ArrayList<Integer>();
                    drawablesResIDArryList.add(R.drawable.ic_demosntration);
                    drawablesResIDArryList.add(R.drawable.ic_street_v2 );
                    drawablesResIDArryList.add(R.drawable.ic_house_v2);
                    break;
                case 1:
                    listOfChoices = getResources().getStringArray(R.array.autority_strings);
                    drawablesResIDArryList = new ArrayList<Integer>();
                    drawablesResIDArryList.add(R.drawable.ic_police);
                    drawablesResIDArryList.add(R.drawable.ic_military);
                    drawablesResIDArryList.add(R.drawable.ic_bully);
                    break;
            }

            /* Make sure that the list of text choices is not null then convert the Array to an ArrayList*/
            assert listOfChoices != null;
            choicesArrayList =   Arrays.asList(listOfChoices);

            /* each item in this list view is not a simple item but instead it is a radio button i.e. single choice.
            * Note that it must be final because the object is used in inner class. */
            final MyArrayAdapter myAdapter = new MyArrayAdapter(
                    getActivity(),
                    R.layout.list_item_row,
                    R.id.text1,
                    choicesArrayList);

            setListAdapter( myAdapter);

            /* You have to track the check option yourself using my custom adapter. */
            this.getListView().setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    myAdapter.updateCheckedStates(position);
                }
            });

             /*Set default value of the list view item */
            /* The following line works fine when I have got only CheckedTextView in the row of the list view
             * but when I have custom list view, You need to keep the  */
            /*this.getListView().setItemChecked(0, true);*/
        }

        private class MyArrayAdapter extends ArrayAdapter<String>{

            /* Assuming that the default position to be set is first one while others are still disabled */
            private int indexOfCheckedRB = 0;

            public MyArrayAdapter(Context context, int resource,
                                  int textViewResourceId, List<String> objects) {
                super(context, resource, textViewResourceId, objects);
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = convertView;
                /*TODO: Chec this out instead of previous line*/
                /*row= super.getView(position, convertView, parent);*/

                Log.i("TAG_ARRESTED","Get View of Array Adapter is called again");

                if(row==null){
                    LayoutInflater inflater=getActivity().getLayoutInflater();
                    row=inflater.inflate(R.layout.list_item_row, parent, false);
                }

                assert row != null;
                /* Set the text beside the radio button and set its state whether it is checked or not. */
                CheckedTextView checkedTextView = (CheckedTextView)row.findViewById(R.id.text1);
                checkedTextView.setText(choicesArrayList.get(position));

                if(position == indexOfCheckedRB)
                    checkedTextView.setChecked(true);
                else
                    checkedTextView.setChecked(false);


                /* Set the corresponding image for each icon in the row of the list view. */
                ImageView icon = (ImageView) row.findViewById(R.id.icon);
                icon.setImageResource(drawablesResIDArryList.get(position));

                return row;
            }

            public void updateCheckedStates (int newPosition){
                indexOfCheckedRB  = newPosition;
                notifyDataSetChanged();
            }
        }
    }

    public static class EditTextFragment extends Fragment{

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static EditTextFragment newInstance(int sectionNumber) {
            Log.i("TAG_ARRESTED","Section number is " + sectionNumber);
            return  new EditTextFragment();
        }

        public EditTextFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return  inflater.inflate(R.layout.fragment_extra_info, container, false);

        }
    }
}

