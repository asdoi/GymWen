package com.asdoi.gymwen.main.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;
import com.asdoi.gymwen.main.activities.MainActivity;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class VertretungFragment extends Fragment implements View.OnClickListener {
    private View root;
    private Context context;
    public static boolean today;
    public static boolean all;
    public static boolean both;

    public VertretungFragment() {
        super();
    }

    public VertretungFragment(boolean today, boolean all) {
        super();
        VertretungFragment.today = today;
        VertretungFragment.all = all;
        both = false;
    }

    public VertretungFragment(boolean both) {
        all = false;
        VertretungFragment.both = true;
        today = false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        root = inflater.inflate(R.layout.fragment_vertretung, container, false);
        FloatingActionButton fab = getActivity().findViewById(R.id.main_fab);
        if (all) {
            fab.setEnabled(false);
            fab.setVisibility(View.GONE);
        } else {
            fab.setEnabled(true);
            fab.bringToFront();
            fab.setVisibility(View.VISIBLE);
        }
        fab.setOnClickListener(this);

        ActivityFeatures.createLoadingPanel(root.findViewById(R.id.vertretung_frame));

        refreshAndTable();

        return root;
    }

    private void refreshAndTable() {
        new Thread(() -> {
            ApplicationFeatures.downloadVertretungsplanDocs(false, true);
            getActivity().runOnUiThread(() -> {
                generateTable();
            });
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_fab:
                share();
                break;
        }
    }


    //Share
    private void share() {
        String message = shareMessage();
        String footprint = getString(R.string.footprint);
        message += footprint;

        if (VertretungsPlanFeatures.getTodayTitle().equals("Keine Internetverbindung!")) {
            //Toast.makeText(getActivity(), "Du bist nicht mit dem Internet verbunden!",Toast.LENGTH_LONG).show();
            Snackbar snackbar = Snackbar
                    .make(root.findViewById(R.id.vertretung_frame), getString(R.string.noInternet), Snackbar.LENGTH_LONG);
            snackbar.show();

            return;
        }

        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, message);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, getString(R.string.share_vertretung)));
    }

    private String shareMessage() {
        String message = "";
        String[][] inhalt = null;
        String title = "";

        if (both) {
            both = false;
            today = true;
            message += shareMessage();
            today = false;
            message += shareMessage();
            return message;
        } else if (today) {
            inhalt = VertretungsPlanFeatures.getTodayArray();
            title = VertretungsPlanFeatures.getTodayTitle();
        } else {
            inhalt = VertretungsPlanFeatures.getTomorrowArray();
            title = VertretungsPlanFeatures.getTomorrowTitle();
        }
        String classes = "";

        if (inhalt == null) {
            return "";
        }

        if (VertretungsPlanFeatures.getOberstufe()) {
            ArrayList<String> courses = new ArrayList<>();
            if (inhalt.length != 0) {
                for (String[] line : inhalt) {
                    if (!courses.contains(line[0])) {
                        courses.add(line[0]);
                    }
                }
                for (int i = 0; i < courses.size() - 1; i++) {
                    classes += courses.get(i) + ", ";
                }
                classes += courses.get(courses.size() - 1);
            }
        } else {
            ArrayList<String> names = VertretungsPlanFeatures.getNames();
            for (int i = 0; i < names.size(); i++) {
                classes += names.get(i) + "";
            }
        }

        if (inhalt.length == 0) {
            message += "Am " + title + " haben wir (" + classes + ") keine Vertretung.\n";
            return message;
        }
        message = "Am " + title + " haben wir (" + classes + ") folgende Vertretung:\n";
        if (VertretungsPlanFeatures.getOberstufe()) {
            for (String[] line : inhalt) {
                if (line[3].equals("entfällt")) {
                    message += line[1] + ". Stunde entfällt für Kurs " + line[0] + "\n";
                } else {
                    message += line[1] + ". Stunde für Kurs " + line[0] + " in Raum " + line[4] + " bei " + line[3] + " " + line[5] + "\n";
                }
            }
        } else {
            for (String[] line : inhalt) {
                if (line[3].equals("entfällt")) {
                    message += line[1] + ". Stunde entfällt\n";
                } else {
                    message += line[1] + ". Stunde " + line[2] + " bei " + line[3] + " in Raum " + line[4] + " " + line[5] + "\n";
                }
            }
        }
        return message;
    }


    //TeacherSearch
    void teacherClick(View view, String teacherQuery, boolean showBorders) {
        if (showBorders)
            view.setBackground(ContextCompat.getDrawable(ApplicationFeatures.getContext(), R.drawable.background_shape));
        view.setOnClickListener((View v) -> {
            if (ApplicationFeatures.isNetworkAvailable()) {
                new Thread(() -> {
                    ApplicationFeatures.downloadLehrerDoc();
                    getActivity().runOnUiThread(() -> {
                        teacherSearch(teacherQuery);
                    });
                }).start();
            } else {
                teacherSearch(teacherQuery);
            }

        });
    }

    void teacherSearch(String query) {
        try {
            createTeacherView(Lehrerliste.getTeacher(query));
        } catch (NullPointerException e) {
            if (!Lehrerliste.isDownloaded()) {
                Snackbar snackbar = Snackbar
                        .make(root.findViewById(R.id.vertretung_frame), getString(R.string.noInternet), Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                Snackbar snackbar = Snackbar
                        .make(root.findViewById(R.id.vertretung_frame), getString(R.string.teacher_no_teacher_found), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    private void createTeacherView(String[] teacher) {
        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.VERTICAL);
        base.setGravity(Gravity.CENTER);
        base.setBackgroundColor(Color.parseColor("#99000000"));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        base.setLayoutParams(params);
        base.setId(ApplicationFeatures.vertretung_teacher_view_id);
        base.setOnClickListener((View v) -> {
            try {
                ((ViewGroup) v.getParent()).removeView(v);
            } catch (NullPointerException e) {
                v.setVisibility(View.GONE);
            }
        });

        ViewGroup teacherEntry = new LinearLayout(context);
        ViewStub viewStub = new ViewStub(context);
        viewStub.setLayoutResource(R.layout.list_lehrerliste_entry);
        teacherEntry.addView(viewStub);
        viewStub.inflate();
        teacherEntry = (ViewGroup) ((MainActivity) getActivity()).getTeacherView(teacherEntry, teacher);

        LinearLayout background = new LinearLayout(context);
        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        background.setLayoutParams(params);
        background.setBackgroundColor(Color.parseColor("#FFFFFF"));

        background.addView(teacherEntry);
        base.addView(background);
        ((ViewGroup) root.findViewById(R.id.vertretung_frame)).addView(base);
    }


    //Generating Table
    boolean oberstufe;
    String[][] inhalt;
    String title;
    boolean sonstiges;

    void generateTable() {
        clear();

        oberstufe = VertretungsPlanFeatures.getOberstufe();
        if (both) {
            inhalt = VertretungsPlanFeatures.getTodayArray();
            title = VertretungsPlanFeatures.getTodayTitle();
            sonstiges = isSonstiges();
//            generateScrollView();
            generateTop();
            generateTableSpecific();

            inhalt = VertretungsPlanFeatures.getTomorrowArray();
            title = VertretungsPlanFeatures.getTomorrowTitle();
            sonstiges = isSonstiges();
            generateTop();
            generateTableSpecific();
        } else if (all) {
            if (today) {
                inhalt = VertretungsPlanFeatures.getTodayArrayAll();
                title = VertretungsPlanFeatures.getTodayTitle();
            } else {
                inhalt = VertretungsPlanFeatures.getTomorrowArrayAll();
                title = VertretungsPlanFeatures.getTomorrowTitle();
            }
            sonstiges = isSonstiges();
            generateTop();
            generateTableAll();
        } else {
            if (today) {
                inhalt = VertretungsPlanFeatures.getTodayArray();
                title = VertretungsPlanFeatures.getTodayTitle();
            } else {
                inhalt = VertretungsPlanFeatures.getTomorrowArray();
                title = VertretungsPlanFeatures.getTomorrowTitle();
            }
            sonstiges = isSonstiges();
            generateTop();
            generateTableSpecific();
        }
    }

    public void clear() {
        ((ViewGroup) root.findViewById(R.id.vertretung_frame)).removeView(root.findViewWithTag("vertretung_loading"));
        LinearLayout base = root.findViewById(R.id.vertretung_linear_layout_layer1);
        base.removeAllViews();
    }


    //Top (Date or noInternet, etc.)
    void generateTop() {
        TextView titleView = createTitleLayout();
        ViewGroup base = root.findViewById(R.id.vertretung_linear_layout_layer1);
        base.addView(titleView);

        if (inhalt == null) {
            titleView.setText(getContext().getString(R.string.noInternetConnection));
            return;
        } else {
            titleView.setText(title);
        }

        if (inhalt.length == 0) {
            TextView tv = new TextView(context);
            tv.setText(context.getString(R.string.nothing));
            tv.setTextSize(20);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            base.addView(tv);
        }

    }

    TextView createTitleLayout() {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return textView;
    }

    boolean isSonstiges() {
        if (inhalt == null)
            return false;
        for (int i = 0; i < inhalt.length; i++) {
            if (!inhalt[i][5].trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    String[] generateHeadline() {
        String[] headline;
        String sonstigesString = isSonstiges() ? context.getString(R.string.other) : "";

        if (all) {
            headline = new String[]{context.getString(R.string.classes), sonstiges ? context.getString(R.string.hours_short) : context.getString(R.string.hours), context.getString(R.string.subject), sonstiges ? context.getString(R.string.teacher_short) : context.getString(R.string.teacher), sonstiges ? context.getString(R.string.room_short) : context.getString(R.string.room), context.getString(R.string.other)};
        } else if (oberstufe) {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.courses), context.getString(R.string.teacher), context.getString(R.string.room), sonstigesString, context.getString(R.string.subject)};
        } else {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.subject), context.getString(R.string.teacher), context.getString(R.string.room), sonstigesString, context.getString(R.string.classes)};
        }

        return headline;
    }


    //Body
    View generateOverviewAll() {
        String[] headline = generateHeadline();
        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.HORIZONTAL);
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < headline.length; i++) {
            LinearLayout.LayoutParams params;


            switch (i) {
                case 0:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2);
                    break;
                case 5:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3);
                    break;
                default:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            }

            TextView hour = createBlankTextView();
            params.setMargins(3, 3, 3, 3);
            hour.setLayoutParams(params);
            hour.setText(headline[i]);
            base.addView(hour);
        }

        return base;
    }

    void generateTableAll() {
        ViewGroup base = root.findViewById(R.id.vertretung_linear_layout_layer1);

        if (inhalt != null && inhalt.length > 0) {
            //Overview
            base.addView(generateOverviewAll());

            //Content
            vertretungListView = new ListView(context);
            vertretungListView.setAdapter(new VertretungListAdapterAll(getContext(), 0, inhalt));
            vertretungListView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            base.addView(vertretungListView);
        }
    }

    View generateOverviewSpecific() {
        String[] headline = generateHeadline();

        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.HORIZONTAL);
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2);
        params.setMargins(3, 3, 3, 3);
        TextView hour = createBlankTextView();
        hour.setLayoutParams(params);
        hour.setText(headline[0]);
        base.addView(hour);

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(3, 3, 3, 3);
        TextView subject = createBlankTextView();
        subject.setLayoutParams(params);
        subject.setText(headline[1]);
        base.addView(subject);

        TextView teacher = createBlankTextView();
        teacher.setLayoutParams(params);
        teacher.setText(headline[2]);
        base.addView(teacher);

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2);
        params.setMargins(3, 3, 3, 3);
        TextView room = createBlankTextView();
        room.setLayoutParams(params);
        room.setText(headline[3]);
        base.addView(room);

        if (sonstiges) {
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3);
            params.setMargins(3, 3, 3, 3);
            TextView other = createBlankTextView();
            other.setLayoutParams(params);
            other.setText(headline[4]);
            base.addView(other);
        }

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(3, 3, 3, 3);
        TextView course = createBlankTextView();
        course.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        course.setLayoutParams(params);
        course.setText(headline[5]);
        base.addView(course);

        return base;
    }

    TextView createBlankTextView() {
        TextView hour = new TextView(context);
        hour.setTypeface(hour.getTypeface(), Typeface.BOLD);
        hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        hour.setGravity(Gravity.CENTER);
        return hour;
    }

    void generateTableSpecific() {
        ViewGroup base = root.findViewById(R.id.vertretung_linear_layout_layer1);

        if (inhalt != null && inhalt.length > 0) {
            //Overview
            base.addView(generateOverviewSpecific());

            //Content
            vertretungListView = new ListView(context);
            vertretungListView.setAdapter(new VertretungListAdapterSpecific(getContext(), 0, inhalt));
            vertretungListView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            base.addView(vertretungListView);
        }
    }

    void generateScrollView() {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ScrollView s = new ScrollView(context);
        s.setLayoutParams(params);
        LinearLayout l = root.findViewById(R.id.vertretung_linear_layout_layer1);
        l.setLayoutParams(params);
        ViewGroup parent = (ViewGroup) l.getParent();
        parent.removeAllViews();
        parent.addView(s);
        s.addView(l);
    }

    private ListView vertretungListView;


    //All ListView
    private class VertretungListAdapterAll extends ArrayAdapter<String[]> {
        String[][] content;

        public VertretungListAdapterAll(Context con, int resource, String[][] content) {
            super(con, resource);
            this.content = content;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_vertretung_all_entry, null);
            }
            return getEntryAll(convertView, content[position]);
        }

        @Override
        public int getCount() {
            return content.length;
        }
    }

    private View getEntryAll(View view, String[] entry) {
        TextView course = view.findViewById(R.id.vertretung_all_entry_textViewCourse);
        course.setText(entry[0]);

        TextView hour = view.findViewById(R.id.vertretung_all_entry_textViewHour);
        hour.setText(entry[1]);

        TextView subject = view.findViewById(R.id.vertretung_all_entry_textViewSubject);
        subject.setText(entry[2]);

        TextView teacher = view.findViewById(R.id.vertretung_all_entry_textViewTeacher);
        teacher.setText(entry[3]);
        teacherClick(teacher, entry[3], !ApplicationFeatures.getBooleanSettings("show_border_specific", true) && ApplicationFeatures.getBooleanSettings("show_borders", false));

        TextView room = view.findViewById(R.id.vertretung_all_entry_textViewRoom);
        room.setText(entry[4]);

        TextView other = view.findViewById(R.id.vertretung_all_entry_textViewOther);
        if (sonstiges) {
            other.setText(entry[5]);
        } else {
            other.setVisibility(View.GONE);
        }

        return view;
    }


    //Specific ListView
    private class VertretungListAdapterSpecific extends ArrayAdapter<String[]> {
        String[][] content;

        public VertretungListAdapterSpecific(Context con, int resource, String[][] content) {
            super(con, resource);
            this.content = content;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_vertretung_specific_entry, null);
            }
            return getEntrySpecific(convertView, content[position], oberstufe);
        }

        @Override
        public int getCount() {
            return content.length;
        }

    }

    private View getEntrySpecific(View view, String[] entry, boolean oberstufe) {
        TextView hour = view.findViewById(R.id.vertretung_specific_entry_textViewHour);
        hour.setText(entry[1]);

        TextView subject = view.findViewById(R.id.vertretung_specific_entry_textViewSubject);
        subject.setText(oberstufe ? entry[0] : entry[2]);

        TextView teacher = view.findViewById(R.id.vertretung_specific_entry_textViewTeacher);
        teacherClick(teacher, entry[3], ApplicationFeatures.getBooleanSettings("show_borders", true));
        teacher.setText(entry[3]);

        TextView room = view.findViewById(R.id.vertretung_specific_entry_textViewRoom);
        SpannableString content = new SpannableString(entry[4]);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        room.setText(content);

        TextView other = view.findViewById(R.id.vertretung_specific_entry_textViewOther);
        if (sonstiges)
            other.setText(entry[5]);
        else
            other.setVisibility(View.GONE);

        TextView course = view.findViewById(R.id.vertretung_specific_entry_textViewClass);
        course.setText(oberstufe ? entry[2] : entry[0]);

        return view;
    }








    /*void setTableParams() {
        clear();

        if (both)
            generateScrollView();

        TextView titleView = createTitleLayout();
        TableLayout table = createTableLayout();

        oberstufe = VertretungsPlanFeatures.getOberstufe();
        if (both) {

            inhalt = VertretungsPlanFeatures.getTodayArray();
            title = VertretungsPlanFeatures.getTodayTitle();
            setTitle(titleView);
            generateTableSpecific(table);

            if (inhalt != null) {
                titleView = createTitleLayout();
                table = createTableLayout();


                inhalt = VertretungsPlanFeatures.getTomorrowArray();
                title = VertretungsPlanFeatures.getTomorrowTitle();
                setTitle(titleView);
                generateTableSpecific(table);
            }
        } else if (all) {
            if (today) {
                inhalt = VertretungsPlanFeatures.getTodayArrayAll();
                title = VertretungsPlanFeatures.getTodayTitle();
            } else {
                inhalt = VertretungsPlanFeatures.getTomorrowArrayAll();
                title = VertretungsPlanFeatures.getTomorrowTitle();
            }
            generateTableNormal(table);
            setTitle(titleView);
        } else {
            if (today) {
                inhalt = VertretungsPlanFeatures.getTodayArray();
                title = VertretungsPlanFeatures.getTodayTitle();
            } else {
                inhalt = VertretungsPlanFeatures.getTomorrowArray();
                title = VertretungsPlanFeatures.getTomorrowTitle();
            }
            generateTableSpecific(table);
            setTitle(titleView);
        }
    }*/

    /*


    TableLayout createTableLayout() {
        TableLayout table = new TableLayout(context);
        table.setStretchAllColumns(true);

        LinearLayout base = root.findViewById(R.id.vertretung_linear_layout_layer1);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ScrollView s1 = new ScrollView(context);
        s1.setLayoutParams(params);

        HorizontalScrollView hs2 = new HorizontalScrollView(context);
        hs2.setLayoutParams(params);

        table.setLayoutParams(params);

        base.addView(s1);
        s1.addView(hs2);
        hs2.addView(table);


        return table;
    }

    TextView createTitleLayout() {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,(int) root.getResources().getDimension(R.dimen.headline_size));
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((LinearLayout) root.findViewById(R.id.vertretung_linear_layout_layer1)).addView(textView);
        return textView;
    }

    void generateTableNormal(TableLayout table) {
//            for (int i = 0; i < table.getChildCount(); i++) {
//                if (root.findViewById(i + 130) != null) {
//                    table.removeView((TableRow) root.findViewById(i + 130));
//                    System.out.println("removed row " + i);
//                }
//            }

        if (inhalt == null) {
            return;
        }

        if (inhalt.length == 0) {
            generateTableNothing(table);
            return;
        }
        generateFirstRowAll(table);
        generateBodyRowsAll(table);

    }

    void generateBodyRowsAll(TableLayout table) {
        int columnNumber = inhalt[0].length;
        int rowNumber = inhalt.length;

        for (int i = 0; i < rowNumber; i++) {
            TableRow row = new TableRow(context);
            row.setId(i + 130);

                *//*Button bt = new Button(context
                );
                bt.setText(inhalt[i][0]);
                bt.setTypeface(Typeface.DEFAULT_BOLD);
                bt.setGravity(Gravity.CENTER);
                row.addView(bt);*//*


            for (int j = 0; j < columnNumber; j++) {
                TextView tv = new TextView(context);
                tv.setText(inhalt[i][j]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                if (j == 3) {
                    teacherClick(tv, inhalt[i][3], !ApplicationFeatures.getBooleanSettings("show_border_specific", true) && ApplicationFeatures.getBooleanSettings("show_borders", false));
                }
                row.addView(tv);

            }
            table.addView(row);
        }
    }

    void generateTableSpecific(TableLayout table) {
        for (int i = 0; i < table.getChildCount(); i++) {
            if (root.findViewById(i + 130) != null) {
                table.removeView(root.findViewById(i + 130));
                System.out.println("removed row " + i);
            }
        }
        if (inhalt == null) {
            return;
        }
        if (inhalt.length == 0) {
            generateTableNothing(table);
            return;
        }
        generateFirstRow(table);
        if (oberstufe)
            generateBodyRowsOberstufe(table);
        else
            generateBodyRowsKlasse(table);
    }

    void generateBodyRowsOberstufe(TableLayout table) {
        int columnNumber = inhalt[0].length;
        int rowNumber = inhalt.length;


        for (int i = 0; i < rowNumber; i++) {
            TableRow row = new TableRow(context);
            row.setId(i + 130);

            //Stunde
            int hourMargin = 5;
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(hourMargin, hourMargin, hourMargin, hourMargin);

            TextView tv = new TextView(context);
            tv.setPadding(hourMargin, hourMargin, hourMargin, hourMargin);
            tv.setLayoutParams(params);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            tv.setTextColor(Color.WHITE);
            tv.setText(inhalt[i][1]);
            tv.setTextSize(36);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv, params);

            params.setMargins(0, 0, 0, 0);

            if (inhalt[i][3].equals("entfällt")) {
                //Kurs
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                SpannableString content = new SpannableString(inhalt[i][3]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv.setText(content);
                tv.setTextSize(24);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                row.addView(tv);

                for (int j = 2; j < columnNumber - 1; j++) {
                    tv = new TextView(context
                    );
                    tv.setText("");
                    tv.setTextSize(18);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                    tv.setGravity(Gravity.CENTER);
                    row.addView(tv);
                }
                //Fach
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][0]);
                tv.setTextSize(12);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setGravity(Gravity.END);
                row.addView(tv);
            } else {
                //Kurs
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][0]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Lehrer
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][3]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                teacherClick(tv, inhalt[i][3], ApplicationFeatures.getBooleanSettings("show_borders", true));
                row.addView(tv);

                //Raum
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                SpannableString content = new SpannableString(inhalt[i][4]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv.setText(content);
                tv.setTextSize(24);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                row.addView(tv);

                //Sonstiges
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][5]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Fach
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][2]);
                tv.setTextSize(12);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setGravity(Gravity.END);
                row.addView(tv);
            }


            table.addView(row);
        }
    }

    void generateBodyRowsKlasse(TableLayout table) {
        int columnNumber = inhalt[0].length;
        int rowNumber = inhalt.length;


        for (int i = 0; i < rowNumber; i++) {
            TableRow row = new TableRow(context
            );
            row.setId(i + 130);

            //Stunde
            int hourMargin = 5;
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(hourMargin, hourMargin, hourMargin, hourMargin);

            TextView tv = new TextView(context
            );
            tv.setPadding(hourMargin, hourMargin, hourMargin, hourMargin);
            tv.setLayoutParams(params);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            tv.setTextColor(Color.WHITE);
            tv.setText(inhalt[i][1]);
            tv.setTextSize(36);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv, params);

            params.setMargins(0, 0, 0, 0);

            if (inhalt[i][3].equals("entfällt")) {
                //Klasse
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                SpannableString content = new SpannableString(inhalt[i][3]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv.setText(content);
                tv.setTextSize(24);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                row.addView(tv);

                for (int j = 2; j < columnNumber; j++) {
                    tv = new TextView(context
                    );
                    tv.setText("");
                    tv.setTextSize(18);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                    tv.setGravity(Gravity.CENTER);
                    row.addView(tv);
                }
                //Klasse
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][0]);
                tv.setTextSize(12);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setGravity(Gravity.END);
                row.addView(tv);

            } else {
                //Fach
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][2]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Lehrer
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][3]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                teacherClick(tv, inhalt[i][3], ApplicationFeatures.getBooleanSettings("show_borders", true));
                row.addView(tv);

                //Raum
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                SpannableString content = new SpannableString(inhalt[i][4]);
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tv.setText(content);
                tv.setTextSize(24);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                row.addView(tv);

                //Sonstiges
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][5]);
                tv.setTextSize(18);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);

                //Klasse
                tv = new TextView(context
                );
                tv.setLayoutParams(params);
                tv.setText(inhalt[i][0]);
                tv.setTextSize(12);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setGravity(Gravity.END);
                row.addView(tv);
            }


            table.addView(row);
        }
    }

    void generateTableNothing(TableLayout table) {
        TableRow row = new TableRow(context
        );
        row.setId(new Integer(130));
        for (int j = 0; j < 2; j++) {
            TextView tv = new TextView(context);
            tv.setText("");
            tv.setTextSize(24);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv);
        }
        TextView tv = new TextView(context
        );
        tv.setText(context.getString(R.string.nothing));
        tv.setTextSize(20);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        row.addView(tv);
        for (int j = 0; j < 2; j++) {
            tv = new TextView(context);
            tv.setText("");
            tv.setTextSize(24);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv);
        }
        table.addView(row);
    }

    void generateFirstRow(TableLayout table) {
        //generate first Row
        String[] headline = new String[6];
        String sonstiges = "";

        for (int i = 0; i < inhalt.length; i++) {
            if (!inhalt[i][5].trim().isEmpty()) {
                sonstiges = context.getString(R.string.other);
                break;
            }
        }

        if (all) {
            headline = new String[]{context.getString(R.string.classes), context.getString(R.string.hours), context.getString(R.string.subject), context.getString(R.string.teacher), context.getString(R.string.room), context.getString(R.string.other)};
        } else if (oberstufe) {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.courses), context.getString(R.string.teacher), context.getString(R.string.room), sonstiges, context.getString(R.string.subject)};
        } else {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.subject), context.getString(R.string.teacher), context.getString(R.string.room), sonstiges, context.getString(R.string.classes)};
        }


        if (root.findViewById(R.integer.vertretung_teacher_view_id) != null) {
            table.removeView(root.findViewById(R.integer.vertretung_teacher_view_id));
        }


        TableRow row = new TableRow(context
        );
        row.setId(R.integer.vertretung_teacher_view_id);
        for (int j = 0; j < 5; j++) {
            TextView tv = new TextView(context
            );
            tv.setText(headline[j]);
            tv.setTextSize(18);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv);

        }
        TextView tv = new TextView(context
        );
        tv.setText(headline[5]);
        tv.setTextSize(12);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.END);
        row.addView(tv);
        table.addView(row);
    }

    void generateFirstRowAll(TableLayout table) {
        //generate first Row
        String[] headline = new String[6];
        String sonstiges = "";

        for (int i = 0; i < inhalt.length; i++) {
            if (!inhalt[i][5].trim().isEmpty()) {
                sonstiges = context.getString(R.string.other);
                break;
            }
        }

        if (all) {
            headline = new String[]{context.getString(R.string.classes), context.getString(R.string.hours), context.getString(R.string.subject), context.getString(R.string.teacher), context.getString(R.string.room), context.getString(R.string.other)};
        } else if (oberstufe) {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.courses), context.getString(R.string.teacher), context.getString(R.string.room), sonstiges, context.getString(R.string.subject)};
        } else {
            headline = new String[]{context.getString(R.string.hours), context.getString(R.string.subject), context.getString(R.string.teacher), context.getString(R.string.room), sonstiges, context.getString(R.string.classes)};
        }


        if (root.findViewById(R.integer.vertretung_teacher_view_id) != null) {
            table.removeView(root.findViewById(R.integer.vertretung_teacher_view_id));
        }


        TableRow row = new TableRow(context);
        row.setId(R.integer.vertretung_teacher_view_id);
        for (int j = 0; j < headline.length; j++) {
            TextView tv = new TextView(context
            );
            tv.setText(headline[j]);
            tv.setTextSize(18);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            row.addView(tv);
        }
        table.addView(row);
    }

    void setTitle(TextView tV) {
        tV.setText(title);
    }

    public void clear() {
        ((ViewGroup) root.findViewById(R.id.vertretung_frame)).removeView(root.findViewWithTag("vertretung_loading"));
        LinearLayout base = root.findViewById(R.id.vertretung_linear_layout_layer1);
        base.removeAllViews();
    }*/
}