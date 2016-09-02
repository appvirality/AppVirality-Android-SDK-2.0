package com.appvirality.appviralityui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.UserDetails;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class WelcomeScreenActivity extends Activity {

    EditText editTextRefCode;
    ProgressDialog progressDialog;
    JSONObject referrerDetails;
    AppVirality appVirality;
    Utils utils;
    String friendRewardEvent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        String referrerDetailsStr = getIntent().getStringExtra("referrer_details");
        appVirality = AppVirality.getInstance(this);
        utils = new Utils(this);

        try {
            Log.i("AppViralitySDK", "Welcome screen Started");
            referrerDetails = new JSONObject(referrerDetailsStr);
            TextView txtReferrerDesc = (TextView) findViewById(R.id.appvirality_reward_details);
            final EditText userEmail = (EditText) findViewById(R.id.appvirality_edittext_email);
            Button btnClaim = (Button) findViewById(R.id.appvirality_btnclaim);
            TextView txtSkipReferrer = (TextView) findViewById(R.id.appvirality_skip_welcome);
            ImageView imgProfile = (ImageView) findViewById(R.id.appvirality_user_profile);
            Button btnSignUp = (Button) findViewById(R.id.appvirality_btnsignup);

            editTextRefCode = (EditText) findViewById(R.id.editTextReferralCode);
            String refCode = referrerDetails.getString("referral_code");
            if (!TextUtils.isEmpty(refCode)) {
                editTextRefCode.setText(refCode.toUpperCase());
            }

            txtReferrerDesc.setText(referrerDetails.optString("welcome_message"));
            if (!TextUtils.isEmpty(referrerDetails.optString("offer_title_color")))
                txtReferrerDesc.setTextColor(Color.parseColor(referrerDetails.optString("offer_title_color")));
            if (!TextUtils.isEmpty(referrerDetails.optString("referrer_image_url"))) {
                utils.downloadAndSetImage(referrerDetails.optString("referrer_image_url"), imgProfile);
            }
            friendRewardEvent = referrerDetails.optString("friend_reward_event");
            if (!friendRewardEvent.equalsIgnoreCase("Install")) {
                btnClaim.setVisibility(View.GONE);
                userEmail.setVisibility(View.GONE);
                editTextRefCode.setVisibility(View.GONE);
                txtSkipReferrer.setText("Close");
            }
            if (referrerDetails.optString("friend_reward_event").equalsIgnoreCase("Signup")) {
                btnSignUp.setVisibility(View.VISIBLE);
                editTextRefCode.setVisibility(View.GONE);
            }
            //hide referral code input filed if attribution setting is only Link
            if ((!TextUtils.isEmpty(referrerDetails.optString("attribution_setting")) && referrerDetails.optString("attribution_setting").equals("0")) || referrerDetails.getBoolean("attribution_confirmed"))
                editTextRefCode.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(referrerDetails.optString("user_email")))
                userEmail.setVisibility(View.GONE);
            else
                userEmail.setText(referrerDetails.optString("user_email"));
            if (!TextUtils.isEmpty(referrerDetails.optString("campaign_bg_color")))
                findViewById(R.id.layout_welcome_screen).setBackgroundColor(Color.parseColor(referrerDetails.optString("campaign_bg_color")));
            btnClaim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!TextUtils.isEmpty(referrerDetails.optString("user_email")) ? true : Patterns.EMAIL_ADDRESS.matcher(userEmail.getText().toString().trim()).matches()) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            setFriendRewardListener(userEmail.getText().toString().trim());
                        } else {
                            Toast.makeText(WelcomeScreenActivity.this, "Please enter valid email address", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Welcome Screen", "Error parsing data.");
                        finish();
                    }
                }
            });

            txtSkipReferrer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            btnSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_OK);
                    //Please add the following code block in your Registration page after successful registration.
                    /*appVirality.getCampaigns(Constants.GrowthHackType.All, new AppVirality.CampaignDetailsReadyListener() {
                        @Override
                        public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                            if (campaignDetails.size() == 0)
                                dismissProgressDialog();
                            if (refreshImages)
                                utils.refreshImages(utils.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails));
                            for (Constants.GrowthHackType growthHackType : Constants.GrowthHackType.values()) {
                                if (growthHackType != Constants.GrowthHackType.All) {
                                    CampaignDetail campaignDetail = utils.getCampaignDetail(growthHackType, campaignDetails);
                                    String campaignId = null;
                                    if (campaignDetail != null)
                                        campaignId = campaignDetail.campaignId;
                                    appVirality.claimSignUpReward(campaignId, growthHackType, new AppVirality.ConversionEventListener() {
                                        @Override
                                        public void onResponse(boolean isSuccess, String message, String errorMsg) {
                                            try {
                                                dismissProgressDialog();
                                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                            } catch (Exception e) {
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });*/

                    finish();
                }
            });

        } catch (Exception e) {
            finish();
        }
    }

    private void setFriendRewardListener(String email) throws JSONException {
        final String userEmail = email;
        progressDialog = new ProgressDialog(WelcomeScreenActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        String refCode = editTextRefCode.getText().toString();
        if (!TextUtils.isEmpty(refCode) && !TextUtils.isEmpty(referrerDetails.optString("attribution_setting"))
                && !referrerDetails.optString("attribution_setting").equals("0") && !referrerDetails.getBoolean("attribution_confirmed")) {
            appVirality.submitReferralCode(refCode, new AppVirality.SubmitReferralCodeListener() {
                @Override
                public void onResponse(boolean isSuccess, String errorMsg) {
                    if (isSuccess) {
                        Log.i("AppViralitySDK : ", "Referral Code applied Successfully");
                        Toast.makeText(WelcomeScreenActivity.this, "Referral Code applied Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(WelcomeScreenActivity.this, "Failed to apply referral code", Toast.LENGTH_SHORT).show();
                    }
                    updateUserInfo(userEmail);
                }
            });
        } else if (friendRewardEvent.equalsIgnoreCase("Install")) {
            updateUserInfo(userEmail);
        }
    }

    private void updateUserInfo(String email) {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserEmail(email);
        appVirality.updateAppUserInfo(userDetails, new AppVirality.UpdateUserInfoListener() {
            @Override
            public void onResponse(boolean isSuccess, String errorMsg) {
                saveInstallConversionEvent(true);
            }
        });
    }

    public void saveInstallConversionEvent(final boolean shouldFinish) {
        appVirality.getCampaigns(Constants.GrowthHackType.Word_of_Mouth, new AppVirality.CampaignDetailsListener() {
            @Override
            public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                CampaignDetail womCampaignDetail = null;
                if (campaignDetails.size() > 0)
                    womCampaignDetail = campaignDetails.get(0);
                if (refreshImages)
                    utils.refreshImages(womCampaignDetail);
                String campaignId = null;
                if (womCampaignDetail != null)
                    campaignId = womCampaignDetail.campaignId;
                appVirality.saveConversionEvent("Install", null, null, campaignId, Constants.GrowthHackType.Word_of_Mouth, new AppVirality.ConversionEventListener() {
                    @Override
                    public void onResponse(boolean isSuccess, String message, String errorMsg) {
                        dismissProgressDialog();
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        if (shouldFinish)
                            finish();
                    }
                });
            }
        });
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressDialog();
    }

}
