package com.damotou.cyjhdev.hookapplication;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.SimpleAdapter;

import com.damotou.cyjhdev.hookapplication.util.NavUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cyjhdev on 15-7-6.
 */
public class XHookDropdownNavActivity extends XhookBaseActivity {
    public static final int TAB_INSTALL = 0;
    public static final int TAB_ABOUT = 1;
    public static final int TAB_COUNT = TAB_ABOUT + 1;

    protected int currentNavItem = -1;
    protected static List<Map<String, Object>> navigationItemList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);

        if (navigationItemList == null) {
            navigationItemList = new ArrayList<Map<String, Object>>();
            navigationItemList.add(makeNavigationItem(getString(R.string.tabInstall), InstallerFragment.class));
            navigationItemList.add(makeNavigationItem(getString(R.string.tabAbout), AboutFragment.class));
        }

        SimpleAdapter adapter = new SimpleAdapter(getActionBar().getThemedContext(),
                navigationItemList,
                android.R.layout.simple_spinner_dropdown_item,
                new String[] { "title" },
                new int[] { android.R.id.text1 });

        bar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                if (currentNavItem == itemPosition)
                    return true;

                if (navigateViaIntent()) {
                    Intent intent = new Intent(XHookDropdownNavActivity.this, XHookInstallerActivity.class);
                    intent.putExtra(XHookInstallerActivity.EXTRA_SECTION, itemPosition);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    return true;
                }

                Map<String, Object> map = navigationItemList.get(itemPosition);
                String fragmentClass = (String) map.get("fragment_class");
                Fragment fragment = Fragment.instantiate(XHookDropdownNavActivity.this, fragmentClass);

                FragmentTransaction tx = getFragmentManager().beginTransaction();
                tx.replace(android.R.id.content, fragment);
                currentNavItem = itemPosition;
                tx.commit();

                getFragmentManager().executePendingTransactions();

                return true;
            }
        });
    }

    private Map<String, Object> makeNavigationItem(String title, Class<? extends Fragment> fragmentClass) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", title);
        map.put("fragment_class", fragmentClass.getName());
        return map;
    }

    void setNavItem(int position) {
        this.currentNavItem = position;
        getActionBar().setSelectedNavigationItem(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!getIntent().getBooleanExtra(NavUtil.FINISH_ON_UP_NAVIGATION, false)) {
                Intent parentIntent = getParentIntent();
                parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(parentIntent);
            }
            finish();
            NavUtil.setTransitionSlideLeave(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean navigateViaIntent() {
        return false;
    }

    protected Intent getParentIntent() {
        return new Intent(this, WelcomeActivity.class);
    }
}
