package com.appvirality.appviralityui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.activities.InviteContactsActivity;
import com.appvirality.appviralityui.custom.RoundedImageView;

import java.util.ArrayList;



public class SectionCursorAdapter extends CursorAdapter {

    Context context;
    LayoutInflater inflater;
    boolean isSmsFragment;
    //    int recommendedContactsCount;
    int sectionCount;
    boolean isSearchOn;
    boolean isDataValid;
    ListView listView;
    ArrayList<String> selectedContactIds;
    ArrayList<String> recommendedContactIds;
    InviteContactsActivity.OnContactSelectedListener onContactSelectedListener;
    //    protected SortedMap<Integer, Object> mSections = new TreeMap<>();
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SECTION = 1;
    private static final String CONTACT_COLUMN_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;

    public SectionCursorAdapter(Context context, Cursor cursor, ListView listView, ArrayList<String> selectedContactIds, boolean isSmsFragment, InviteContactsActivity.OnContactSelectedListener onContactSelectedListener) {
        super(context, cursor, false);
        this.context = context;
        this.listView = listView;
        this.selectedContactIds = selectedContactIds;
        recommendedContactIds = new ArrayList<>();
        this.isSmsFragment = isSmsFragment;
        this.onContactSelectedListener = onContactSelectedListener;
        inflater = LayoutInflater.from(context);
        init(cursor, null, false);
    }

    private void init(Cursor cursor, ArrayList<String> recommendedContactIds, boolean isSearchOn) {
        isDataValid = cursor != null;
        if (recommendedContactIds != null) {
            this.recommendedContactIds = recommendedContactIds;
        } else {
            recommendedContactIds = new ArrayList<>();
        }
        this.isSearchOn = isSearchOn;
        if (isSearchOn) {
            sectionCount = 0;
        } else {
            sectionCount = recommendedContactIds.size() > 0 ? 2 : 1;
        }
    }

    @Override
    public int getCount() {
//        if (isDataValid && getCursor() != null && !getCursor().isClosed()) {
        try {
            return getCursor().getCount() + sectionCount;
        } catch (Exception e) {
            return 0;
        }
//        } else {
//            return 0;
//        }
    }

    @Override
    public int getViewTypeCount() {
        return sectionCount > 0 ? 2 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        return isSection(position) ? TYPE_SECTION : TYPE_ITEM;
    }

    public void changeCursor(Cursor cursor, ArrayList<String> recommendedContactIds, boolean isSearchOn) {
        init(cursor, recommendedContactIds, isSearchOn);
        super.changeCursor(cursor);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Cursor cursor = getCursor();
        boolean isSection = isSection(position);
        View view;
        if (!isDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!isSection) {
            int newPosition = getCursorPositionWithoutSections(position);
            Log.i("SectionAdapter", "Position : " + position + "\nNewPosition : " + newPosition);
            if (!cursor.moveToPosition(newPosition))
                throw new IllegalStateException("couldn't move cursor to position " + newPosition);
        }
        if (convertView == null) {
            view = isSection ? newSectionView() : newItemView();
        } else {
            view = convertView;
        }
        if (isSection) {
            bindSectionView(view, position);
        } else {
            bindItemView(cursor, view, position);
        }
        return view;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        throw new IllegalStateException("This method is not used by " + SectionCursorAdapter.class.getSimpleName());
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        throw new IllegalStateException("This method is not used by " + SectionCursorAdapter.class.getSimpleName());
    }

    private View newItemView() {
        View view = inflater.inflate(R.layout.invite_contact_list_item, null);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    private View newSectionView(/*ViewGroup parent*/) {
//        View view = inflater.inflate(R.layout.sms_section_item, null);
//        view.setClickable(false);
//        return view;
        return inflater.inflate(R.layout.invite_contact_section_item, null);
    }

    private void bindItemView(Cursor cursor, View view, final int position) {
        Log.i("SmsCursorAdapter", "Inside BindView");
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
//        RoundedImageView ivContact = (RoundedImageView) view.findViewById(R.id.iv_contact);
//        TextView tvContact = (TextView) view.findViewById(R.id.tv_contact);
//        final CheckBox cbContact = (CheckBox) view.findViewById(R.id.cb_contact);
        String imgUriStr = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
        if (!TextUtils.isEmpty(imgUriStr)) {
            viewHolder.ivContact.setImageURI(Uri.parse(imgUriStr));
        } else {
            viewHolder.ivContact.setImageResource(R.drawable.appvirality_user_image);
        }
        viewHolder.tvContact.setText(cursor.getString(cursor.getColumnIndex(isSmsFragment ? CONTACT_COLUMN_NAME : ContactsContract.CommonDataKinds.Email.DATA)));
        final String contactId = cursor.getString(cursor.getColumnIndex(isSmsFragment ? ContactsContract.Contacts._ID : ContactsContract.CommonDataKinds.Email.DATA));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedContactIds.contains(contactId)) {
                    viewHolder.cbContact.setChecked(false);
                } else {
                    viewHolder.cbContact.setChecked(true);
                }
            }
        });
        viewHolder.cbContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("SmsCursorAdapter", "Checked State : " + isChecked);
                boolean containsContactId = selectedContactIds.contains(contactId);
                if (isChecked && !containsContactId) {
                    selectedContactIds.add(contactId);
                } else if (!isChecked && containsContactId) {
                    selectedContactIds.remove(contactId);
                }
                if (recommendedContactIds.contains(contactId)) {
                    View duplicateView = getDuplicateView(contactId, position);
                    if (duplicateView != null) {
                        ((CheckBox) duplicateView.findViewById(R.id.cb_contact)).setChecked(isChecked);
                    }
                }
                if (onContactSelectedListener != null)
                    onContactSelectedListener.onContactSelected();
            }
        });
        viewHolder.cbContact.setChecked(selectedContactIds.contains(contactId));
    }

    private void bindSectionView(View view, int position) {
        TextView tvSection = (TextView) view.findViewById(R.id.tv_section);
        tvSection.setText(sectionCount > 1 && position == 0 ? "Recommended Contacts" : "All Contacts");
    }

    private boolean isSection(int pos) {
        if (!isSearchOn && (pos == 0 || (recommendedContactIds.size() > 0 && pos == recommendedContactIds.size() + 1))) {
            return true;
        }
        return false;
    }

    protected int getCursorPositionWithoutSections(int position) {
        if (sectionCount == 0 || position == 0) {
            return position;
        } else {
            return (position <= recommendedContactIds.size()) || !isSmsFragment ? position - 1 : position - 2;
        }
    }

    private View getDuplicateView(String selectedContactId, int selectedViewPos) {
        int start = listView.getFirstVisiblePosition();
        Cursor cursor = getCursor();
        for (int i = 0; i < listView.getChildCount(); i++) {
            int listPos = i + start;
            if (listPos != selectedViewPos && !isSection(listPos) && cursor.moveToPosition(getCursorPositionWithoutSections(listPos))
                    && selectedContactId.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(isSmsFragment ? ContactsContract.Contacts._ID : ContactsContract.CommonDataKinds.Email.DATA)))) {
                return listView.getChildAt(i);
            }
        }
        return null;
    }

    static class ViewHolder {
        RoundedImageView ivContact;
        TextView tvContact;
        CheckBox cbContact;

        public ViewHolder(View view) {
            ivContact = (RoundedImageView) view.findViewById(R.id.iv_contact);
            tvContact = (TextView) view.findViewById(R.id.tv_contact);
            cbContact = (CheckBox) view.findViewById(R.id.cb_contact);
        }
    }

}
