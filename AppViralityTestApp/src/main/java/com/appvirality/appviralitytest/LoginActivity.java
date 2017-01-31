package com.appvirality.appviralitytest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.Constants;
import com.appvirality.UserDetails;
import com.appvirality.appviralityui.Utils;

import org.json.JSONObject;

/**
 * Created by AppVirality on 4/19/2016.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editReferralCode, editAppUserId, editPushToken, editEmail, editName, editMobileNo, editCity, editState, editCountry;
    CheckBox cbExistingUser;
    AppVirality appVirality;
    private static final int WRITE_EXT_REQ_CODE = 2;
    Utils utils;
    boolean isSdkInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        utils = new Utils(this);
        appVirality = AppVirality.getInstance(this);
        isSdkInitialized = appVirality.isSessionInitialized();
        editReferralCode = (EditText) findViewById(R.id.edit_referral_code);
        editAppUserId = (EditText) findViewById(R.id.edit_app_user_id);
        editPushToken = (EditText) findViewById(R.id.edit_push_token);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editName = (EditText) findViewById(R.id.edit_name);
        editMobileNo = (EditText) findViewById(R.id.edit_mobile);
        editCity = (EditText) findViewById(R.id.edit_city);
        editState = (EditText) findViewById(R.id.edit_state);
        editCountry = (EditText) findViewById(R.id.edit_country);
        cbExistingUser = (CheckBox) findViewById(R.id.cb_existing_user);

        String refCodeIntentExtra = getIntent().getStringExtra("referral_code");
        String referralCode = TextUtils.isEmpty(refCodeIntentExtra) ? appVirality.getReferrerRefCode() : refCodeIntentExtra;
        if (!TextUtils.isEmpty(referralCode))
            editReferralCode.setText(referralCode);

//        cbExistingUser.setChecked(appVirality.isExistingUser());
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXT_REQ_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
//                checkAttribution();
                login();
                break;
            case R.id.btn_sign_up:
//                init();
                signUp();
                break;
            case R.id.btn_skip:
                finishActivity();
                break;
        }
    }

//    public void checkAttribution() {
//        if (appVirality != null) {
//            utils.showProgressDialog();
//            appVirality.checkAttribution(new AppVirality.CheckAttributionListener() {
//                @Override
//                public void onResponse(JSONObject responseData, String errorMsg) {
//                    utils.dismissProgressDialog();
//                    try {
//                        String toastMsg;
//                        if (responseData != null) {
//                            if (responseData.getBoolean("isExistingUser")) {
//                                toastMsg = "User already exists";
//                            } else {
//                                toastMsg = "Its a new user";
//                            }
//                        } else {
//                            toastMsg = errorMsg;
//                        }
//                        Toast.makeText(InitActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//    }

    private void login() {
        init(false);
    }

    private void signUp() {
        init(true);
    }

    private void init(final boolean isSignUp) {
        if (appVirality != null) {
            UserDetails userDetails = new UserDetails();
            userDetails.setReferralCode(editReferralCode.getText().toString().trim());
            userDetails.setAppUserId(editAppUserId.getText().toString().trim());
            userDetails.setPushToken(editPushToken.getText().toString().trim());
            userDetails.setUserEmail(editEmail.getText().toString().trim());
            userDetails.setUserName(editName.getText().toString());
            userDetails.setMobileNo(editMobileNo.getText().toString());
            userDetails.setCity(editCity.getText().toString());
            userDetails.setState(editState.getText().toString());
            userDetails.setCountry(editCountry.getText().toString());
            userDetails.setExistingUser(cbExistingUser.isChecked());
            Toast.makeText(getApplicationContext(), "Starting Init...", Toast.LENGTH_LONG).show();
            Log.i("AppVirality: ", "Init Starting");
            appVirality.init(userDetails, new AppVirality.AppViralitySessionInitListener() {
                @Override
                public void onInitFinished(boolean isInitialized, JSONObject responseData, String errorMsg) {
                    if (isInitialized) {
                        isSdkInitialized = isInitialized;
                        Toast.makeText(getApplicationContext(), "Init Status: " + isInitialized, Toast.LENGTH_LONG).show();
                        Log.i("AppVirality: ", "Init Status " + isInitialized);
                        if (responseData != null)
                            Log.i("AppVirality: ", "userDetails " + responseData.toString());
                        if (responseData.optBoolean("isNewSession")) {
                            String eventName = isSignUp ? "signup" : "install";
                            submitConversionEvent(eventName);
                        }
                    }
                    finishActivity();
                }
            });
        }
    }

    private void finishActivity() {
        if (getCallingActivity() == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else {
            Intent returnIntent = new Intent();
            if (isSdkInitialized) {
                setResult(RESULT_OK, returnIntent);
            } else {
                setResult(RESULT_CANCELED, returnIntent);
            }
        }
        finish();
    }

    protected void submitConversionEvent(String eventName) {
        appVirality.saveConversionEvent(eventName, null, null, null, Constants.GrowthHackType.Word_of_Mouth, new AppVirality.ConversionEventListener() {
            @Override
            public void onResponse(boolean isSuccess, String message, String errorMsg) {
                try {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
            }
        });
    }

    private boolean checkPermission(String permissionName, int requestCode) {
        boolean granted = ContextCompat.checkSelfPermission(LoginActivity.this, permissionName) == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{permissionName}, requestCode);
        }
        return granted;
    }

}
