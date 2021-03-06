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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.fragments.WebsiteActivityFragment;
import com.asdoi.gymwen.ui.fragments.WebsiteSearchFragment;
import com.asdoi.gymwen.util.External_Const;
import com.pd.chocobar.ChocoBar;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class WebsiteActivity extends ActivityFeatures implements View.OnClickListener {
    @NonNull
    public static final String LOADURL = "url";
    @NonNull
    public static final String SEARCH = "search";

    private boolean search = false;

    @Nullable
    public ArrayList<String> history = new ArrayList<>();

    private static String[][] con;
    private Document doc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website);
        start();
    }

    public void setupColors() {
        setToolbar(true);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_website, menu);
        menu.findItem(R.id.action_website_search).setVisible(!search);
        menu.findItem(R.id.action_website).setVisible(search);
        return true;
    }


    private void start() {

        if (!ApplicationFeatures.isNetworkAvailable()) {
            ChocoBar.builder().setActivity(this)
                    .setActionText(getString(R.string.ok))
                    .setText(getString(R.string.noInternetConnection))
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    .setActionClickListener((View v) -> finish())
                    .setIcon(R.drawable.ic_no_wifi)
                    .orange()
                    .show();
            return;
        }

        try {
            if (getIntent() != null) {
                String intentURL;
                if (getIntent().hasExtra(LOADURL)) {
                    intentURL = getIntent().getStringExtra(LOADURL);
                } else if (getIntent().hasExtra(SEARCH)) {
                    search = true;
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.website_host, new WebsiteSearchFragment()).commit();
                    Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.action_search);
                    invalidateOptionsMenu();
                    setIntent(null);
                    return;
                } else {
                    intentURL = Objects.requireNonNull(getIntent().getData()).toString();
                }

                if (intentURL != null) {
                    loadPage(intentURL);
                    setIntent(null);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ApplicationFeatures.websiteHistorySaveInstance == null) {
            HomepageLoad();
        } else {
            history = ApplicationFeatures.websiteHistorySaveInstance;
            if (history.size() > 0)
                loadPage(history.get(history.size() - 1));
            else
                HomepageLoad();
        }

    }

    public void loadPage(@NonNull String url) {
        search = false;
        if (!url.trim().isEmpty()) {

            final String formattedUrl = ApplicationFeatures.urlToRightFormat(url);
            if (ApplicationFeatures.isURLValid(formattedUrl) && formattedUrl.contains(External_Const.page_start)) {
                (new Thread(() -> {
                    boolean isHTML;
                    try {
                        doc = Jsoup.connect(formattedUrl).get();
                        isHTML = true;
                    } catch (Exception e) {
                        e.getStackTrace();
                        isHTML = false;
                    }


                    if (isHTML) {
                        if (Objects.requireNonNull(history).size() > 0) {
                            if (!history.get(history.size() - 1).equalsIgnoreCase(formattedUrl))
                                history.add(formattedUrl);
                        } else
                            history.add(formattedUrl);
                        setWebsiteTitle(doc);
                        //Check Site
                        if (External_Const.homeOfPagesIndexes.contains(formattedUrl)) {
                            HomeOfPages(formattedUrl);
                        } else {
                            ContentPagesMixed(formattedUrl);
                        }
                    } else {
                        try {
                            openInTabIntent(formattedUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })).start();
            } else {
                openInTabIntent(formattedUrl);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_in_browser:
                try {
                    openInTabIntent(Objects.requireNonNull(history).get(history.size() - 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_share:
                Intent i = new Intent();
                i.setAction(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, Objects.requireNonNull(history).get(history.size() - 1));
                i.setType("text/plan");
                startActivity(Intent.createChooser(i, getString(R.string.share_link)));
                break;
            case R.id.action_website_search:
                getSupportFragmentManager().beginTransaction().replace(R.id.website_host, new WebsiteSearchFragment()).commit();
                search = true;
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.action_search);
                invalidateOptionsMenu();
                break;
            case R.id.action_website:
                HomepageLoad();
                search = false;
                invalidateOptionsMenu();
                break;
            case R.id.website_close:
                onSupportNavigateUp();
                break;
            case R.id.action_open_rss_feed:
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(External_Const.rss_feed_gymwen));
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Check if the key event was the Back button and if there's history

        //If image is expanded
        if (WebsiteActivityFragment.isExpanded) {
            try {
                //Not working
                WebsiteActivityFragment.expandImage.performClick();
                WebsiteActivityFragment.expandImage.callOnClick();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(this, getString(R.string.tap_picture), Toast.LENGTH_SHORT).show();
            return;
        }

        if (Objects.requireNonNull(history).size() >= 2) {
            String url = history.get(history.size() - 2);
            //Remove last two Sites, because the side that will be loaded will also be added by loadSite()
            history.remove(history.size() - 1);
            history.remove(history.size() - 1);
            loadPage(url);
        } else if (search) {
            HomepageLoad();
            search = false;
            invalidateOptionsMenu();
        } else {
            onSupportNavigateUp();
            super.onBackPressed();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        ApplicationFeatures.websiteHistorySaveInstance = null;
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id < con.length && id > 0) {
            loadPage(con[id][3]);
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        ApplicationFeatures.websiteHistorySaveInstance = history;
    }

    @Override
    public void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        history = ApplicationFeatures.websiteHistorySaveInstance;
    }


    //Loading pages
    private void HomepageLoad() {
        loadPage(External_Const.homepage);
    }

    private void HomeOfPages(String url) {


        //Get Elements
        Elements values = new Elements();
        Elements v0 = doc.select("div.tx-t3sheaderslider-pi1");
        Elements v1 = doc.select("div.csc-default");
        Elements v2 = doc.select("div.csc-frame");
        values.add(v0.get(0));
        values.addAll(v1);
        values.addAll(v2);

        String[][] content = new String[values.size()][4];
        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[0].length; j++) {
                content[i][j] = "";
            }
        }

        //Generate content
        String whole;
        for (int i = 0; i < content.length; i++) {
            Elements text = values.get(i).select("div.csc-textpic-text");
            if (text.size() > 0) {
                Elements titleElements = text.get(0).select("h1");
                if (titleElements.size() > 0) {
                    //Title
                    whole = titleElements.get(0).toString();
                    content[i][1] = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");
                }

                Elements descriptionElements = text.get(0).select("p.bodytext");
                if (descriptionElements.size() > 0) {
                    //Description
                    whole = descriptionElements.get(0).toString();
                    content[i][2] = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");

                }
            }

            Elements linkElements = values.get(i).select("a");
            if (linkElements.size() > 0) {
                whole = linkElements.get(0).toString();
                int beginIndex = whole.indexOf("href=");
                int endIndex = whole.indexOf("\"", beginIndex + "href=".length() + 1);
                if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                    content[i][3] = whole.substring(beginIndex + "href=".length() + 1, endIndex);
                }

                if (!content[i][3].substring(0, "http".length()).equals("http")) {
                    content[i][3] = "http://gym-wen.de/" + content[i][3];
                }
            }

            Elements imgElements = values.get(i).select("img");
            if (imgElements.size() > 0) {
                //Images
                whole = imgElements.get(0).toString();
                int beginIndex = whole.indexOf("src=");
                int endIndex = whole.indexOf("\"", beginIndex + "src=".length() + 1);
                if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                    content[i][0] = whole.substring(beginIndex + "src=".length() + 1, endIndex);
                }

                                /*if (whole.indexOf("src=") > 0 && whole.indexOf("width") > 0 && whole.indexOf("src=") - whole.indexOf("width") < 0) {
                                    content[i][0] = whole.substring(whole.indexOf("src=") + "src=".length() + 1, whole.indexOf("width") - 2);
                                } else if (whole.indexOf("src=") > 0 && whole.indexOf("alt") > 0 && whole.indexOf("src=") - whole.indexOf("alt") < 0) {
                                    content[i][0] = whole.substring(whole.indexOf("src=") + "src=".length() + 1, whole.indexOf("alt") - 2);
                                } else {
                                    content[i][0] = whole;
                                }*/

                if (!content[i][0].substring(0, "http".length()).equals("http")) {
                    content[i][0] = "http://gym-wen.de/" + content[i][0];
                }

            }
        }

        //Trim content
        ArrayList<String[]> trimmedContentList = new ArrayList<>();

//                trimmedContentList.add(new String[]{"http://www.gym-wen.de/fileadmin/user_upload/logo.jpg", "Gymnasium Wendelstein", "Startseite", ""});


        trimmedContentList.add(content[0]);
        for (int i = 1; i < content.length; i++) {
            if ((!content[i][0].trim().isEmpty() && !content[i][3].trim().isEmpty()) ||
                    !content[i][1].trim().isEmpty() ||
                    !content[i][2].trim().isEmpty()) {
                trimmedContentList.add(content[i]);
            }
        }

        String[][] trimmedContent = new String[trimmedContentList.size()][trimmedContentList.get(0).length];
        for (int i = 0; i < trimmedContentList.size(); i++) {
            trimmedContent[i] = trimmedContentList.get(i);
        }

        content = trimmedContent;


        overwriteContent(content);
        loadFragment(1);
    }

    private void ContentPages(final String url) {

        //Get Elements
        Elements values = doc.select("#content_wrap");

        ArrayList<String[]> contentList = new ArrayList<>();

        //Generate content
        String whole;
        for (int i = 0; i < values.size(); i++) {
            Element text = values.get(i);
            String[] littleCon = new String[4];
            Arrays.fill(littleCon, "");

            if (text != null) {
                Elements titleElements = text.select("div.csc-header");
                if (titleElements.size() > 0) {
                    //Title
                    whole = titleElements.get(0).toString();
                    littleCon[1] = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");
                }

                Elements descriptionElements = text.select("p.bodytext");
                for (int j = 0; j < descriptionElements.size(); j++) {

                    //Description
                    whole = descriptionElements.get(j).toString();
                    String des = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");
                    if (des.equals("\n")) {
                        des = "";
                    }
                    littleCon[2] += des + "\n";

                }
            }

            littleCon[3] = getLink(values.get(i));

            if (!littleCon[1].isEmpty() || !littleCon[2].isEmpty()) {
                contentList.add(littleCon);
                littleCon = new String[4];
                Arrays.fill(littleCon, "");
            }

            Elements imgElements = values.get(i).select("img");
            for (int j = 0; j < imgElements.size(); j++) {
                //Images
                whole = imgElements.get(j).toString();
                int beginIndex = whole.indexOf("src=");
                int endIndex = whole.indexOf("\"", beginIndex + "src=".length() + 1);
                if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                    littleCon[0] = whole.substring(beginIndex + "src=".length() + 1, endIndex);
                }

                if (!littleCon[0].substring(0, "http".length()).equals("http")) {
                    littleCon[0] = "http://gym-wen.de/" + littleCon[0];
                }

                if (!littleCon[0].isEmpty()) {
                    contentList.add(littleCon);
                    littleCon = new String[4];
                    Arrays.fill(littleCon, "");
                }
            }

        }

        //Create Content Array
        String[][] content = new String[contentList.size()][4];
        for (int i = 0; i < contentList.size(); i++) {
            content[i] = contentList.get(i);
        }

        //HeadImgLink
        Elements header = doc.select("div.tx-t3sheaderslider-pi1");
        Elements img = header.select("img");
        String imgLink = "";
        if (img.size() > 0) {
            //Images
            whole = img.get(0).toString();
            int beginIndex = whole.indexOf("src=");
            int endIndex = whole.indexOf("\"", beginIndex + "src=".length() + 1);
            if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                imgLink = whole.substring(beginIndex + "src=".length() + 1, endIndex);
            }

            if (!imgLink.substring(0, "http".length()).equals("http")) {
                imgLink = "http://gym-wen.de/" + imgLink;
            }

        }

        //Trim content
        ArrayList<String[]> trimmedContentList = new ArrayList<>();
        trimmedContentList.add(new String[]{imgLink, "", "", ""});
        for (String[] strings : content) {
            if (!strings[0].isEmpty() || !strings[1].isEmpty() || !strings[2].isEmpty() || !strings[3].isEmpty()) {
                trimmedContentList.add(strings);
            }
        }

        String[][] trimmedContent = new String[trimmedContentList.size()][trimmedContentList.get(0).length];
        for (int i = 0; i < trimmedContentList.size(); i++) {
            trimmedContent[i] = trimmedContentList.get(i);
        }

        content = trimmedContent;


        overwriteContent(content);
        loadFragment(2);

    }

    private void ContentPagesMixed(final String url) {


        //Get Elements
        Elements values = new Elements();
        Elements v0 = doc.select("div.tx-t3sheaderslider-pi1");
        Elements v1 = doc.select("div.csc-default");
        Elements v2 = doc.select("div.csc-frame");
        values.add(v0.get(0));
        values.addAll(v1);
        values.addAll(v2);

        ArrayList<String[]> content = new ArrayList<>();

        //Generate content
        String whole;
        for (int i = 0; i < values.size(); i++) {
            Element currentValue = values.get(i);
            if (currentValue != null) {
                Elements titleElements = currentValue.select("h1");
                for (int j = 0; j < titleElements.size(); j++) {
                    //Title
                    String link = getLink(currentValue);

                    whole = titleElements.get(j).toString();
                    content.add(new String[]{"", HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", ""), "", link});

                }

                Elements descriptionElements = currentValue.select("p.bodytext");
                if (descriptionElements.size() > 0) {
                    String[] s = new String[4];
                    Arrays.fill(s, "");
                    for (int j1 = 0; j1 < descriptionElements.size(); j1++) {
                        //Description
                        whole = descriptionElements.get(j1).toString();
                        s[2] += HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n\n", "\n");
                    }
                    content.add(s);
                }


                Elements imgElements = values.get(i).select("img");
                String link = getLink(currentValue);
                for (int j = 0; j < imgElements.size(); j++) {
                    //Images
                    whole = imgElements.get(j).toString();
                    int beginIndex = whole.indexOf("src=");
                    int endIndex = whole.indexOf("\"", beginIndex + "src=".length() + 1);
                    if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                        content.add(new String[]{whole.substring(beginIndex + "src=".length() + 1, endIndex), "", "", link});
                    }

                    if (!content.get(content.size() - 1)[0].substring(0, "http".length()).equals("http")) {
                        content.get(content.size() - 1)[0] = "http://gym-wen.de/" + content.get(content.size() - 1)[0];
                    }

                }
            }

        }
        String[][] contentArray = new String[content.size()][];
        for (int i = 0; i < content.size(); i++) {
            contentArray[i] = content.get(i);
        }

        overwriteContent(contentArray);

        loadFragment(3);
    }

    @NonNull
    private static String getLink(@NonNull Element e) {
        String link = "";
        Elements linkElements = e.select("a");
        if (linkElements.size() > 0) {
            String whole = linkElements.get(0).toString();
            int beginIndex = whole.indexOf("href=");
            int endIndex = whole.indexOf("\"", beginIndex + "href=".length() + 1);
            if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                link = whole.substring(beginIndex + "href=".length() + 1, endIndex);
            }

            if (!link.substring(0, "http".length()).equals("http")) {
                link = "http://gym-wen.de/" + link;
            }
        }
        return link;
    }

    private void setWebsiteTitle(@NonNull Document doc) {
        String whole = doc.select("head").select("title").toString();
        final String title = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");

        runOnUiThread(() -> Objects.requireNonNull(getSupportActionBar()).setTitle(title));
    }

    private void loadFragment(final int pageCode) {
        try {
            WebsiteActivityFragment f = new WebsiteActivityFragment(con, pageCode);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.website_host, f).commit();
        } catch (Exception ignore) {
//            runOnUiThread(() -> Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show());
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
        }
    }

    private static void overwriteContent(String[][] c) {
        con = c;
    }

    private void openInTabIntent(@NonNull String url) {
        if (url.charAt(url.length() - 1) == '/')
            url = url.substring(0, url.length() - 1);
        tabIntent(url);
    }
}