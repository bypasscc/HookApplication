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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class WelcomeActivity  extends XhookBaseActivity
{
    private WelcomeAdapter mAdapter;
    /* 数据段begin */
    private final String TAG = "server";

    private ServerSocketThread mServerSocketThread;
    /* 数据段end */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_welcome);

        mServerSocketThread = new ServerSocketThread();
        mServerSocketThread.start();

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
    /* 内部类begin */
    private class ServerSocketThread extends Thread
    {
        private boolean keepRunning = true;
        private LocalServerSocket serverSocket;

        private void stopRun()
        {
            keepRunning = false;
        }

        @Override
        public void run()
        {
            try
            {
                serverSocket = new LocalServerSocket("elf_local_socket");
            }
            catch (IOException e)
            {
                e.printStackTrace();

                keepRunning = false;
            }

            while(keepRunning)
            {
                Log.d(TAG, "wait for new client coming !");

                try
                {
                    LocalSocket interactClientSocket = serverSocket.accept();

                    //由于accept()在阻塞时，可能Activity已经finish掉了，所以再次检查keepRunning
                    if (keepRunning)
                    {
                        Log.d(TAG, "new client coming !");

                        new InteractClientSocketThread(interactClientSocket).start();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();

                    keepRunning = false;
                }
            }

            if (serverSocket != null)
            {
                try
                {
                    serverSocket.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class InteractClientSocketThread extends Thread
    {
        private LocalSocket interactClientSocket;

        public InteractClientSocketThread(LocalSocket interactClientSocket)
        {
            this.interactClientSocket = interactClientSocket;
        }

        @Override
        public void run()
        {
            StringBuilder recvStrBuilder = new StringBuilder();
            InputStream inputStream = null;
            try
            {
                inputStream = interactClientSocket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                char[] buf = new char[4096];
                int readBytes = -1;
                while ((readBytes = inputStreamReader.read(buf)) != -1)
                {
                    String tempStr = new String(buf, 0, readBytes);
                    recvStrBuilder.append(tempStr);
                    String recvString = recvStrBuilder.toString();
                    if(recvString.contains("NULL"))
                    {
                        Log.d(TAG,"FAILED");
                    }else if(recvString.contains("SUCCESS"))
                    {
                        Log.d(TAG,"SUCCESS");
                    }
                    Log.d(TAG,recvStrBuilder.toString());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();

                Log.d(TAG, "resolve data error !");
            }
            finally
            {
                if (inputStream != null)
                {
                    try
                    {
                        inputStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
