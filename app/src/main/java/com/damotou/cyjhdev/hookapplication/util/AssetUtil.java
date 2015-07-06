package com.damotou.cyjhdev.hookapplication.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;

import com.damotou.cyjhdev.hookapplication.XhookApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cyjhdev on 15-7-6.
 */
public class AssetUtil {
    public static final File BUSYBOX_FILE = new File(XhookApp.getInstance().getCacheDir(), "busybox-xposed");
    public static final String STATIC_BUSYBOX_PACKAGE = "de.robv.android.xposed.installer.staticbusybox";
    private static final int STATIC_BUSYBOX_REQUIRED_VERSION = 1;
    private static PackageInfo mStaticBusyboxInfo = null;

    public static String getBinariesFolder() {
        if (Build.CPU_ABI.startsWith("armeabi")) {
            return "arm/";
        } else if (Build.CPU_ABI.startsWith("x86")) {
            return "x86/";
        } else {
            return null;
        }
    }

    public static File writeAssetToCacheFile(String name, int mode) {
        return writeAssetToCacheFile(name, name, mode);
    }

    public static File writeAssetToCacheFile(String assetName, String fileName, int mode) {
        return writeAssetToFile(assetName, new File(XhookApp.getInstance().getCacheDir(), fileName), mode);
    }

    public static File writeAssetToSdcardFile(Context context,String name, int mode) {
        return writeAssetToSdcardFile(context,name, name, mode);
    }

    public static File writeAssetToSdcardFile(Context context,String assetName, String fileName, int mode) {
        File dir = context.getCacheDir();
        return writeAssetToFile(assetName, new File(dir, fileName), mode);
    }

    public static File writeAssetToFile(String assetName, File targetFile, int mode) {
        return writeAssetToFile(null, assetName, targetFile, mode);
    }

    public static File writeAssetToFile(AssetManager assets, String assetName, File targetFile, int mode) {
        try {
            if (assets == null)
                assets = XhookApp.getInstance().getAssets();
            InputStream in = assets.open(assetName);
            FileOutputStream out = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0){
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();

            FileUtils.setPermissions(targetFile.getAbsolutePath(), mode, -1, -1);

            return targetFile;
        } catch (IOException e) {
            Log.e(XhookApp.TAG, "could not extract asset", e);
            if (targetFile != null)
                targetFile.delete();

            return null;
        }
    }

    public synchronized static void extractBusybox() {
        if (BUSYBOX_FILE.exists())
            return;

        AssetManager assets = null;
        if (isStaticBusyboxAvailable()) {
            try {
                PackageManager pm = XhookApp.getInstance().getPackageManager();
                assets = pm.getResourcesForApplication(mStaticBusyboxInfo.applicationInfo).getAssets();
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(XhookApp.TAG, "could not load assets from " + STATIC_BUSYBOX_PACKAGE, e);
            }
        }

        writeAssetToFile(assets, getBinariesFolder() + "busybox-xposed", BUSYBOX_FILE, 00700);
    }

    public synchronized static void removeBusybox() {
        BUSYBOX_FILE.delete();
    }

    public synchronized static void checkStaticBusyboxAvailability() {
        boolean wasAvailable = isStaticBusyboxAvailable();
        mStaticBusyboxInfo = null;

        PackageManager pm = XhookApp.getInstance().getPackageManager();
        try {
            mStaticBusyboxInfo = pm.getPackageInfo(STATIC_BUSYBOX_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        String myPackageName = ModuleUtil.getInstance().getFrameworkPackageName();
        if (pm.checkSignatures(STATIC_BUSYBOX_PACKAGE, myPackageName) != PackageManager.SIGNATURE_MATCH) {
            Log.e(XhookApp.TAG, "Rejecting static Busybox package because it is signed with a different key");
            return;
        }

        if (mStaticBusyboxInfo.versionCode != STATIC_BUSYBOX_REQUIRED_VERSION) {
            Log.e(XhookApp.TAG, String.format("Ignoring static BusyBox package with version %d, we need version %d",
                    mStaticBusyboxInfo.versionCode, STATIC_BUSYBOX_REQUIRED_VERSION));
            mStaticBusyboxInfo = null;
            return;
        } else if (!wasAvailable) {
            Log.i(XhookApp.TAG, "Detected static Busybox package");
        }
    }

    public synchronized static boolean isStaticBusyboxAvailable() {
        return mStaticBusyboxInfo != null;
    }
}
