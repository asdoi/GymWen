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

package com.ulan.timetable.appwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SubstitutionTimeTableActivity;
import com.ulan.timetable.appwidget.Dao.AppWidgetDao;
import com.ulan.timetable.utils.PreferenceUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * From https://github.com/SubhamTyagi/TimeTable
 */
public class DayAppWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_RESTORE = "com.ulan.timetable" + ".ACTION_RESTORE";
    private static final String ACTION_YESTERDAY = "com.ulan.timetable" + ".ACTION_YESTERDAY";
    private static final String ACTION_TOMORROW = "com.ulan.timetable" + ".ACTION_TOMORROW";
    private static final String ACTION_NEW_DAY = "com.ulan.timetable" + ".ACTION_NEW_DAY";

    private static final int ONE_DAY_MILLIS = 86400000;

    @Override
    public void onEnabled(@NonNull Context context) {
        registerNewDayBroadcast(context);
    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        new Thread(() -> {
            ApplicationFeatures.downloadSubstitutionplanDocs(false, true);

            if (isAlarmManagerNotSet(context)) {
                registerNewDayBroadcast(context);
            }

            for (int appWidgetId : appWidgetIds) {
                Intent intent = new Intent(context, DayAppWidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

                long currentTimeMillis = System.currentTimeMillis();
                AppWidgetDao.saveAppWidgetCurrentTime(appWidgetId, currentTimeMillis, context);

                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.timetable_day_appwidget);
                rv.setRemoteAdapter(R.id.lv_day_appwidget, intent);
                rv.setEmptyView(R.id.lv_day_appwidget, R.id.empty_view);
                rv.setTextViewText(R.id.tv_date, getDateText(currentTimeMillis, context));
                rv.setInt(R.id.fl_root, "setBackgroundColor", AppWidgetDao.getAppWidgetBackgroundColor(appWidgetId, Color.TRANSPARENT, context));

                rv.setOnClickPendingIntent(R.id.imgBtn_restore, makePendingIntent(context, appWidgetId, ACTION_RESTORE));
                rv.setOnClickPendingIntent(R.id.imgBtn_yesterday, makePendingIntent(context, appWidgetId, ACTION_YESTERDAY));
                rv.setOnClickPendingIntent(R.id.imgBtn_tomorrow, makePendingIntent(context, appWidgetId, ACTION_TOMORROW));

                Intent listviewClickIntent = new Intent(context, SubstitutionTimeTableActivity.class);
                listviewClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                listviewClickIntent.setAction(Intent.ACTION_VIEW);
                PendingIntent listviewPendingIntent = PendingIntent.getActivity(context, appWidgetId, listviewClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setPendingIntentTemplate(R.id.lv_day_appwidget, listviewPendingIntent);

                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_day_appwidget);
                appWidgetManager.updateAppWidget(appWidgetId, rv);
            }
        }).start();
    }

    private static String getDateText(long currentTimeMillis, @NonNull Context context) {
        String date = new SimpleDateFormat("E  d.M.", Locale.getDefault()).format(currentTimeMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);

        if (PreferenceUtil.isTwoWeeksEnabled(context)) {
            date += " (";
            if (PreferenceUtil.isEvenWeek(context, calendar))
                date += context.getString(R.string.even_week);
            else
                date += context.getString(R.string.odd_week);
            date += ")";
        }

        return date;
    }

    @Override
    public void onDeleted(Context context, @NonNull int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            AppWidgetDao.deleteAppWidget(appWidgetId, context);
        }
    }

    @Override
    public void onDisabled(@NonNull Context context) {
        unregisterNewDayBroadcast(context);
        AppWidgetDao.clear(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        onUpdate(context, appWidgetManager, new int[]{appWidgetId});
    }

    private PendingIntent makePendingIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static void updateAppWidgetConfig(@NonNull AppWidgetManager appWidgetManager, int appWidgetId, int backgroundColor, int timeStyle, @NonNull Context context) {
        AppWidgetDao.saveAppWidgetConfig(appWidgetId, backgroundColor, timeStyle, context);

        Intent intent = new Intent(context, DayAppWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_day_appwidget);
        views.setRemoteAdapter(R.id.lv_day_appwidget, intent);
        views.setEmptyView(R.id.lv_day_appwidget, R.id.empty_view);
        views.setInt(R.id.fl_root, "setBackgroundColor", backgroundColor);
        views.setTextViewText(R.id.tv_date, getDateText(System.currentTimeMillis(), context));
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {

        String action = intent.getAction();

        if (ACTION_NEW_DAY.equals(action)) {
            notifyUpdate(context);
            return;
        }

        if (ACTION_RESTORE.equals(action) || ACTION_YESTERDAY.equals(action) || ACTION_TOMORROW.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.timetable_day_appwidget);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            long currentTime;
            long newTime;

            if (ACTION_RESTORE.equals(action)) {
                rv.setViewVisibility(R.id.imgBtn_restore, View.INVISIBLE);
                newTime = System.currentTimeMillis();
            } else if (ACTION_YESTERDAY.equals(action)) {
                rv.setViewVisibility(R.id.imgBtn_restore, View.VISIBLE);
                currentTime = AppWidgetDao.getAppWidgetCurrentTime(appWidgetId, System.currentTimeMillis(), context);
                newTime = currentTime - ONE_DAY_MILLIS;
            } else { //ACTION_TOMORROW
                rv.setViewVisibility(R.id.imgBtn_restore, View.VISIBLE);
                currentTime = AppWidgetDao.getAppWidgetCurrentTime(appWidgetId, System.currentTimeMillis(), context);
                newTime = currentTime + ONE_DAY_MILLIS;
            }
            if (("" + newTime).substring(0, 7).equalsIgnoreCase(("" + System.currentTimeMillis()).substring(0, 7))) {
                rv.setViewVisibility(R.id.imgBtn_restore, View.INVISIBLE);
            }

            AppWidgetDao.saveAppWidgetCurrentTime(appWidgetId, newTime, context);
            rv.setTextViewText(R.id.tv_date, getDateText(newTime, context));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_day_appwidget);
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, rv);
        }

        super.onReceive(context, intent);
    }

    public void notifyUpdate(@NonNull Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                DayAppWidgetProvider.class));
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void registerNewDayBroadcast(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar midnight = Calendar.getInstance(Locale.getDefault());
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 1); //
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_YEAR, 1);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(), ONE_DAY_MILLIS, pendingIntent);
    }

    private void unregisterNewDayBroadcast(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private boolean isAlarmManagerNotSet(Context context) {
        Intent intent = new Intent(context, DayAppWidgetProvider.class);
        intent.setAction(ACTION_NEW_DAY);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) == null;
    }

}