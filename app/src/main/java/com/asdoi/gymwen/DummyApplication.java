package com.asdoi.gymwen;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.ChoiceActivity;
import com.asdoi.gymwen.main.SignInActivity;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;

public class DummyApplication extends Application {
    private static Context mContext;

    public static void tabIntent(String url) {
        Context context = getContext();
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setShowTitle(true)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_back_white_24dp))
                    .build();

            // This is optional but recommended
            CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent);

            // This is where the magic happens...
            CustomTabsHelper.openCustomTab(context, customTabsIntent,
                    Uri.parse(url),
                    new WebViewFallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean initSettings(boolean isWidget) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean signedIn = sharedPref.getBoolean("signed", false);

        if (signedIn) {
            boolean oberstufe = sharedPref.getBoolean("oberstufe", true);
            String courses = sharedPref.getString("courses", "");
            if (courses.trim().isEmpty()) {
                Intent i = new Intent(context, ChoiceActivity.class);
                context.startActivity(i);
                return signedIn;
            }
            VertretungsPlan.setup(oberstufe, courses.split("#"), courses);

//            System.out.println("settings: " + oberstufe + courses);

            String username = sharedPref.getString("username", "");
            String password = sharedPref.getString("password", "");

            VertretungsPlan.signin(username, password);
            if (!isWidget) {
                proofeNotification();
                updateMyWidgets(context);
            }
        } else {
            Intent i = new Intent(context, SignInActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        return signedIn;
    }

    public static void proofeNotification() {
        Context context = getContext();
        Intent intent = new Intent(context, NotificationService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        try {
        context.stopService(intent);
        context.startService(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static void updateMyWidgets(Context context) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(new ComponentName(context, VertretungsplanWidget.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(VertretungsplanWidget.WIDGET_ID_KEY, ids);
        context.sendBroadcast(updateIntent);
    }

    public Runnable downloadRunnable = new Runnable() {
        @Override
        public void run() {
            downloadDocs();
        }

        private void downloadDocs() {

            //DownloadDocs
            if (!VertretungsPlan.areDocsDownloaded() && DummyApplication.isNetworkAvailable()) {
                if (!DummyApplication.initSettings(true)) {
                    return;
                }
                String[] strURL = new String[]{VertretungsPlan.todayURL, VertretungsPlan.tomorrowURL};
                Document[] doc = new Document[strURL.length];
                for (int i = 0; i < 2; i++) {

                    String authString = VertretungsPlan.strUserId + ":" + VertretungsPlan.strPasword;

                    String lastAuthString = VertretungsPlan.lastAuthString;
                    //Check if already tried logging in with this authentication and if it failed before, return null
                    if (lastAuthString.length() > 1 && lastAuthString.substring(0, lastAuthString.length() - 1).equals(authString) && lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
                        System.out.println("failed before with same authString");
                        //return doc;
                    }

                    String encodedString =
                            new String(Base64.encodeBase64(authString.getBytes()));

                    try {
                        doc[i] = Jsoup.connect(strURL[i])
                                .header("Authorization", "Basic " + encodedString)
                                .get();

                        VertretungsPlan.lastAuthString = authString + "t";


                    } catch (IOException e) {
                        e.printStackTrace();
                        VertretungsPlan.lastAuthString = authString + "f";
                        return;
                    }
                }
                VertretungsPlan.setDocs(doc[0], doc[1]);
            }
        }
    };

    public static class downloadDocsTask extends AsyncTask<String, Void, Document[]> {
        @Override
        protected Document[] doInBackground(String... strURL) {
            Document[] doc = new Document[strURL.length];
            if (!VertretungsPlan.areDocsDownloaded()) {
                for (int i = 0; i < strURL.length; i++) {

                    String authString = VertretungsPlan.strUserId + ":" + VertretungsPlan.strPasword;

                    String lastAuthString = VertretungsPlan.lastAuthString;
                    //Check if already tried logging in with this authentication and if it failed before, return null
                    if (lastAuthString.length() > 1 && lastAuthString.substring(0, lastAuthString.length() - 1).equals(authString) && lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
                        System.out.println("failed before with same authString");
                        //return doc;
                    }

                    String encodedString =
                            new String(Base64.encodeBase64(authString.getBytes()));

                    try {
                        doc[i] = Jsoup.connect(strURL[i])
                                .header("Authorization", "Basic " + encodedString)
                                .get();

                        System.out.println("Logged in using basic authentication");
                        VertretungsPlan.lastAuthString = authString + "t";


                    } catch (IOException e) {
                        e.printStackTrace();
                        VertretungsPlan.lastAuthString = authString + "f";
                        return null;
                    }
                }
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document[] result) {
//            new createTable().execute(result);

            //Set Document
            setDocs(result);
        }

        public void setDocs(Document[] values) {
            if (!VertretungsPlan.areDocsDownloaded()) {
                if (values == null) {
                    VertretungsPlan.setDocs(null, null);
                    return;
                }
                if (values.length == 2) {
                    VertretungsPlan.setDocs(values[0], values[1]);
                } else if (values.length == 1) {
                    VertretungsPlan.setTodayDoc(values[0]);
                }
            }
        }
    }

    public static class downloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public downloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            if (!urldisplay.trim().isEmpty()) {
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
//                Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return mIcon11;
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
