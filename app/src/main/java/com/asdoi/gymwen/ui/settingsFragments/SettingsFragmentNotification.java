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

package com.asdoi.gymwen.ui.settingsFragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;
import com.asdoi.gymwen.util.PreferenceUtil;

public class SettingsFragmentNotification extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_notification, rootKey);

        ((SettingsActivity) getActivity()).loadedFragments++;

        setNotif();

        Preference myPref = findPreference("showNotification");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setNotif();
            return true;
        });

        myPref = findPreference("alarm");
        myPref.setOnPreferenceClickListener((Preference p) -> {
            PreferenceUtil.setAlarmTime(0);
            ((ActivityFeatures) getActivity()).createTimePicker();
            return true;
        });

        setSummary();
        myPref = findPreference("showSummaryNotification");
        myPref.setOnPreferenceClickListener((Preference p) -> {
            setSummary();
            return true;
        });
    }

    private void setNotif() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showNotification", true);
        findPreference("alwaysNotification").setVisible(showNotif);
        findPreference("alarm").setVisible(showNotif);
        findPreference("two_notifs").setVisible(showNotif);
        findPreference("main_notif_for_all").setVisible(showNotif);
        findPreference("showSummaryNotification").setVisible(showNotif);
        findPreference("summary_notif_as_usual").setVisible(showNotif);
    }

    private void setSummary() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("showSummaryNotification", true);
        findPreference("summary_notif_as_usual").setVisible(showNotif);
    }
}
