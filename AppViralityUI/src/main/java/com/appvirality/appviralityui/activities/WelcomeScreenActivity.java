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
            String refCode = appVirality.getReferrerRefCode();
            if (!TextUtils.isEmpty(refCode)) {
                editTextRefCode.setText(refCode.toUpperCase());
            }

            txtReferrerDesc.setText(referrerDetails.optString("welcomeMessage"));
            if (!TextUtils.isEmpty(referrerDetails.optString("offerTitleColor")))
                txtReferrerDesc.setTextColor(Color.parseColor(referrerDetails.optString("offerTitleColor")));
            if (!TextUtils.isEmpty(referrerDetails.optString("profileImage"))) {
                utils.downloadAndSetImage(referrerDetails.optString("profileImage"), imgProfile);
            }
            friendRewardEvent = referrerDetails.optString("friendRewardEvent");
            if (!friendRewardEvent.equalsIgnoreCase("Install")) {
                btnClaim.setVisibility(View.GONE);
                userEmail.setVisibility(View.GONE);
                editTextRefCode.setVisibility(View.GONE);
                txtSkipReferrer.setText("Close");
            }
            if (friendRewardEvent.equalsIgnoreCase("Signup")) {
                btnSignUp.setVisibility(View.VISIBLE);
                editTextRefCode.setVisibility(View.GONE);
            }
            //hide referral code input filed if attribution setting is only Link
            if ((!TextUtils.isEmpty(referrerDetails.optString("attributionSetting")) && referrerDetails.optString("attributionSetting").equals("0")) || referrerDetails.getBoolean("isReferrerConfirmed"))
                editTextRefCode.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(referrerDetails.optString("userEmail")))
                userEmail.setVisibility(View.GONE);
            else
                userEmail.setText(referrerDetails.optString("userEmail"));
            if (!TextUtils.isEmpty(referrerDetails.optString("campaignBGColor")))
                findViewById(R.id.layout_welcome_screen).setBackgroundColor(Color.parseColor(referrerDetails.optString("campaignBGColor")));
            btnClaim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!TextUtils.isEmpty(referrerDetails.optString("userEmail")) ? true : Patterns.EMAIL_ADDRESS.matcher(userEmail.getText().toString().trim()).matches()) {
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
                    /*appVirality.getCampaigns(Constants.GrowthHackType.Word_of_Mouth, new AppVirality.CampaignDetailsListener() {
                        @Override
                        public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                            if (campaignDetails.size() > 0) {
                                CampaignDetail womCampaignDetail = campaignDetails.get(0);
                                if (refreshImages)
                                    utils.refreshImages(womCampaignDetail);
                                appVirality.saveConversionEvent("signup", null, null, womCampaignDetail.campaignId, Constants.GrowthHackType.Word_of_Mouth, new AppVirality.ConversionEventListener() {
                                    @Override
                                    public void onResponse(boolean isSuccess, String message, String errorMsg) {
                                        dismissProgressDialog();
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                dismissProgressDialog();
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
        if (!TextUtils.isEmpty(refCode) && !TextUtils.isEmpty(referrerDetails.optString("attributionSetting"))
                && !referrerDetails.optString("attributionSetting").equals("0") && !referrerDetails.getBoolean("isReferrerConfirmed")) {
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
