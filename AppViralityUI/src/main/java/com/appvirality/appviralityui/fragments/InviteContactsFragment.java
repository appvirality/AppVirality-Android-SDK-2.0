package com.appvirality.appviralityui.fragments;

import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appvirality.AppVirality;
import com.appvirality.appviralityui.R;
import com.appvirality.appviralityui.activities.InviteContactsActivity;
import com.appvirality.appviralityui.adapters.SectionCursorAdapter;

import java.util.ArrayList;


public class InviteContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks {

    AppVirality appVirality;
    SectionCursorAdapter adapter;
    ListView lvContacts;
    String searchString = "";
    Cursor recentContactsCursor;
    public boolean isSmsFragment = true;
    ProgressBar progressBar;
    ArrayList<String> selectedContactIds = new ArrayList();
    private static final String CONTACT_COLUMN_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;
    protected static final String[] CONTACTS_PROJECTION = {
            ContactsContract.Contacts._ID,
            CONTACT_COLUMN_NAME,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
    String[] NUMBERS_PROJECTION = {
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    String[] EMAIL_PROJECTION = {
            ContactsContract.CommonDataKinds.Email._ID,
            ContactsContract.CommonDataKinds.Email.DATA,
            ContactsContract.CommonDataKinds.Email.PHOTO_THUMBNAIL_URI
    };
    private static final String NUMBERS_SELECTION = ContactsContract.Data.CONTACT_ID + " IN";
    String contactsSortOrder = CONTACT_COLUMN_NAME + " COLLATE LOCALIZED ASC";
    String emailsSortOrder = ContactsContract.CommonDataKinds.Email.DATA + " COLLATE LOCALIZED ASC";
    private static final int RECENT_CONTACTS_LOADER_ID = 6;
    private static final int CONTACTS_LOADER_ID = 2;
    private static final int NUMBERS_LOADER_ID = 3;
    private static final int EMAIL_LOADER_ID = 4;
    int earningsBarColor;
    InviteContactsActivity.OnContactSelectedListener onContactSelectedListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_contacts, container, false);
        lvContacts = (ListView) view.findViewById(R.id.lv_contacts);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        appVirality = AppVirality.getInstance(getActivity());
        isSmsFragment = getArguments().getBoolean("is_sms_fragment");
        adapter = new SectionCursorAdapter(getActivity(), null, lvContacts, selectedContactIds, this, onContactSelectedListener);
        lvContacts.setAdapter(adapter);
        int[] attrs = {R.attr.av_earnings_bar_color};
        TypedArray typedValue = getActivity().obtainStyledAttributes(attrs);
        earningsBarColor = typedValue.getColor(0, Color.BLACK);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getLoaderManager().destroyLoader(RECENT_CONTACTS_LOADER_ID);
        getLoaderManager().destroyLoader(CONTACTS_LOADER_ID);
        getLoaderManager().destroyLoader(NUMBERS_LOADER_ID);
        getLoaderManager().destroyLoader(EMAIL_LOADER_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isSmsFragment) {
            getLoaderManager().initLoader(RECENT_CONTACTS_LOADER_ID, null, this);
        } else {
            getLoaderManager().initLoader(EMAIL_LOADER_ID, null, this);
        }
    }

    @Override
    public android.support.v4.content.Loader onCreateLoader(int id, Bundle args) {
        String selection;
        switch (id) {
            case RECENT_CONTACTS_LOADER_ID:
                progressBar.setVisibility(View.VISIBLE);
                selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " =1";
                String sortOrder = ContactsContract.Contacts.LAST_TIME_CONTACTED + " DESC limit 15";
                return new CursorLoader(getActivity(), ContactsContract.Contacts.CONTENT_URI, CONTACTS_PROJECTION, selection, null, sortOrder);
            case CONTACTS_LOADER_ID:
                progressBar.setVisibility(View.VISIBLE);
                selection = CONTACT_COLUMN_NAME + " LIKE '" + searchString + "%' AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER + " =1";
                return new CursorLoader(getActivity(), ContactsContract.Contacts.CONTENT_URI, CONTACTS_PROJECTION, selection, null, contactsSortOrder);
            case NUMBERS_LOADER_ID:
                selection = NUMBERS_SELECTION + "(" + TextUtils.join(",", selectedContactIds) + ")";
                return new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, NUMBERS_PROJECTION, selection, null, null);
            case EMAIL_LOADER_ID:
                progressBar.setVisibility(View.VISIBLE);
                selection = ContactsContract.CommonDataKinds.Email.DATA + " LIKE '" + searchString + "%'";
                return new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Email.CONTENT_URI, EMAIL_PROJECTION, selection, null, emailsSortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader loader, Object data) {
        Cursor cursor;
        boolean isSearchOn = !TextUtils.isEmpty(searchString);
        progressBar.setVisibility(View.GONE);
        switch (loader.getId()) {
            case RECENT_CONTACTS_LOADER_ID:
                cursor = (Cursor) data;
                recentContactsCursor = cursor;
                Log.i("SmsFragment", "Recent Contacts Count : " + cursor.getCount());
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        String name = cursor.getString(cursor.getColumnIndex(CONTACT_COLUMN_NAME));
                        Log.i("SmsFragment", "Recent Contact is " + name);
                        cursor.moveToNext();
                    }
                }
                getLoaderManager().restartLoader(CONTACTS_LOADER_ID, null, this);
                break;
            case CONTACTS_LOADER_ID:
                lvContacts.setVisibility(View.VISIBLE);
                Log.i("SmsFragment", "Contacts Count : " + ((Cursor) data).getCount());
                cursor = (Cursor) data;
                ArrayList<String> recommendedContactIds = null;
                if (recentContactsCursor != null && !recentContactsCursor.isClosed() && !isSearchOn && recentContactsCursor.getCount() > 0) {
                    cursor = new MergeCursor(new Cursor[]{recentContactsCursor, cursor});
                    recommendedContactIds = getRecommendedContactIds(recentContactsCursor);
                }
                adapter.changeCursor(cursor, recommendedContactIds, isSearchOn);
                break;
            case NUMBERS_LOADER_ID:
                cursor = (Cursor) data;
                ArrayList<String> phoneNumbers = new ArrayList<>();
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i("InviteFromContactsDemo", "Contact Number : " + phoneNumber);
                        phoneNumbers.add(phoneNumber);
                        cursor.moveToNext();
                    }
                }
                sendSmsInvite(phoneNumbers);
                break;
            case EMAIL_LOADER_ID:
                lvContacts.setVisibility(View.VISIBLE);
                Log.i("SmsFragment", "Emails Count : " + ((Cursor) data).getCount());
                adapter.changeCursor((Cursor) data, null, isSearchOn);
                break;
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader loader) {

    }

    public void sendInvites() {
        if (selectedContactIds.size() == 0) {
            Log.i("InviteContactsFragment", "First Pos : " + lvContacts.getFirstVisiblePosition() + "\nLast Pos : " + lvContacts.getLastVisiblePosition());
            Toast.makeText(getActivity(), "Select at least 1 contact to continue.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSmsFragment) {
            getLoaderManager().restartLoader(NUMBERS_LOADER_ID, null, InviteContactsFragment.this);
        } else {
            sendEmailInvite();
        }
    }

    /**
     * It will launch the SMS client app for sending SMS to the selected
     * contacts but if you want to send it through web, just replace the
     * invokeInvite() method call with the web service for sending SMS,
     * also un-comment the recordSocialAction() method call to record the
     * invite and social action used for sending the invite.
     */
    private void sendSmsInvite(ArrayList<String> phoneNumbers) {
        appVirality.invokeInvite(null, phoneNumbers, null);
        // appVirality.recordSocialAction("15", Constants.GrowthHackType.Word_of_Mouth, shortCode, shareMsg);
    }

    /**
     * It will launch the Email client app for sending Email to the selected
     * contacts but if you want to send it through web, just replace the
     * invokeInvite() method call with the web service for sending Email,
     * also un-comment the recordSocialAction() method call to record the
     * invite and social action used sending the invite.
     */
    private void sendEmailInvite() {
        appVirality.invokeInvite(null, null, selectedContactIds);
        // appVirality.recordSocialAction("15", Constants.GrowthHackType.Word_of_Mouth, shortCode, shareMsg);
    }

    public void searchContacts(String searchString) {
        try {
            this.searchString = searchString;
            getLoaderManager().restartLoader(isSmsFragment ? (searchString.equals("") ? RECENT_CONTACTS_LOADER_ID : CONTACTS_LOADER_ID) : EMAIL_LOADER_ID, null, InviteContactsFragment.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getRecommendedContactIds(Cursor cursor) {
        ArrayList<String> recommendedContactIds = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                recommendedContactIds.add(cursor.getString(cursor.getColumnIndex(isSmsFragment ? ContactsContract.Contacts._ID : ContactsContract.CommonDataKinds.Email.DATA)));
                cursor.moveToNext();
            }
        }
        return recommendedContactIds;
    }

    public ArrayList<String> getSelectedContactIds() {
        return selectedContactIds;
    }

    public void setOnContactSelectedListener(InviteContactsActivity.OnContactSelectedListener onContactSelectedListener) {
        this.onContactSelectedListener = onContactSelectedListener;
    }

}
