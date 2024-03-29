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

package com.ulan.timetable.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.substitutionplan.SubstitutionTitle;
import com.asdoi.gymwen.substitutionplan.WeekChar;
import com.ulan.timetable.activities.SettingsActivity;
import com.ulan.timetable.receivers.DoNotDisturbReceiversKt;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.asdoi.gymwen.util.PreferenceUtil.getBooleanSettings;

public class PreferenceUtil {

    public static boolean isTimeTableSubstitution() {
        return ApplicationFeatures.getBooleanSettings("timetable_subs", true);
    }

    public static void setTimeTableSubstitution(@NonNull Context context, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("timetable_subs", value);
        editor.commit();
    }

    public static boolean isTimeTableNotification() {
        return ApplicationFeatures.getBooleanSettings("timetableNotif", true);
    }

    public static void setTimeTableAlarmTime(@NonNull int... times) {
        if (times.length != 3) {
            if (times.length > 0 && times[0] == 0) {
                setTimeTableAlarm(ApplicationFeatures.getContext(), false);
            } else {
                System.out.println("wrong parameters");
            }
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        setTimeTableAlarm(ApplicationFeatures.getContext(), true);
        editor.putInt("timetable_Alarm_hour", times[0]);
        editor.putInt("timetable_Alarm_minute", times[1]);
        editor.putInt("timetable_Alarm_second", times[2]);
        editor.commit();
    }

    @NonNull
    public static int[] getTimeTableAlarmTime() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        return new int[]{sharedPref.getInt("timetable_Alarm_hour", 7), sharedPref.getInt("timetable_Alarm_minute", 55), sharedPref.getInt("timetable_Alarm_second", 0)};
    }

    private static void setTimeTableAlarm(@NonNull Context context, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("timetable_alarm", value);
        editor.commit();
    }


    public static boolean isTimeTableAlarmOn(@NonNull Context context) {
        return getBooleanSettings("timetable_alarm", false, context);
    }

    public static boolean doNotDisturbDontAskAgain() {
        return ApplicationFeatures.getBooleanSettings("do_not_disturb_dont_ask", false);
    }

    public static void setDoNotDisturbDontAskAgain(@NonNull Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("do_not_disturb_dont_ask", value).apply();
    }

    public static boolean isAutomaticDoNotDisturb() {
        return ApplicationFeatures.getBooleanSettings("automatic_do_not_disturb", true);
    }

    public static void setDoNotDisturb(@NonNull Activity activity, boolean dontAskAgain) {
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if the notification policy access has been granted for the app.
            if (!Objects.requireNonNull(notificationManager).isNotificationPolicyAccessGranted() && !dontAskAgain) {
                Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.ic_do_not_disturb_on_black_24dp);
                try {
                    Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(drawable));
                    DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(activity));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new MaterialDialog.Builder(activity)
                        .title(R.string.permission_required)
                        .content(R.string.do_not_disturb_permission_desc)
                        .onPositive((dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            activity.startActivity(intent);
                        })
                        .positiveText(R.string.permission_ok_button)
                        .negativeText(R.string.permission_cancel_button)
                        .onNegative(((dialog, which) -> dialog.dismiss()))
                        .icon(Objects.requireNonNull(drawable))
                        .onNeutral(((dialog, which) -> setDoNotDisturbDontAskAgain(activity, true)))
                        .neutralText(R.string.dont_show_again)
                        .show();
            }
        }
        DoNotDisturbReceiversKt.setDoNotDisturbReceivers(activity, false);
    }

    public static boolean isDoNotDisturbTurnOff(Context context) {
        return getBooleanSettings("do_not_disturb_turn_off", false, context);
    }

    public static boolean isSevenDays() {
        return ApplicationFeatures.getBooleanSettings(SettingsActivity.KEY_SEVEN_DAYS_SETTING, false);
    }

    public static boolean isSummaryLibrary1() {
        return ApplicationFeatures.getBooleanSettings("summary_lib", true);
    }

    public static void setSummaryLibrary(@NonNull Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("summary_lib", value).commit();
    }

    public static boolean showTimes(@NonNull Context context) {
        return getBooleanSettings("show_times", false, context);
    }

    //Even, odd weeks
    public static boolean isTwoWeeksEnabled(@NonNull Context context) {
        return getBooleanSettings("two_weeks", false, context);
    }

    public static void setTermStart(@NonNull Context context, int year, int month, int day) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt("term_year", year);
        editor.putInt("term_month", month);
        editor.putInt("term_day", day);
        editor.commit();
    }

    public static void setTermStart(@Nullable SubstitutionTitle today, @NonNull Context context) {
        if (today != null) {
            try {
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTime(today.getDate().toDate());
                if (today.getWeek() != WeekChar.WEEK_A)
                    todayCal.set(Calendar.WEEK_OF_YEAR, todayCal.get(Calendar.WEEK_OF_YEAR) - 1);

                setTermStart(context, todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH), todayCal.get(Calendar.DAY_OF_MONTH));
            } catch (Exception ignore) {
            }
        }
    }

    @NonNull
    public static Calendar getTermStart(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar calendar = Calendar.getInstance();
        int year = sharedPref.getInt("term_year", -999999999);

        //If start has not been set
        if (year == -999999999) {
            setTermStart(context, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            return getTermStart(context);
        }

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, sharedPref.getInt("term_month", 0));
        calendar.set(Calendar.DAY_OF_MONTH, sharedPref.getInt("term_day", 0));

        return calendar;
    }

    public static boolean isEvenWeek(@NonNull Context context, @NonNull Calendar now) {
        if (isTwoWeeksEnabled(context)) {
            return WeekUtils.isEvenWeek(getTermStart(context), now);
        } else
            return true;
    }

    public static boolean isIntelligentAutoFill(Context context) {
        return getBooleanSettings("auto_fill", true, context);
    }

    public static boolean isPreselectionList(Context context) {
        return getBooleanSettings("is_preselection", true, context);
    }

    public static void setPreselectionElements(Context context, String[] value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringSet = new HashSet<>();
        Collections.addAll(stringSet, value);
        sharedPrefs.edit().putStringSet("preselection_elements", stringSet).apply();
    }

    public static String[] getPreselectionElements(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> preselection = sharedPrefs.getStringSet("preselection_elements", null);
        if (preselection == null)
            return context.getResources().getStringArray(R.array.preselected_subjects_values);
        else
            return preselection.toArray(new String[]{});
    }
}
