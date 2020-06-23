package com.example.tuti;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class TimelineViewAdapter extends ArrayAdapter<TimelineRow> {

    private Context context;
    private Resources res;
    private List<TimelineRow> RowDataList;
    private Boolean needUpdate = true;


    public TimelineViewAdapter(Context context, int resource, ArrayList<TimelineRow> objects, boolean orderTheList) {
        super(context, resource, objects);
        this.context = context;
        res = context.getResources();
        if (orderTheList)
            this.RowDataList = rearrangeByDate(objects);
        else
            this.RowDataList = objects;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        needUpdate = true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (needUpdate == false && convertView != null)
            return convertView;

        TimelineRow currentRow = RowDataList.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.ctimeline_row, null);

        TextView rowTitle = (TextView) view.findViewById(R.id.crowTitle);
        TextView rowDate = (TextView) view.findViewById(R.id.crowDate);
        TextView rowDuration = (TextView) view.findViewById(R.id.crowDuration);
        TextView rowDescription = (TextView) view.findViewById(R.id.crowDesc);
        ImageView rowImage = (ImageView) view.findViewById(R.id.crowImg);
        View rowUpperLine = (View) view.findViewById(R.id.crowUpperLine);
        View rowLowerLine = (View) view.findViewById(R.id.crowLowerLine);


        final float scale = getContext().getResources().getDisplayMetrics().density;


        if (position == 0 && position == RowDataList.size() - 1) {
            rowUpperLine.setVisibility(View.INVISIBLE);
            rowLowerLine.setVisibility(View.INVISIBLE);
        } else if (position == 0) {
            int pixels = (int) (currentRow.getBellowLineSize() * scale + 0.5f);

            rowUpperLine.setVisibility(View.INVISIBLE);
            rowLowerLine.setBackgroundColor(currentRow.getBellowLineColor());
            rowLowerLine.getLayoutParams().width = pixels;
        } else if (position == RowDataList.size() - 1) {
            int pixels = (int) (RowDataList.get(position - 1).getBellowLineSize() * scale + 0.5f);

            rowLowerLine.setVisibility(View.INVISIBLE);
            rowUpperLine.setBackgroundColor(RowDataList.get(position - 1).getBellowLineColor());
            rowUpperLine.getLayoutParams().width = pixels;
        } else {
            int pixels = (int) (currentRow.getBellowLineSize() * scale + 0.5f);
            int pixels2 = (int) (RowDataList.get(position - 1).getBellowLineSize() * scale + 0.5f);

            rowLowerLine.setBackgroundColor(currentRow.getBellowLineColor());
            rowUpperLine.setBackgroundColor(RowDataList.get(position - 1).getBellowLineColor());
            rowLowerLine.getLayoutParams().width = pixels;
            rowUpperLine.getLayoutParams().width = pixels2;
        }

        if (currentRow.getType() >= 0) {
            rowDate.setText(res.getString(R.string.ACTION_START_ON, getStringTime(currentRow.getTimeBegin())));
            if (currentRow.getDateColor() != 0)
                rowDate.setTextColor(currentRow.getDateColor());

        }

        if (currentRow.getTitle() == null)
            rowTitle.setVisibility(View.GONE);
        else {
            rowTitle.setText(currentRow.getTitle());
            if (currentRow.getTitleColor() != 0)
                rowTitle.setTextColor(currentRow.getTitleColor());
        }

        if (currentRow.getType() < 0 || position == RowDataList.size() - 1)
            rowDuration.setText(res.getString(R.string.INSERT_NEW_ACTION));
        else {
            rowDuration.setText(res.getString(R.string.ACTION_IN_PAST, getPastTime(currentRow.getTimeBegin(), currentRow.getTimeEnd())));
            if (currentRow.getDescription() == null)
                rowDescription.setText(res.getString(R.string.NO_DESCRIPTION));
            else
                rowDescription.setText(currentRow.getDescription());
            if (currentRow.getDescriptionColor() != 0)
                rowDescription.setTextColor(currentRow.getDescriptionColor());
        }
        if (currentRow.getImage() != null) {
            rowImage.setImageBitmap(currentRow.getImage());
        }

        int pixels = (int) (currentRow.getImageSize() * scale + 0.5f);
        rowImage.getLayoutParams().width = pixels;
        rowImage.getLayoutParams().height = pixels;

        View backgroundView = view.findViewById(R.id.crowBackground);
        if (currentRow.getBackgroundColor() == 0)
            backgroundView.setBackground(null);
        else {
            if (currentRow.getBackgroundSize() == -1) {
                backgroundView.getLayoutParams().width = pixels;
                backgroundView.getLayoutParams().height = pixels;
            } else {
                int BackgroundPixels = (int) (currentRow.getBackgroundSize() * scale + 0.5f);
                backgroundView.getLayoutParams().width = BackgroundPixels;
                backgroundView.getLayoutParams().height = BackgroundPixels;
            }
            GradientDrawable background = (GradientDrawable) backgroundView.getBackground();
            if (background != null) {
                background.setColor(currentRow.getBackgroundColor());
            }
        }


        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) rowImage.getLayoutParams();
        marginParams.setMargins(0, (int) (pixels / 2) * -1, 0, (pixels / 2) * -1);


        return view;
    }

    private String getStringTime(long time) {
        if (time < 0)
            return "";
        Date date = new Date(time);
        String strDateTime =
                res.getString(R.string.DATE_AND_TIME,
                        (String) android.text.format.DateFormat.format(" HH:mm", date),
                        (String) android.text.format.DateFormat.format("dd/MM/yyyy", date));
        return strDateTime;
    }

    private String getPastTime(long beginDate, long endDate) {

        if (beginDate < 0 || endDate < 0)
            return res.getString(R.string.ACTION_TIME_INVALID);
        StringBuilder dateText = new StringBuilder();
        long diff = (endDate - beginDate) / 1000;

        long years = diff / (60 * 60 * 24 * 30 * 12);
        long months = (diff / (60 * 60 * 24 * 30)) % 12;
        long days = (diff / (60 * 60 * 24)) % 30;
        long hours = (diff / (60 * 60)) % 24;
        long minutes = (diff / 60) % 60;
        long seconds = diff % 60;

        if (years > 0) {
            appendPastTime(dateText, years, R.plurals.years, months, R.plurals.months);
        } else if (months > 0) {
            appendPastTime(dateText, months, R.plurals.months, days, R.plurals.days);
        } else if (days > 0) {
            appendPastTime(dateText, days, R.plurals.days, hours, R.plurals.hours);
        } else if (hours > 0) {
            appendPastTime(dateText, hours, R.plurals.hours, minutes, R.plurals.minutes);
        } else if (minutes > 0) {
            appendPastTime(dateText, minutes, R.plurals.minutes, seconds, R.plurals.seconds);
        } else if (seconds >= 0) {
            dateText.append(res.getQuantityString(R.plurals.seconds, (int) seconds, (int) seconds));
        }

        return dateText.toString();
    }

    private void appendPastTime(StringBuilder s,
                                long timespan, int nameId,
                                long timespanNext, int nameNextId) {

        s.append(res.getQuantityString(nameId, (int) timespan, timespan));
        if (timespanNext > 0) {
            s.append(' ');
            s.append(res.getQuantityString(nameNextId, (int) timespanNext, timespanNext));
        }
    }

    private ArrayList<TimelineRow> rearrangeByDate(ArrayList<TimelineRow> objects) {
        if (objects.get(0) == null) return objects;
        int size = objects.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (objects.get(i).getTimeBegin() > 0 && objects.get(j).getTimeBegin() > 0)
                    if (objects.get(i).getTimeBegin() < objects.get(j).getTimeBegin())
                        Collections.swap(objects, i, j);
            }
        }
        return objects;
    }

}
