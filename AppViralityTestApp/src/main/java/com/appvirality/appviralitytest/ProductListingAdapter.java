package com.appvirality.appviralitytest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by AppVirality on 6/30/2016.
 */
public class ProductListingAdapter extends BaseAdapter {

    Context context;

    ProductListingAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return Constants.productImages.length;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listing_row_item, null);
            holder.ivProductImage = (ImageView) convertView.findViewById(R.id.iv_product_image);
            holder.tvProductTitle = (TextView) convertView.findViewById(R.id.tv_product_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ivProductImage.setImageResource(Constants.productImages[position]);
        holder.tvProductTitle.setText(Constants.productTitles[position]);
        return convertView;
    }

    class ViewHolder {
        ImageView ivProductImage;
        TextView tvProductTitle;
    }
}
