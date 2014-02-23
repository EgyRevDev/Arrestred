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


public class CustomActionProvider extends ActionProvider implements MenuItem.OnMenuItemClickListener {

    private Context mContext;

    public CustomActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public boolean hasSubMenu(){
        return true;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu){
        subMenu.clear();

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

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mContext.startActivity(intent);
        return true;
    }
}
