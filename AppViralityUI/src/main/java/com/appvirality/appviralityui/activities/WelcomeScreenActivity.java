package com.appvirality.appviralityui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.Utils;
import com.appvirality.appviralityui.custom.RoundedImageView;

import org.json.JSONObject;


public class WelcomeScreenActivity extends Activity {

    JSONObject referrerDetails;
    AppVirality appVirality;
    Utils utils;
    String friendRewardEvent, signUpBtnText;
    TextView tvWelcomeTitle, tvReferrerDesc, tvSkipReferrer;
    RelativeLayout referralCodeLayout;
    EditText editTextRefCode;
    RoundedImageView imgProfile;
    Button btnSignUp;
    boolean isSignUpVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        int[] attrs = {R.attr.sign_up_btn_visible, R.attr.sign_up_btn_text};
        TypedArray typedValue = obtainStyledAttributes(attrs);
        isSignUpVisible = typedValue.getBoolean(0, true);
        signUpBtnText = typedValue.getString(1);
        String referrerDetailsStr = getIntent().getStringExtra("referrer_details");
        appVirality = AppVirality.getInstance(this);
        utils = new Utils(this);
        try {
            Log.i("AppViralitySDK", "Welcome Screen Started");
            referrerDetails = new JSONObject(referrerDetailsStr);
            setViewsData();
        } catch (Exception e) {
            finish();
        }
    }

    private void setViewsData() throws Exception {
        tvWelcomeTitle = (TextView) findViewById(R.id.tv_welcome_title);
        tvReferrerDesc = (TextView) findViewById(R.id.appvirality_reward_details);
        tvSkipReferrer = (TextView) findViewById(R.id.appvirality_skip_welcome);
        imgProfile = (RoundedImageView) findViewById(R.id.appvirality_user_profile);
        btnSignUp = (Button) findViewById(R.id.appvirality_btnsignup);
        referralCodeLayout = (RelativeLayout) findViewById(R.id.referral_code_layout) ;
        editTextRefCode = (EditText) findViewById(R.id.editTextReferralCode);
        tvReferrerDesc.setText(referrerDetails.optString("welcomeMessage"));
        if (!TextUtils.isEmpty(referrerDetails.optString("offerTitleColor"))) {
            int offerTitleColor = Color.parseColor(referrerDetails.optString("offerTitleColor"));
            tvWelcomeTitle.setTextColor(offerTitleColor);
            tvSkipReferrer.setTextColor(offerTitleColor);
            findViewById(R.id.border_line).setBackgroundColor(offerTitleColor);
        }
        if (!TextUtils.isEmpty(referrerDetails.optString("offerDescriptionColor"))) {
            tvReferrerDesc.setTextColor(Color.parseColor(referrerDetails.optString("offerDescriptionColor")));
        }
        if (!TextUtils.isEmpty(referrerDetails.optString("profileImage"))) {
            imgProfile.setVisibility(View.VISIBLE);
            utils.downloadAndSetImage(referrerDetails.optString("profileImage"), imgProfile);
        }
        friendRewardEvent = referrerDetails.optString("friendRewardEvent");
        String attributionSetting = referrerDetails.optString("attributionSetting");

        // shouldEnterRefCode will be True if Referrer Details are not available, so
        // as to take referral code from user to get probable Referrer Details
        final boolean shouldEnterRefCode = !attributionSetting.equals("0") && (!referrerDetails.getBoolean("hasReferrer") || appVirality.isSessionInitialized());

        // Displaying Welcome Message if Referrer has been confirmed
        if (attributionSetting.equals("0") || referrerDetails.getBoolean("isReferrerConfirmed")) {
            if (referralCodeLayout.getVisibility() == View.VISIBLE) {
                tvWelcomeTitle.setText(getString(R.string.appvirality_welcome_title));
                tvReferrerDesc.setVisibility(View.VISIBLE);
                editTextRefCode.setEnabled(false);
                btnSignUp.setVisibility(View.GONE);
                imgProfile.setVisibility(View.VISIBLE);
            }
        } else {
            // If Attribution Setting is Only Referral Code or (Referral Link + Referral Code)
            // and Referrer is not confirmed
//            btnSignUp.setVisibility(isSignUpVisible ? View.VISIBLE : View.GONE);
            if (referrerDetails.getBoolean("hasReferrer")) {
                tvWelcomeTitle.setText(getString(R.string.appvirality_welcome_title));
                tvReferrerDesc.setVisibility(View.VISIBLE);
                // If user should enter Referral Code to get the Referrer Details
                if (shouldEnterRefCode) {
                    referralCodeLayout.setVisibility(View.VISIBLE);
                    editTextRefCode.setText(referrerDetails.optString("referrerCode"));
                    btnSignUp.setVisibility(View.VISIBLE);
                    btnSignUp.setText("Apply");
                } else {
                    imgProfile.setVisibility(View.VISIBLE);
                    editTextRefCode.setEnabled(false);
                    btnSignUp.setVisibility(isSignUpVisible ? View.VISIBLE : View.GONE);
                    btnSignUp.setText(!TextUtils.isEmpty(signUpBtnText) ? signUpBtnText : "Sign Up");
                }
            } else {
                // Doesn't have Referrer Details, user should enter the Referral Code
                tvWelcomeTitle.setText(getString(R.string.appvirality_welcome_ref_code));
                tvReferrerDesc.setVisibility(View.GONE);
                imgProfile.setVisibility(View.GONE);
                referralCodeLayout.setVisibility(View.VISIBLE);
                btnSignUp.setVisibility(View.VISIBLE);
                btnSignUp.setText("Apply");
            }
        }

        if (!TextUtils.isEmpty(referrerDetails.optString("campaignBGColor")))
            findViewById(R.id.layout_welcome_screen).setBackgroundColor(Color.parseColor(referrerDetails.optString("campaignBGColor")));

        tvSkipReferrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // User clicked on SignUp, return control to the calling activity
                // and launch SignUp screen from there
                if (!shouldEnterRefCode) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("referral_code", referrerDetails.optString("referrerCode"));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    // Apply entered Referral Code to get the probable Referrer Details
                    String refCode = editTextRefCode.getText().toString().trim();
                    if (!TextUtils.isEmpty(refCode)) {
                        checkReferralCode(refCode);
                    } else {
                        editTextRefCode.setError("Required");
                    }
                }
            }
        });
    }

    /**
     * Applying Referral Code entered by the user
     */
    private void checkReferralCode(final String refCode) {
        utils.showProgressDialog();
        if (appVirality.isSessionInitialized()) {
            appVirality.submitReferralCode(refCode, new AppVirality.SubmitReferralCodeListener() {
                @Override
                public void onResponse(boolean isSuccess, JSONObject responseData, String errorMsg) {
                    String message = isSuccess ? "Referral Code applied Successfully" : (errorMsg != null ? errorMsg : "Failed to apply referral code");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    refreshData(responseData);
                    finish();
                }
            });
        } else {
            appVirality.checkAttribution(refCode, new AppVirality.CheckAttributionListener() {
                @Override
                public void onResponse(JSONObject responseData, String errorMsg) {
                    refreshData(responseData);
                }
            });
        }
    }

    private void refreshData(JSONObject responseData) {
        try {
            utils.dismissProgressDialog();
            if (editTextRefCode.getText().toString().trim().equalsIgnoreCase(responseData.optString("referrerCode"))) {
                referrerDetails = responseData;
                setViewsData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        utils.dismissProgressDialog();
    }

}
