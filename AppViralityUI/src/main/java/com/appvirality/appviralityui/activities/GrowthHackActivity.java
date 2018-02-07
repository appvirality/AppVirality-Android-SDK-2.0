package com.appvirality.appviralityui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.Utils;
import com.appvirality.appviralityui.adapters.GrowthHackPagerAdapter;
import com.appvirality.appviralityui.fragments.EarningsFragment;
import com.appvirality.appviralityui.fragments.ReferFragment;

import java.util.ArrayList;

;

/**
 * Created by AppVirality on 3/28/2016.
 */
public class GrowthHackActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView toolbarTitle;
    AppVirality appVirality;
    ReferFragment referFragment;
    EarningsFragment earningsFragment;
    public boolean refCodeModified = false;
    public static int tabLayoutHeight = 0;
    public ArrayList<CampaignDetail> campaignDetails;
    CampaignDetail womCampaignDetail;
    boolean isEarnings;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_growth_hack);

        campaignDetails = (ArrayList<CampaignDetail>) getIntent().getSerializableExtra("campaign_details");
        isEarnings = getIntent().getBooleanExtra("is_earnings", false);
        utils = new Utils(this);
        appVirality = AppVirality.getInstance(this);
        womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
            if (isEarnings) {
            toolbarTitle.setText("EARNINGS");
        } else if (womCampaignDetail != null) {
            if (!TextUtils.isEmpty(womCampaignDetail.campaignTitle)) {
                toolbar.setVisibility(View.VISIBLE);
                toolbarTitle.setText(Html.fromHtml(womCampaignDetail.campaignTitle));
            }
            if (!TextUtils.isEmpty(womCampaignDetail.campaignTitleColor))
                toolbarTitle.setTextColor(Color.parseColor(womCampaignDetail.campaignTitleColor));
        }
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        tabLayoutHeight = tabLayout.getMeasuredHeight();
        if (womCampaignDetail != null && !TextUtils.isEmpty(womCampaignDetail.campaignTitleColor)){
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor(womCampaignDetail.campaignTitleColor));
        }
        referFragment = new ReferFragment();
        earningsFragment = new EarningsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("campaign_details", campaignDetails);
        bundle.putBoolean("is_earnings", isEarnings);
        referFragment.setArguments(bundle);
        earningsFragment.setArguments(bundle);
        setUpPagerAdapter();
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0 && refCodeModified) {
                    referFragment.refreshLinkCode(campaignDetails);
                    refCodeModified = false;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ReferFragment.READ_CONTACTS_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(GrowthHackActivity.this, InviteContactsActivity.class);
                    intent.putExtra("campaign_detail", womCampaignDetail);
                    startActivity(intent);
                }
                break;
        }
    }

    public void setUpPagerAdapter() {
        GrowthHackPagerAdapter adapter = new GrowthHackPagerAdapter(getSupportFragmentManager());
        if (!isEarnings)
            adapter.addFragment(referFragment, "Refer");
        adapter.addFragment(earningsFragment, "Earnings");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (referFragment.popupWindow != null && referFragment.popupWindow.isShowing()) {
            referFragment.popupWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }

}
