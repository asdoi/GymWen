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

package com.ulan.timetable.fragments;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.ulan.timetable.receivers.DailyReceiver;
import com.ulan.timetable.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.timetable_settings, rootKey);

        tintIcons(getPreferenceScreen(), ApplicationFeatures.getTextColorPrimary(requireContext()));

        Preference allPrefs = findPreference("allprefs");
        Objects.requireNonNull(allPrefs).setOnPreferenceClickListener((Preference p) -> {
            startActivity(new Intent(requireActivity(), com.asdoi.gymwen.ui.activities.SettingsActivity.class));
            requireActivity().finish();
            return true;
        });

        setNotif();

        Preference myPref = findPreference("timetableNotif");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            setNotif();
            return true;
        });

        myPref = findPreference("timetable_alarm");
        Preference finalMyPref = myPref;
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            int[] oldTimes = PreferenceUtil.getTimeTableAlarmTime();
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireActivity(),
                    (view, hourOfDay, minute) -> {
                        PreferenceUtil.setTimeTableAlarmTime(hourOfDay, minute, 0);
                        ApplicationFeatures.setRepeatingAlarm(requireContext(), DailyReceiver.class, hourOfDay, minute, 0, DailyReceiver.DailyReceiverID, AlarmManager.INTERVAL_DAY);
                        finalMyPref.setSummary(hourOfDay + ":" + minute);
                    }, oldTimes[0], oldTimes[1], true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
            return true;
        });
        int[] oldTimes = PreferenceUtil.getTimeTableAlarmTime();
        myPref.setSummary(oldTimes[0] + ":" + oldTimes[1]);

        setTurnOff();
        myPref = findPreference("automatic_do_not_disturb");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            PreferenceUtil.setDoNotDisturb(requireActivity(), false);
            setTurnOff();
            return true;
        });

        setCourses();
        myPref = findPreference("timetable_subs");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference p) -> {
            setCourses();
            return true;
        });

        showPreselectionElements();
        myPref = findPreference("is_preselection");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener(p -> {
            showPreselectionElements();
            return true;
        });

        myPref = findPreference("preselection_elements");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener(p -> {
            ArrayList<String> preselectedValues = new ArrayList<>(Arrays.asList(requireContext().getResources().getStringArray(R.array.preselected_subjects_values)));

            String[] preselected = PreferenceUtil.getPreselectionElements(requireContext());
            List<Integer> preselectedIndices = new ArrayList<>();

            for (int i = 0; i < preselected.length; i++) {
                preselectedIndices.add(preselectedValues.indexOf(preselected[i]));
            }


            new MaterialDialog.Builder(requireContext())
                    .title(R.string.set_preselection_elements)
                    .items(R.array.preselected_subjects)
                    .itemsCallbackMultiChoice(preselectedIndices.toArray(new Integer[]{}), (dialog, which, text) -> {
                        List<String> selection = new ArrayList<>();
                        for (int i = 0; i < which.length; i++) {
                            selection.add(preselectedValues.get(which[i]));
                        }
                        PreferenceUtil.setPreselectionElements(requireContext(), selection.toArray(new String[]{}));
                        return true;
                    })
                    .positiveText(R.string.ok)
                    .onPositive(((dialog, which) -> dialog.dismiss()))
                    .negativeText(R.string.cancel)
                    .onNegative((dialog, action) -> dialog.dismiss())
                    .neutralText(R.string.de_select_all)
                    .onNeutral((dialog, action) -> {
                        Integer[] selection = dialog.getSelectedIndices();
                        if (Objects.requireNonNull(selection).length == 0) {
                            Integer[] select = new Integer[preselectedValues.size()];
                            for (int i = 0; i < select.length; i++) {
                                select[i] = i;
                            }
                            dialog.setSelectedIndices(select);
                        } else {
                            dialog.setSelectedIndices(new Integer[]{});
                        }
                    })
                    .autoDismiss(false)
                    .show();
            return true;
        });
    }

    private void setNotif() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("timetableNotif", true);
        findPreference("alwaysNotification").setVisible(show);
        findPreference("timetable_alarm").setVisible(show);
    }

    private void setTurnOff() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("automatic_do_not_disturb", true);
        findPreference("do_not_disturb_turn_off").setVisible(show);
    }

    private void setCourses() {
        boolean show = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("timetable_subs", true);
        findPreference("courses").setVisible(show);
    }

    private void showPreselectionElements() {
        boolean show = PreferenceUtil.isPreselectionList(requireContext());
        findPreference("preselection_elements").setVisible(show);
    }

    private static void tintIcons(Preference preference, int color) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup group = ((PreferenceGroup) preference);
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                tintIcons(group.getPreference(i), color);
            }
        } else {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                DrawableCompat.setTint(icon, color);
            }
        }
    }
}
