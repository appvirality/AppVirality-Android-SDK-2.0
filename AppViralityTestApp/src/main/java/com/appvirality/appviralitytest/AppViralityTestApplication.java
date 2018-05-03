package com.appvirality.appviralitytest;

import android.app.Application;

import com.appvirality.AppVirality;
import com.appvirality.Config;


public class AppViralityTestApplication extends Application {

    AppVirality appVirality;

    @Override
    public void onCreate() {
        super.onCreate();

        Config config = new Config();
//        config.printLogs = true;
        //Fraud check configuration
        //config.runEmulatorChecks = true;
        //config.runRootChecks = true;
        //adding custom social actions having Apps with sharable intents
        //config.setCustomSocialActionData("Hootsuite", "com.hootsuite.droid.full", null, false, 0);
        //config.setCustomSocialActionData("Buffer", "org.buffer.android", null, false, 0);
       
        //adding custom social action with custom implementation
        config.setCustomSocialActionData("Invite Contacts", null, null, true, R.drawable.invite_contacts);

        appVirality = AppVirality.getInstance(this, config);

    }

}
