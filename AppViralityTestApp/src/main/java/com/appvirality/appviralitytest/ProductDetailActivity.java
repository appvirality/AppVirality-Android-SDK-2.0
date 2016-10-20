package com.appvirality.appviralitytest;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.appviralityui.Utils;
import com.appvirality.appviralityui.adapters.GridViewAdapter;
import com.appvirality.appviralityui.custom.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created by AppVirality on 6/30/2016.
 */
public class ProductDetailActivity extends AppCompatActivity {

    RoundedImageView ivProductReferrer;
    ImageView ivProductImage;
    TextView tvProductTitle, tvOffer;
    String productTitleStr;
    int productImageId;
    String productCode;
    String category;
    int price;
    RelativeLayout offerDescLayout, productSharingLayout;
    AppVirality appVirality;
    DisplayMetrics metrics;
    PopupWindow popupWindow;
    CampaignDetail psCampaignDetail;
    Utils utils;
    LayoutInflater inflater;
    boolean isRewardExists;
    boolean isOfferShown = false;
    JSONObject productData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        utils = new Utils(this);
        appVirality = AppVirality.getInstance(this);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ivProductReferrer = (RoundedImageView) findViewById(R.id.iv_product_referrer);
        tvOffer = (TextView) findViewById(R.id.tv_product_offer);
        ivProductImage = (ImageView) findViewById(R.id.iv_product_image);
        tvProductTitle = (TextView) findViewById(R.id.tv_product_title);
        offerDescLayout = (RelativeLayout) findViewById(R.id.offer_desc_layout);
        productSharingLayout = (RelativeLayout) findViewById(R.id.product_sharing_layout);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        initCampaignDetail();
        String action = getIntent().getAction();
        Uri data = getIntent().getData();
        if (action != null && action.equals(Intent.ACTION_VIEW) && data != null) {
            String referrer = data.getQueryParameter("referrer");
            String clickId = getClickId(referrer);
            productCode = getProductCode(referrer);
            appVirality.recordProductAttribution(clickId, new AppVirality.ProductAttributionListener() {
                @Override
                public void onResponse(JSONObject jsonObject, String s) {
                    productData = jsonObject;
                    try {
                        productData.put("ps_referrer_name", "Srinivas");
                        productData.put("ps_referrer_image", "http://www.verizon.com/about/sites/default/files/styles/vzc_leadership_bio_large/public/leadership-headshots/Robert-Mudge.gif?itok=pQUpucDm");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showProductOfferPopup();
                }
            });
        } else {
            productCode = getIntent().getStringExtra("product_sku");
        }
        loadProductData();
        productSharingLayout.setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showSocialItemsPopUp();
        }
    };

    private void showSocialItemsPopUp() {
        View view = inflater.inflate(com.appvirality.appviralityui.R.layout.custom_share_dialog, null, false);
        GridView gridView = (GridView) view.findViewById(com.appvirality.appviralityui.R.id.appvirality_gridView);
        GridViewAdapter adapter = new GridViewAdapter(ProductDetailActivity.this, com.appvirality.appviralityui.R.layout.grid_item, psCampaignDetail.allSocialActions);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popupWindow.dismiss();
                appVirality.invokeInvite(psCampaignDetail, psCampaignDetail.allSocialActions.get(position).packagename, new ComponentName(psCampaignDetail.allSocialActions.get(position).packagename, psCampaignDetail.allSocialActions.get(position).classname), isRewardExists, productCode);
            }
        });
        popupWindow = new PopupWindow(view, metrics.widthPixels, metrics.heightPixels, false);
        popupWindow.setAnimationStyle(com.appvirality.appviralityui.R.style.appvirality_slide_activity);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    private int getTitlePos() {
        if (TextUtils.isEmpty(productCode))
            return 0;
        for (int i = 0; i < Constants.sku.length; i++) {
            if (productCode.equalsIgnoreCase(Constants.sku[i]))
                return i;
        }
        return 0;
    }

    public String getClickId(String referrer) {
        try {
            referrer = URLDecoder.decode(referrer, "UTF-8");
            String[] pair = referrer.split("&")[0].split("=");
            if (pair.length > 1 && pair[0].equals("avclk") && !TextUtils.isEmpty(pair[1])) {
                String[] refPair = pair[1].trim().split("-");
                if (refPair.length > 0 && !TextUtils.isEmpty(refPair[0]))
                    return refPair[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getProductCode(String referrer) {
        try {
            referrer = URLDecoder.decode(referrer, "UTF-8");
            String[] pair = referrer.split("&")[1].split("=");
            if (pair.length > 1 && pair[0].equals("pcode") && !TextUtils.isEmpty(pair[1])) {
                String[] refPair = pair[1].trim().split("-");
                if (refPair.length > 0 && !TextUtils.isEmpty(refPair[0]))
                    return refPair[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initCampaignDetail() {
        appVirality.getCampaigns(com.appvirality.Constants.GrowthHackType.Product_Sharing, new AppVirality.CampaignDetailsListener() {
            @Override
            public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                if (campaignDetails.size() > 0) {
                    psCampaignDetail = campaignDetails.get(0);
                    if (appVirality.hasProductSharingReward(productCode, category, price)) {
                        productSharingLayout.setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.tv_share_campaign_msg)).setText(appVirality.getProductSharingRewardMsg(productCode, category, price));
                    }
                    showProductOfferPopup();
                }
            }
        });
    }

    private void loadProductData() {
        int titlePos = getTitlePos();
        productTitleStr = Constants.productTitles[titlePos];
        productImageId = Constants.productImages[titlePos];
        category = Constants.category[titlePos];
        price = Constants.price[titlePos];
        ivProductImage.setImageResource(productImageId);
        tvProductTitle.setText(productTitleStr);
        isRewardExists = appVirality.hasProductSharingReward(productCode, category, price);
        if (isRewardExists) {
            productSharingLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tv_share_campaign_msg)).setText(appVirality.getProductSharingRewardMsg(productCode, category, price));
        }
    }

    private void showProductOfferPopup() {
        try {
            if (productData != null && psCampaignDetail != null && !isOfferShown) {
                tvOffer.setText(appVirality.getProductSharingWelcomeMsg(productCode, category, price, productData.optString("ps_referrer_name")));
                utils.downloadAndSetImage(productData.optString("ps_referrer_image"), ivProductReferrer);
                isOfferShown = true;
                Animation animation = AnimationUtils.loadAnimation(ProductDetailActivity.this, R.anim.top_to_down);
                offerDescLayout.setAnimation(animation);
                offerDescLayout.setVisibility(View.VISIBLE);
                animation.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
