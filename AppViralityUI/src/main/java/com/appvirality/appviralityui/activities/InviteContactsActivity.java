package com.appvirality.appviralityui.activities;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appvirality.CampaignDetail;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.adapters.InviteContactsPagerAdapter;
import com.appvirality.appviralityui.fragments.InviteContactsFragment;


public class InviteContactsActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    InviteContactsFragment smsFragment;
    InviteContactsFragment emailFragment;
    //    TextView leftTab, rightTab;
    RelativeLayout inviteBtnLayout;
    EditText editSearch;
    TextView tvInvite;
    int inviteContactsAction = 2;
    Toolbar toolbar;
    TextView toolbarTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_contacts);

        CampaignDetail womCampaignDetail = (CampaignDetail) getIntent().getSerializableExtra("campaign_detail");
        int[] attrs = {R.attr.av_invite_contacts_action};
        TypedArray typedValue = obtainStyledAttributes(attrs);
        inviteContactsAction = typedValue.getInt(0, 2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        inviteBtnLayout = (RelativeLayout) findViewById(R.id.invite_btn_layout);
        setSupportActionBar(toolbar);
        if (!TextUtils.isEmpty(womCampaignDetail.campaignTitleColor)) {
            int titleColor = Color.parseColor(womCampaignDetail.campaignTitleColor);
            toolbarTitle.setTextColor(titleColor);
        }
        tvInvite = (TextView) findViewById(R.id.tv_invite);
//        leftTab = (TextView) findViewById(R.id.left_tab);
//        rightTab = (TextView) findViewById(R.id.right_tab);
        editSearch = (EditText) findViewById(R.id.edit_search);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        inviteBtnLayout.setOnClickListener(btnInviteClickListener);
        editSearch.addTextChangedListener(searchTextWatcher);
//        leftTab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                viewPager.setCurrentItem(0);
//                selectTab(0);
//            }
//        });
//        rightTab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                viewPager.setCurrentItem(1);
//                selectTab(1);
//            }
//        });
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        smsFragment = new InviteContactsFragment();
        Bundle smsArgs = new Bundle();
        smsArgs.putBoolean("is_sms_fragment", true);
        smsFragment.setArguments(smsArgs);
        smsFragment.setOnContactSelectedListener(onContactSelectedListener);
        Bundle emailArgs = new Bundle();
        emailArgs.putBoolean("is_sms_fragment", false);
        emailFragment = new InviteContactsFragment();
        emailFragment.setArguments(emailArgs);
        emailFragment.setOnContactSelectedListener(onContactSelectedListener);
        InviteContactsPagerAdapter adapter = new InviteContactsPagerAdapter(getSupportFragmentManager());
        if (inviteContactsAction != 1)
            adapter.addFragment(smsFragment, "SMS");
        if (inviteContactsAction != 0)
            adapter.addFragment(emailFragment, "EMAIL");
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setAdapter(adapter);
//        selectTab(0);
        tabLayout.setupWithViewPager(viewPager);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        viewPager.removeOnPageChangeListener(onPageChangeListener);
//    }

//    private void selectTab(int pos) {
//        leftTab.setBackgroundColor(pos==0 ? getResources().getColor(R.color.hollowRed) : Color.WHITE);
//        rightTab.setBackgroundColor(pos==1 ? getResources().getColor(R.color.hollowRed) : Color.WHITE);
//        leftTab.setSelected(pos == 0);
//        leftTab.setPressed(pos == 0);
//        rightTab.setSelected(pos == 1);
//        rightTab.setPressed(pos == 1);
//    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                tvInvite.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.invite_sms_icon), null);
            } else {
                tvInvite.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.invite_mail_icon), null);
            }
            refreshCounts();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    View.OnClickListener btnInviteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (viewPager.getCurrentItem() == 0 && inviteContactsAction != 1) {
                smsFragment.sendInvites();
            } else {
                emailFragment.sendInvites();
            }
        }
    };

    TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (viewPager.getCurrentItem() == 0 && inviteContactsAction != 1) {
                smsFragment.searchContacts(s.toString());
            } else {
                emailFragment.searchContacts(s.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public interface OnContactSelectedListener {
        void onContactSelected();
    }

    OnContactSelectedListener onContactSelectedListener = new OnContactSelectedListener() {
        @Override
        public void onContactSelected() {
            refreshCounts();
        }
    };

    private void refreshCounts() {
        if (viewPager.getCurrentItem() == 0 && inviteContactsAction != 1) {
            int count = smsFragment.getSelectedContactIds().size();
            tvInvite.setText("Invite via SMS" + (count > 0 ? "(" + count + ")" : ""));
        } else {
            int count = emailFragment.getSelectedContactIds().size();
            tvInvite.setText("Invite via EMAIL" + (count > 0 ? "(" + count + ")" : ""));
        }
    }

}
