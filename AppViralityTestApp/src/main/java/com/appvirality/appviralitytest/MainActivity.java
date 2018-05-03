package com.appvirality.appviralitytest;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.appviralityui.Utils;
import com.appvirality.appviralityui.activities.GrowthHackActivity;
import com.appvirality.appviralityui.activities.WelcomeScreenActivity;
import com.appvirality.appviralityui.custom.CustomPopUp;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by AppVirality on 3/1/2016.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppVirality appVirality;
    Utils utils;
    CustomPopUp customPopUp;
    private static final int REG_SCREEN_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("");
        appVirality = AppVirality.getInstance(this);
        customPopUp = new CustomPopUp(MainActivity.this);
        utils = new Utils(MainActivity.this);

        if (appVirality != null && !appVirality.isExistingUser())
            showWelcomeScreen();
        // Get GCM Registration key to enable push notifications.
//        Intent intent = new Intent(this, GcmRegistrationIntentService.class);
//        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_invite_friends:
                utils.showGrowthHack(appVirality);
                break;
            case R.id.btn_popup:
                showCustomPopUp();
                break;
            case R.id.btn_mini_notification:
                showMiniNotification();
                break;
            case R.id.btn_earnings:
                showEarnings();
                break;
            case R.id.btn_apply_referral_code:
                startActivity(new Intent(MainActivity.this, ApplyRefCodeActivity.class));
                break;
            case R.id.btn_update_user_details:
                startActivity(new Intent(MainActivity.this, UpdateUserDetailsActivity.class));
                break;
            case R.id.btn_custom_event:
                showSaveEventDialog();
                break;
            case R.id.btn_logout:
                appVirality.logout();
                utils.deleteCampaignImages();
                Toast.makeText(MainActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.btn_check_attribution:
                appVirality.checkAttribution(null, new AppVirality.CheckAttributionListener() {
                    @Override
                    public void onResponse(JSONObject responseData, String errorMsg) {
                        Toast.makeText(getApplicationContext(), responseData != null ? "Response Callback" : errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REG_SCREEN_REQ_CODE && resultCode == RESULT_OK) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.putExtra("referral_code", data.getStringExtra("referral_code"));
            startActivity(loginIntent);
        }
    }

    private void showWelcomeScreen() {
        appVirality.checkAttribution(null, new AppVirality.CheckAttributionListener() {
            @Override
            public void onResponse(JSONObject responseData, String errorMsg) {
                if (responseData != null) {
                    String attributionSetting = responseData.optString("attributionSetting");
                    if (!responseData.optBoolean("isExistingUser") && !(attributionSetting.equalsIgnoreCase("0") && !responseData.optBoolean("hasReferrer"))) {
                        Intent intent = new Intent(MainActivity.this, WelcomeScreenActivity.class);
                        intent.putExtra("referrer_details", responseData.toString());
                        startActivityForResult(intent, REG_SCREEN_REQ_CODE);
                        // Set the user as existing user after displaying the welcome screen, to
                        // avoid welcome screen getting displayed on subsequent launches
                        appVirality.setExistingUser();
                    }
                }
            }
        });
    }

    private void showCustomPopUp() {
        appVirality.getCampaigns(null, new AppVirality.CampaignDetailsListener() {
            @Override
            public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                CampaignDetail womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);
                if (refreshImages) {
                    utils.refreshImages(womCampaignDetail);
                }
                if (womCampaignDetail != null) {
                    if (appVirality.checkUserTargeting(womCampaignDetail, false))
                        customPopUp.showPopUp(campaignDetails, womCampaignDetail);
                }
            }
        });
    }

    private void showMiniNotification() {
        appVirality.getCampaigns(null, new AppVirality.CampaignDetailsListener() {
            @Override
            public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                if (refreshImages)
                    utils.refreshImages(appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails));
                CampaignDetail womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);
                if (womCampaignDetail != null) {
                    if (appVirality.checkUserTargeting(womCampaignDetail, true))
                        customPopUp.showMiniNotification(campaignDetails, womCampaignDetail);
                }
            }
        });
    }

    private void showEarnings() {
        utils.showProgressDialog();
        appVirality.getCampaigns(null, new AppVirality.CampaignDetailsListener() {
            @Override
            public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                utils.dismissProgressDialog();
                if (refreshImages)
                    utils.refreshImages(appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails));
                Intent growthHackIntent = new Intent(MainActivity.this, GrowthHackActivity.class);
                growthHackIntent.putExtra("campaign_details", campaignDetails);
                growthHackIntent.putExtra("is_earnings", true);
                growthHackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(growthHackIntent);
            }
        });
    }

//    private void showRegisterScreen() {
//        startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
//    }

    private void showSaveEventDialog() {
        final Dialog popUp = new Dialog(MainActivity.this);
        popUp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popUp.setCancelable(true);
        popUp.setContentView(R.layout.dialog_transaction);
        final CheckBox cbNoGrowthHack = (CheckBox) popUp.findViewById(R.id.cb_no_growth_hack);
        final Spinner spinnerGrowthHack = (Spinner) popUp.findViewById(R.id.spinner_growth_hack);
        final ArrayList<String> growthHackTypes = new ArrayList();
        for (Constants.GrowthHackType growthHackType : Constants.GrowthHackType.values()) {
            growthHackTypes.add(growthHackType.name());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, growthHackTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrowthHack.setAdapter(spinnerAdapter);
        final EditText editCustomEvent = (EditText) popUp.findViewById(R.id.edit_custom_event);
        final EditText editTransactionAmount = (EditText) popUp.findViewById(R.id.edit_transaction_amount);
        popUp.findViewById(R.id.btn_make_transaction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String customEvent = editCustomEvent.getText().toString().trim();
                final String transactionAmount = editTransactionAmount.getText().toString().trim();
                if (customEvent.equals("")) {
                    editCustomEvent.setError("Required");
                } else {
                    appVirality.getCampaigns(null, new AppVirality.CampaignDetailsListener() {
                        @Override
                        public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                            if (refreshImages)
                                utils.refreshImages(appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails));
                            Constants.GrowthHackType growthHackType = Constants.GrowthHackType.valueOf(growthHackTypes.get(spinnerGrowthHack.getSelectedItemPosition()));
                            if (cbNoGrowthHack.isChecked())
                                growthHackType = null;
                            appVirality.saveConversionEvent(customEvent, transactionAmount, null, null, growthHackType, new AppVirality.ConversionEventListener() {
                                @Override
                                public void onResponse(boolean isSuccess, String message, String errorMsg) {
                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    popUp.dismiss();
                }
            }
        });
        popUp.show();
    }

}
