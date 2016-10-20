package com.appvirality.appviralitytest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * Created by AppVirality on 8/9/2016.
 */
public class ProductListingActivity extends AppCompatActivity {

    GridView gvProductListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_listing);

        gvProductListing = (GridView) findViewById(R.id.gv_product_listing);
        ProductListingAdapter adapter = new ProductListingAdapter(this);
        gvProductListing.setAdapter(adapter);

        gvProductListing.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProductListingActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_sku", Constants.sku[position]);
                startActivity(intent);
            }
        });

    }

}
