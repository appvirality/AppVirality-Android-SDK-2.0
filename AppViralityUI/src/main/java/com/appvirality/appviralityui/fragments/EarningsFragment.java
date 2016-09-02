package com.appvirality.appviralityui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.Utils;
import com.appvirality.appviralityui.activities.GrowthHackActivity;
import com.appvirality.appviralityui.custom.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by AppVirality on 3/28/2016.
 */
public class EarningsFragment extends Fragment {

    private boolean friendsVisible, couponsVisible, couponPoolVisible;
    CampaignDetail womCampaignDetail;
    ArrayList<CampaignDetail> campaignDetails;
    private EditText edUserParms;
    //    private TextView tvRefCodeFixed;
    private GradientDrawable grdEarnings, grdLinkBG, grdFriends, grdCoupons, grdCouponPool;
    private ProgressBar progress, progressBarFriends, progressBarCoupons, progressBar, progressBarCouponPool;
    private ImageView ddCustomLink, ddReferredUsers, ddCoupon, ddCouponPool;
    private TableLayout tblFriends, tblUserRewards;
    private LinearLayout linkLayout, earningsRetry, referrersDetails, link, friendsNetFailLayout, userCouponsNetFailLayout, couponPoolNetFailLayout, tblCouponPool, allEarningsLayout, customUrlLayout;
    private LinearLayout couponLayout, friendsLoading, couponsLoading, couponPoolLoading, couponPoolLayout;
    private TableLayout tblCoupons;
    TableRow trEarningsHeader;
    AppVirality appVirality;
    View fragmentView;
    LayoutInflater inflater;
    Utils utils;
    DisplayMetrics metrics;
    boolean isEarnings;
    HashMap<String, Integer> userPendingRewards = new HashMap<>();
    int earningsBarColor;
    Button btnSaveLink;

    public EarningsFragment() {
        appVirality = AppVirality.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        campaignDetails = (ArrayList<CampaignDetail>) getArguments().getSerializable("campaign_details");
        isEarnings = getArguments().getBoolean("is_earnings", false);
        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int[] attrs = {R.attr.av_earnings_bar_color};
        TypedArray typedValue = getActivity().obtainStyledAttributes(attrs);
        earningsBarColor = typedValue.getColor(0, Color.BLACK);
        final View view = inflater.inflate(R.layout.fragment_earnings, container, false);
        fragmentView = view;
        try {
            trEarningsHeader = (TableRow) view.findViewById(R.id.tr_earnings_header);
            allEarningsLayout = (LinearLayout) view.findViewById(R.id.all_earnings_layout);
            customUrlLayout = (LinearLayout) view.findViewById(R.id.custom_url_layout);
            edUserParms = (EditText) view.findViewById(R.id.appvirality_custom_param);
            btnSaveLink = (Button) view.findViewById(R.id.appvirality_savelink);
//            tvRefCodeFixed = (TextView) view.findViewById(R.id.tv_ref_code_fixed);
            linkLayout = (LinearLayout) view.findViewById(R.id.appvirality_custom_link_layout);
            referrersDetails = (LinearLayout) view.findViewById(R.id.appvirality_settings_friends);
            link = (LinearLayout) view.findViewById(R.id.appvirality_custom_link_top);
            progress = (ProgressBar) view.findViewById(R.id.appvirality_progressbar);
            ddCustomLink = (ImageView) view.findViewById(R.id.appvirality_dropdown1);
            ddReferredUsers = (ImageView) view.findViewById(R.id.appvirality_dropdown2);
            tblFriends = (TableLayout) view.findViewById(R.id.appvirality_tblrewarded);
            tblUserRewards = (TableLayout) view.findViewById(R.id.appvirality_user_earnings);
            couponLayout = (LinearLayout) view.findViewById(R.id.appvirality_coupons_layout);
            couponPoolLayout = (LinearLayout) view.findViewById(R.id.coupon_pools_header);
            tblCouponPool = (LinearLayout) view.findViewById(R.id.coupon_pools_tbl);
            tblCoupons = (TableLayout) view.findViewById(R.id.appvirality_tblcoupons);
            ddCoupon = (ImageView) view.findViewById(R.id.appvirality_dropdown_coupon);
            ddCouponPool = (ImageView) view.findViewById(R.id.coupon_pools_dropdown);
            progressBarFriends = (ProgressBar) view.findViewById(R.id.appvirality_progressbar_friends);
            progressBarCoupons = (ProgressBar) view.findViewById(R.id.appvirality_progressbar_cpn);
            progressBarCouponPool = (ProgressBar) view.findViewById(R.id.coupon_pool_progressbar);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            friendsNetFailLayout = (LinearLayout) view.findViewById(R.id.user_friends_network_message);
            userCouponsNetFailLayout = (LinearLayout) view.findViewById(R.id.user_coupons_network_message);
            couponPoolNetFailLayout = (LinearLayout) view.findViewById(R.id.coupon_pool_network_message);
            earningsRetry = (LinearLayout) view.findViewById(R.id.appvirality_userearnings_network_message);
            friendsLoading = (LinearLayout) view.findViewById(R.id.appvirality_progress_friends_layout);
            couponsLoading = (LinearLayout) view.findViewById(R.id.appvirality_progress_coupon_layout);
            couponPoolLoading = (LinearLayout) view.findViewById(R.id.coupon_pool_progress_layout);
            this.inflater = inflater;
            utils = new Utils(getActivity());
            womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);
            if (isEarnings)
                customUrlLayout.setVisibility(View.GONE);

            btnSaveLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSaveLink.setEnabled(false);
                    progress.setVisibility(View.VISIBLE);
                    appVirality.customizeReferralCode(edUserParms.getText().toString().trim(), new AppVirality.CustomizeRefCodeListener() {
                        @Override
                        public void onCustomRefCodeSet(boolean isSet, String errorMsg) {
                            try {
                                if (isSet) {
                                    ((GrowthHackActivity) getActivity()).refCodeModified = true;
                                    Toast.makeText(getActivity(), "Referral Code changed", Toast.LENGTH_SHORT).show();
                                    for (CampaignDetail campaignDetail : campaignDetails) {
                                        campaignDetail.referralCode = edUserParms.getText().toString().trim();
                                        String subShortCode = campaignDetail.shortCode.substring(campaignDetail.shortCode.lastIndexOf("-") + 1);
                                        campaignDetail.shortCode = campaignDetail.referralCode + "-" + subShortCode;
                                        String shareBaseUrl = campaignDetail.shareUrl.substring(0, campaignDetail.shareUrl.lastIndexOf("/") + 1);
                                        campaignDetail.shareUrl = shareBaseUrl + campaignDetail.shortCode;
                                    }

//                                    womCampaignDetail.referralCode = edUserParms.getText().toString().trim();
//                                    String subShortCode = womCampaignDetail.shortCode.substring(womCampaignDetail.shortCode.lastIndexOf("-") + 1);
//                                    womCampaignDetail.shortCode = womCampaignDetail.referralCode + "-" + subShortCode;
//                                    String shareBaseUrl = womCampaignDetail.shareUrl.substring(0, womCampaignDetail.shareUrl.lastIndexOf("/") + 1);
//                                    womCampaignDetail.shareUrl = shareBaseUrl + womCampaignDetail.shortCode;
//                                    campaignDetails = utils.updateCampaignDetails(campaignDetails, womCampaignDetail);
                                    ((GrowthHackActivity) getActivity()).campaignDetails = campaignDetails;
                                } else
                                    Toast.makeText(getActivity(), "Problem in saving custom link, try again later.", Toast.LENGTH_SHORT).show();
                                progress.setVisibility(View.INVISIBLE);
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(edUserParms.getWindowToken(), 0);
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            });

            edUserParms.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String newStr = edUserParms.getText().toString().trim();
                    if (newStr.length() > 0 && !newStr.equalsIgnoreCase(womCampaignDetail.referralCode.split("-")[0])) {
                        btnSaveLink.setEnabled(true);
                    } else {
                        btnSaveLink.setEnabled(false);
                    }
                }
            });

            grdEarnings = new GradientDrawable();
            grdEarnings.setColor(earningsBarColor);
            grdEarnings.setCornerRadii(new float[]{10, 10, 10, 10, 0, 0, 0, 0});
            setBackground(trEarningsHeader, grdEarnings);

            if (utils.hasUserWillChoose(campaignDetails)) {
                grdCouponPool = new GradientDrawable();
                grdCouponPool.setColor(earningsBarColor);
                grdCouponPool.setCornerRadius(10);
                setBackground(couponPoolLayout, grdCouponPool);
                view.findViewById(R.id.coupon_pools_header).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        collapseTabs(5);
                        if (!couponPoolVisible) {
                            grdCouponPool.setCornerRadii(new float[]{10, 10, 10, 10, 0, 0, 0, 0});
                            ddCouponPool.setSelected(true);
                            getCouponPools();
                            setBackground(couponPoolLayout, grdCouponPool);
                        } else {
                            collapseTabs(0);
                        }
                    }
                });
            } else {
                view.findViewById(R.id.coupon_pool_layout).setVisibility(View.GONE);
            }

            grdLinkBG = new GradientDrawable();
            grdLinkBG.setColor(earningsBarColor);
            grdLinkBG.setCornerRadius(10);
            setBackground(link, grdLinkBG);
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collapseTabs(1);
                    if (linkLayout.getVisibility() == View.GONE) {
                        grdLinkBG.setCornerRadii(new float[]{10, 10, 10, 10, 0, 0, 0, 0});
                        ddCustomLink.setSelected(true);
                        linkLayout.setVisibility(View.VISIBLE);
                        setBackground(v, grdLinkBG);
                    } else {
                        collapseTabs(0);
                    }
                }
            });

            grdFriends = new GradientDrawable();
            grdFriends.setColor(earningsBarColor);
            grdFriends.setCornerRadius(10);
            setBackground(referrersDetails, grdFriends);
            referrersDetails.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    collapseTabs(2);
                    if (!friendsVisible) {
                        grdFriends.setCornerRadii(new float[]{10, 10, 10, 10, 0, 0, 0, 0});
                        ddReferredUsers.setSelected(true);
                        getUserFriends();
                        setBackground(couponLayout, grdCoupons);
                    } else {
                        collapseTabs(0);
                    }
                }
            });

            ddCoupon.setBackgroundResource(R.drawable.down);
            grdCoupons = new GradientDrawable();
            grdCoupons.setColor(earningsBarColor);
            grdCoupons.setCornerRadius(10);
            setBackground(couponLayout, grdCoupons);
            couponLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collapseTabs(4);
                    if (!couponsVisible) {
                        grdCoupons.setCornerRadii(new float[]{10, 10, 10, 10, 0, 0, 0, 0});
                        ddCoupon.setSelected(true);
                        getCoupons();
                        setBackground(couponLayout, grdCoupons);
                    } else {
                        collapseTabs(0);
                    }
                }
            });

            if (womCampaignDetail != null) {
//                String[] refCode = womCampaignDetail.referralCode.split("-");
//                for (int i = 0; i < refCode.length; i++) {
//                    if (i == 0)
//                        edUserParms.setText(refCode[0]);
//                    else
//                        tvRefCodeFixed.setText("-" + refCode[1]);
//                }
                edUserParms.setText(womCampaignDetail.referralCode);
            }

//            if (!womCampaignDetail.isRewardExists) {
//                tblUserRewards.setVisibility(View.GONE);
//                grdLinkBG.setCornerRadii(new float[]{10, 10, 10, 10, 0, 0, 0, 0});
//                ddCustomLink.setBackgroundResource(R.drawable.av_ui_up);
//                linkLayout.setVisibility(View.VISIBLE);
//                saveLink.setVisibility(View.VISIBLE);
//            }

            tblFriends.setVisibility(View.GONE);
            view.findViewById(R.id.appvirality_progress).setVisibility(View.VISIBLE);

            view.findViewById(R.id.appvirality_earnings_reload).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getUserRewardDetails();
                }
            });

            view.findViewById(R.id.user_coupons_reload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getCoupons();
                }
            });

            view.findViewById(R.id.coupon_pool_reload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getCouponPools();
                }
            });

            view.findViewById(R.id.user_friends_reload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getUserFriends();
                }
            });

            getUserRewardDetails();

            try {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
                    f.setAccessible(true);
                    f.set(edUserParms, R.drawable.cursor_color);
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
            getActivity().finish();
        }
        return view;
    }

    public void getCouponPools() {
        couponPoolLoading.setVisibility(View.VISIBLE);
        progressBarCouponPool.setVisibility(View.VISIBLE);
        couponPoolVisible = true;
        fragmentView.findViewById(R.id.coupon_pool_no_coupons).setVisibility(View.GONE);
        couponPoolNetFailLayout.setVisibility(View.GONE);
        appVirality.getCouponPools(null, new AppVirality.CouponPoolsListener() {
            @Override
            public void onGetCouponPools(JSONObject responseData, String errorMsg) {
//                if (progressBarCouponPool.getVisibility() == View.VISIBLE) {
                if (responseData != null) {
                    JSONArray campaignsArray = null;
                    JSONArray couponPool = new JSONArray();
                    try {
                        String status = responseData.getString("success");
                        if (status != null && Boolean.parseBoolean(status)) {
                            campaignsArray = responseData.getJSONArray("campaigns");
                            for (int i = 0; i < campaignsArray.length(); i++) {
                                JSONArray receivedPoolsArray = campaignsArray.getJSONObject(i).getJSONArray("pools");
                                for (int j = 0; j < receivedPoolsArray.length(); j++) {
                                    couponPool.put(receivedPoolsArray.getJSONObject(j));
                                }
                            }
                        }
                        if (couponPool != null && couponPool.length() > 0) {
                            tblCouponPool.removeAllViewsInLayout();
                            setCouponPool(campaignsArray);
                            couponPoolLoading.setVisibility(View.GONE);
                        } else {
                            fragmentView.findViewById(R.id.coupon_pool_no_coupons).setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {

                    }
                } else {
                    couponPoolNetFailLayout.setVisibility(View.VISIBLE);
                    couponPoolLoading.setVisibility(View.GONE);
                }
                progressBarCouponPool.setVisibility(View.GONE);
//                }
            }
        });
    }

    public void setCouponPool(JSONArray campaignArray) {
        try {
            ViewGroup.LayoutParams rowParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            for (int i = 0; i < campaignArray.length(); i++) {
                JSONObject campaign = campaignArray.getJSONObject(i);
                String ghType = campaign.getString("growthhack");
                JSONArray couponPoolArray = campaign.getJSONArray("pools");
                int userPendingReward = userPendingRewards.containsKey(ghType) ? userPendingRewards.get(ghType) : 0;
                for (int j = 0; j < couponPoolArray.length(); j++) {
                    JSONObject coupon = couponPoolArray.getJSONObject(j);
                    View view = inflater.inflate(R.layout.coupon_pools_item, null, false);
                    RoundedImageView companyLogo = (RoundedImageView) view.findViewById(R.id.company_logo);
                    TextView couponTitle = (TextView) view.findViewById(R.id.coupon_title);
                    TextView couponDesc = (TextView) view.findViewById(R.id.coupon_desc);
                    TextView couponAmount = (TextView) view.findViewById(R.id.coupon_amount);
                    TextView btnClaim = (TextView) view.findViewById(R.id.btn_claim_coupon);

                    if (i == campaignArray.length() - 1 && j == couponPoolArray.length() - 1)
                        view.findViewById(R.id.divider).setVisibility(View.GONE);

                    if (userPendingReward < Integer.parseInt(coupon.getString("value")))
                        btnClaim.setEnabled(false);

                    utils.downloadAndSetImage(coupon.getString("image"), companyLogo);
                    couponTitle.setText(coupon.getString("name"));
                    couponDesc.setText(coupon.getString("details"));
                    couponAmount.setText(Html.fromHtml("Required Credits : " + "<font color=#00BFFF>" + coupon.getString("value") + "</font>"));
                    btnClaim.setTag(coupon.getString("poolId"));
                    btnClaim.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String poolId = (String) v.getTag();
                            redeemFromPool(poolId);
                        }
                    });
                    tblCouponPool.addView(view, rowParams);
                }
            }
            tblCouponPool.setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }
    }

    public void redeemFromPool(String poolId) {
        progressBar.setVisibility(View.VISIBLE);
        appVirality.redeemFromPool(poolId, new AppVirality.CouponRedeemListener() {
            @Override
            public void onResponse(boolean isRedeemed, String errorMsg) {
                try {
                    progressBar.setVisibility(View.GONE);
                    String toastMsg = isRedeemed ? "Coupon claimed successfully" : "Failed to claim coupon";
                    Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
                    if (isRedeemed) {
                        getUserRewardDetails();
                        getCouponPools();
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void getUserRewardDetails() {
        earningsRetry.setVisibility(View.GONE);
        fragmentView.findViewById(R.id.appvirality_progress).setVisibility(View.VISIBLE);
        appVirality.getUserRewardDetail(null, new AppVirality.UserBalanceListener() {
            @Override
            public void onGetRewardDetails(JSONObject responseData, String errorMsg) {
                try {
                    if (responseData != null) {
                        allEarningsLayout.removeAllViews();
                        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
                        if (!responseData.get("userBalance").equals(null)) {
                            JSONArray growthHacks = responseData.getJSONObject("userBalance").getJSONArray("growthHacks");
                            if (growthHacks.length() > 0) {
                                userPendingRewards = new HashMap<>();
                                for (int i = 0; i < growthHacks.length(); i++) {
                                    userPendingRewards.put(growthHacks.getJSONObject(i).getString("ghName"), growthHacks.getJSONObject(i).getInt("pending"));

                                    View earningsLayout = inflater.inflate(R.layout.earnings_layout, null);
                                    TextView tvTitle = (TextView) earningsLayout.findViewById(R.id.tv_title);
                                    TextView tvTotalEarnings = (TextView) earningsLayout.findViewById(R.id.tv_total_earnings);
                                    TextView tvClaimed = (TextView) earningsLayout.findViewById(R.id.tv_claimed);
                                    TextView tvOnHold = (TextView) earningsLayout.findViewById(R.id.tv_on_hold);
                                    String ghName = growthHacks.getJSONObject(i).getString("ghName");
                                    ghName = ghName.replace("_", " ");
                                    tvTitle.setText(ghName + " Earnings");
                                    tvTotalEarnings.setText(growthHacks.getJSONObject(i).getString("total") + " " + growthHacks.getJSONObject(i).getJSONArray("campaigns").getJSONObject(0).getString("rewardUnit"));
                                    tvClaimed.setText(growthHacks.getJSONObject(i).getString("claimed") + " " + growthHacks.getJSONObject(i).getJSONArray("campaigns").getJSONObject(0).getString("rewardUnit"));
                                    tvOnHold.setText(growthHacks.getJSONObject(i).getString("pending") + " " + growthHacks.getJSONObject(i).getJSONArray("campaigns").getJSONObject(0).getString("rewardUnit"));
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    if (i == growthHacks.length() - 1) {
                                        params.setMargins(margin, margin, margin, margin);
                                    } else {
                                        params.setMargins(margin, margin, margin, 0);
                                    }
                                    allEarningsLayout.addView(earningsLayout, params);
                                }
                            } else {
                                allEarningsLayout.addView(utils.getNoInfoTextView("You don't have any earnings yet.", margin));
                            }
                        } else {
                            allEarningsLayout.addView(utils.getNoInfoTextView("You don't have any earnings yet.", margin));
                        }
                        fragmentView.findViewById(R.id.appvirality_progress).setVisibility(View.GONE);
                        tblUserRewards.setVisibility(View.VISIBLE);
                    } else {
                        earningsRetry.setVisibility(View.VISIBLE);
                        fragmentView.findViewById(R.id.appvirality_progress).setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getUserFriends() {
        friendsLoading.setVisibility(View.VISIBLE);
        progressBarFriends.setVisibility(View.VISIBLE);
        friendsVisible = true;
        fragmentView.findViewById(R.id.appvirality_no_friends).setVisibility(View.GONE);
        friendsNetFailLayout.setVisibility(View.GONE);
        appVirality.getFriends(new AppVirality.GetFriendsListener() {
            @Override
            public void onGetFriends(JSONObject responseData, String errorMsg) {
                try {
                    if (responseData != null) {
                        JSONArray friendsArray = responseData.getJSONArray("friends");
                        if (friendsArray.length() > 0) {
                            tblFriends.removeAllViewsInLayout();
                            setUserFriends(friendsArray);
                            friendsLoading.setVisibility(View.GONE);
                        } else {
                            fragmentView.findViewById(R.id.appvirality_no_friends).setVisibility(View.VISIBLE);
                        }
                    } else {
                        friendsNetFailLayout.setVisibility(View.VISIBLE);
                        friendsLoading.setVisibility(View.GONE);
                    }
                    progressBarFriends.setVisibility(View.GONE);
                } catch (Exception e) {
                }
            }
        });
    }

    private void setUserFriends(JSONArray friendsArray) {
        try {
            Bitmap userImage = BitmapFactory.decodeResource(getResources(), R.drawable.user_image);
            int imageWidth = userImage.getWidth();
            int imageHeight = userImage.getHeight();
            userImage = Bitmap.createScaledBitmap(userImage, (int) (imageWidth * 0.6), (int) (imageHeight * 0.6), true);
            tblFriends.removeAllViews();
            for (int i = 0; i < friendsArray.length(); i++) {
                JSONObject referredUserData = friendsArray.getJSONObject(i);
                TableRow tblRow = new TableRow(getActivity());
                LinearLayout profile = new LinearLayout(getActivity());
                profile.setMinimumWidth(imageWidth);
                profile.setMinimumHeight(imageHeight);
                profile.setGravity(Gravity.CENTER);
                RoundedImageView imgUserProfile = new RoundedImageView(getActivity());
                imgUserProfile.setImageBitmap(userImage);
                profile.addView(imgUserProfile);
                TextView txtUserName = new TextView(getActivity());
                String user = TextUtils.isEmpty(referredUserData.getString("name")) ? referredUserData.getString("emailId") : referredUserData.getString("name");
                txtUserName.setText(TextUtils.isEmpty(user) ? "Unknown friend" : user);
                txtUserName.setTextColor(Color.BLACK);

                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rowParams.gravity = Gravity.CENTER_VERTICAL;
                TextView txtRegisterDate = new TextView(getActivity());
                txtRegisterDate.setTextColor(Color.BLACK);
                txtRegisterDate.setText(referredUserData.getString("regDate"));
                txtRegisterDate.setGravity(Gravity.CENTER_VERTICAL);
                txtRegisterDate.setPadding(0, 0, 10, 0);
                tblRow.addView(profile);
                tblRow.addView(txtUserName, rowParams);
                tblRow.addView(txtRegisterDate, rowParams);
                tblFriends.addView(tblRow);
                View lineSeparator = new View(getActivity());
                lineSeparator.setBackgroundColor(Color.parseColor("#bababa"));
                lineSeparator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                tblFriends.addView(lineSeparator);
            }
            tblFriends.setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }
    }

    private void getCoupons() {
        couponsLoading.setVisibility(View.VISIBLE);
        progressBarCoupons.setVisibility(View.VISIBLE);
        userCouponsNetFailLayout.setVisibility(View.GONE);
        couponsVisible = true;
        fragmentView.findViewById(R.id.appvirality_nocoupons).setVisibility(View.GONE);
        appVirality.getUserCoupons(new AppVirality.UserCouponsListener() {
            @Override
            public void onGetCoupons(boolean isSuccess, JSONArray userCoupons, String errorMsg) {
                if (progressBarCoupons.getVisibility() == View.VISIBLE) {
                    if (isSuccess) {
                        if (userCoupons != null && userCoupons.length() > 0) {
                            setUserCoupons(userCoupons);
                            couponsLoading.setVisibility(View.GONE);
                        } else {
                            fragmentView.findViewById(R.id.appvirality_nocoupons).setVisibility(View.VISIBLE);
                            couponsLoading.setVisibility(View.VISIBLE);
                        }
                    } else {
                        userCouponsNetFailLayout.setVisibility(View.VISIBLE);
                        couponsLoading.setVisibility(View.GONE);
                    }
                    progressBarCoupons.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setUserCoupons(JSONArray userCoupons) {
        try {
            tblCoupons.removeAllViewsInLayout();
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            int padding10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

            TextView tvCouponHeader = new TextView(getActivity());
            tvCouponHeader.setText("Coupon");
            tvCouponHeader.setTextColor(Color.BLACK);
            tvCouponHeader.setPadding(padding, padding10, padding, padding10);
            tvCouponHeader.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvCouponValueHeader = new TextView(getActivity());
            tvCouponValueHeader.setText("Value");
            tvCouponValueHeader.setTextColor(Color.BLACK);
            tvCouponValueHeader.setPadding(padding, padding10, padding, padding10);

            TextView tvCouponExpiryHeader = new TextView(getActivity());
            tvCouponExpiryHeader.setText("Expiry");
            tvCouponExpiryHeader.setTextColor(Color.BLACK);
            tvCouponExpiryHeader.setPadding(padding, padding10, padding, padding10);
            TableRow tblRowHeader = new TableRow(getActivity());
            tblRowHeader.addView(tvCouponHeader);
            tblRowHeader.addView(tvCouponValueHeader);
            tblRowHeader.addView(tvCouponExpiryHeader);
            tblRowHeader.setBackgroundColor(Color.parseColor("#A6A6A4"));

            tblCoupons.addView(tblRowHeader);

            for (int i = 0; i < userCoupons.length(); i++) {
                TableRow tblRow = new TableRow(getActivity());

                TextView tvCoupon = new TextView(getActivity());
                tvCoupon.setText(userCoupons.getJSONObject(i).getString("couponCode"));
                tvCoupon.setTextColor(Color.BLACK);
                tvCoupon.setPadding(padding, padding, padding, padding);
                tvCoupon.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                tvCoupon.setTag(userCoupons.getJSONObject(i).getString("couponCode"));

                TextView tvCouponValue = new TextView(getActivity());
                tvCouponValue.setText(userCoupons.getJSONObject(i).getString("couponValue") + " " + userCoupons.getJSONObject(i).getString("couponUnit"));
                tvCouponValue.setTextColor(Color.BLACK);
                tvCouponValue.setPadding(padding, padding10, padding, padding10);

                TextView tvCouponExpiry = new TextView(getActivity());
                tvCouponExpiry.setText(userCoupons.getJSONObject(i).getString("couponExpiry"));
                tvCouponExpiry.setTextColor(Color.BLACK);
                tvCouponExpiry.setPadding(padding, padding10, padding, padding10);

                tblRow.addView(tvCoupon);
                tblRow.addView(tvCouponValue);
                tblRow.addView(tvCouponExpiry);

                tvCoupon.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (android.os.Build.VERSION.SDK_INT >= 11) {
                            try {
                                ClipboardManager myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData myClip = ClipData.newPlainText("Coupon Code", v.getTag().toString());
                                myClipboard.setPrimaryClip(myClip);

                                Toast.makeText(getActivity(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                            }
                        }

                        return false;
                    }
                });
                if (i % 2 == 0)
                    tblRow.setBackgroundColor(Color.parseColor("#ffffff"));
                else
                    tblRow.setBackgroundColor(Color.parseColor("#E6E6E5"));
                tblCoupons.addView(tblRow);
            }
            tblCoupons.setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }
    }

    private void setBackground(View v, GradientDrawable grdrawable) {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            v.setBackground(grdrawable);
        } else {
            v.setBackgroundDrawable(grdrawable);
        }
    }

    private void collapseTabs(int tabId) {
        if (tabId != 1) {
            grdLinkBG.setCornerRadius(10);
            ddCustomLink.setSelected(false);
            linkLayout.setVisibility(View.GONE);
            setBackground(link, grdLinkBG);
        }

        if (tabId != 2) {
            grdFriends.setCornerRadius(10);
            progressBarFriends.setVisibility(View.GONE);
            friendsVisible = false;
            ddReferredUsers.setSelected(false);
            friendsNetFailLayout.setVisibility(View.GONE);
            friendsLoading.setVisibility(View.GONE);
            tblFriends.setVisibility(View.GONE);
            setBackground(referrersDetails, grdFriends);
        }

        if (tabId != 4) {
            grdCoupons.setCornerRadius(10);
            progressBarCoupons.setVisibility(View.GONE);
            couponsVisible = false;
            ddCoupon.setSelected(false);
            userCouponsNetFailLayout.setVisibility(View.GONE);
            couponsLoading.setVisibility(View.GONE);
            tblCoupons.setVisibility(View.GONE);
            setBackground(couponLayout, grdCoupons);
        }

        if (tabId != 5 && utils.hasUserWillChoose(campaignDetails)) {
            grdCouponPool.setCornerRadius(10);
            progressBarCouponPool.setVisibility(View.GONE);
            couponPoolVisible = false;
            ddCouponPool.setSelected(false);
            couponPoolNetFailLayout.setVisibility(View.GONE);
            couponPoolLoading.setVisibility(View.GONE);
            tblCouponPool.setVisibility(View.GONE);
            setBackground(couponPoolLayout, grdCouponPool);
        }
    }
}
