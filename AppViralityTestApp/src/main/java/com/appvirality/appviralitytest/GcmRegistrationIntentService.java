package com.appvirality.appviralitytest;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.appvirality.AppVirality;
import com.appvirality.UserDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

/**
 * Created by AppVirality on 5/6/2016.
 */
public class GcmRegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    String SENDER_ID = "667746729160";
    public static final String PROPERTY_PUSH_TOKEN = "push_token";
    String token;
    AppVirality appVirality;

    public GcmRegistrationIntentService() {
        super(TAG);
        appVirality = AppVirality.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean shouldRefreshToken = intent.getBooleanExtra("should_refresh_token", false);
        try {
            if (checkPlayServices()) {
                if (!checkGCMConfiguration())
                    return;
                token = getRegistrationId();
                if (TextUtils.isEmpty(token) || shouldRefreshToken) {
                    InstanceID instanceID = InstanceID.getInstance(this);
                    token = instanceID.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    storePushToken();
                }
                Log.i(TAG, "GCM Registration Token: " + token);
                if (!TextUtils.isEmpty(token))
                    sendPushTokenToServer();
            } else {
                Log.i(TAG, "No valid Google Play Services APK found.");
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
//        // Notify UI that registration has completed, so the progress indicator can be hidden.
//        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS)
            return false;
        return true;
    }

    public boolean checkGCMConfiguration() {
        if (Build.VERSION.SDK_INT < 8) {
            Log.i(TAG, "push notifications not supported in SDK " + Build.VERSION.SDK_INT);
            return false;
        }

        final PackageManager packageManager = this.getPackageManager();
        final String packageName = this.getPackageName();
        final String permissionName = packageName + ".permission.C2D_MESSAGE";

        try {
            packageManager.getPermissionInfo(permissionName, PackageManager.GET_PERMISSIONS);
        } catch (final PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Application does not define permission " + permissionName);
            Log.i(TAG, "You will need to add the following lines to your application manifest:\n" +
                    "<permission android:name=\"" + packageName + ".permission.C2D_MESSAGE\" android:protectionLevel=\"signature\" />\n" +
                    "<uses-permission android:name=\"" + packageName + ".permission.C2D_MESSAGE\" />");
            return false;
        }

        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission("com.google.android.c2dm.permission.RECEIVE", packageName)) {
            Log.w(TAG, "Package does not have permission com.google.android.c2dm.permission.RECEIVE");
            Log.i(TAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n" +
                    "<uses-permission android:name=\"com.google.android.c2dm.permission.RECEIVE\" />");
            return false;
        }

        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission("android.permission.INTERNET", packageName)) {
            Log.w(TAG, "Package does not have permission android.permission.INTERNET");
            Log.i(TAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n" +
                    "<uses-permission android:name=\"android.permission.INTERNET\" />");
            return false;
        }

        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission("android.permission.WAKE_LOCK", packageName)) {
            Log.w(TAG, "Package does not have permission android.permission.WAKE_LOCK");
            Log.i(TAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n" +
                    "<uses-permission android:name=\"android.permission.WAKE_LOCK\" />");
            return false;
        }

        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission("android.permission.GET_ACCOUNTS", packageName)) {
            Log.i(TAG, "Package does not have permission android.permission.GET_ACCOUNTS");
            Log.i(TAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n" +
                    "<uses-permission android:name=\"android.permission.GET_ACCOUNTS\" />");
        }

        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_RECEIVERS);
        } catch (final PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Could not get receivers for package " + packageName);
            return false;
        }

        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId() {
        SharedPreferences prefs = getGcmPreferences();
        String registrationId = prefs.getString(PROPERTY_PUSH_TOKEN, "");
        if (registrationId.equals(""))
            Log.i(TAG, "Registration not found.");
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences() {
        return getSharedPreferences(GcmRegistrationIntentService.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private void storePushToken() {
        SharedPreferences prefs = getGcmPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_PUSH_TOKEN, token);
        editor.apply();
    }

    private void sendPushTokenToServer() {
        UserDetails userDetails = new UserDetails();
        userDetails.setPushToken(token);
        if(appVirality != null)
        appVirality.updateAppUserInfo(userDetails, null);
    }
}
