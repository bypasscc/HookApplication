package com.damotou.cyjhdev.hookapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import com.damotou.cyjhdev.hookapplication.util.NavUtil;
import com.damotou.cyjhdev.hookapplication.util.RootUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class WelcomeActivity  extends XhookBaseActivity
{
    private WelcomeAdapter mAdapter;
    /* 数据段begin */
    private final String TAG = "server";



    /* 数据段end */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_welcome);

        Intent intent = new Intent(WelcomeActivity.this, XHookInstallerActivity.class);
        intent.putExtra(XHookInstallerActivity.EXTRA_SECTION, 0);
        intent.putExtra(NavUtil.FINISH_ON_UP_NAVIGATION, true);
        startActivity(intent);

    }

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mAdapter) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    class WelcomeAdapter extends ArrayAdapter<WelcomeItem> {
        public WelcomeAdapter(Context context) {
            super(context, R.layout.list_item_welcome, android.R.id.text1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            WelcomeItem item = getItem(position);

            TextView description = (TextView) view.findViewById(android.R.id.text2);
            description.setText(item.description);

            boolean xposedActive = true;
            String frameworkUpdateVersion = null;
            boolean moduleUpdateAvailable = false;
            if (position == XHookInstallerActivity.TAB_INSTALL) {
                xposedActive = XhookApp.getActiveXposedVersion() >= InstallerFragment.getJarLatestVersion();
            }

            view.findViewById(R.id.txtXposedNotActive).setVisibility(!xposedActive ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.txtFrameworkUpdateAvailable).setVisibility(frameworkUpdateVersion != null ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.txtUpdateAvailable).setVisibility(moduleUpdateAvailable ? View.VISIBLE : View.GONE);

            return view;
        }
    }

    class WelcomeItem {
        public final String title;
        public final String description;

        protected WelcomeItem(int titleResId, int descriptionResId) {
            this.title = getString(titleResId);
            this.description = getString(descriptionResId);
        }

        @Override
        public String toString() {
            return title;
        }
    }

}
