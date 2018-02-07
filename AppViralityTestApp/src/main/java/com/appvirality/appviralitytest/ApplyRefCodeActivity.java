package com.appvirality.appviralitytest;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.appviralityui.Utils;

import org.json.JSONObject;

/**
 * Created by AppVirality on 4/19/2016.
 */
public class ApplyRefCodeActivity extends AppCompatActivity {

    EditText editReferralCode;
    Button btnApplyRefCode;
    AppVirality appVirality;
    ProgressDialog progressDialog;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_ref_code);

        utils = new Utils(this);
        appVirality = AppVirality.getInstance(this);
        editReferralCode = (EditText) findViewById(R.id.edit_referral_code);
        btnApplyRefCode = (Button) findViewById(R.id.btn_apply_referral_code);
        String friendReferralCode = appVirality.getReferrerRefCode();
        if (!TextUtils.isEmpty(friendReferralCode))
            editReferralCode.setText(friendReferralCode);

        if ((!TextUtils.isEmpty(appVirality.getAttributionSetting())
                && appVirality.getAttributionSetting().equals("0")) || appVirality.isAttributionConfirmed()) {
            editReferralCode.setEnabled(false);
            btnApplyRefCode.setEnabled(false);
        } else {
            btnApplyRefCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String referralCode = editReferralCode.getText().toString().trim();
                    if (TextUtils.isEmpty(referralCode))
                        return;
                    if (!TextUtils.isEmpty(appVirality.getAttributionSetting())
                            && !appVirality.getAttributionSetting().equals("0") && !appVirality.isAttributionConfirmed()) {
                        utils.showProgressDialog();
                        appVirality.submitReferralCode(referralCode, new AppVirality.SubmitReferralCodeListener() {
                            @Override
                            public void onResponse(boolean isSuccess, JSONObject responseData, String errorMsg) {
                                utils.dismissProgressDialog();
                                if (isSuccess) {
                                    Toast.makeText(ApplyRefCodeActivity.this, "Referral Code applied Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ApplyRefCodeActivity.this, errorMsg != null ? errorMsg : "Failed to apply referral code", Toast.LENGTH_SHORT).show();
                                }
//                            submitSignUpConversionEvent();
                            }
                        });
                    } /*else {
                    submitSignUpConversionEvent();
                }*/
                }
            });
        }
    }

//    protected void submitSignUpConversionEvent() {
//        appVirality.saveConversionEvent("signup", null, null, null, Constants.GrowthHackType.Word_of_Mouth, new AppVirality.ConversionEventListener() {
//            @Override
//            public void onResponse(boolean isSuccess, String message, String errorMsg) {
//                try {
//                    dismissProgressDialog();
//                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//                } catch (Exception e) {
//                }
//            }
//        });
//    }

    @Override
    protected void onPause() {
        super.onPause();
        utils.dismissProgressDialog();
    }

}