package com.appvirality.appviralityui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.appvirality.AppVirality;
import com.appvirality.appviralityui.R;

/**
 * Created by AppVirality on 3/29/2016.
 */
public class WebViewActivity extends AppCompatActivity {

    WebView webView;
    AppVirality appVirality;
    String campaignId;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        try {
            campaignId = getIntent().getStringExtra("campaign_id");
            appVirality = AppVirality.getInstance(this);
            webView = (WebView) findViewById(R.id.web_view);
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
            appVirality.getCampaignTerms(campaignId, new AppVirality.CampaignTermsListener() {
                @Override
                public void onGetCampaignTerms(String terms, String errorMsg) {
                    progressBar.setVisibility(View.GONE);
                    terms = (terms == null) ? "No terms specified" : terms;
                    webView.loadDataWithBaseURL(null, "<html><body>" + terms.replaceAll("\\n", "<br/>") + "</body></html>", "text/html", "UTF-8", null);
                    webView.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            finish();
        }
    }
}
