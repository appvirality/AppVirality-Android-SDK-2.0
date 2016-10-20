package com.appvirality.appviralitytest;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by AppVirality on 5/6/2016.
 */
public class GcmInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and update the same on over server.
        Intent intent = new Intent(this, GcmRegistrationIntentService.class);
        intent.putExtra("should_refresh_token", true);
        startService(intent);
    }
}
