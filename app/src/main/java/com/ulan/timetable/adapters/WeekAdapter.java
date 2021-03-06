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

package com.ulan.timetable.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.RoomPlanActivity;
import com.asdoi.gymwen.ui.activities.TeacherListActivity;
import com.asdoi.gymwen.util.External_Const;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.ulan.timetable.databaseUtils.DbHelper;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.receivers.DoNotDisturbReceiversKt;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.ColorPalette;
import com.ulan.timetable.utils.WeekUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


/**
 * Created by Ulan on 08.09.2018.
 */
public class WeekAdapter extends ArrayAdapter<Week> {

    @NonNull
    private final ActivityFeatures mActivity;
    @NonNull
    private final ArrayList<Week> weeklist;
    private Week week;
    private final ListView mListView;

    private static class ViewHolder {
        TextView subject;
        TextView teacher;
        TextView time;
        TextView room;
        ImageView popup;
        CardView cardView;
    }

    public WeekAdapter(@NonNull ActivityFeatures activity, ListView listView, int resource, @NonNull ArrayList<Week> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        weeklist = objects;
        mListView = listView;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String subject = Objects.requireNonNull(getItem(position)).getSubject();
        String teacher = Objects.requireNonNull(getItem(position)).getTeacher();
        String time_from = Objects.requireNonNull(getItem(position)).getFromTime();
        String time_to = Objects.requireNonNull(getItem(position)).getToTime();
        String room = Objects.requireNonNull(getItem(position)).getRoom();
        String moreInfo = Objects.requireNonNull(getItem(position)).getMoreInfos();
        boolean edit = Objects.requireNonNull(getItem(position)).getEditable();
        int color = Objects.requireNonNull(getItem(position)).getColor();

        week = new Week(subject, teacher, room, time_from, time_to, color, edit);
        if (moreInfo != null && !moreInfo.trim().isEmpty())
            week.setMoreInfos(moreInfo);
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.timetable_listview_week_adapter, parent, false);
            holder = new ViewHolder();
            holder.subject = convertView.findViewById(R.id.subject);
            holder.teacher = convertView.findViewById(R.id.teacher);
            holder.time = convertView.findViewById(R.id.time);
            holder.room = convertView.findViewById(R.id.room);
            holder.popup = convertView.findViewById(R.id.popupbtn);
            holder.cardView = convertView.findViewById(R.id.week_cardview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Setup colors based on Background
        int textColor = ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK);
        holder.subject.setTextColor(textColor);
        holder.teacher.setTextColor(textColor);
        holder.time.setTextColor(textColor);
        holder.room.setTextColor(textColor);
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.roomimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.teacherimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.timeimage), ColorStateList.valueOf(textColor));
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.popupbtn), ColorStateList.valueOf(textColor));
        convertView.findViewById(R.id.line).setBackgroundColor(textColor);


        holder.subject.setText(week.getSubject());

        TeacherListActivity.removeTeacherClick(holder.teacher, getContext());
        holder.teacher.setText(week.getTeacher());
        if (!Arrays.asList(External_Const.nothing).contains(week.getTeacher()))
            TeacherListActivity.teacherClick(holder.teacher, week.getTeacher(), PreferenceUtil.isFullTeacherNames(), mActivity);

        if (week.getMoreInfos() != null && !getWeek().getMoreInfos().trim().isEmpty()) {
            holder.teacher.setText(holder.teacher.getText() + " (" + week.getMoreInfos() + ")");
        }

        holder.room.setText(week.getRoom());
        holder.room.setOnClickListener(null);
        holder.room.setOnClickListener((View v) -> {
            Intent intent = new Intent(getContext(), RoomPlanActivity.class);
            intent.putExtra(RoomPlanActivity.SELECT_ROOM_NAME, holder.room.getText());
            mActivity.startActivity(intent);
        });
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        holder.room.setBackgroundResource(outValue.resourceId);

        if (com.ulan.timetable.utils.PreferenceUtil.showTimes(getContext()))
            holder.time.setText(week.getFromTime() + " - " + week.getToTime());
        else {
            int start = WeekUtils.getMatchingScheduleBegin(week.getFromTime());
            int end = WeekUtils.getMatchingScheduleEnd(week.getToTime());
            if (start == end) {
                holder.time.setText(start + ". " + getContext().getString(R.string.lesson));
            } else {
                holder.time.setText(start + ".-" + end + ". " + getContext().getString(R.string.lesson));
            }
        }

        holder.cardView.setCardBackgroundColor(week.getColor());
        holder.popup.setOnClickListener(v -> {
            ContextThemeWrapper theme = new ContextThemeWrapper(mActivity, PreferenceUtil.isDark() ? R.style.Widget_AppCompat_PopupMenu : R.style.Widget_AppCompat_Light_PopupMenu);
            final PopupMenu popup = new PopupMenu(theme, holder.popup);
            final DbHelper db = new DbHelper(mActivity);
            popup.inflate(R.menu.timetable_popup_menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.delete_popup) {
                        AlertDialogsHelper.getDeleteDialog(getContext(), () -> {
                            db.deleteWeekById(Objects.requireNonNull(getItem(position)));
                            db.updateWeek(Objects.requireNonNull(getItem(position)));
                            weeklist.remove(position);
                            notifyDataSetChanged();
                            DoNotDisturbReceiversKt.setDoNotDisturbReceivers(mActivity, false);
                        }, getContext().getString(R.string.timetable_delete_week, week.getSubject()));
                        return true;
                    } else if (itemId == R.id.edit_popup) {
                        final View alertLayout = mActivity.getLayoutInflater().inflate(R.layout.timetable_dialog_add_subject, null);
                        AlertDialogsHelper.getEditSubjectDialog(mActivity, alertLayout, () -> notifyDataSetChanged(), weeklist.get(position));
                        notifyDataSetChanged();
                        return true;
                    }
                    return onMenuItemClick(item);
                }
            });
            popup.show();
        });

        hidePopUpMenu(holder);
        if (!week.getEditable())
            holder.popup.setVisibility(View.INVISIBLE);


        return convertView;
    }

    @NonNull
    public ArrayList<Week> getWeekList() {
        return weeklist;
    }

    public Week getWeek() {
        return week;
    }

    private void hidePopUpMenu(@NonNull ViewHolder holder) {
        SparseBooleanArray checkedItems = mListView.getCheckedItemPositions();
        if (checkedItems.size() > 0) {
            for (int i = 0; i < checkedItems.size(); i++) {
                int key = checkedItems.keyAt(i);
                if (checkedItems.get(key)) {
                    holder.popup.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            holder.popup.setVisibility(View.VISIBLE);
        }
    }

}
