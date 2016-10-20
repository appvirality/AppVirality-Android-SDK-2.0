package com.appvirality.appviralityui.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.Utils;
import com.appvirality.appviralityui.activities.GrowthHackActivity;

import java.util.ArrayList;

/**
 * Created by AppVirality on 3/21/2016.
 */
public class CustomPopUp {

    Activity activity;
    private PopupWindow miniNotification;
    private Dialog popUp;
    Utils utils;
    AppVirality appVirality;

    public CustomPopUp(Activity activity) {
        this.activity = activity;
        utils = new Utils(activity);
        appVirality = AppVirality.getInstance(activity);
    }

    public void showPopUp(ArrayList<CampaignDetail> campaignDetails, final CampaignDetail womCampaignDetail){
        showLaunchPopUp(campaignDetails, womCampaignDetail, false);
    }

    public void showMiniNotification(ArrayList<CampaignDetail> campaignDetails, final CampaignDetail womCampaignDetail){
        showLaunchPopUp(campaignDetails, womCampaignDetail, true);
    }

    private void showLaunchPopUp(final ArrayList<CampaignDetail> campaignDetails, final CampaignDetail womCampaignDetail, boolean isMiniNotification) {
        try {
            if ((popUp != null && popUp.isShowing()) || (miniNotification != null && miniNotification.isShowing()))
                return;
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View launchBarLayout;
            if (isMiniNotification)
                launchBarLayout = inflater.inflate(R.layout.lauch_mini_notification, null);
            else
                launchBarLayout = inflater.inflate(R.layout.launch_popup, null);
            Button btnLaunchPopup = (Button) launchBarLayout.findViewById(R.id.appvirality_btnlaunchbar);
            Button btnRemindLater = (Button) launchBarLayout.findViewById(R.id.appvirality_btnremindlater);
            ImageView launchIcon = (ImageView) launchBarLayout.findViewById(R.id.appvirality_launchimage);
            launchIcon.setImageResource(getLaunchIcon(womCampaignDetail.launchIconId));
            TextView txtLaunchMessage = (TextView) launchBarLayout.findViewById(R.id.appvirality_txtlaunchmessage);
            txtLaunchMessage.setText(Html.fromHtml(TextUtils.isEmpty(womCampaignDetail.launchMessage) ? womCampaignDetail.campaignTitle : womCampaignDetail.launchMessage));
            if (womCampaignDetail.launchMsgColor != null)
                txtLaunchMessage.setTextColor(Color.parseColor(womCampaignDetail.launchMsgColor));
            if (!TextUtils.isEmpty(womCampaignDetail.remindBtnTxt))
                btnRemindLater.setText(womCampaignDetail.remindBtnTxt);
            if (!TextUtils.isEmpty(womCampaignDetail.launchBtnTxt))
                btnLaunchPopup.setText(womCampaignDetail.launchBtnTxt);
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadii(new float[]{1, 1, 0, 0, 1, 1, 0, 0});
            shape.setStroke(1, Color.parseColor("#FFFFFF"));
            shape.setBounds(0, 1, 1, 0);
            if (womCampaignDetail.launchBtnTxtColor != null) {
                btnLaunchPopup.setTextColor(Color.parseColor(womCampaignDetail.launchBtnTxtColor));
                btnRemindLater.setTextColor(Color.parseColor(womCampaignDetail.launchBtnTxtColor));
            }
            if (womCampaignDetail.launchBtnBgColor != null) {
                shape.setColor(Color.parseColor(womCampaignDetail.launchBtnBgColor));

                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    btnLaunchPopup.setBackground(shape);
                    btnRemindLater.setBackground(shape);
                } else {
                    btnLaunchPopup.setBackgroundDrawable(shape);
                    btnRemindLater.setBackgroundDrawable(shape);
                }
            }
            launchBarLayout.setBackgroundColor(Color.parseColor(womCampaignDetail.launchBgColor));
            if (isMiniNotification) {
                DisplayMetrics metrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int popupWidth = metrics.widthPixels;
                int popupHeight = utils.isScreenPortrait() ? (int) (metrics.heightPixels * 0.2) : (int) (metrics.widthPixels * 0.2);
                if (miniNotification == null ? true : !miniNotification.isShowing()) {
                    miniNotification = new PopupWindow(launchBarLayout, popupWidth, popupHeight, false);
                    miniNotification.setAnimationStyle(R.style.appvirality_slide_activity);
                    miniNotification.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                    miniNotification.showAtLocation(launchBarLayout, Gravity.BOTTOM, 0, 0);
                    appVirality.popUpShown(womCampaignDetail.campaignId);
                }
            } else {
                if (popUp == null ? true : !popUp.isShowing()) {
                    popUp = new Dialog(activity);
                    popUp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    popUp.setCancelable(false);
                    popUp.setContentView(launchBarLayout);
                    popUp.show();
                    appVirality.popUpShown(womCampaignDetail.campaignId);
                }
            }

            btnLaunchPopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent growthHackIntent = new Intent(activity, GrowthHackActivity.class);
                        growthHackIntent.putExtra("campaign_details", campaignDetails);
                        growthHackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(growthHackIntent);
                        appVirality.recordImpressionsClicks(womCampaignDetail.campaignId, false, true);
                        dismissLaunchBar();
                    } catch (Exception e) {
                    }
                }
            });
            btnRemindLater.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        appVirality.saveRemindLater(womCampaignDetail);
                        dismissLaunchBar();
                    } catch (Exception e) {
                    }
                }
            });
            appVirality.recordImpressionsClicks(womCampaignDetail.campaignId, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissLaunchBar() {
        try {
            if (miniNotification != null && miniNotification.isShowing())
                miniNotification.dismiss();

            if (popUp != null && popUp.isShowing())
                popUp.dismiss();
        } catch (Exception e) {

        }
    }

    private int getLaunchIcon(int launchId) {
        switch (launchId) {
            case 1:
                return R.drawable.megaphone;
            case 2:
                return R.drawable.alert;
            case 3:
                return R.drawable.bell;
            case 4:
                return R.drawable.flag;
            case 5:
                return R.drawable.trophy;
            default:
                return R.drawable.megaphone;
        }
    }

}
