package com.appvirality.appviralityui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appvirality.Items;
import com.appvirality.appviralityui.R;

import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter<Items> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<Items> socialActions = new ArrayList<>();

    public GridViewAdapter(Context context, int layoutResourceId,
                           ArrayList<Items> socialActions) {
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

        Items item = socialActions.get(position);
        holder.title.setText(item.appname);
//		if(item.getTitleColor() != -1)
//			holder.title.setTextColor(item.getTitleColor());
        holder.logo.setImageDrawable(context.getPackageManager().getDrawable(item.packagename, item.resId, null));
        return row;
    }

    class ViewHolder {
        TextView title;
        ImageView logo;
    }
}