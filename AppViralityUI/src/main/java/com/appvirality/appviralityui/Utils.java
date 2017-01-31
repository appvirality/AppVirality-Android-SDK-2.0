package com.appvirality.appviralityui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.widget.ImageView;

import com.appvirality.AppVirality;
import com.appvirality.CampaignDetail;
import com.appvirality.Constants;
import com.appvirality.SocialAction;
import com.appvirality.appviralityui.activities.GrowthHackActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by AppVirality on 3/14/2016.
 */
public class Utils {

    Context context;
    ProgressDialog progressDialog;
    AppVirality appVirality;
    public static final String TAG = "AppViralityTestApp";
    public static final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.androidlav";

    public Utils(Context context) {
        this.context = context;
        appVirality = AppVirality.getInstance(context);
    }

    public static boolean hasInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean hasPermission(Context context, String permission) {
        int hasPermission = context.checkCallingOrSelfPermission(permission);
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isExternalStorageWritable(Context context) {
        try {
            return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                    hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        } catch (Exception e) {
        }
        return false;
    }

    public void downloadAndSetImage(String imageUrl, ImageView imgView) {
        if (!TextUtils.isEmpty(imageUrl)) {
            new ImgDownloadTask(imgView).execute(new String[]{imageUrl, null});
        }
    }

    public void downloadAndSaveImage(String imageUrl, String imageName) {
        boolean hasImage = !TextUtils.isEmpty(imageUrl) && !TextUtils.isEmpty(imageName);
        if (hasImage) {
            new ImgDownloadTask(null).execute(new String[]{imageUrl, imageName});
        } else {
            deleteImage(imageName);
        }
    }

    private Bitmap downloadImage(String imageUrl) {
        int number = 0;
        int redirectCount = 0;
        boolean succeeded = false;
        Bitmap bitmapImage = null;
        HttpURLConnection connection = null;
        while (hasInternet(context) && number < 3 && !succeeded) {
            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                final int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK && redirectCount <= 3) {
                    String newUrl = connection.getHeaderField("Location");
                    redirectCount++;
                    return downloadImage(newUrl);
                }
                InputStream input = connection.getInputStream();
                bitmapImage = BitmapFactory.decodeStream(input);
                succeeded = true;
            } catch (SocketTimeoutException e) {
                Log.d(TAG, "" + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            number++;
        }
        return bitmapImage;
    }

    public void saveImageToExternalStorage(Context context, Bitmap campaignImage, String campaignImageName) {
        try {
            if (isExternalStorageWritable(context)) {
                File dir = new File(directory);
                if (!dir.exists())
                    dir.mkdirs();

                FileOutputStream fOut = null;
                if (campaignImageName != null) {
                    File imgFile = new File(dir, campaignImageName);
                    fOut = new FileOutputStream(imgFile);
                    campaignImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                }

                if (fOut != null) {
                    fOut.flush();
                    fOut.close();
                }
                campaignImage.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteImage(String campaignImageName) {
        try {
            File dir = new File(directory);
            if (!dir.exists())
                return;
            File imgFile = new File(dir, campaignImageName);
            imgFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap readImageFromExtStorage(String imageName) {
        Bitmap bitmap = null;
        try {
            File f = new File(directory, imageName);
            if (f.exists()) {
                Log.d("Image", "Exists");
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void showAlertDialog(Activity activity, String message) {
        try {
            if (activity != null && !activity.isFinishing()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle("Alert");
                alert.setMessage(message);
                alert.setPositiveButton("OK", null);
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isScreenPortrait() {
        try {
            int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            int orientation;
            // if the device's natural orientation is portrait:
            if ((rotation == Surface.ROTATION_0
                    || rotation == Surface.ROTATION_180) && height > width ||
                    (rotation == Surface.ROTATION_90
                            || rotation == Surface.ROTATION_270) && width > height) {
                switch (rotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                    default:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                }
            }
            // if the device's natural orientation is landscape or if the device
            // is square:
            else {
                switch (rotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                    default:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                }
            }
            if (!isTablet())
                return orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? true : false;
            else
                return orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? true : false;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isTablet() {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public void showProgressDialog() {
        if (progressDialog == null ? true : !progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public void showGrowthHack(final AppVirality appVirality) {
        if (appVirality != null) {
            showProgressDialog();
            appVirality.getCampaigns(null, new AppVirality.CampaignDetailsListener() {
                @Override
                public void onGetCampaignDetails(ArrayList<CampaignDetail> campaignDetails, boolean refreshImages, String errorMsg) {
                    dismissProgressDialog();
                    CampaignDetail womCampaignDetail = appVirality.getCampaignDetail(Constants.GrowthHackType.Word_of_Mouth, campaignDetails);
                    if (refreshImages)
                        refreshImages(womCampaignDetail);
                    if (campaignDetails.size() > 0 && womCampaignDetail != null) {
                        Intent growthHackIntent = new Intent(context, GrowthHackActivity.class);
                        growthHackIntent.putExtra("campaign_details", campaignDetails);
                        growthHackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(growthHackIntent);
                        appVirality.recordImpressionsClicks(womCampaignDetail.campaignId, false, true);
                    } else {
                        showAlertDialog((Activity) context, "Sorry, no active referrals at this time, please try again later.");
                    }
                }
            });
        }
    }

    public boolean hasUserWillChoose(ArrayList<CampaignDetail> campaignDetails) {
        for (CampaignDetail campaignDetail : campaignDetails) {
            if (campaignDetail.userWillChoose)
                return true;
        }
        return false;
    }

    public void refreshImages(CampaignDetail campaignDetail) {
        if (campaignDetail != null) {
            downloadAndSaveImage(campaignDetail.campaignImgUrl, campaignDetail.campaignId + "-" + campaignDetail.campaignImgUrl.substring(campaignDetail.campaignImgUrl.lastIndexOf("/") + 1));
            for (SocialAction socialAction : campaignDetail.campaignSocialActions) {
                boolean hasImage = !TextUtils.isEmpty(socialAction.shareImageUrl);
                String imageName = campaignDetail.campaignId + "-" + socialAction.socialActionName.toLowerCase() + ".jpg";
                String imagePath = hasImage ? directory + "/" + imageName : null;
                switch (socialAction.socialActionName.toLowerCase()) {
                    case "instagram":
                        downloadAndSaveImage(socialAction.shareImageUrl, imageName);
                        appVirality.setInstagramImagePath(imagePath);
                        break;
                    case "pinterest":
                        downloadAndSaveImage(socialAction.shareImageUrl, imageName);
                        appVirality.setPinterestImagePath(imagePath);
                        break;
                    case "twitter":
                        downloadAndSaveImage(socialAction.shareImageUrl, imageName);
                        appVirality.setTwitterImagePath(imagePath);
                        break;
                    case "whatsapp":
                        downloadAndSaveImage(socialAction.shareImageUrl, imageName);
                        appVirality.setWhatsAppImagePath(imagePath);
                        break;
                }
            }
        }
    }

    class ImgDownloadTask extends AsyncTask<String, Void, Bitmap> {

        ImageView imgView;

        ImgDownloadTask(ImageView imgView) {
            this.imgView = imgView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = downloadImage(params[0]);
            if (params[1] != null)
                saveImageToExternalStorage(context, bitmap, params[1]);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (imgView != null && bitmap != null)
                imgView.setImageBitmap(bitmap);
        }
    }
}
