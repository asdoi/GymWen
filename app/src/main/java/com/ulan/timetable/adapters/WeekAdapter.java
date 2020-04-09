package com.ulan.timetable.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.teacherlist.TeacherlistFeatures;
import com.asdoi.gymwen.ui.activities.RoomPlanActivity;
import com.asdoi.gymwen.ui.fragments.SubstitutionFragment;
import com.asdoi.gymwen.util.External_Const;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.ulan.timetable.model.Week;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


/**
 * Created by Ulan on 08.09.2018.
 */
public class WeekAdapter extends ArrayAdapter<Week> {

    private ActivityFeatures mActivity;
    private int mResource;
    private ArrayList<Week> weeklist;
    private Week week;
    private ListView mListView;

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
        mResource = resource;
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
            convertView = inflater.inflate(mResource, parent, false);
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

        holder.subject.setText(week.getSubject());

        removeTeacherClick(holder.teacher);
        holder.teacher.setText(week.getTeacher());
        if (!Arrays.asList(External_Const.nothing).contains(week.getTeacher()))
            teacherClick(holder.teacher, week.getTeacher(), PreferenceUtil.isFullTeacherNames());
        if (week.getMoreInfos() != null && !getWeek().getMoreInfos().trim().isEmpty()) {
            holder.teacher.setText(holder.teacher.getText() + " (" + week.getMoreInfos() + ")");
        }

        holder.room.setText(week.getRoom());
        holder.room.setOnClickListener(null);
        holder.room.setOnClickListener((View v) -> {
            Intent intent = new Intent(getContext(), RoomPlanActivity.class);
            intent.putExtra(RoomPlanActivity.SELECT_ROOM, holder.room.getText());
            mActivity.startActivity(intent);
        });
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        holder.room.setBackgroundResource(outValue.resourceId);

        holder.time.setText(week.getFromTime() + " - " + week.getToTime());
        holder.cardView.setCardBackgroundColor(week.getColor());
        holder.popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextThemeWrapper theme = new ContextThemeWrapper(mActivity, PreferenceUtil.isDark() ? R.style.Widget_AppCompat_PopupMenu : R.style.Widget_AppCompat_Light_PopupMenu);
                final PopupMenu popup = new PopupMenu(theme, holder.popup);
                final DbHelper db = new DbHelper(mActivity);
                popup.getMenuInflater().inflate(R.menu.timetable_popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.delete_popup) {
                            db.deleteWeekById(getItem(position));
                            db.updateWeek(getItem(position));
                            weeklist.remove(position);
                            notifyDataSetChanged();
                            return true;
                        } else if (itemId == R.id.edit_popup) {
                            final View alertLayout = mActivity.getLayoutInflater().inflate(R.layout.timetable_dialog_add_subject, null);
                            AlertDialogsHelper.getEditSubjectDialog(mActivity, alertLayout, mListView, weeklist.get(position));
                            notifyDataSetChanged();
                            return true;
                        }
                        return onMenuItemClick(item);
                    }
                });
                popup.show();
            }
        });

        hidePopUpMenu(holder);
        if (!week.getEditable())
            holder.popup.setVisibility(View.INVISIBLE);

        return convertView;
    }

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

    private void removeTeacherClick(@NonNull View view) {
        view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        view.setBackgroundResource(0);
        view.setClickable(false);
        view.setOnClickListener(null);
    }

    //TeacherSearch
    private void teacherClick(@NonNull TextView view, @NonNull String teacherQuery, boolean fullNames) {
        if (TeacherlistFeatures.isAOL(teacherQuery))
            return;
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setBackgroundResource(outValue.resourceId);


        if (fullNames) {
            new Thread(() -> {
                ApplicationFeatures.downloadTeacherlistDoc();
                try {
                    mActivity.runOnUiThread(() -> {
                        String match = SubstitutionFragment.getMatchingTeacher(teacherQuery);
                        if (match != null)
                            view.setText(match);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            view.setText(teacherQuery);
        }


        view.setClickable(true);
        view.setOnClickListener((View v) -> {
            //TeacherList Activity
        });
    }
}