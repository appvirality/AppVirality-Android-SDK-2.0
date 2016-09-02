package com.appvirality.appviralitytest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import java.lang.reflect.Method;

/**
 * Created by AppVirality on 5/6/2016.
 */
public class GcmCustomListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        handleNotificationIntent(data);
    }

    private void handleNotificationIntent(Bundle data) {
        final Notification notification = buildNotification(data);

        if (null != notification) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
        }
    }

    private Notification buildNotification(Bundle data) {
        final NotificationData notificationData = readInboundIntent(data);
        if (null == notificationData) {
            return null;
        }

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationData.intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification notification;
        if (Build.VERSION.SDK_INT >= 16) {
            notification = makeNotificationSDK16OrHigher(contentIntent, notificationData);
        } else if (Build.VERSION.SDK_INT >= 11) {
            notification = makeNotificationSDK11OrHigher(contentIntent, notificationData);
        } else {
            notification = makeNotificationSDKLessThan11(contentIntent, notificationData);
        }

        return notification;
    }

    NotificationData readInboundIntent(Bundle data) {
        final PackageManager manager = getPackageManager();

        final String message = data.getString("message");
        final String uriString = data.getString("deeplink");
        CharSequence notificationTitle = data.getString("title");

        if (message == null) {
            return null;
        }

        ApplicationInfo appInfo;
        try {
            appInfo = manager.getApplicationInfo(getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            appInfo = null;
        }

        int notificationIcon = -1;
        if (null != appInfo) {
            notificationIcon = appInfo.icon;
        }

        if (notificationIcon == -1) {
            notificationIcon = android.R.drawable.sym_def_app_icon;
        }

        if (null == notificationTitle && null != appInfo) {
            notificationTitle = manager.getApplicationLabel(appInfo);
        }

        if (null == notificationTitle) {
            return null;
        }

        final Intent notificationIntent = buildNotificationIntent(uriString);

        return new NotificationData(notificationIcon, notificationTitle, message, notificationIntent);
    }

    private Intent buildNotificationIntent(String uriString) {
        Uri uri = null;
        if (null != uriString) {
            uri = Uri.parse(uriString);
        }

        final Intent ret;
        if (null == uri) {
            ret = getDefaultIntent();
        } else {
            ret = new Intent(Intent.ACTION_VIEW, uri);
        }

        return ret;
    }

    Intent getDefaultIntent() {
        final PackageManager manager = getPackageManager();
        return manager.getLaunchIntentForPackage(getPackageName());
    }

    @SuppressWarnings("deprecation")
    private Notification makeNotificationSDKLessThan11(PendingIntent pendingIntent, NotificationData notificationData) {
        Notification notification = new Notification(notificationData.icon, "", System.currentTimeMillis());
        try {
            // try to call "setLatestEventInfo" if available
            Method m = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
            m.invoke(notification, this, notificationData.title, notificationData.message, pendingIntent);
        } catch (Exception e) {
        }
        return notification;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(11)
    private Notification makeNotificationSDK11OrHigher(PendingIntent intent, NotificationData notificationData) {
        final Notification.Builder builder = new Notification.Builder(this).
                setSmallIcon(notificationData.icon).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent);

        final Notification n = builder.getNotification();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    @SuppressLint("NewApi")
    @TargetApi(16)
    private Notification makeNotificationSDK16OrHigher(PendingIntent intent, NotificationData notificationData) {
        final Notification.Builder builder = new Notification.Builder(this).
                setSmallIcon(notificationData.icon).
                setTicker(notificationData.message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(notificationData.title).
                setContentText(notificationData.message).
                setContentIntent(intent).
                setStyle(new Notification.BigTextStyle().bigText(notificationData.message));

        final Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return n;
    }

    static class NotificationData {

        private NotificationData(int icon, CharSequence title, String message, Intent intent) {
            this.icon = icon;
            this.title = title;
            this.message = message;
            this.intent = intent;
        }

        public final int icon;
        public final CharSequence title;
        public final String message;
        public final Intent intent;
    }
}
