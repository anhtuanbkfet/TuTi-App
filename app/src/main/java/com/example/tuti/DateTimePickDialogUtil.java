package com.example.tuti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DateTimePickDialogUtil implements OnDateChangedListener, OnTimeChangedListener {
    private DatePicker datePicker;
    private TimePicker timePicker;
    private AlertDialog ad;
    private String strDateTime;
    private String initDateTime;
    private Activity activity;
    private Resources res;

    public DateTimePickDialogUtil(Activity activity, String initDateTime) {
        this.activity = activity;
        this.initDateTime = initDateTime;
        res = this.activity.getResources();
    }

    public void init(DatePicker datePicker, TimePicker timePicker) {
        Calendar calendar = Calendar.getInstance();
        if (!(null == initDateTime || "".equals(initDateTime))) {
            calendar.setTime(new Date(Utils.getTimeFromString(initDateTime)));
        } else {
            initDateTime = res.getString(R.string.DATE_AND_TIME,
                    (String) android.text.format.DateFormat.format(" HH:mm", new Date()),
                    (String) android.text.format.DateFormat.format("dd/MM/yyyy", new Date()));

        }

        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), this);
        DateTime dateTime = new DateTime(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                0,
                0);
        //datePicker.setMinDate(dateTime.getMillis());
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));
    }

    public AlertDialog dateTimePickDialog(final EditText inputDate) {

        LinearLayout dateTimeLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.datetime_picker, null);
        datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.datepicker);
        timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.timepicker);
        resizePicker(datePicker);
        resizePicker(timePicker);

        init(datePicker, timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(this);

        ad = new AlertDialog.Builder(activity)
                .setTitle(initDateTime)
                .setView(dateTimeLayout)
                .setPositiveButton(res.getString(R.string.BTN_OK),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                datePicker.clearFocus();
                                timePicker.clearFocus();

                                updateTimeText();
                                inputDate.setText(strDateTime);
                            }
                        })
                .setNegativeButton(res.getString(R.string.BTN_CANCEL), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

        //onDateChanged(null, 0, 0, 0);
        return ad;
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        updateTimeText();
    }

    public void onDateChanged(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
        updateTimeText();
    }

    private void updateTimeText() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(),
                datePicker.getDayOfMonth(), timePicker.getHour(),
                timePicker.getMinute());
        strDateTime = res.getString(R.string.DATE_AND_TIME,
                (String) android.text.format.DateFormat.format(" HH:mm", calendar.getTime()),
                (String) android.text.format.DateFormat.format("dd/MM/yyyy", calendar.getTime()));
        if (ad != null)
            ad.setTitle(strDateTime);
    }

    private void resizePicker(FrameLayout tp) {
        List<NumberPicker> npList = findNumberPicker(tp);
        for (int i = 0; i < npList.size(); i++) {
            NumberPicker np = npList.get(i);
            if (i == 2)
                resizeNumberPicker(np, true);
            else
                resizeNumberPicker(np, false);
        }
    }

    private List<NumberPicker> findNumberPicker(ViewGroup viewGroup) {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;
        if (null != viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker) {
                    npList.add((NumberPicker) child);
                } else if (child instanceof LinearLayout) {
                    List<NumberPicker> result = findNumberPicker((ViewGroup) child);
                    if (result.size() > 0) {
                        return result;
                    }
                }
            }
        }
        return npList;
    }

    private void resizeNumberPicker(NumberPicker np, boolean isYearPicker) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int pickerWidth = screenWidth / 7;
        if (isYearPicker)
            pickerWidth *= 1.35;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                pickerWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        np.setLayoutParams(params);
    }

}
