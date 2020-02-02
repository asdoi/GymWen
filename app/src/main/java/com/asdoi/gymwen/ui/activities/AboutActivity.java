package com.asdoi.gymwen.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @author Karim Abou Zeid (kabouzeid) from VinylMusicPlayer
 */
@SuppressWarnings("FieldCanBeLocal")
public class AboutActivity extends ActivityFeatures implements View.OnClickListener, ColorChooserDialog.ColorCallback {

    private static String GITLAB = "https://gitlab.com/asdoi/GymWen/";

    private static String WEBSITE = "https://asdoi.gitlab.io/";

    private static String BUGSITE = "https://gitlab.com/asdoi/GymWen/issues";

    @BindView(R.id.app_version)
    TextView appVersion;
    @BindView(R.id.share)
    LinearLayout share;
    @BindView(R.id.changelog)
    LinearLayout changelog;
    @BindView(R.id.intro)
    LinearLayout intro;
    @BindView(R.id.fork_on_github)
    LinearLayout forkOnGitHub;
    @BindView(R.id.privacy)
    LinearLayout privacy;
    @BindView(R.id.licenses)
    LinearLayout licenses;
    @BindView(R.id.image_sources)
    LinearLayout image_sources;
    @BindView(R.id.libs)
    LinearLayout libs;

    @BindView(R.id.write_an_email)
    LinearLayout writeAnEmail;
    @BindView(R.id.visit_website)
    LinearLayout visitWebsite;
    @BindView(R.id.colorush)
    LinearLayout colorush;

    @BindView(R.id.report_bugs)
    LinearLayout reportBugs;

    @BindView(R.id.color_accent)
    LinearLayout color_accent;

    @BindView(R.id.color_primary)
    LinearLayout color_primary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.about_frame, new Fragment1()).commit();
//        setDrawUnderStatusbar();


//        setStatusbarColorAuto();
//        setNavigationbarColorAuto();
//        setTaskDescriptionColorAuto();


    }

    public void setupColors() {
        setToolbar(true);
    }

    public static class Fragment1 extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.content_about, container, false);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        ButterKnife.bind(this);
        setUpViews();
        if (ApplicationFeatures.isBetaEnabled()) {
            color_accent.setVisibility(View.VISIBLE);
            color_primary.setVisibility(View.VISIBLE);
        } else {
            color_accent.setVisibility(View.GONE);
            color_primary.setVisibility(View.GONE);
        }
    }

    private void setUpViews() {
        setUpAppVersion();
        setUpOnClickListeners();
    }

    private void setUpAppVersion() {
        appVersion.setText(getCurrentVersionName(this));
    }

    private void setUpOnClickListeners() {
        changelog.setOnClickListener(this);
        intro.setOnClickListener(this);
        licenses.setOnClickListener(this);
        forkOnGitHub.setOnClickListener(this);
        visitWebsite.setOnClickListener(this);
        reportBugs.setOnClickListener(this);
        writeAnEmail.setOnClickListener(this);
        share.setOnClickListener(this);
        privacy.setOnClickListener(this);
        image_sources.setOnClickListener(this);
        libs.setOnClickListener(this);
        colorush.setOnClickListener(this);
        color_accent.setOnClickListener(this);
        color_primary.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static String getCurrentVersionName(@NonNull final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "Unkown";
    }

    @Override
    public void onClick(View v) {
        if (v == changelog) {
            showChangelogCK(false);
        } else if (v == licenses) {
            showLicenseDialog();
        } else if (v == intro) {
            startActivity(new Intent(this, AppIntroActivity.class));
        } else if (v == forkOnGitHub) {
            tabIntent(GITLAB);
        } else if (v == visitWebsite) {
            tabIntent(WEBSITE);
        } else if (v == reportBugs) {
            tabIntent(BUGSITE);
        } else if (v == writeAnEmail) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:GymWenApp@t-online.de"));
            intent.putExtra(Intent.EXTRA_EMAIL, "GymWenApp@t-online.de");
            intent.putExtra(Intent.EXTRA_SUBJECT, "GymWenApp");
            startActivity(Intent.createChooser(intent, "E-Mail"));
        } else if (v == colorush) {
            final String downloadSite = "https://gitlab.com/asdoi/colorrush/blob/master/Apk/ColoRush.apk";
            tabIntent(downloadSite);
        } else if (v == share) {
            share();
        } else if (v == privacy) {
            String datenschutz = getString(R.string.privacy);

            if (Build.VERSION.SDK_INT > 24)
                datenschutz = Html.fromHtml(datenschutz, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                datenschutz = Html.fromHtml(datenschutz).toString();

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_fingerprint_black_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.menu_privacy))
                    .setMessage(datenschutz)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(drawable)
                    .show();
        } else if (v == image_sources) {
            String sources = getString(R.string.credits);

            if (Build.VERSION.SDK_INT > 24)
                sources = Html.fromHtml(sources, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                sources = Html.fromHtml(sources).toString();

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_image_black_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            final TextView message = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            message.setLayoutParams(params);

            final SpannableString s = new SpannableString(sources);
            Linkify.addLinks(s, Linkify.WEB_URLS);
            message.setText(s);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.image_sources))
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                        }
                    })
                    .setIcon(drawable)
                    .setView(message)
                    .create()
                    .show();

        } else if (v == libs) {
            Intent intent = new LibsBuilder()
                    .withActivityTitle(getString(R.string.impressum_AboutLibs_Title))
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withFields(R.string.class.getFields())
                    .withAutoDetect(true)
                    .withAboutIconShown(true)
                    .withLicenseShown(true)
                    .withAboutDescription(getString(R.string.subtitle))
                    .withAboutAppName(getString(R.string.app_name))
                    .withActivityTheme(R.style.AboutLibrariesTheme)
                    .intent(this);

            startActivity(intent);
        } else if (v == color_accent) {
            new ColorChooserDialog.Builder(this, R.string.color_accent)
                    .accentMode(false)
                    .allowUserColorInput(true)
                    .allowUserColorInputAlpha(false)
                    .show(this);
        } else if (v == color_primary) {
            new ColorChooserDialog.Builder(this, R.string.color_primary)
                    .accentMode(false)
                    .allowUserColorInput(true)
                    .allowUserColorInputAlpha(false)
                    .show(this);
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (dialog.getTitle()) {
            case R.string.color_accent:
                editor.putInt("colorAccent", selectedColor);
                break;
            case R.string.color_primary:
                editor.putInt("colorPrimary", selectedColor);
                break;
        }
        editor.apply();

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }*/
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    private void showLicenseDialog() {
        startActivity(new Intent(this, OssLicensesMenuActivity.class));
    }

    private void share() {
        String link = "https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk";
        String message = getString(R.string.share_app_message) + " " + link;
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, message);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, getString(R.string.share_app)));
    }
}