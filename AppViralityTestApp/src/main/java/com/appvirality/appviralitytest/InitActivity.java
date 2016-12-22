package com.appvirality.appviralitytest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import com.appvirality.UserDetails;
import com.appvirality.appviralityui.Utils;

import org.json.JSONObject;

/**
 * Created by AppVirality on 4/19/2016.
 */
public class InitActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editReferralCode, editAppUserId, editEmail, editName, editMobileNo, editCity, editState, editCountry;
    CheckBox cbExistingUser;
    AppVirality appVirality;
    private static final int WRITE_EXT_REQ_CODE = 2;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        utils = new Utils(this);
        appVirality = AppVirality.getInstance(this);
        editReferralCode = (EditText) findViewById(R.id.edit_referral_code);
        editAppUserId = (EditText) findViewById(R.id.edit_app_user_id);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editName = (EditText) findViewById(R.id.edit_name);
        editMobileNo = (EditText) findViewById(R.id.edit_mobile);
        editCity = (EditText) findViewById(R.id.edit_city);
        editState = (EditText) findViewById(R.id.edit_state);
        editCountry = (EditText) findViewById(R.id.edit_country);
        cbExistingUser = (CheckBox) findViewById(R.id.cb_existing_user);

        String referralCode = appVirality.getReferrerRefCode();
        if (TextUtils.isEmpty(referralCode))
            editReferralCode.setText(referralCode);

        cbExistingUser.setChecked(appVirality.isExistingUser());
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXT_REQ_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check_attribution:
                checkAttribution();
                break;
            case R.id.btn_init:
                init();
                break;
        }
    }

    public void checkAttribution() {
        if (appVirality != null) {
            utils.showProgressDialog();
            appVirality.checkAttribution(editReferralCode.getText().toString() ,new AppVirality.CheckAttributionListener() {
                @Override
                public void onResponse(JSONObject responseData, String errorMsg) {
                    utils.dismissProgressDialog();
                    try {
                        String toastMsg;
                        if (responseData != null) {
                            if (responseData.getBoolean("isExistingUser")) {
                                toastMsg = "User already exists";
                            } else {
                                toastMsg = "Its a new user";
                            }
                        } else {
                            toastMsg = errorMsg;
                        }
                        Toast.makeText(InitActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void init() {
        if (appVirality != null) {
            UserDetails userDetails = new UserDetails();
            userDetails.setReferralCode(editReferralCode.getText().toString().trim());
            userDetails.setAppUserId(editAppUserId.getText().toString().trim());
            userDetails.setUserEmail(editEmail.getText().toString().trim());
            userDetails.setUserName(editName.getText().toString());
            userDetails.setMobileNo(editMobileNo.getText().toString());
            userDetails.setCity(editCity.getText().toString());
            userDetails.setState(editState.getText().toString());
            userDetails.setCountry(editCountry.getText().toString());
            userDetails.setExistingUser(cbExistingUser.isChecked());
            Toast.makeText(InitActivity.this, "Starting Init...", Toast.LENGTH_LONG).show();
            appVirality.init(userDetails, new AppVirality.AppViralitySessionInitListener() {
                @Override
                public void onInitFinished(boolean isInitialized, JSONObject responseData, String errorMsg) {
                    Toast.makeText(InitActivity.this, "Init Status: " + isInitialized, Toast.LENGTH_LONG).show();
                    Log.i("AppVirality: ", "Init Status " + isInitialized);
                    if (responseData != null)
                        Log.i("AppVirality: ", "userDetails " + responseData.toString());
                    String productSharingReferrer = appVirality.getProductSharingReferrer();
                    if (productSharingReferrer == null) {
                        startActivity(new Intent(InitActivity.this, MainActivity.class));
                        finish();
                    }
                }
            });
        }
    }

    private boolean checkPermission(String permissionName, int requestCode) {
        boolean granted = ContextCompat.checkSelfPermission(InitActivity.this, permissionName) == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            ActivityCompat.requestPermissions(InitActivity.this, new String[]{permissionName}, requestCode);
        }
        return granted;
    }

}
