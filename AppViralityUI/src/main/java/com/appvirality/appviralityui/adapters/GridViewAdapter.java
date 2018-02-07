package com.appvirality.appviralityui.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appvirality.SocialItem;
import com.appvirality.appviralityui.R;

import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter<SocialItem> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<SocialItem> socialActions = new ArrayList<>();

    public GridViewAdapter(Context context, int layoutResourceId,
                           ArrayList<SocialItem> socialActions) {
        super(context, layoutResourceId, socialActions);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.socialActions = socialActions;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) row.findViewById(R.id.appvirality_text);
            holder.logo = (ImageView) row.findViewById(R.id.appvirality_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        SocialItem item = socialActions.get(position);
        holder.title.setText(item.appname);
//		if(item.getTitleColor() != -1)
//			holder.title.setTextColor(item.getTitleColor());
        if (!item.isCustom || !TextUtils.isEmpty(item.packagename)) {
            holder.logo.setImageDrawable(context.getPackageManager().getDrawable(item.packagename, item.resId, null));
            row.setTag(R.string.custom_impl_tag, false);
        } else if (item.isCustom) {
            if (item.resId != 0)
                holder.logo.setImageResource(item.resId);
            row.setTag(R.string.custom_impl_tag, true);
        }
        return row;
    }

    class ViewHolder {
        TextView title;
        ImageView logo;
    }
}