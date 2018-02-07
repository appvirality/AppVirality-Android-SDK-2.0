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
import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by AppVirality on 10/5/2016.
 */
public class EarningsFragment extends Fragment {

    AppVirality appVirality;
    ArrayList<CampaignDetail> campaignDetails;
    CampaignDetail womCampaignDetail;
    boolean isEarnings;
    LinearLayout userEarningsLayout, allEarningsLayout;
    HashMap<String, Integer> userApprovedRewards = new HashMap<>();
    LayoutInflater inflater;
    Utils utils;
    View fragmentView;
    DisplayMetrics metrics;
    int earningsBarColor;
    GradientDrawable grdTwoRounded, grdFourRounded;
    LinearLayout couponPoolsLayout, friendsLayout, userCouponsLayout;
    LinearLayout earningsHeader, couponPoolsHeader, customRefCodeHeader, friendsHeader, userCouponsHeader;
    LinearLayout couponPools, customRefCodeInnerLayout, friendsInnerLayout, userCouponsInnerLayout;
    LinearLayout prevNextBtnLayout;
    View activeHeader, activeLayout;
    ImageView activeDropDown;
    EditText editRefCode;
    Button btnSaveRefCode;
    ProgressBar progressBar, progressBarRefCode;
    TableLayout friendsTable, userCouponsTable;
    int pageIndex = 1, pageSize = 5;
    TextView tvPrev, tvNext;
    boolean isReload;

    enum TabName {
        Earnings,
        CouponPools,
        Friends,
        UserCoupons
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_earnings, container, false);
        try {
            appVirality = AppVirality.getInstance(getActivity());
            this.inflater = inflater;
            utils = new Utils(getActivity());
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int[] attrs = {R.attr.av_earnings_bar_color};
            TypedArray typedValue = getActivity().obtainStyledAttributes(attrs);
            earningsBarColor = typedValue.getColor(0, Color.BLACK);
            grdTwoRounded = new GradientDrawable();
            grdFourRounded = new GradientDrawable();
            grdTwoRounded.setColor(earningsBarColor);
            grdFourRounded.setColor(earningsBarColor);
            grdTwoRounded.setCornerRadii(new float[]{10, 10, 10, 10, 0, 0, 0, 0});
            grdFourRounded.setCornerRadius(10);
            campaignDetails = (ArrayList<CampaignDetail>) getArguments().getSerializable("campaign_details");
            isEarnings = getArguments().getBoolean("is_earnings", false);
            userEarningsLayout = (LinearLayout) view.findViewById(R.id.earnings_layout);
            earningsHeader = (LinearLayout) view.findViewById(R.id.earnings_header_layout);
            allEarningsLayout = (LinearLayout) view.findViewById(R.id.all_earnings_layout);
            couponPoolsHeader = (LinearLayout) view.findViewById(R.id.coupon_pools_header);
            customRefCodeHeader = (LinearLayout) view.findViewById(R.id.custom_ref_code_header);
            friendsLayout = (LinearLayout) view.findViewById(R.id.user_friends_layout);
            friendsHeader = (LinearLayout) view.findViewById(R.id.friends_header);
            friendsInnerLayout = (LinearLayout) view.findViewById(R.id.friends_inner_layout);
            friendsTable = (TableLayout) view.findViewById(R.id.friends_table);
            couponPoolsLayout = (LinearLayout) view.findViewById(R.id.coupon_pools_layout);
            userCouponsLayout = (LinearLayout) view.findViewById(R.id.user_coupons_layout);
            userCouponsHeader = (LinearLayout) view.findViewById(R.id.user_coupons_header);
            userCouponsInnerLayout = (LinearLayout) view.findViewById(R.id.user_coupons_inner_layout);
            userCouponsTable = (TableLayout) view.findViewById(R.id.user_coupons_table);
            couponPools = (LinearLayout) view.findViewById(R.id.coupon_pools);
            customRefCodeInnerLayout = (LinearLayout) view.findViewById(R.id.custom_ref_code_inner_layout);
            editRefCode = (EditText) view.findViewById(R.id.edit_ref_code);
            btnSaveRefCode = (Button) view.findViewById(R.id.btn_save_ref_code);
            progressBarRefCode = (ProgressBar) view.findViewById(R.id.progress_bar_ref_code);
            prevNextBtnLayout = (LinearLayout) view.findViewById(R.id.prev_next_btn_layout);
            tvPrev = (TextView) view.findViewById(R.id.tv_prev_friends);
            tvNext = (TextView) view.findViewById(R.id.tv_next_friends);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);
            setBackground(earningsHeader, grdTwoRounded);
            setBackground(couponPoolsHeader, grdFourRounded);
            setBackground(customRefCodeHeader, grdFourRounded);
            setBackground(friendsHeader, grdFourRounded);
            setBackground(userCouponsHeader, grdFourRounded);
            if (isEarnings)
                view.findViewById(R.id.custom_ref_code_layout).setVisibility(View.GONE);
            if (!utils.hasUserWillChoose(campaignDetails))
                couponPoolsLayout.setVisibility(View.GONE);
            if (womCampaignDetail != null) {
                editRefCode.setText(womCampaignDetail.referralCode);
                if (!TextUtils.isEmpty(womCampaignDetail.campaignBgColor))
                    view.setBackgroundColor(Color.parseColor(womCampaignDetail.campaignBgColor));
            }
            getUserRewardDetails();
            couponPoolsHeader.setOnClickListener(couponPoolsClickListener);
            customRefCodeHeader.setOnClickListener(customRefCodeClickListener);
            friendsHeader.setOnClickListener(friendsClickListener);
            userCouponsHeader.setOnClickListener(userCouponsClickListener);
            editRefCode.addTextChangedListener(refCodeTextWatcher);
            btnSaveRefCode.setOnClickListener(saveRefCodeClickListener);
            tvPrev.setOnClickListener(prevClickListener);
            tvNext.setOnClickListener(nextClickListener);
            fragmentView = view;
        } catch (Exception ex) {
            Log.i("AppVirality UI", ex.getMessage());
        }

        return view;
    }

    private void getUserRewardDetails() {
        userEarningsLayout.removeView(userEarningsLayout.findViewById(R.string.progress_layout_id));
        userEarningsLayout.removeView(userEarningsLayout.findViewById(R.string.network_error_layout_id));
        userEarningsLayout.addView(getProgressView());
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
                                userApprovedRewards = new HashMap<>();
                                for (int i = 0; i < growthHacks.length(); i++) {
                                    userApprovedRewards.put(growthHacks.getJSONObject(i).getString("ghName"), growthHacks.getJSONObject(i).getInt("approved"));

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
                                allEarningsLayout.addView(getNoDataTextView(getString(R.string.no_earnings_label)));
                            }
                        } else {
                            allEarningsLayout.addView(getNoDataTextView(getString(R.string.no_earnings_label)));
                        }
                    } else {
                        userEarningsLayout.addView(getNetworkErrorView(TabName.Earnings));
                    }
                    userEarningsLayout.removeView(userEarningsLayout.findViewById(R.string.progress_layout_id));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getCouponPools() {
        couponPools.removeView(couponPools.findViewById(R.string.progress_layout_id));
        couponPools.removeView(couponPools.findViewById(R.string.no_data_text_view_id));
        couponPools.removeView(couponPools.findViewById(R.string.network_error_layout_id));
        couponPools.removeAllViews();
        couponPools.addView(getProgressView());
        appVirality.getCouponPools(null, new AppVirality.CouponPoolsListener() {
            @Override
            public void onGetCouponPools(JSONObject responseData, String errorMsg) {
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
                            setCouponPools(campaignsArray);
                        } else {
                            allEarningsLayout.addView(getNoDataTextView(getString(R.string.no_coupon_pool)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    couponPools.addView(getNetworkErrorView(TabName.CouponPools));
                }
                couponPools.removeView(couponPools.findViewById(R.string.progress_layout_id));
            }
        });
    }

    public void setCouponPools(JSONArray campaignArray) {
        try {
            ViewGroup.LayoutParams rowParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            for (int i = 0; i < campaignArray.length(); i++) {
                JSONObject campaign = campaignArray.getJSONObject(i);
                String ghType = campaign.getString("growthhack");
                JSONArray couponPoolArray = campaign.getJSONArray("pools");
                int userApprovedReward = userApprovedRewards.containsKey(ghType) ? userApprovedRewards.get(ghType) : 0;
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

                    if (userApprovedReward < Integer.parseInt(coupon.getString("value")))
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
                    couponPools.addView(view, rowParams);
                }
            }
            couponPools.setVisibility(View.VISIBLE);
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
                    String toastMsg = isRedeemed ? "Coupon claimed successfully" : errorMsg != null ? errorMsg :"Failed to claim coupon";
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

    private void getUserFriends(final int tempPageIndex) {
        friendsInnerLayout.removeView(friendsInnerLayout.findViewById(R.string.progress_layout_id));
        friendsInnerLayout.removeView(friendsInnerLayout.findViewById(R.string.no_data_text_view_id));
        friendsInnerLayout.removeView(friendsInnerLayout.findViewById(R.string.network_error_layout_id));
        friendsTable.removeAllViews();
        friendsInnerLayout.addView(getProgressView());
        appVirality.getFriends(tempPageIndex, pageSize, new AppVirality.GetFriendsListener() {
            @Override
            public void onGetFriends(JSONObject responseData, String errorMsg) {
                try {
                    if (responseData != null) {
                        JSONArray friendsArray = responseData.getJSONArray("friends");
                        if (friendsArray.length() > 0) {
                            int totalFriendsCount = responseData.optInt("totalFriends");
                            pageIndex = tempPageIndex;
                            if (pageIndex > 1) {
                                tvPrev.setEnabled(true);
                            } else {
                                tvPrev.setEnabled(false);
                            }
                            if (pageIndex * pageSize < totalFriendsCount) {
                                prevNextBtnLayout.setVisibility(View.VISIBLE);
                                tvNext.setEnabled(true);
                            } else {
                                tvNext.setEnabled(false);
                            }
                            setUserFriends(friendsArray);
                        } else {
                            friendsInnerLayout.addView(getNoDataTextView(getString(R.string.no_friends_label)));
                        }
                    } else {
                        prevNextBtnLayout.setVisibility(View.GONE);
                        friendsInnerLayout.addView(getNetworkErrorView(TabName.Friends));
                    }
                    friendsInnerLayout.removeView(friendsInnerLayout.findViewById(R.string.progress_layout_id));
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
//            friendsTable.removeAllViews();
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
                friendsTable.addView(tblRow);
                View lineSeparator = new View(getActivity());
                lineSeparator.setBackgroundColor(Color.parseColor("#bababa"));
                lineSeparator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                friendsTable.addView(lineSeparator);
            }
        } catch (Exception e) {
        }
    }

    private void getUserCoupons() {
        userCouponsInnerLayout.removeView(userCouponsInnerLayout.findViewById(R.string.progress_layout_id));
        userCouponsInnerLayout.removeView(userCouponsInnerLayout.findViewById(R.string.no_data_text_view_id));
        userCouponsInnerLayout.removeView(userCouponsInnerLayout.findViewById(R.string.network_error_layout_id));
        userCouponsTable.removeAllViews();
        userCouponsInnerLayout.addView(getProgressView());
        appVirality.getUserCoupons(new AppVirality.UserCouponsListener() {
            @Override
            public void onGetCoupons(boolean isSuccess, JSONArray userCoupons, String errorMsg) {
                if (isSuccess) {
                    if (userCoupons != null && userCoupons.length() > 0) {
                        setUserCoupons(userCoupons);
                    } else {
                        userCouponsInnerLayout.addView(getNoDataTextView(getString(R.string.no_coupons_label)));
                    }
                } else {
                    userCouponsInnerLayout.addView(getNetworkErrorView(TabName.UserCoupons));
                }
                userCouponsInnerLayout.removeView(userCouponsInnerLayout.findViewById(R.string.progress_layout_id));
            }
        });
    }

    private void setUserCoupons(JSONArray userCoupons) {
        try {
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

            userCouponsTable.addView(tblRowHeader);

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
                userCouponsTable.addView(tblRow);
            }
        } catch (Exception e) {
        }
    }

    private View getProgressView() {
        View view = inflater.inflate(R.layout.progress_layout, null, false);
        view.setId(R.string.progress_layout_id);
        return view;
    }

    private View getNetworkErrorView(TabName tabName) {
        View view = inflater.inflate(R.layout.network_error_layout, null);
        view.setId(R.string.network_error_layout_id);
        Button btnReload = (Button) view.findViewById(R.id.appvirality_earnings_reload);
        btnReload.setId(R.string.network_error_reload_btn_id);
        isReload = true;
        switch (tabName) {
            case Earnings:
                btnReload.setOnClickListener(earningsClickListener);
                break;
            case CouponPools:
                btnReload.setOnClickListener(couponPoolsClickListener);
                break;
            case Friends:
                btnReload.setOnClickListener(friendsClickListener);
                break;
            case UserCoupons:
                btnReload.setOnClickListener(userCouponsClickListener);
                break;
            default:
                isReload = false;
                break;
        }
        return view;
    }

    private View getNoDataTextView(String noDataMsg) {
        TextView textView = new TextView(getActivity());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
        textView.setPadding(margin, margin, margin, margin);
        textView.setId(R.string.no_data_text_view_id);
        textView.setText(noDataMsg);
        textView.setGravity(Gravity.CENTER);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        return textView;
    }

    View.OnClickListener earningsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getUserRewardDetails();
        }
    };

    View.OnClickListener couponPoolsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (couponPools.getVisibility() != View.VISIBLE || (isReload && v.getId() == R.string.network_error_reload_btn_id)) {
                setSelectedTab(couponPoolsHeader, couponPools, (ImageView) couponPoolsHeader.findViewById(R.id.coupon_pools_dropdown));
                getCouponPools();
            } else {
                closePrevTab();
            }
        }
    };

    View.OnClickListener customRefCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (customRefCodeInnerLayout.getVisibility() != View.VISIBLE) {
                setSelectedTab(customRefCodeHeader, customRefCodeInnerLayout, (ImageView) customRefCodeHeader.findViewById(R.id.custom_ref_code_dropdown));
            } else {
                closePrevTab();
            }
        }
    };

    View.OnClickListener friendsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (friendsInnerLayout.getVisibility() != View.VISIBLE || (isReload && v.getId() == R.string.network_error_reload_btn_id)) {
                setSelectedTab(friendsHeader, friendsInnerLayout, (ImageView) friendsHeader.findViewById(R.id.friends_dropdown));
                pageIndex = 1;
                prevNextBtnLayout.setVisibility(View.GONE);
                getUserFriends(pageIndex);
            } else {
                closePrevTab();
            }
        }
    };

    View.OnClickListener userCouponsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userCouponsInnerLayout.getVisibility() != View.VISIBLE || (isReload && v.getId() == R.string.network_error_reload_btn_id)) {
                setSelectedTab(userCouponsHeader, userCouponsInnerLayout, (ImageView) userCouponsHeader.findViewById(R.id.user_coupons_dropdown));
                getUserCoupons();
            } else {
                closePrevTab();
            }
        }
    };

    View.OnClickListener saveRefCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnSaveRefCode.setEnabled(false);
            progressBarRefCode.setVisibility(View.VISIBLE);
            final String enteredRefCode = editRefCode.getText().toString().trim();
            appVirality.customizeReferralCode(enteredRefCode, new AppVirality.CustomizeRefCodeListener() {
                @Override
                public void onCustomRefCodeSet(boolean isSuccess, String errorMsg) {
                    try {
                        if (isSuccess) {
                            ((GrowthHackActivity) getActivity()).refCodeModified = true;
                            Toast.makeText(getActivity(), "Referral Code changed", Toast.LENGTH_SHORT).show();
                            for (CampaignDetail campaignDetail : campaignDetails) {
                                campaignDetail.referralCode = enteredRefCode;
                                String subShortCode = campaignDetail.shortCode.substring(campaignDetail.shortCode.lastIndexOf("-") + 1);
                                campaignDetail.shortCode = campaignDetail.referralCode + "-" + subShortCode;
                                String shareBaseUrl = campaignDetail.shareUrl.substring(0, campaignDetail.shareUrl.lastIndexOf("/") + 1);
                                campaignDetail.shareUrl = shareBaseUrl + campaignDetail.shortCode;
                            }
                            ((GrowthHackActivity) getActivity()).campaignDetails = campaignDetails;
                        } else {
                            Toast.makeText(getActivity(), "Referral Code not changed.", Toast.LENGTH_SHORT).show();
                        }
                        progressBarRefCode.setVisibility(View.INVISIBLE);
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editRefCode.getWindowToken(), 0);
                    } catch (Exception e) {
                    }
                }
            });
        }
    };

    View.OnClickListener prevClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getUserFriends(pageIndex - 1);
        }
    };

    View.OnClickListener nextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getUserFriends(pageIndex + 1);
        }
    };

    TextWatcher refCodeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String newStr = editRefCode.getText().toString().trim();
            btnSaveRefCode.setEnabled(newStr.length() > 0 && !newStr.equalsIgnoreCase(womCampaignDetail.referralCode.split("-")[0]));
        }
    };

    private void setSelectedTab(View selectedHeader, View selectedLayout, ImageView selectedDropDown) {
        closePrevTab();
        activeHeader = selectedHeader;
        activeLayout = selectedLayout;
        activeDropDown = selectedDropDown;
        setBackground(activeHeader, grdTwoRounded);
        activeLayout.setVisibility(View.VISIBLE);
        activeDropDown.setSelected(true);
    }

    private void closePrevTab() {
        if (activeLayout != null && activeDropDown != null) {
            setBackground(activeHeader, grdFourRounded);
            activeLayout.setVisibility(View.GONE);
            activeDropDown.setSelected(false);
            isReload = false;
        }
    }

    private void setBackground(View v, GradientDrawable drawable) {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            v.setBackground(drawable);
        } else {
            v.setBackgroundDrawable(drawable);
        }
    }

}
