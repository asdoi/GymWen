package com.ulan.timetable.model;

import androidx.annotation.NonNull;

/**
 * Created by Ulan on 07.09.2018.
 */
public class Week {

    private String subject, fragment, teacher, room, fromtime, totime, time;
    private int id, color;

    private boolean editable = true;
    private String moreInfos;

    public Week() {
    }

    public Week(String subject, String teacher, String room, String fromtime, String totime, int color, boolean editable) {
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
        this.fromtime = fromtime;
        this.totime = totime;
        this.color = color;
        this.editable = editable;
    }

    public void setMoreInfos(String value) {
        moreInfos = value;
    }

    public String getMoreInfos() {
        return moreInfos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getFromTime() {
        return fromtime;
    }

    public void setFromTime(String fromtime) {
        this.fromtime = fromtime;
    }

    public String getToTime() {
        return totime;
    }

    public void setToTime(String totime) {
        this.totime = totime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    @NonNull
    public String toString() {
        return subject;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean value) {
        editable = value;
    }
}
