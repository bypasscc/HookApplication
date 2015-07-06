package com.damotou.cyjhdev.hookapplication;

import android.app.Activity;
import android.os.Bundle;

import com.damotou.cyjhdev.hookapplication.util.NavUtil;
import com.damotou.cyjhdev.hookapplication.util.ThemeUtil;

/**
 * Created by cyjhdev on 15-7-6.
 */
public class XhookBaseActivity extends Activity {
    public boolean leftActivityWithSlideAnim = false;
    public int mTheme = -1;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        ThemeUtil.setTheme(this);
    }

    @Override
    protected void onResume() {

        super.onResume();
        ThemeUtil.reloadTheme(this);

        if (leftActivityWithSlideAnim)
            NavUtil.setTransitionSlideLeave(this);
        leftActivityWithSlideAnim = false;
    }

    public void setLeftWithSlideAnim(boolean newValue) {
        this.leftActivityWithSlideAnim = newValue;
    }
}
