package com.ulan.timetable.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.substitutionplan.SubstitutionList;
import com.ulan.timetable.adapters.WeekAdapter;
import com.ulan.timetable.utils.DbHelper;
import com.ulan.timetable.utils.FragmentHelper;

public class SaturdayFragment extends Fragment {

    public static final String KEY_SATURDAY_FRAGMENT = "Saturday";
    private DbHelper db;
    private ListView listView;
    private WeekAdapter adapter;

    private SubstitutionList entries = null;

    public SaturdayFragment(SubstitutionList entries) {
        super();
        this.entries = entries;
    }

    public SaturdayFragment() {
        super();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timetable_fragment_saturday, container, false);
        setupAdapter(view);
        setupListViewMultiSelect();
        return view;
    }

    private void setupAdapter(View view) {
        db = new DbHelper(getActivity());
        listView = view.findViewById(R.id.saturdaylist);
        adapter = new WeekAdapter(getActivity(), listView, R.layout.timetable_listview_week_adapter, db.getWeek(KEY_SATURDAY_FRAGMENT));
        listView.setAdapter(adapter);
    }

    private void setupListViewMultiSelect() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(FragmentHelper.setupListViewMultiSelect(getActivity(), listView, adapter, db));
    }
}
