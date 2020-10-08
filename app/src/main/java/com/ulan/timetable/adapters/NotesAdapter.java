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

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.SparseBooleanArray;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.ulan.timetable.databaseUtils.DbHelper;
import com.ulan.timetable.model.Note;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.ColorPalette;

import java.util.ArrayList;
import java.util.Objects;


/**
 * Created by Ulan on 28.09.2018.
 */
public class NotesAdapter extends ArrayAdapter<Note> {

    @NonNull
    private final AppCompatActivity mActivity;
    @NonNull
    private final ArrayList<Note> notelist;
    private Note note;
    private final ListView mListView;

    private static class ViewHolder {
        TextView title;
        ImageView popup;
        CardView cardView;
    }

    public NotesAdapter(@NonNull AppCompatActivity activity, ListView listView, int resource, @NonNull ArrayList<Note> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        mListView = listView;
        notelist = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String title = Objects.requireNonNull(getItem(position)).getTitle();
        String text = Objects.requireNonNull(getItem(position)).getText();
        int color = Objects.requireNonNull(getItem(position)).getColor();

        note = new Note(title, text, color);
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.timetable_listview_notes_adapter, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.titlenote);
            holder.popup = convertView.findViewById(R.id.popupbtn);
            holder.cardView = convertView.findViewById(R.id.notes_cardview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Setup colors based on Background
        int textColor = ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK);
        holder.title.setTextColor(textColor);
        ImageViewCompat.setImageTintList(convertView.findViewById(R.id.popupbtn), ColorStateList.valueOf(textColor));


        holder.title.setText(note.getTitle());
        holder.cardView.setCardBackgroundColor(note.getColor());
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
                            db.deleteNoteById(Objects.requireNonNull(getItem(position)));
                            db.updateNote(Objects.requireNonNull(getItem(position)));
                            notelist.remove(position);
                            notifyDataSetChanged();
                        }, getContext().getString(R.string.timetable_delete_note, note.getTitle()));
                        return true;
                    } else if (itemId == R.id.edit_popup) {
                        final View alertLayout = mActivity.getLayoutInflater().inflate(R.layout.timetable_dialog_add_note, null);
                        AlertDialogsHelper.getEditNoteDialog(mActivity, alertLayout, notelist, mListView, position);
                        notifyDataSetChanged();
                        return true;
                    }
                    return

                            onMenuItemClick(item);
                }
            });
            popup.show();
        });

        hidePopUpMenu(holder);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    public ArrayList<Note> getNoteList() {
        return notelist;
    }

    public Note getNote() {
        return note;
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
