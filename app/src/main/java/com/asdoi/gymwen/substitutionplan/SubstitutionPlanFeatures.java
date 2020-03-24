package com.asdoi.gymwen.substitutionplan;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.External_Const;
import com.asdoi.gymwen.util.PreferenceUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

/**
 * An abstract class which organizes the substitution plan object for easier access, not necessary
 */
public abstract class SubstitutionPlanFeatures {
    public static String strUserId = "";
    public static String strPasword = "";

    //ChoiceActivity -> Step 5
    public static final String[][] choiceCourseNames = new String[][]{{ApplicationFeatures.getContext().getString(R.string.math), ApplicationFeatures.getContext().getString(R.string.mathShort)},
            {ApplicationFeatures.getContext().getString(R.string.german), ApplicationFeatures.getContext().getString(R.string.germanShort)},
            {ApplicationFeatures.getContext().getString(R.string.history), ApplicationFeatures.getContext().getString(R.string.historyShort)},
            {ApplicationFeatures.getContext().getString(R.string.social_education), ApplicationFeatures.getContext().getString(R.string.social_educationShort)},
            {ApplicationFeatures.getContext().getString(R.string.PE), ApplicationFeatures.getContext().getString(R.string.PEShort)},
            {ApplicationFeatures.getContext().getString(R.string.Religious_education), ApplicationFeatures.getContext().getString(R.string.Religious_educationShort)},
            {ApplicationFeatures.getContext().getString(R.string.english), ApplicationFeatures.getContext().getString(R.string.englishShort)},
            {ApplicationFeatures.getContext().getString(R.string.france), ApplicationFeatures.getContext().getString(R.string.franceShort)},
            {ApplicationFeatures.getContext().getString(R.string.latin), ApplicationFeatures.getContext().getString(R.string.latinShort)},
            {ApplicationFeatures.getContext().getString(R.string.spanish), ApplicationFeatures.getContext().getString(R.string.spanishShort)},
            {ApplicationFeatures.getContext().getString(R.string.biology), ApplicationFeatures.getContext().getString(R.string.biologyShort)},
            {ApplicationFeatures.getContext().getString(R.string.chemistry), ApplicationFeatures.getContext().getString(R.string.chemistryShort)},
            {ApplicationFeatures.getContext().getString(R.string.physics), ApplicationFeatures.getContext().getString(R.string.physicsShort)},
            {ApplicationFeatures.getContext().getString(R.string.programming), ApplicationFeatures.getContext().getString(R.string.programmingShort)},
            {ApplicationFeatures.getContext().getString(R.string.geography), ApplicationFeatures.getContext().getString(R.string.geographyShort)},
            {ApplicationFeatures.getContext().getString(R.string.finance), ApplicationFeatures.getContext().getString(R.string.financeShort)},
            {ApplicationFeatures.getContext().getString(R.string.art), ApplicationFeatures.getContext().getString(R.string.artShort)},
            {ApplicationFeatures.getContext().getString(R.string.music), ApplicationFeatures.getContext().getString(R.string.musicShort)},
            {ApplicationFeatures.getContext().getString(R.string.W_Seminar), ApplicationFeatures.getContext().getString(R.string.W_SeminarShort)},
            {ApplicationFeatures.getContext().getString(R.string.P_Seminar), ApplicationFeatures.getContext().getString(R.string.P_SeminarShort)},
            {ApplicationFeatures.getContext().getString(R.string.profile_subject), ApplicationFeatures.getContext().getString(R.string.profile_subjectShort)},
            {ApplicationFeatures.getContext().getString(R.string.additum), ApplicationFeatures.getContext().getString(R.string.additumShort)}
    };

    @NonNull
    private static SubstitutionPlan substitutionPlan = new SubstitutionPlan();

    public static void setContext(Context context) {
        substitutionPlan.setContext(context);
    }

    public static void setup(boolean hours, String... courses) {
        if (substitutionPlan == null) {
            substitutionPlan = new SubstitutionPlan(hours, courses);
        } else {
            substitutionPlan.reCreate(hours, courses);
        }
    }

    @NonNull
    public static SubstitutionPlan createTempSubstitutionplan(boolean hours, String[] courses) {
        if (!isUninit() && PreferenceUtil.isOfflineMode())
            reloadDocs();
        SubstitutionPlan temp = new SubstitutionPlan(hours, courses);
        temp.setDocs(substitutionPlan.getDoc(true), substitutionPlan.getDoc(false));
        return temp;
    }

    public static void signin(String username, String password) {
        strUserId = username;
        strPasword = password;
    }

    public static boolean isUninit() {
        return substitutionPlan == null;
    }

    @NonNull
    public static SubstitutionList getToday() {
        return substitutionPlan.getToday();
    }

    @NonNull
    public static SubstitutionList getTomorrow() {
        return substitutionPlan.getTomorrow();
    }

    @NonNull
    public static SubstitutionList getTodaySummarized() {
        return substitutionPlan.getTodaySummarized();
    }

    @NonNull
    public static SubstitutionList getTomorrowSummarized() {
        return substitutionPlan.getTomorrowSummarized();
    }

    @Nullable
    public static SubstitutionList getTodayAll() {
        return substitutionPlan.getTodayAll();
    }

    @Nullable
    public static SubstitutionList getTomorrowAll() {
        return substitutionPlan.getTomorrowAll();
    }

    @NonNull
    public static SubstitutionList getTodayAllSummarized() {
        return substitutionPlan.getTodayAllSummarized();
    }

    @NonNull
    public static SubstitutionList getTomorrowAllSummarized() {
        return substitutionPlan.getTomorrowAllSummarized();
    }

    @NonNull
    public static String getTodayTitleString() {
        return substitutionPlan.getTodayTitleString();
    }

    @NonNull
    public static String getTomorrowTitleString() {
        return substitutionPlan.getTomorrowTitleString();
    }

    @NonNull
    public static SubstitutionTitle getTodayTitle() {
        return substitutionPlan.getTodayTitle();
    }

    @NonNull
    public static SubstitutionTitle getTomorrowTitle() {
        return substitutionPlan.getTomorrowTitle();
    }

    public static int getTodayTitleCode() {
        return substitutionPlan.getTodayTitleCode();
    }

    public static int getTomorrowTitleCode() {
        return substitutionPlan.getTomorrowTitleCode();
    }

    public static boolean getSenior() {
        return substitutionPlan.getSenior();
    }

    public static void setTodayDoc(Document value) {
        substitutionPlan.setTodayDoc(value);
    }

    public static void setTomorrowDoc(Document value) {
        substitutionPlan.setTomorrowDoc(value);
    }

    public static void setDocs(Document today, Document tomorrow) {
        substitutionPlan.setDocs(today, tomorrow);
    }

    public static Document getTodayDoc() {
        return substitutionPlan.getTodayDoc();
    }

    public static Document getTomorrowDoc() {
        return substitutionPlan.getTomorrowDoc();
    }

    @NonNull
    public static Document[] getDocs() {
        return new Document[]{getTodayDoc(), getTomorrowDoc()};
    }

    public static boolean areDocsDownloaded() {
        return substitutionPlan.areDocsDownloaded();
    }

    /**
     * saves the docs to SharedPreferences for offline use
     */
    public static void saveDocs() {
        if (!areDocsDownloaded())
            return;

        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).edit();
        String doc = substitutionPlan.getDoc(true).toString();
        prefsEditor.putString("doc1", doc);

        String doc2 = substitutionPlan.getDoc(false).toString();
        prefsEditor.putString("doc2", doc2);

        prefsEditor.commit();
    }

    /**
     * @return a boolean if the reloading of the docs was successful, also reloads the docs from sharedPreferences
     * @see #saveDocs
     */
    public static boolean reloadDocs() {
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
            String docString1 = sharedPref.getString("doc1", "");
            Document doc1 = Jsoup.parse(docString1);

            String docString2 = sharedPref.getString("doc2", "");
            Document doc2 = Jsoup.parse(docString2);

            if (!docString1.trim().isEmpty() && !docString2.trim().isEmpty()) {
                setDocs(doc1, doc2);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable
    public static ArrayList<String> getNames() {
        return substitutionPlan.getCourses();
    }

    /**
     * @param query The subject of the substitution plan
     * @return boolean if the subject equals the specific value for the hour is omitted
     */
    public static boolean isNothing(String query) {
        for (String s : External_Const.nothing) {
            if (s.equalsIgnoreCase(query))
                return true;
        }
        return false;
    }

    @NonNull
    public static String[] getNothing() {
        return External_Const.nothing;
    }

    public static boolean isTitleCodeInPast(int titleCode) {
        return SubstitutionPlan.isTitleCodeInPast(titleCode);
    }

    public static boolean isTitleCodeToday(int titleCode) {
        return SubstitutionPlan.isTitleCodeToday(titleCode);
    }
}
