package com.ifraag.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.ifraag.arrested.R;

/* An action provider replaces an action button with a customized layout.
It takes control of all the action's behaviors and an action provider can display a submenu when pressed*/
public class CustomActionProvider extends ActionProvider implements MenuItem.OnMenuItemClickListener {

    /* An instance of the activity context within which Custom Provider is running. */
    private Context mContext;

    public CustomActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        /* According to this post: http://goo.gl/BuPCFq
        * this method should return null so that onPrepareSubMenu can be called successfully to show the sub-menu items. */
        return null;
    }

    @Override
    public boolean hasSubMenu(){
        /* Since Like Action Bar button has 3 sub-menu items, let this method return true. */
        return true;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu){
        /* Clear any sub-menu items from the parent menu. */
        subMenu.clear();

        /* Add sub-menu item for No Military Trials, Front to Defend Egypt Protesters & Ifraag Facebook Page.
        * 1- add
        *   1.1- Group ID: id of the parent menu item for the new created sub-menu which is Like Action button in this case.
        *   1.2- Sub-Menu Item ID: This is a dynamic resource id that is created by the resources attribute <item type="id">
        *   1.3- Order: is not important, set it to None.
        *   1.4- Title String resource id that is defined in values.xml file.
        * 2- Set click listener that will be executed upon clicking on new sub-menu item.
        * 3- Set icon resource id for the new sub-menu item.*/
        subMenu.add(R.id.action_like, R.id.menu_id_no_mil_trials, Menu.NONE, mContext.getResources().getString(R.string.no_mil_trials))
                .setOnMenuItemClickListener(this)
                .setIcon(R.drawable.ic_logo_nomiltrials);

        subMenu.add(R.id.action_like, R.id.menu_id_fdep, Menu.NONE, mContext.getResources().getString(R.string.fdep))
                .setOnMenuItemClickListener(this)
                .setIcon(R.drawable.ic_logo_fdep);

        subMenu.add(R.id.action_like, R.id.menu_id_ifraag, Menu.NONE, mContext.getResources().getString(R.string.ifraag))
                .setOnMenuItemClickListener(this)
                .setIcon(R.drawable.ic_logo_ifraag);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item){
    /* Method to be invoked upon clicking on sub-menu item that is created by this Action Provider. */
        /* Get resource id of the pressed sub-menu item, set the corresponding intent to view the corresponding page
        * so that user can like it. Note that according to Facebook Policy you can't let the user likes a Facebook page
        * within your mobile application.*/
        int id = item.getItemId();
        String url ;
        switch (id){
            case R.id.menu_id_no_mil_trials:
                url = "https://www.facebook.com/NoMilTrials";
                break;
            case R.id.menu_id_fdep:
                url = "https://www.facebook.com/fdep.egypt";
                break;
            case R.id.menu_id_ifraag:
                url = "https://www.facebook.com/Ifraag";
                break;
            default:
                return false;
        }

        /* Action view for a given URL, will open an internet browser on user's device. */
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mContext.startActivity(intent);
        return true;
    }
}
