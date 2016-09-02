package com.appvirality.appviralitytest;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.Constants;
import com.appvirality.appviralityui.Utils;

/**
 * Created by AppVirality on 4/19/2016.
 */
public class RegistrationActivity extends AppCompatActivity {

    EditText editReferralCode;
    AppVirality appVirality;
    ProgressDialog progressDialog;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        utils = new Utils(this);
        appVirality = AppVirality.getInstance(this);
        editReferralCode = (EditText) findViewById(R.id.edit_referral_code);
        String friendReferralCode = appVirality.getReferrerRefCode();
        if (!TextUtils.isEmpty(friendReferralCode))
            editReferralCode.setText(friendReferralCode);

        if ((!TextUtils.isEmpty(appVirality.getAttributionSetting())
                && appVirality.getAttributionSetting().equals("0")) || appVirality.isAttributionConfirmed())
            editReferralCode.setVisibility(View.GONE);

        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String referralCode = editReferralCode.getText().toString().trim();
                if (editReferralCode.getVisibility() == View.VISIBLE) {
                    if (referralCode.equals("")) {
                        editReferralCode.setError("Required");
                        return;
                    }
                }
                progressDialog = new ProgressDialog(RegistrationActivity.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(true);
                progressDialog.show();
                if (!TextUtils.isEmpty(referralCode) && !TextUtils.isEmpty(appVirality.getAttributionSetting())
                        && !appVirality.getAttributionSetting().equals("0") && !appVirality.isAttributionConfirmed()) {
                    appVirality.submitReferralCode(referralCode, new AppVirality.SubmitReferralCodeListener() {
                        @Override
                        public void onResponse(boolean isSuccess, String errorMsg) {
                            if (isSuccess) {
                                Log.i("AppViralitySDK : ", "Referral Code applied Successfully");
                            } else {
                                Toast.makeText(RegistrationActivity.this, "Failed to apply referral code", Toast.LENGTH_SHORT).show();
                            }
                            submitSignUpConversionEvent();
                        }
                    });
                } else {
                    submitSignUpConversionEvent();
                }
            }
        });
    }

    protected void submitSignUpConversionEvent() {
//        appVirality.getCampaigns(Constants.GrowthHackType.All, new AppVirality.CampaignDetailsListener() {
//            @Override
//            public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
//                if (campaignDetails.size() == 0)
//                    dismissProgressDialog();
//                if (refreshImages)
//                    utils.refreshImages(utils.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails));
//                for (Constants.GrowthHackType growthHackType : Constants.GrowthHackType.values()) {
//                    if (growthHackType != Constants.GrowthHackType.All) {
//                        CampaignDetail campaignDetail = utils.getCampaignDetail(growthHackType, campaignDetails);
//                        String campaignId = null;
//                        if (campaignDetail != null)
//                            campaignId = campaignDetail.campaignId;
//                        appVirality.claimSignUpReward(campaignId, growthHackType, new AppVirality.ConversionEventListener() {
//                            @Override
//                            public void onResponse(boolean isSuccess, String message, String errorMsg) {
//                                try {
//                                    dismissProgressDialog();
//                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//                                } catch (Exception e) {
//                                }
//                            }
//                        });
                        appVirality.saveConversionEvent("signup", null, null, null, Constants.GrowthHackType.Word_of_Mouth, new AppVirality.ConversionEventListener() {
                            @Override
                            public void onResponse(boolean isSuccess, String message, String errorMsg) {
                                try {
                                    dismissProgressDialog();
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                }
                            }
                        });
//                    }
//                }
//            }
//        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}