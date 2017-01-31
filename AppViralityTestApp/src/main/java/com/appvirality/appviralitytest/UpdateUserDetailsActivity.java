package com.appvirality.appviralitytest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.UserDetails;
import com.appvirality.appviralityui.Utils;

/**
 * Created by AppVirality on 5/9/2016.
 */
public class UpdateUserDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editName, editEmail, editImageUrl, editAppUserId, editMobile, editCity, editState, editCountry;
    CheckBox cbExistingUser;
    Utils utils;
    AppVirality appVirality;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_details);
        appVirality = AppVirality.getInstance(this);

        utils = new Utils(this);
        editName = (EditText) findViewById(R.id.edit_name);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editImageUrl = (EditText) findViewById(R.id.edit_image_url);
        editAppUserId = (EditText) findViewById(R.id.edit_app_user_id);
        editMobile = (EditText) findViewById(R.id.edit_mobile);
        editCity = (EditText) findViewById(R.id.edit_city);
        editState = (EditText) findViewById(R.id.edit_state);
        editCountry = (EditText) findViewById(R.id.edit_country);
        cbExistingUser = (CheckBox) findViewById(R.id.cb_existing_user);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_update:
                updateUserDetails();
                break;
        }
    }

    private void updateUserDetails() {
        utils.showProgressDialog();
        UserDetails userDetails = new UserDetails();
        userDetails.setUserName(editName.getText().toString().trim());
        userDetails.setUserEmail(editEmail.getText().toString().trim());
        userDetails.setProfileImage(editImageUrl.getText().toString().trim());
        userDetails.setAppUserId(editAppUserId.getText().toString().trim());
        userDetails.setMobileNo(editMobile.getText().toString());
        userDetails.setCity(editCity.getText().toString());
        userDetails.setState(editState.getText().toString());
        userDetails.setCountry(editCountry.getText().toString());
        userDetails.setExistingUser(cbExistingUser.isChecked());
        appVirality.updateAppUserInfo(userDetails, new AppVirality.UpdateUserInfoListener() {
            @Override
            public void onResponse(boolean isSuccess, String errorMsg) {
                try {
                    utils.dismissProgressDialog();
                    if (isSuccess) {
                        Toast.makeText(getApplicationContext(), "Updated Successfully", Toast.LENGTH_LONG).show();
                        resetValues();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to update the user details", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void resetValues(){
        editName.setText("");
        editEmail.setText("");
        editImageUrl.setText("");
        editAppUserId.setText("");
        editMobile.setText("");
        editCity.setText("");
        editState.setText("");
        editCountry.setText("");
        cbExistingUser.setChecked(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        utils.dismissProgressDialog();
    }
}
