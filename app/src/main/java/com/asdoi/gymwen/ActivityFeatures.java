package com.asdoi.gymwen;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.receivers.AlarmReceiver;
import com.asdoi.gymwen.ui.activities.MainActivity;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;
import com.pd.chocobar.ChocoBar;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import de.cketti.library.changelog.ChangeLog;
import info.isuru.sheriff.enums.SheriffPermission;
import info.isuru.sheriff.helper.Sheriff;
import info.isuru.sheriff.interfaces.PermissionListener;
import ru.github.igla.ferriswheel.FerrisWheelView;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;


public abstract class ActivityFeatures extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    public Context getContext() {
        return this;
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupColors();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ApplicationFeatures.getGeneralTheme());
        setStatusbarColorAuto();
        super.onCreate(savedInstanceState);
        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);
    }


    //Colors

    /**
     * @author Karim Abou Zeid (kabouzeid) from VinylMusicPlayer
     */

    private void setupColors2() {

    }

    public abstract void setupColors();

    public void setToolbar(boolean backButton) {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setBackgroundColor(ApplicationFeatures.getPrimaryColor(this));
                setSupportActionBar(toolbar);
            }
            //noinspection ConstantConditions
            if (backButton)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNavigationbarColor(int color) {
        if (ThemeStore.coloredNavigationBar(this)) {
            ATH.setNavigationbarColor(this, color);
        } else {
            ATH.setNavigationbarColor(this, Color.BLACK);
        }
    }

    public void setNavigationbarColorAuto() {
//        setNavigationbarColor(ThemeStore.navigationBarColor(this));
        setNavigationbarColor(ApplicationFeatures.getPrimaryColor(this));
    }

    public void setStatusbarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View statusBar = /*getWindow().getDecorView().getRootView().findViewById(R.id.status_bar)*/ null;
            if (statusBar != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBar.setBackgroundColor(ColorUtil.darkenColor(color));
                    setLightStatusbarAuto(color);
                } else {
                    statusBar.setBackgroundColor(color);
                }
            } else if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(ColorUtil.darkenColor(color));
                setLightStatusbarAuto(color);
            }
        }
    }

    public void setStatusbarColorAuto() {
        // we don't want to use statusbar color because we are doing the color darkening on our own to support KitKat
//        setStatusbarColor(ThemeStore.primaryColor(this));
        setStatusbarColor(ApplicationFeatures.getPrimaryColor(this));
    }

    public void setLightStatusbar(boolean enabled) {
        ATH.setLightStatusbar(this, enabled);
    }

    public void setLightStatusbarAuto(int bgColor) {
        setLightStatusbar(ColorUtil.isColorLight(bgColor));
    }


    //Changelog
    public void showChangelogCK(boolean checkFirstRun) {
        ChangeLog cl = new ChangeLog(this);
        try {
            if (checkFirstRun) {
                if (cl.isFirstRun())
                    cl.getLogDialog().show();
            } else {
                cl.getFullLogDialog().show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TabIntent and UpdateCheck
    public void tabIntent(String url) {
        Context context = this;
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(ApplicationFeatures.getPrimaryColor(this))
                    .setShowTitle(true)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(), R.attr.colorPrimary))
                    .build();

//            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    public void checkUpdates(Display display, boolean showUpdated) {
        Context context = this;
        String url = "https://gitlab.com/asdoi/gymwenreleases/raw/master/UpdaterFile.json";

        try {
            AppUpdater appUpdater = new AppUpdater(context)
                    .setDisplay(display)
                    .setUpdateFrom(UpdateFrom.JSON)
                    .setUpdateJSON(url)
                    .setTitleOnUpdateAvailable(R.string.update_available_title)
                    .setContentOnUpdateAvailable(R.string.update_available_content)
                    .setTitleOnUpdateNotAvailable(R.string.update_not_available_title)
                    .setContentOnUpdateNotAvailable(R.string.update_not_available_content)
                    .setButtonUpdate(R.string.update_now)
                    .setButtonUpdateClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                String apkUrl = "https://gitlab.com/asdoi/gymwenreleases/raw/master/GymWenApp.apk";
                                startDownload(apkUrl, "GymWen Version " + (BuildConfig.VERSION_CODE + 1), getContext().getString(R.string.update_down_title), Environment.DIRECTORY_DOWNLOADS, "GymWenAppv" + (BuildConfig.VERSION_CODE + 1) + ".apk", new installApk("GymWenAppv" + (BuildConfig.VERSION_CODE + 1) + ".apk"));
                            } catch (Exception e) {
                                tabIntent("https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk");
                            }
                        }
                    })
                    .setButtonDismiss(R.string.update_later)
                    .setButtonDoNotShowAgain(/*R.string.update_website*/ null)
                    /*.setButtonDismissClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tabIntent("https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk");
                        }
                    })*/
                    .setIcon(R.drawable.ic_system_update_black_24dp)
                    .showAppUpdated(showUpdated);
            appUpdater.start();
        } catch (Exception e) {
            AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                    .setUpdateFrom(UpdateFrom.JSON)
                    .setUpdateJSON(url)
                    .withListener(new AppUpdaterUtils.UpdateListener() {
                        @Override
                        public void onSuccess(Update update, Boolean isUpdateAvailable) {
                            if (isUpdateAvailable) {
                                Toast.makeText(getContext(), getContext().getString(R.string.update_available_title), Toast.LENGTH_LONG).show();
                                tabIntent("https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk");
                            }
                        }

                        @Override
                        public void onFailed(AppUpdaterError error) {

                        }
                    });
            appUpdaterUtils.start();
        }
    }

    public void createLoadingPanel(ViewGroup view) {
        Context context = ApplicationFeatures.getContext();
        FrameLayout base = new FrameLayout(context);
        base.setBackgroundColor(ApplicationFeatures.getBackgroundColor(this));
        base.setTag("vertretung_loading");
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout panel = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 30);
        panel.setLayoutParams(params);
        panel.setGravity(Gravity.BOTTOM);
        panel.setOrientation(LinearLayout.VERTICAL);

        FerrisWheelView ferrisWheelView = new FerrisWheelView(context);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ferrisWheelView.setLayoutParams(params2);
        ferrisWheelView.setNumberOfCabins(8);
        ferrisWheelView.setRotateDegreeSpeedInSec(35);
//        ferrisWheelView.setWheelColor(R.color.wheel_wheel);
//        ferrisWheelView.setClockwise(false);
        ferrisWheelView.setAutoRotate(true);
        ferrisWheelView.startAnimation();


        ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setIndeterminate(true);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 5, 20, 0);
        bar.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                bar.getIndeterminateDrawable().setColorFilter(new BlendModeColorFilter(ApplicationFeatures.getAccentColor(this), BlendMode.SRC_ATOP));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bar.getIndeterminateDrawable().setColorFilter(ApplicationFeatures.getAccentColor(this), PorterDuff.Mode.SRC_ATOP);
        }


        TextView textView = new TextView(context);
        textView.setTextColor(ApplicationFeatures.getTextColorPrimary(this));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText(ApplicationFeatures.getContext().getString(R.string.downloading));

        base.addView(ferrisWheelView);

        panel.addView(bar);
        panel.addView(textView);

        base.addView(panel);


        view.addView(base);
    }

    public View getTeacherView(View view, String[] entry) {
        TextView kuerzel = view.findViewById(R.id.teacher_kürzel);
        kuerzel.setText(entry[0]);

        TextView nname = view.findViewById(R.id.teacher_nname);
        nname.setText(entry[1]);

        TextView vname = view.findViewById(R.id.teacher_vname);
        vname.setText(" " + entry[2]);

        TextView hour = view.findViewById(R.id.teacher_hour);
        hour.setText(entry[3]);
        hour.setVisibility(View.GONE);

        LinearLayout hideLayout = view.findViewById(R.id.teacher_list_linear);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        hideLayout.setLayoutParams(params);


        Button mailButton = view.findViewById(R.id.teacher_mail);
        mailButton.setOnClickListener((View v) -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + entry[0] + "@gym-wendelstein.de"));
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                try {
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(emailIntent);
                } catch (ActivityNotFoundException e2) {
                    ChocoBar.builder().setActivity(this).setText(getString(R.string.no_email_app)).setDuration(ChocoBar.LENGTH_LONG).red().show();
                }
            }
        });

        try {
            FrameLayout root = view.findViewById(R.id.teacher_rootLayout);
            root.setOnClickListener((View v) -> {
                hour.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                hideLayout.setLayoutParams(params2);
            });
        } catch (Exception e) {
            //NullPointerException for root on API 17
            e.printStackTrace();
        }

        return view;
    }


    //Permissions
    private Sheriff sheriffPermission;
    private static final int REQUEST_MULTIPLE_PERMISSION = 101;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sheriffPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestPermission(Runnable runAfter, SheriffPermission... permissions) {
        PermissionListener pl = new MyPermissionListener(runAfter);

        sheriffPermission = Sheriff.Builder()
                .with(this)
                .requestCode(REQUEST_MULTIPLE_PERMISSION)
                .setPermissionResultCallback(pl)
                .askFor(permissions)
                .rationalMessage(getContext().getString(R.string.sheriff_permission_rational))
                .build();

        sheriffPermission.requestPermissions();
    }

    private class MyPermissionListener implements PermissionListener {
        Runnable runAfter;

        public MyPermissionListener(Runnable r) {
            runAfter = r;
        }

        @Override
        public void onPermissionsGranted(int requestCode, ArrayList<String> acceptedPermissionList) {
            if (runAfter == null)
                return;
            try {
                runAfter.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPermissionsDenied(int requestCode, ArrayList<String> deniedPermissionList) {
            // setup the alert builder
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
            builder.title(getContext().getString(R.string.permission_required));
            builder.content(getContext().getString(R.string.permission_required_description));

            // add the buttons
            builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(MaterialDialog dialog, DialogAction which) {
                    openAppPermissionSettings();
                }
            });
            builder.negativeText(getContext().getString(R.string.permission_ok_button));

            builder.negativeText(getContext().getString(R.string.permission_cancel_button));
            builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(MaterialDialog dialog, DialogAction which) {
                    dialog.dismiss();
                }
            });

            // create and show the alert dialog
            MaterialDialog dialog = builder.build();
            dialog.show();
        }
    }

    private void openAppPermissionSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    //DownloadManager
    private long downloadID;

    public void startDownload(String url, String title, String description, String dirType, String subPath, BroadcastReceiver onComplete) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermission(() -> {
                startDownload(url, title, description, dirType, subPath, onComplete);
            }, SheriffPermission.STORAGE);
            return;

        }

        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        Uri uri = Uri.parse(url);

        DownloadManager mgr = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(title)
                .setDescription(description)
                .setDestinationInExternalPublicDir(dirType, subPath)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        downloadID = mgr.enqueue(request);

        Toast.makeText(this, getString(R.string.download_start), Toast.LENGTH_LONG).show();

    }

    private class installApk extends BroadcastReceiver {
        String subPath;

        public installApk(String subPath) {
            this.subPath = subPath;
        }

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
//                installApk(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + subPath);
                installApk(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + subPath);
                unregisterReceiver(this);
            }
        }
    }

    BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
            unregisterReceiver(this);
        }
    };


    //Apk Installer
    public void installApk(String path) {
        File apkFile = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", apkFile);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


    //Make Call
    public void makeCall(String telNr) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(() -> {
                makeCall(telNr);
            }, SheriffPermission.PHONE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + telNr));
        startActivity(intent);
    }


    //Time picker
    public void createTimePicker() {
        Calendar now = Calendar.getInstance();

        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.setTitle(ApplicationFeatures.getContext().getString(R.string.time_picker_title));
        tpd.setAccentColor(ApplicationFeatures.getAccentColor(this));
        tpd.setCancelColor(ApplicationFeatures.getAccentColor(this));
        tpd.setOkColor(ApplicationFeatures.getAccentColor(this));
        tpd.show(this.getSupportFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        ApplicationFeatures.setAlarmTime(hourOfDay, minute, second);
        ApplicationFeatures.setAlarm(this, AlarmReceiver.class, hourOfDay, minute, second);
    }


    //Save and Reload Documents
    public void saveDocs() {
        VertretungsPlanFeatures.saveDocs();
        Lehrerliste.saveDoc();
        ProfileManagement.save(false);
//        Toast.makeText(ApplicationFeatures.getContext(), R.string.saved_docs, Toast.LENGTH_SHORT).show();
    }

    public static void reloadVertretungDocs(Context context) {
        if (VertretungsPlanFeatures.reloadDocs()) {
            try {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast toast = Toast.makeText(context, R.string.reloaded_docs, Toast.LENGTH_SHORT);
                    TextView v = toast.getView().findViewById(android.R.id.message);
                    if (v != null)
                        v.setGravity(Gravity.CENTER);
                    toast.show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //Grades Management
    private final static String gradesFileName = "Notenverwaltung.xlsx";
    public final static String downloadGradesTable = "https://gitlab.com/asdoi/Overview-about-your-grades/raw/master/Gesamtes_Notenbild.xlsx?inline=false";

    public void checkGradesFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(this::checkGradesFile, SheriffPermission.STORAGE);
            return;
        }

//        String path = this.getExternalFilesDir(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS) + File.separator + gradesFileName;
        String path = Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS) + File.separator + gradesFileName;
        File file = new File(path);
        if (file.exists()) {
            openGradesFile();
        } else {
            startDownload(downloadGradesTable, getString(R.string.grades_management), getString(R.string.grades_down_title), Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS, gradesFileName, openGradesFile);
        }
    }

    BroadcastReceiver openGradesFile = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            openGradesFile();
            unregisterReceiver(this);
        }
    };

    private void openGradesFile() {
//        String path = this.getExternalFilesDir(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS) + File.separator + gradesFileName;
        String path = Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS) + File.separator + gradesFileName;
        File file = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, this.getString(R.string.grades_no_app), Toast.LENGTH_LONG).show();
            String packageNameMSExcel = "com.microsoft.office.excel";
            String packageNameLibreOffice = "org.documentfoundation.libreoffice";
            String packageNameWPS = "cn.wps.moffice_eng";

            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageNameMSExcel)));
            } catch (android.content.ActivityNotFoundException anfe) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageNameLibreOffice)));
                } catch (android.content.ActivityNotFoundException a) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageNameWPS)));
                    } catch (android.content.ActivityNotFoundException a2) {
                        //Open Browser to Download
                        tabIntent("https://f-droid.org/de/packages/org.documentfoundation.libreoffice/");
                    }
                }
            }
        }
    }


    //Language
    @Override
    protected void attachBaseContext(Context newBase) {
//        newBase = LocaleChanger.configureBaseContext(newBase);
        super.attachBaseContext(newBase);
    }
}
