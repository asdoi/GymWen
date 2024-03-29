/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.asdoi.gymwen.ui.activities;

import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.External_Const;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.util.Objects;


/**
 * @author Karim Abou Zeid (kabouzeid) from VinylMusicPlayer
 */
public class AboutActivity extends ActivityFeatures implements View.OnClickListener {

    @Nullable
    TextView appVersion;
    @Nullable
    LinearLayout share;
    @Nullable
    LinearLayout changelog;
    @Nullable
    LinearLayout intro;
    @Nullable
    LinearLayout forkOnGitHub;
    @Nullable
    LinearLayout privacy;
    @Nullable
    LinearLayout licenses;
    @Nullable
    LinearLayout image_sources;
    @Nullable
    LinearLayout libs;

    @Nullable
    LinearLayout writeAnEmail;
    @Nullable
    LinearLayout visitWebsite;
    @Nullable
    LinearLayout colorush;

    @Nullable
    LinearLayout reportBugs;

    @Nullable
    LinearLayout imprint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.about_frame, new AboutFragment()).commit();
//        setDrawUnderStatusbar();


//        setStatusbarColorAuto();
//        setNavigationbarColorAuto();
//        setTaskDescriptionColorAuto();


    }

    public void setupColors() {
        setToolbar(true);
    }

    public static class AboutFragment extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.content_about, container, false);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        appVersion = findViewById(R.id.app_version);
        share = findViewById(R.id.share);
        changelog = findViewById(R.id.changelog);
        intro = findViewById(R.id.intro);
        forkOnGitHub = findViewById(R.id.fork_on_github);
        privacy = findViewById(R.id.privacy);
        licenses = findViewById(R.id.licenses);
        image_sources = findViewById(R.id.image_sources);
        libs = findViewById(R.id.libs);
        writeAnEmail = findViewById(R.id.write_an_email);
        visitWebsite = findViewById(R.id.visit_website);
        colorush = findViewById(R.id.colorush);
        reportBugs = findViewById(R.id.report_bugs);
        imprint = findViewById(R.id.imprint);

        setUpViews();
    }

    private void setUpViews() {
        setUpAppVersion();
        setUpOnClickListeners();
    }

    private void setUpAppVersion() {
        Objects.requireNonNull(appVersion).setText(getCurrentVersionName(this));
    }

    private void setUpOnClickListeners() {
        Objects.requireNonNull(changelog).setOnClickListener(this);
        Objects.requireNonNull(intro).setOnClickListener(this);
        Objects.requireNonNull(licenses).setOnClickListener(this);
        Objects.requireNonNull(forkOnGitHub).setOnClickListener(this);
        Objects.requireNonNull(visitWebsite).setOnClickListener(this);
        Objects.requireNonNull(reportBugs).setOnClickListener(this);
        Objects.requireNonNull(writeAnEmail).setOnClickListener(this);
        Objects.requireNonNull(share).setOnClickListener(this);
        Objects.requireNonNull(privacy).setOnClickListener(this);
        Objects.requireNonNull(image_sources).setOnClickListener(this);
        Objects.requireNonNull(libs).setOnClickListener(this);
        Objects.requireNonNull(colorush).setOnClickListener(this);
        Objects.requireNonNull(imprint).setOnClickListener(this);
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
            showChangelog(false);
        } else if (v == licenses) {
            String license = getString(R.string.gnu_license);

            if (Build.VERSION.SDK_INT > 24)
                license = Html.fromHtml(license, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                license = Html.fromHtml(license).toString();

            SpannableString s = new SpannableString(license);
            Linkify.addLinks(s, Linkify.WEB_URLS);

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_description_white_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(drawable));
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(requireContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            new MaterialDialog.Builder(requireContext())
                    .title(getString(R.string.licenses))
                    .content(s)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .positiveText(R.string.ok)
                    .icon(Objects.requireNonNull(drawable))
                    .show();
        } else if (v == intro) {
            startActivity(new Intent(this, AppIntroActivity.class));
            finish();
        } else if (v == forkOnGitHub) {
            tabIntent(External_Const.GITLAB);
        } else if (v == visitWebsite) {
            tabIntent(External_Const.WEBSITE);
        } else if (v == reportBugs) {
            tabIntent(External_Const.BUGSITE);
        } else if (v == writeAnEmail) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + External_Const.author_mail));
            intent.putExtra(Intent.EXTRA_EMAIL, External_Const.author_mail);
            intent.putExtra(Intent.EXTRA_SUBJECT, "GymWenApp");
            startActivity(Intent.createChooser(intent, "E-Mail"));
        } else if (v == colorush) {
            final String downloadSite = External_Const.downloadApp_colorush;
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
                Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(drawable));
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(requireContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            new MaterialDialog.Builder(requireContext())
                    .title(getString(R.string.menu_privacy))
                    .content(datenschutz)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .positiveText(R.string.ok)
                    .icon(Objects.requireNonNull(drawable))
                    .show();
        } else if (v == image_sources) {
            String sources = getString(R.string.credits);

            if (Build.VERSION.SDK_INT > 24)
                sources = Html.fromHtml(sources, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                sources = Html.fromHtml(sources).toString();
            sources = sources.replaceAll("\n\n", "\n");

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_image_black_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(drawable));
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(requireContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            final TextView message = new TextView(requireContext());
            final SpannableString s = new SpannableString(sources);
            Linkify.addLinks(s, Linkify.WEB_URLS);
            message.setText(s);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            new MaterialDialog.Builder(requireContext())
                    .title(getString(R.string.image_sources))
                    .cancelable(true)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .icon(Objects.requireNonNull(drawable))
                    .customView(message, true)
                    .show();

        } else if (v == libs) {
            new LibsBuilder()
                    .withActivityTitle(getString(R.string.impressum_AboutLibs_Title))
                    .withAboutIconShown(true)
                    .withFields(R.string.class.getFields())
                    .withLicenseShown(true)
                    .withAboutDescription(getString(R.string.subtitle))
                    .withAboutAppName(getString(R.string.app_name))
                    .start(this);
        } else if (v == imprint) {
            String sources = getString(R.string.imprint_text);

            if (Build.VERSION.SDK_INT > 24)
                sources = Html.fromHtml(sources, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                sources = Html.fromHtml(sources).toString();
            sources = sources.replaceAll("\n\n", "\n");

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_credit_card_black_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(drawable));
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(requireContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            final TextView message = new TextView(requireContext());
            final SpannableString s = new SpannableString(sources);
            Linkify.addLinks(s, Linkify.WEB_URLS);
            message.setText(s);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            new MaterialDialog.Builder(requireContext())
                    .title(getString(R.string.imprint))
                    .cancelable(true)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .icon(Objects.requireNonNull(drawable))
                    .customView(message, true)
                    .show();
        }
    }

    private void share() {
        String message = getString(R.string.share_app_message) + " " + External_Const.DOWNLOAD_LINK;
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, message);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, getString(R.string.share_app)));
    }
}
