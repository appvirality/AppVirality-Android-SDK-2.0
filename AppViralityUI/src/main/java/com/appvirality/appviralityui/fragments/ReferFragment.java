package com.appvirality.appviralityui.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.SocialItem;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.Utils;
import com.appvirality.appviralityui.adapters.GridViewAdapter;

import java.util.ArrayList;

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
    TextView txtShareLink, txtShareCode;
    RelativeLayout imgTitleLayout, titleDescLayout, upperLayout;
    ImageView ivCampaignBg;
    int imgWidth = 1040, imgHeight = 910;
    Utils utils;

    public ReferFragment() {
    }

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
                upperLayout = (RelativeLayout) view.findViewById(R.id.upper_layout);
                imgTitleLayout = (RelativeLayout) view.findViewById(R.id.img_title_layout);
                titleDescLayout = (RelativeLayout) view.findViewById(R.id.title_desc_layout);
                final TextView txtOfferTitle = (TextView) view.findViewById(R.id.campaign_title);
                final TextView txtOfferDescription = (TextView) view.findViewById(R.id.campaign_desc);
                final TextView txtNoSocialInstalled = (TextView) view.findViewById(R.id.appvirality_no_social_installed);
                final LinearLayout referralShareUrl = (LinearLayout) view.findViewById(R.id.appvirality_custom_share_link);
                socialItemsLayout = (LinearLayout) view.findViewById(R.id.social_items_layout);

                txtShareLink = (TextView) view.findViewById(R.id.appvirality_share_link);
                txtShareCode = (TextView) view.findViewById(R.id.appvirality_share_code);

                metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                Bitmap campaignImg = utils.readImageFromExtStorage(womCampaignDetail.campaignId + "-" + womCampaignDetail.campaignImgUrl.substring(womCampaignDetail.campaignImgUrl.lastIndexOf("/") + 1));
                if (campaignImg != null) {
                    ivCampaignBg.setImageBitmap(campaignImg);
                }

                int maxWidth = metrics.widthPixels - 4 * (int) getResources().getDimension(R.dimen.campaign_image_margin);
                float aspectRatio = (float) imgWidth / imgHeight;
                int newWidth = maxWidth;
                int newHeight = (int) (newWidth / aspectRatio);
                imgTitleLayout.getLayoutParams().width = newWidth;
                imgTitleLayout.getLayoutParams().height = newHeight;
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleDescLayout.getLayoutParams();
                int leftRightMargin = (int) getResources().getDimension(R.dimen.campaign_title_desc_left_right_margin);
                params.setMargins(leftRightMargin, newHeight / 2, leftRightMargin, Math.abs((int) getResources().getDimension(R.dimen.av_ref_code_margin_top)));

                txtOfferTitle.setText(Html.fromHtml("<b>" + womCampaignDetail.campaignTitle + "</b>"));
                txtOfferDescription.setText(Html.fromHtml(womCampaignDetail.campaignDescription));
                if (womCampaignDetail.noSocialActionsFound) {
                    txtNoSocialInstalled.setText(womCampaignDetail.noSocialActionsMessage);
                    txtNoSocialInstalled.setVisibility(View.VISIBLE);
                }
                txtShareCode.setOnClickListener(new View.OnClickListener() {
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
                    txtShareCode.setVisibility(View.GONE);

                addUpperSocialItems(inflater);

                if (!TextUtils.isEmpty(womCampaignDetail.campaignBgColor)) {
                    upperLayout.setBackgroundColor(Color.parseColor(womCampaignDetail.campaignBgColor));
                }
                if (!TextUtils.isEmpty(womCampaignDetail.campaignTitleColor))
                    txtOfferTitle.setTextColor(Color.parseColor(womCampaignDetail.campaignTitleColor));
                if (!TextUtils.isEmpty(womCampaignDetail.campaignDescriptionColor))
                    txtOfferDescription.setTextColor(Color.parseColor(womCampaignDetail.campaignDescriptionColor));
                int bgDrawableId = R.drawable.bg_rect_gray;
                Drawable bgDrawable = android.os.Build.VERSION.SDK_INT >= 21 ? getResources().getDrawable(bgDrawableId, null) : getResources().getDrawable(bgDrawableId);
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    referralShareUrl.setBackground(bgDrawable);
                    txtShareCode.setBackground(bgDrawable);
                } else {
                    referralShareUrl.setBackgroundDrawable(bgDrawable);
                    txtShareCode.setBackgroundDrawable(bgDrawable);
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
                    imageView.setImageDrawable(getActivity().getPackageManager().getDrawable(socialItem.packagename, socialItem.resId, null));
                }
                view.setTag(i);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = (int) v.getTag();
                        if (index == 3 && womCampaignDetail.allSocialItems.size() > 4) {
                            showSocialItemsPopUp(inflater);
                        } else {
                            appVirality.invokeInvite(womCampaignDetail.allSocialItems.get(index), womCampaignDetail.isRewardExists);
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
                appVirality.invokeInvite(womCampaignDetail.allSocialItems.get(position), womCampaignDetail.isRewardExists);
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
        txtShareCode.setText(Html.fromHtml("Your Code : " + "<b><font color=black>" + womCampaignDetail.referralCode.toUpperCase() + "</font></b>"));
    }

}
