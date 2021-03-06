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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.substitutionplan.SubstitutionEntry;
import com.asdoi.gymwen.substitutionplan.SubstitutionList;
import com.ulan.timetable.databaseUtils.DbHelper;
import com.ulan.timetable.fragments.WeekdayFragment;
import com.ulan.timetable.model.Week;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

public class WeekUtils {
    @NonNull
    public static ArrayList<Week> compareSubstitutionAndWeeks(@NonNull Context context, @NonNull ArrayList<Week> weeks, SubstitutionList entries, boolean senior, @NonNull DbHelper dbHelper) {
        boolean empty = weeks.isEmpty();
        if (entries != null) {
            if (!entries.getTitle().showDay())
                return weeks;

            for (int i = 0; i < entries.size(); i++) {
                SubstitutionEntry entry = entries.getEntry(i);
                int color = ContextCompat.getColor(context, entry.isNothing() ? R.color.notification_icon_background_omitted : R.color.notification_icon_background_substitution);
                String subject = entry.getSubject();
                if (subject.trim().isEmpty() && senior) {
                    subject = entry.getCourse();
                }

                String teacher = entry.getTeacher();
                String room = entry.getRoom();
                String begin = entry.getStartTime().toString("HH:mm");
                if (begin.length() < 5)
                    begin = "0" + begin;

                String end = entry.getEndTime().toString("HH:mm");
                if (end.length() < 5)
                    end = "0" + end;

                for (Week w : getAllWeeks(dbHelper)) {
                    if (w.getSubject().equalsIgnoreCase(subject))
                        color = w.getColor();
                }

                Week weekEntry = new Week(subject, teacher, room, begin, end, color, false);
                weekEntry.setMoreInfos(entry.getMoreInformation());

                if (empty) {
                    weeks.add(weekEntry);
                } else {
                    for (int j = 0; j < weeks.size(); j++) {
                        Week week = weeks.get(j);
                        boolean beginIsBeforeFrom = begin.compareToIgnoreCase(week.getFromTime()) < 0;
                        boolean beginIsFrom = begin.equalsIgnoreCase(week.getFromTime());
                        boolean beginIsBeforeTo = begin.compareToIgnoreCase(week.getToTime()) < 0;

                        boolean endIsAfterFrom = end.compareToIgnoreCase(week.getFromTime()) > 0;
                        boolean endIsTo = end.equalsIgnoreCase(week.getToTime());
                        boolean endIsAfterTo = end.compareToIgnoreCase(week.getToTime()) > 0;

                        if (beginIsBeforeFrom) {
                            if (endIsAfterFrom) {
                                if (endIsAfterTo) {
                                    //Check next, remove
                                    checkNextAndRemove(weeks, begin, end, weekEntry, j);
                                } else {
                                    if (endIsTo) {
                                        //replace
                                        if (subject.trim().isEmpty())
                                            weekEntry.setSubject(weeks.get(j).getSubject());
                                        weeks.remove(j);
                                        weeks.add(j, weekEntry);
                                    } else {
                                        //split
                                        split(weeks, begin, end, weekEntry, j, week, beginIsFrom);
                                    }
                                }
                            } else {
                                //add before
                                weeks.add(j, weekEntry);
                            }
                        } else {
                            if (beginIsFrom) {
                                if (endIsAfterTo) {
                                    //Check next, remove
                                    checkNextAndRemove(weeks, begin, end, weekEntry, j);
                                } else {
                                    if (endIsTo) {
                                        //replace
                                        if (subject.trim().isEmpty())
                                            weekEntry.setSubject(weeks.get(j).getSubject());
                                        weeks.remove(j);
                                        weeks.add(j, weekEntry);
                                    } else {
                                        //split
                                        split(weeks, begin, end, weekEntry, j, week, true);
                                    }
                                }
                            } else {
                                if (beginIsBeforeTo) {
                                    if (endIsAfterTo) {
                                        //check next, split
                                        checkNextAndRemove(weeks, begin, end, weekEntry, j);

                                        week.setToTime(begin);
//                                        week.setEditable(false);
                                        weeks.set(j, week);
                                    } else {
                                        if (endIsTo) {
                                            //split
                                            split(weeks, begin, end, weekEntry, j, week, false);
                                        } else {
                                            //split3
                                            if (subject.trim().isEmpty())
                                                weekEntry.setSubject(weeks.get(j).getSubject());

                                            Week week2 = new Week(week.getSubject(), week.getTeacher(), week.getRoom(), week.getFromTime(), week.getToTime(), week.getColor(), false);
                                            week.setToTime(begin);
                                            week2.setFromTime(end);

                                            weeks.set(j, week);
                                            weeks.add(j + 1, weekEntry);
                                            weeks.add(j + 2, week2);
                                        }
                                    }
                                } else {
                                    //check next
                                    if (j >= weeks.size() - 1) {
                                        weeks.add(weekEntry);
                                        break;
                                    }
                                    continue;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        return sortWeekList(weeks);
    }

    private static void split(@NonNull ArrayList<Week> weeks, String begin, String end, @NonNull Week weekEntry, int j, @NonNull Week week, boolean beginIsFrom) {
        if (weekEntry.getSubject().trim().isEmpty())
            weekEntry.setSubject(weeks.get(j).getSubject());

        if (beginIsFrom) {
            week.setFromTime(end);
//            week.setEditable(false);
            weeks.set(j, weekEntry);
            weeks.add(j + 1, week);
        } else {
            week.setToTime(begin);
//            week.setEditable(false);
            weeks.set(j, week);
            weeks.add(j + 1, weekEntry);
        }
    }

    private static void checkNextAndRemove(@NonNull ArrayList<Week> weeks, String begin, @NonNull String end, @NonNull Week weekEntry, int j) {
        for (int j2 = j; j2 < weeks.size(); j2++) {
            Week week = weeks.get(j2);
            boolean endIsAfterFrom = end.compareToIgnoreCase(week.getFromTime()) > 0;
            boolean endIsBeforeTo = end.compareToIgnoreCase(week.getFromTime()) < 0;

            if (j2 >= weeks.size() - 1) {
                weeks.add(weekEntry);
                break;
            }

            if (endIsAfterFrom) {
                if (endIsBeforeTo) {
                    split(weeks, begin, end, weekEntry, j, week, true);
                } else {
                    //check next, remove
                    weeks.remove(j2);
                    if (j2 >= weeks.size() - 1) {
                        weeks.add(weekEntry);
                        break;
                    }
                    continue;
                }
            } else {
                //add before
                weeks.add(j2, weekEntry);
            }
            break;
        }
    }

    @Nullable
    public static Week getNextWeek(@NonNull ArrayList<Week> weeks) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
        String hour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        if (hour.length() < 2)
            hour = "0" + hour;
        String minutes = "" + calendar.get(Calendar.MINUTE);
        if (minutes.length() < 2)
            minutes = "0" + minutes;
        String now = hour + ":" + minutes;

        for (int i = 0; i < weeks.size(); i++) {
            Week week = weeks.get(i);
            if ((now.compareToIgnoreCase(week.getFromTime()) >= 0 && now.compareToIgnoreCase(week.getToTime()) <= 0) || now.compareToIgnoreCase(week.getToTime()) <= 0) {
                return week;
            }
        }
        return null;
    }

    @NonNull
    public static ArrayList<Week> sortWeekList(@NonNull ArrayList<Week> weeks) {
        Collections.sort(weeks, (o1, o2) -> o1.getFromTime().compareToIgnoreCase(o2.getFromTime()));
        return weeks;
    }

    @NonNull
    public static ArrayList<Week> getAllWeeks(@NonNull DbHelper dbHelper) {
        String[] keys = new String[]{WeekdayFragment.KEY_MONDAY_FRAGMENT,
                WeekdayFragment.KEY_TUESDAY_FRAGMENT,
                WeekdayFragment.KEY_WEDNESDAY_FRAGMENT,
                WeekdayFragment.KEY_THURSDAY_FRAGMENT,
                WeekdayFragment.KEY_FRIDAY_FRAGMENT,
                WeekdayFragment.KEY_SATURDAY_FRAGMENT,
                WeekdayFragment.KEY_SUNDAY_FRAGMENT};

        Calendar calendar = Calendar.getInstance();
        ArrayList<Week> customWeeks = getWeeks(dbHelper, keys, calendar);

        calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR) - 1);
        customWeeks.addAll(getWeeks(dbHelper, keys, calendar));
        return removeDuplicates(customWeeks);
    }

    @NonNull
    private static ArrayList<Week> getWeeks(@NonNull DbHelper dbHelper, @NonNull String[] keys, Calendar calendar) {
        ArrayList<Week> weeks = new ArrayList<>();
        for (String key : keys) {
            weeks.addAll(dbHelper.getWeek(key, calendar));
        }
        return weeks;
    }

    @NotNull
    public static ArrayList<Week> getPreselection(@NonNull AppCompatActivity activity) {
        DbHelper dbHelper = new DbHelper(activity);
        ArrayList<Week> customWeeks = getAllWeeks(dbHelper);

        ArrayList<String> subjects = new ArrayList<>();
        for (Week w : customWeeks) {
            subjects.add(w.getSubject().toUpperCase());
        }

        ArrayList<String> preselectedValues = new ArrayList<>(Arrays.asList(activity.getResources().getStringArray(R.array.preselected_subjects_values)));
        String[] preselected = PreferenceUtil.getPreselectionElements(activity);

        int[] preselectedColors = activity.getResources().getIntArray(R.array.preselected_subjects_colors);
        String[] preselectedLanguage = activity.getResources().getStringArray(R.array.preselected_subjects);

        for (int i = 0; i < preselected.length; i++) {
            if (preselectedValues.contains(preselected[i])) {
                String langValue = preselectedLanguage[preselectedValues.indexOf(preselected[i])];
                if (!subjects.contains(langValue.toUpperCase()))
                    customWeeks.add(0, new Week(langValue, "", "", "", "", preselectedColors[i], true));
            }
        }

        Collections.sort(customWeeks, (week1, week2) -> week1.getSubject().compareToIgnoreCase(week2.getSubject()));
        return customWeeks;
    }

    @NonNull
    private static ArrayList<Week> removeDuplicates(@NonNull ArrayList<Week> weeks) {
        ArrayList<Week> returnValue = new ArrayList<>();
        ArrayList<String> returnValueSubjects = new ArrayList<>();
        for (Week w : weeks) {
            if (!returnValueSubjects.contains(w.getSubject().toUpperCase())) {
                returnValue.add(w);
                returnValueSubjects.add(w.getSubject().toUpperCase());
            }
        }
        return returnValue;
    }

    public static int getMatchingScheduleBegin(@NonNull String time) {
        return SubstitutionEntry.Companion.getStartLessonOfLocalTime(time);
    }

    public static int getMatchingScheduleEnd(@NonNull String time) {
        return SubstitutionEntry.Companion.getEndLessonOfLocalTime(time);
    }

    @NonNull
    public static String getMatchingTimeBegin(int hour) {
        String time = SubstitutionEntry.Companion.getStartLocalTimeOfLesson(hour).toString("HH:mm");
        if (time.length() == 4)
            time = "0" + time;
        return time;
    }

    @NonNull
    public static String getMatchingTimeEnd(int hour) {
        String time = SubstitutionEntry.Companion.getEndLocalTimeOfLesson(hour).toString("HH:mm");
        if (time.length() == 4)
            time = "0" + time;
        return time;
    }

    public static boolean isEvenWeek(@NonNull Calendar termStart, @NonNull Calendar now) {
        boolean isEven = true;

        int weekDifference = now.get(Calendar.WEEK_OF_YEAR) - termStart.get(Calendar.WEEK_OF_YEAR);
        if (weekDifference < 0) {
            weekDifference = -weekDifference;
        }

        for (int i = 0; i < weekDifference; i++) {
            isEven = !isEven;
        }

        return isEven;
    }
}
