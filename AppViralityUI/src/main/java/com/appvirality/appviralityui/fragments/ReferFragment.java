package com.appvirality.appviralityui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.SocialAction;
import com.appvirality.SocialItem;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.Utils;
import com.appvirality.appviralityui.activities.InviteContactsActivity;
import com.appvirality.appviralityui.activities.WebViewActivity;
import com.appvirality.appviralityui.adapters.GridViewAdapter;

import java.util.ArrayList;

import static com.appvirality.appviralityui.R.id.appvirality_share_link;

/**
 * Created by AppVirality on 3/28/2016.
 */
public class ReferFragment extends Fragment {

    AppVirality appVirality;
    ArrayList<CampaignDetail> campaignDetails;
    CampaignDetail womCampaignDetail;
    LinearLayout socialItemsLayout;
    public PopupWindow popupWindow;
    DisplayMetrics metrics;
    TextView txtShareLink, txtShareCode, tvTnC, tvShareVia;
    ImageView ivCampaignBg;
    Utils utils;
    Button btnInviteContacts;
    public static final int READ_CONTACTS_PERMISSION_CODE = 100;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refer, container, false);

        try {
            appVirality = AppVirality.getInstance(getActivity());
            utils = new Utils(getActivity());
            campaignDetails = (ArrayList<CampaignDetail>) getArguments().getSerializable("campaign_details");
            womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);

            if (womCampaignDetail != null) {
                ivCampaignBg = (ImageView) view.findViewById(R.id.iv_campaign_bg);
                final TextView txtOfferDescription = (TextView) view.findViewById(R.id.campaign_desc);
                final TextView txtNoSocialInstalled = (TextView) view.findViewById(R.id.appvirality_no_social_installed);
                final LinearLayout referralShareUrl = (LinearLayout) view.findViewById(R.id.appvirality_custom_share_link);
                socialItemsLayout = (LinearLayout) view.findViewById(R.id.social_items_layout);
                btnInviteContacts = (Button) view.findViewById(R.id.btn_invite_contacts);

                tvShareVia = (TextView) view.findViewById(R.id.tv_or_share_via);
                txtShareLink = (TextView) view.findViewById(appvirality_share_link);
                txtShareCode = (TextView) view.findViewById(R.id.appvirality_share_code);
                tvTnC = (TextView) view.findViewById(R.id.tv_tnc);

                metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                if (!TextUtils.isEmpty(womCampaignDetail.campaignImgUrl)) {
                    Bitmap campaignImg = utils.readImageFromExtStorage(appVirality.getCampaignImagePath());
                    if (campaignImg != null && !campaignImg.equals("")) {
                        ivCampaignBg.setVisibility(View.VISIBLE);
                        ivCampaignBg.setImageBitmap(campaignImg);
                    } else if (womCampaignDetail.campaignImgUrl.contains("app-poster.png")) {
                        ivCampaignBg.setVisibility(View.VISIBLE);
                        ivCampaignBg.setImageResource(R.drawable.refer_image);
                    }
                }

                if (!TextUtils.isEmpty(womCampaignDetail.campaignDescription)) {
                    txtOfferDescription.setText(Html.fromHtml(womCampaignDetail.campaignDescription));
                } else {
                    txtOfferDescription.setVisibility(View.GONE);
                }
                if (womCampaignDetail.noSocialActionsFound) {
                    txtNoSocialInstalled.setText(womCampaignDetail.noSocialActionsMessage);
                    txtNoSocialInstalled.setVisibility(View.VISIBLE);
                }
                view.findViewById(R.id.ref_code_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (android.os.Build.VERSION.SDK_INT >= 11) {
                            try {
                                ClipboardManager myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData myClip = ClipData.newPlainText("Referral Code", womCampaignDetail.referralCode);
                                myClipboard.setPrimaryClip(myClip);

                                Toast.makeText(getActivity(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                            }
                        }
                    }
                });
                referralShareUrl.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View view) {
                        if (android.os.Build.VERSION.SDK_INT >= 11) {
                            try {
                                ClipboardManager myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData myClip = ClipData.newPlainText("Share Url", womCampaignDetail.shareUrl);
                                myClipboard.setPrimaryClip(myClip);

                                Toast.makeText(getActivity(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                appVirality.copiedToClipboard();
                            } catch (Exception e) {
                            }
                        }
                    }
                });

                refreshLinkCode(campaignDetails);
                if (TextUtils.isEmpty(womCampaignDetail.referralCode))
                    view.findViewById(R.id.ref_code_layout).setVisibility(View.GONE);

                addUpperSocialItems(inflater);

                if (womCampaignDetail.campaignSocialActions.contains(new SocialAction("InviteContacts"))
                        && (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 || Utils.hasPermission(getActivity(), Manifest.permission.READ_CONTACTS))) {
                    tvShareVia.setText("Or Share Via");
                    if (!TextUtils.isEmpty(womCampaignDetail.campaignTitleColor)) {
                        btnInviteContacts.setBackgroundColor(Color.parseColor(womCampaignDetail.campaignTitleColor));
                    }
                    btnInviteContacts.setVisibility(View.VISIBLE);
                    btnInviteContacts.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Utils.hasPermission(getActivity(), Manifest.permission.READ_CONTACTS)) {
                                try {
                                    Intent intent = new Intent(getActivity(), InviteContactsActivity.class);
                                    intent.putExtra("campaign_detail", womCampaignDetail);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_CODE);
                            }
                        }
                    });
                }

                tvTnC.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            startActivity(new Intent(getActivity(), WebViewActivity.class).putExtra("campaign_id", womCampaignDetail.campaignId));
                        } catch (Exception e) {
                        }
                    }
                });

                if (!TextUtils.isEmpty(womCampaignDetail.campaignBgColor)) {
                    view.setBackgroundColor(Color.parseColor(womCampaignDetail.campaignBgColor));
                }
//                if (!TextUtils.isEmpty(womCampaignDetail.campaignTitleColor)) {
//                    int titleColor = Color.parseColor(womCampaignDetail.campaignTitleColor);
//                    tvShareVia.setTextColor(titleColor);
//                    ((TextView) view.findViewById(R.id.tv_your_ref_link)).setTextColor(titleColor);
//                }
                if (!TextUtils.isEmpty(womCampaignDetail.campaignDescriptionColor)) {
                    int descColor = Color.parseColor(womCampaignDetail.campaignDescriptionColor);
                    //tvShareVia.setTextColor(descColor);
                    //((TextView) view.findViewById(R.id.tv_your_code)).setTextColor(descColor);
                    //((TextView) view.findViewById(R.id.tv_your_ref_link)).setTextColor(descColor);
                    txtOfferDescription.setTextColor(descColor);
                    txtNoSocialInstalled.setTextColor(descColor);
                    txtShareLink.setTextColor(descColor);
                    txtShareCode.setTextColor(descColor);
                    tvTnC.setTextColor(descColor);
                }
            } else {
                getActivity().finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            getActivity().finish();
        }
        return view;
    }

    private void addUpperSocialItems(final LayoutInflater inflater) {
        if (womCampaignDetail.allSocialItems.size() > 0) {
            for (int i = 0; i < womCampaignDetail.allSocialItems.size() && i < 4; i++) {
                SocialItem socialItem = womCampaignDetail.allSocialItems.get(i);
                View view = inflater.inflate(R.layout.grid_item, null, false);
                ImageView imageView = (ImageView) view.findViewById(R.id.appvirality_image);
                TextView textView = (TextView) view.findViewById(R.id.appvirality_text);
                if (i == 3 && womCampaignDetail.allSocialItems.size() > 4) {
                    textView.setText("More");
                    imageView.setImageResource(R.drawable.more);
                } else {
                    textView.setText(socialItem.appname);
                    if (!socialItem.isCustom || !TextUtils.isEmpty(socialItem.packagename)) {
                        imageView.setImageDrawable(getActivity().getPackageManager().getDrawable(socialItem.packagename, socialItem.resId, null));
                    } else if (socialItem.isCustom) {
                        if (socialItem.resId != 0)
                            imageView.setImageResource(socialItem.resId);
                        view.setTag(R.string.custom_impl_tag, true);
                    }
                }
//                if (!TextUtils.isEmpty(womCampaignDetail.campaignDescriptionColor))
//                    textView.setTextColor(Color.parseColor(womCampaignDetail.campaignDescriptionColor));
                view.setTag(i);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = (int) v.getTag();
                        Object isCustomImpl = v.getTag(R.string.custom_impl_tag);
                        if (index == 3 && womCampaignDetail.allSocialItems.size() > 4) {
                            showSocialItemsPopUp(inflater);
                        } else {
                            if (isCustomImpl == null || !(boolean) isCustomImpl) {
                                appVirality.invokeInvite(womCampaignDetail.allSocialItems.get(index));
                            } else if ((boolean) isCustomImpl) {
                                SocialItem socialItem = womCampaignDetail.allSocialItems.get(index);
                                invokeCustomInvite(socialItem, getSocialActionForId(socialItem.socialActionId));
                            }
                        }
                    }
                });
                socialItemsLayout.addView(view, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            }
        } else {
            socialItemsLayout.setVisibility(View.GONE);
        }
    }

    private void showSocialItemsPopUp(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.custom_share_dialog, null, false);
        GridView gridView = (GridView) view.findViewById(R.id.appvirality_gridView);
        GridViewAdapter adapter = new GridViewAdapter(getActivity(), R.layout.grid_item, womCampaignDetail.allSocialItems);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popupWindow.dismiss();
                Object isCustomImpl = view.getTag(R.string.custom_impl_tag);
                if (isCustomImpl == null || !(boolean) isCustomImpl) {
                    appVirality.invokeInvite(womCampaignDetail.allSocialItems.get(position));
                } else if ((boolean) isCustomImpl) {
                    SocialItem socialItem = womCampaignDetail.allSocialItems.get(position);
                    invokeCustomInvite(socialItem, getSocialActionForId(socialItem.socialActionId));
                }
            }
        });
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        popupWindow = new PopupWindow(view, metrics.widthPixels, metrics.heightPixels - statusBarHeight, false);
        popupWindow.setAnimationStyle(R.style.appvirality_slide_activity);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    public void refreshLinkCode(ArrayList<CampaignDetail> details) {
        campaignDetails = details;
        womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);
        if (!TextUtils.isEmpty(womCampaignDetail.shareUrl))
            txtShareLink.setText(womCampaignDetail.shareUrl + "/");
        txtShareCode.setText(/*Html.fromHtml("Your Referral Code : " + "<b><font color=" + womCampaignDetail.campaignTitleColor + ">" +*/ womCampaignDetail.referralCode.toUpperCase() /*+ "</font></b>")*/);
    }

    private void invokeCustomInvite(SocialItem socialItem, SocialAction socialAction) {
        switch (socialItem.appname) {
            case "custom_social_action_1":
                /**
                 * Replace "custom_social_action_1" with your custom social action
                 * name and write your custom social action implementation here.
                 **/
                break;
        }
        appVirality.recordSocialAction(socialItem.socialActionId, Constants.GrowthHackType.Word_of_Mouth, womCampaignDetail.shortCode, constructShareMsg(socialAction));
    }

    private SocialAction getSocialActionForId(String socialActionId) {
        for (SocialAction socialAction : womCampaignDetail.campaignSocialActions) {
            if (socialAction.socialActionId.equals(socialActionId))
                return socialAction;
        }
        return null;
    }

    private String constructShareMsg(SocialAction socialAction) {
        if (socialAction != null) {
            return socialAction.shareMessage.replaceAll("SHARE_URL", " " + socialAction.shareUrl + " ").replaceAll("SHARE_CODE", " " + womCampaignDetail.referralCode + " ");
        } else {
            return womCampaignDetail.campaignSocialActions.get(0).shareMessage.replaceAll("SHARE_URL", " " + womCampaignDetail.shareUrl + " ").replaceAll("SHARE_CODE", " " + womCampaignDetail.referralCode + " ");
        }
    }

}
