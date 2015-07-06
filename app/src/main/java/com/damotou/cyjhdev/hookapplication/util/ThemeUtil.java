package com.damotou.cyjhdev.hookapplication.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.damotou.cyjhdev.hookapplication.R;
import com.damotou.cyjhdev.hookapplication.XhookApp;
import com.damotou.cyjhdev.hookapplication.XhookBaseActivity;

/**
 * Created by cyjhdev on 15-7-6.
 */
public final class ThemeUtil {
    private ThemeUtil() {};

    private static int[] THEMES = new int[] {
            R.style.Theme_Light,
            R.style.Theme_Dark,
            R.style.Theme_Dark_Black,
    };

    public static int getSelectTheme() {
        int theme = XhookApp.getPreferences().getInt("theme", 0);
        return (theme >= 0 && theme < THEMES.length) ? theme : 0;
    }

    public static void setTheme(XhookBaseActivity activity) {
        activity.mTheme = getSelectTheme();
        activity.setTheme(THEMES[activity.mTheme]);
    }

    public static void reloadTheme(XhookBaseActivity activity) {
        int theme = getSelectTheme();
        if (theme != activity.mTheme)
            activity.recreate();
    }

    public static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[] {id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }
}

