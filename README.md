# AppVirality-Android-SDK-2.0
Referrals &amp; Loyalty Program

<H3>Introduction:</H3>
AppVirality is a Plug and Play Growth Hacking Toolkit for Mobile Apps. 

It helps to identify and implement the right growth hacks, within seconds. No Coding Required. We are providing easy to integrate SDK's for Android, iOS and Windows(coming soon) platforms. 

Appvirality Android SDK supports from Android (API level 9) and higher. 

Version History 
---------------

Current Version : 2.0.0

[Version Info](https://github.com/farazAV/AppVirality-Android-SDK-2.0/wiki/Android-SDK-Version-History)

Integrating Appvirality into your App
-------------------------------------

Throughout the document, invitation sender will be called as "Referrer" and receiver will be called as "Friend".


<H4>STEP 1 - Adding AppVirality SDK to your app</H4>

Use Gradle dependency for core SDK + Default UI

```java
    compile 'com.appvirality:AppViralityUI:2.0.0'
```

OR

Use Gradle dependency for core SDK
```java
    compile 'com.appvirality:AppViralitySDK:2.0.0'
```

OR

Copy AppVirality SDK jar to the <b>libs</b> folder of your application and then add it as a file dependency for the application module.

<H4>STEP 2 - Set up your AppVirality Keys</H4>

Once you've registered with AppVirality.com and add a new app, you will be given an App key.

![Alt text](https://github.com/appvirality/appvirality-sdk-android/blob/master/images/App-key-obtaining.jpg?raw=true)


<H4>STEP 3 - Configure the <b>AndroidManifest.xml</b> file of your project as follows</H4> 

1) Add the following <i>meta-data</i> elements to the <i>application</i> element

* Replace "02e1r5e99b94f56t69f42a32a00d2e7ff" with your AppVirality App key

```java
<application android:label="@string/app_name" ...>
    ...
    <meta-data
        android:name="com.appvirality.sdk.AppViralityApiKey"
        android:value="02e1r5e99b94f56t69f42a32a00d2e7ff" />
    ...
</application>
```

* Set <i>value</i> as <i>true</i> or <i>false</i>, depending on whether you want to run the app in <b>Test Mode</b> or <b>Live Mode</b> respectively. Set the value as <i>false</i> before publishing the app to the play store.

```java
<application android:label="@string/app_name" ...>
    ...
    <meta-data
        android:name="com.appvirality.sdk.TestMode"
        android:value="true" />
    ...
</application>
```

2) Add the following permissions within the <code>&lt;manifest...&gt;</code>

```java
<manifest..>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- Optional permissions. ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION are used to send location targeted campaigns to the user. -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <!-- Optional permissions. WRITE_EXTERNAL_STORAGE and READ_EXTERNAL_STORAGE are used to improve the performance by storing and reading campaign images. -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <!-- Optional permissions. READ_PHONE_STATE is used to read device id and other device params to recognize a user. -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />

</manifest>
```

3) Add Install Referrer Receiver

AppVirality Uses Google Install Referrer for attribution as a fallback, in addition to device finger printing.

Add the following code block if you don't already have an <b>INSTALL_REFERRER</b> receiver in your manifest file <i>Application</i> Tag

```java
<receiver
    android:name="com.appvirality.AppViralityInstallReferrerReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />
    </intent-filter>
</receiver>
```
(or)

If you already have an <b>INSTALL_REFERRER</b> receiver, use following code block in the onReceive method of your broadcast receiver

```java
import com.appvirality.AppVirality;
...

if (extras != null && extras.containsKey("referrer")) {
	String referrer = intent.getStringExtra("referrer");
	AppVirality.setReferrerKey(context, referrer);
}
```
(or)

If you have multiple <b>INSTALL_REFERRER</b> receivers in your App, please go through the documentation [here](https://github.com/farazAV/AppVirality-Android-SDK-2.0/wiki/Using-Multiple-Install-Referrer-Receivers).

4) Declare following activities if you are using AppViralityUI dependency. With GrowthHackActivity.java, you must either use <i>AppViralityTheme</i> as the theme or create a new style extending <i>AppViralityTheme</i> as its parent, modifying the style attributes values as per your requirements and then use this theme.

```java
<activity
        android:name="com.appvirality.appviralityui.activities.GrowthHackActivity"
        android:theme="@style/AppViralityTheme" />
<activity android:name="com.appvirality.appviralityui.activities.WebViewActivity" />
<!-- Optional. Required only if you want to show Welcome Screen to the new user. -->
<activity
        android:name="com.appvirality.appviralityui.activities.WelcomeScreenActivity"
        android:theme="@style/AppTheme.NoActionBar"
        android:windowSoftInputMode="stateHidden" />
```

<H4>STEP 4 - Initializing the AppVirality SDK</H4>

1) Before actually initializing the SDK we need to instantiate the <i>AppVirality</i> class, which is the main class in the SDK we will need for various SDK operations. The best way to instantiate this class is in your app's launcher activity so that all the required SDK classes will get instantiated by the time user will be redirected to the app's home screen. Use the following code for the same:

```java
import com.appvirality.AppVirality;
...

AppVirality appVirality = AppVirality.getInstance(this);
```

This method returns the <i>AppVirality</i> class instance, after instantiating if it was not already instantiated.

<b>NOTE:</b> Use the same above method to retrieve the already initialized singleton throughout your application.

2) Initializing the SDK

* Create a <i>UserDetails</i> class object and set the various user details to recognize the user same as your backend system. Also, it is required to personalize the referral messages and welcome screen, which will be shown to new users upon app installation. (Friends shall be able to see the referrer's name and profile picture). We will also pass these user details through web-hooks to notify you on successful referral or conversion(install,signup or transaction,etc.)

```java
import com.appvirality.UserDetails;
...

UserDetails userDetails = new UserDetails();
userDetails.setReferralCode(referralCode);
userDetails.setAppUserId(userId);
userDetails.setPushRegId(pushRegistrationId);
userDetails.setUserEmail(email);
userDetails.setUserName(name);
userDetails.setProfileImage(userImage);
userDetails.setMobileNo(mobileNo);
userDetails.setCity(city);
userDetails.setState(state);
userDetails.setCountry(country);
userDetails.setExistingUser(isExistingUser);
```

a) <b>referralCode</b> - <i>String</i>. Referrer's Referral Code  
b) <b>userId</b> - <i>String</i>. ID of the user in your App(helps to identify users on dashboard as you do in your app)  
c) <b>pushRegistrationId</b> - <i>String</i>. Unique id assigned to the device by your Push Notification Service. Providing this helps AppVirality in sending Push Notifications to Users  
d) <b>email</b> - <i>String</i>. User's email address  
e) <b>name</b> - <i>String</i>. First Name of the user, required to personalize the referral messages  
f) <b>userImage</b> - <i>String</i>. User profile picture URL, required to personalize the referral messages  
g) <b>mobileNo</b> - <i>String</i>. User's mobile number  
h) <b>city</b> - <i>String</i>. User's city  
i) <b>state</b> - <i>String</i>. User's state  
j) <b>country</b> - <i>String</i>. User's country  
k) <b>isExistingUser</b> - <i>boolean</i>. Set this as True, only if you identify the user as an existing user(this is useful if you don't want to reward existing users) ; else False

* Invoke <b>init</b> method of the <b>AppVirality</b> class to start the AppVirality's initialization API calls, passing the <i>UserDetail</i> object created in the previous step and an <i>AppViralitySessionInitListener</i> instance. Use this method preferably in your splash activity or main activity, so that your campaigns will be ready in the background by the time your app gets loaded. This ensures smooth user experience. Use the following code to initialize the sdk

```java
import com.appvirality.AppVirality;
...

AppVirality appVirality = AppVirality.getInstance(this);
appVirality.init(userDetails, new AppVirality.AppViralitySessionInitListener() {
        @Override
        public void onInitFinished(boolean isInitialized, JSONObject responseData, String errorMessage) {
                Log.i("AppVirality: ", "Is Initialized " + isInitialized);
                if (responseData != null)
                        Log.i("AppVirality: ", "userDetails " + responseData.toString());
	}
});
```  

<H4>STEP 5 - Launching Growth Hack</H4>

In-App referral growth hack can be launched in 3 different ways. You can use any/all of these 3 options to launch the growth hack.

##### Option 1 - Launch from custom button i.e from "Invite Friends" or "Refer & Earn" button on your App menu

You can use the following method if you want to show some label or message bar, only if there is any campaign available for the user.<i>CampaignDetailsListener</i> will get called irrespective of campaign availability but if campaign is not available the <i>onCampaignReady</i> method shall receive empty campaign list. This is mainly useful when you want to have some control over the "Invite" or "Share" button visibility.

Use below code block to get the campaign details configured on AppVirality dashboard.

```java
import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
...  

AppVirality appVirality = AppVirality.getInstance(this);
appVirality.getCampaigns(null, new AppVirality.CampaignDetailsListener() {
        @Override
        public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
        	// Get Word of Mouth campaign details from list of campaign details
                CampaignDetail womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);
                if (refreshImages)
                	// Refresh Word of Mouth campaign images
                if (campaignDetails.size() > 0 && womCampaignDetail != null) {
                	// Campaigns available, display Refer & Earn button or launch growth hack screen
                } else {
                        // Campaigns not available, hide Refer & Earn button or display some message to the user
                }
        }
});
```

<b>NOTE:</b> You must check for <i>refreshImages</i> value whenever you use <i>CampaignDetailsListener.onGetCampaignDetails</i> callback, if its true, download the Word of Mouth campaign images because this value will be provided only once whenever campaign data will change. So in order to have latest campaign images you must check <i>refreshImages</i> value each time you use this callback.

##### Option 2 - Launch from Popup

You can launch the growth hack from popup dialog. You can configure the popup dialog message and style from AppVirality dashboard and you need not update your app every time you make the modifications.

You can control the visibility of this mini notification from dashboard.(i.e. By setting launch conditions like after how many app launches you want to show the notification or after how many days of first install you want to show the notification).

Use the below code to create a popup for launching growth hack

```java
import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.appviralityui.custom.CustomPopUp;
...  

AppVirality appVirality = AppVirality.getInstance(this);
CustomPopUp customPopUp = new CustomPopUp(this);
appVirality.getCampaigns(Constants.GrowthHackType.Word_of_Mouth, new AppVirality.CampaignDetailsListener() {
        @Override
        public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
        	if(campaignDetails.size() > 0) {
        		CampaignDetail womCampaignDetail = campaignDetails.get(0);
	                if (womCampaignDetail != null) {
	                	if (refreshImages)
	                        	// Refresh Word of Mouth campaign images
	                	// Checking Popup visibility conditions as set by you on the AppVirality dashboard
	                	if (appVirality.checkUserTargeting(womCampaignDetail, false))
	                        	customPopUp.showPopUp(campaignDetails, womCampaignDetail);
	                }
        	}
        }
});
```

##### Option 3 - Launch from Mini Notification

You can launch the GrowthHack from Mini notification. You can configure the Mini notification style and message from your AppVirality dashboard. You can control the visibility of this mini notification from dashboard, same as for a Popup.

Use the below code to create a mini notification for launching growth hack

```java
import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.appviralityui.custom.CustomPopUp;
...  

AppVirality appVirality = AppVirality.getInstance(this);
CustomPopUp customPopUp = new CustomPopUp(this);
appVirality.getCampaigns(Constants.GrowthHackType.Word_of_Mouth, new AppVirality.CampaignDetailsListener() {
        @Override
        public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
        	if(campaignDetails.size() > 0) {
        		CampaignDetail womCampaignDetail = campaignDetails.get(0);
	                if (womCampaignDetail != null) {
	                	if (refreshImages)
	                        	// Refresh Word of Mouth campaign images
	                	// Checking Mini Notification visibility conditions as set by you on the AppVirality dashboard
	                	if (appVirality.checkUserTargeting(womCampaignDetail, true))
	                        	customPopUp.showMiniNotification(campaignDetails, womCampaignDetail);
	                }
        	}
        }
});
```

Tip: Let the App users know about referral program by showing mini notification or some banner to achieve great results.

###### How to launch growth hack screen from default UI

If you are using the AppViralityUI dependency you can launch the default growth hack screen using the below code:

```java
import com.appvirality.appviralityui.activities.GrowthHackActivity;
...

Intent growthHackIntent = new Intent(MainActivity.this, GrowthHackActivity.class);
growthHackIntent.putExtra("campaign_details", campaignDetails);
growthHackIntent.putExtra("is_earnings", false);
growthHackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
startActivity(growthHackIntent);
```

<b>NOTE:</b> Use "campaign_details" and "is_earnings" as intent extras while launching <i>GrowthHackActivity</i>, passing the <i>CampaignDetails</i> class object and a boolean respectively. If "is_earnings" is <i>true</i>, only the Earnings fragment shall be displayed; else both Refer and Earnings fragments shall be displayed.

<H4>STEP 6 - Record Events</H4>

Recording Events are very important to reward your participants (Referrer/Friend) in case of a successful event. Also to calculate the LTV of participant (Referrer/Friend)

Tip: Identify top influencer's and make most of their network.

Please add the following code to send a Install conversion event when a user installs the app

```java
import com.appvirality.AppVirality;
...

AppVirality appVirality = AppVirality.getInstance(this);
appVirality.saveConversionEvent(event, transactionValue, transactionUnit, campaignId, growthHackType, conversionEventListener);
```

a) <b>event</b> - <i>String</i>. Name of the event to be recorded.  
b) <b>transactionValue</b> - <i>String</i>. Transaction amount for the event if applicable ; else null.  
c) <b>transactionUnit</b> - <i>String</i>. Transaction unit for the event if applicable ; else null.  
d) <b>campaignId</b> - <i>String</i>. Campaign Id for which to record the event, required only if multiple campaigns exists for a growth hack else can be null.  
f) <b>growthHackType</b> - <i>enum</i>. Type of growth hack for which recording event. Ex, Constants.GrowthHackType.Word_of_Mouth, Constants.GrowthHackType.Loyalty_Program, etc.  
g) <b>conversionEventListener</b> - <i>ConversionEventListener</i>. ConversionEventListener instance if you want to get the callback after API execution ; else null.  

Some example custom events that you may want to track and reward users for the same are:

"Finished_Level_5"  
"Clicked_Reorder"  
"Completed_Purchase"

<b>Proguard Configuration:</b>
If you use proguard with your application, there are a set of rules that you will need to include to get AppVirality to work. AppVirality will not function correctly if proguard obfuscates its classes.

```java
-keep class com.appvirality.** { *; }
-dontwarn com.appvirality.**
```

<H4>Finished Integration</H4>

Congratulations!
You have successfully completed the AppVirality SDK integration process. 

Get Referrer Details
-------------------------------------

You can get the referrer details from the SDK using the following code block

```java
import com.appvirality.AppVirality;
...

AppVirality appVirality = AppVirality.getInstance(this);
appVirality.getReferrerDetails(new AppVirality.ReferrerDetailsReadyListener() {
        @Override
        public void onReferrerDetailsReady(JSONObject referrerDetails) {
        	// Use referrerDetails json object returned here to get various referrer details
        }
});
```

Update User Info
-------------------------------------

User Info can be updated anytime using the following steps

<H4>STEP 1 - Set user info you want to update</H4>

Create a <b>UserDetails</b> class object and set the user info you want to update using its setter methods. You can update only the following user info via <i>updateappuserinfo</i> API

```java
import com.appvirality.UserDetails;
...

UserDetails userDetails = new UserDetails();
userDetails.setAppUserId(userId);
userDetails.setPushRegId(pushRegistrationId);
userDetails.setUserEmail(email);
userDetails.setUserName(name);
userDetails.setProfileImage(userImage);
userDetails.setMobileNo(mobileNo);
userDetails.setCity(city);
userDetails.setState(state);
userDetails.setCountry(country);
userDetails.setExistingUser(isExistingUser);
```

a) <b>userId</b> —  ID of the user in your App(helps to identify users on dashboard as you do in your app)  
b) <b>pushRegistrationId</b> —  Unique id assigned to the device by your Push Notification Service. Providing this helps AppVirality in sending Push Notifications to Users  
c) <b>email</b> —  User's email address  
d) <b>name</b> — First Name of the user, required to personalize the referral messages  
e) <b>userImage</b> —  User profile picture URL, required to personalize the referral messages  
f) <b>mobileNo</b> —  User's mobile number  
g) <b>city</b> —  User's city  
h) <b>state</b> —  User's state  
i) <b>country</b> —  User's country  
j) <b>isExistingUser</b> — Set this as True, only if you identify the user as an existing user(this is useful if you don't want to reward existing users) ; else False

<H4>STEP 2 - Update the user info</H4>

Use the following code block to update the user info, passing the <b>UserDetails</b> object created in the previous step

```java
import com.appvirality.AppVirality;
...

AppVirality appVirality = AppVirality.getInstance(this);
appVirality.updateAppUserInfo(userDetails, new AppVirality.UpdateUserInfoListener() {
        @Override
        public void onResponse(boolean isSuccess, String errorMsg) {
        	// isSuccess would be true if user info updated successfully, else would be false with some errorMsg
        }
});
```

<H4>Displaying Welcome screen</H4>
[Displaying Welcome screen to new users](https://github.com/farazAV/AppVirality-Android-SDK-2.0/wiki/Displaying-Welcome-screen-to-new-users)

<H4>Whats Next</H4>

Sit back and watch AppVirality in action by creating the campaigns from <a href="http://growth.appvirality.com">AppVirality Dashboard.</a>
