package com.example.tuti;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SumaryActivity extends AppCompatActivity {

    private static final String TAG = "TuTi";
    //String strActivityType = "unknown";
    private int min_distance = 100;
    private float fstX, sndX;
    private int m_year = 0, m_month = 0, m_date = 0;

    PieChart m_chart;
    private static final String m_settingFileName = "app_setting.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sumary);
        //action bar:
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setup chart:
        m_chart = findViewById(R.id.piechart);
        m_chart.setHighlightPerTapEnabled(true);
        m_chart.setCenterText("Easy Tuti");
        m_chart.setCenterTextSize(10);
        m_chart.animateXY(500, 500);
        m_chart.setRotationEnabled(false);
        //Event handler:
        m_chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        fstX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        sndX = event.getX();
                        float deltaX = sndX - fstX;
                        if (Math.abs(deltaX) > min_distance) {
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.YEAR, m_year);
                            c.set(Calendar.MONTH, m_month);
                            c.set(Calendar.DAY_OF_MONTH, m_date);
                            if (sndX > fstX) {
                                c.add(Calendar.DAY_OF_MONTH, -1);  // number of days to add
                                showSumary(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                            } else {
                                Calendar today = Calendar.getInstance();
                                today.setTime(new Date());
                                if (c.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR)) {
                                    c.add(Calendar.DAY_OF_MONTH, 1);  // number of days to add
                                    showSumary(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                                }
                            }

                        }
                        break;
                }
                return false;
            }
        });

        showSumary(m_year, m_month, m_date);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Get back data if needed:
        String strMode = Utils.getSettingValueFromFile(m_settingFileName, "statistic_mode");
        if (!strMode.equals("All")) {
            WebServerConnect webService = new WebServerConnect();
            webService.setWebServerCallback(new WebServerCallback() {
                @Override
                public void OnWebServerResposed() {
                    //Do nothing
                }

                @Override
                public boolean OnRequestDownloadApk(String linkFile) {
                    return false;
                }
            });
            webService.syncWithServer(StatisticMode.Today); //get today data only
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSumary(int year, int month, int date) {
        //get date time:
        Calendar calendar = Calendar.getInstance();
        if (year == 0 && month == 0 && date == 0) {
            calendar.setTime(new Date());
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            date = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, date);
            calendar.set(Calendar.HOUR, 1);
        }
        m_year = year;
        m_month = month;
        m_date = date;

        ArrayList<PieEntry> actionTimeList = new ArrayList<PieEntry>();
        Map<String, Float> actionTimeMap = sumaryDataByDayFromJsonFile(year, month, date);
        if (actionTimeMap == null) {
            m_chart.setCenterText("Không có dữ liệu hoạt động trong ngày " + (String) android.text.format.DateFormat.format("dd/MM/yyyy", calendar.getTime()));
            actionTimeList.add(new PieEntry(0, "Không có dữ liệu hoạt động trong ngày"));
        } else {
            m_chart.setCenterText((String) android.text.format.DateFormat.format("dd/MM/yyyy", calendar.getTime()));
            for (Object key : actionTimeMap.keySet()) {
                Object value = actionTimeMap.get(key);
                actionTimeList.add(new PieEntry((Float) value, (String) key));
            }
        }

        PieDataSet dataSet = new PieDataSet(actionTimeList, "");
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);

        m_chart.setData(data);
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.notifyDataSetChanged();

        String strDay = (String) android.text.format.DateFormat.format("dd/MM/yyyy", calendar.getTime());
        m_chart.getDescription().setText("Thống kê hoạt động trong ngày " + strDay);
        m_chart.getDescription().setTextSize(12f);

        m_chart.getData().notifyDataChanged();
        m_chart.notifyDataSetChanged();
        m_chart.invalidate(); // refreshes chart
    }

    private JSON loadJsonFromFile() {
        String strFileName = "data_storage.json";
        String strJson = Utils.readFileFromAppDataFolder(strFileName);
        JSON jRoot = new JSON(strJson);
        return jRoot;
    }

    private Map sumaryDataByDayFromJsonFile(int year, int month, int date) {
        Map<String, Float> actionTimeList = new HashMap<String, Float>();
        //Init:
        for (int actionType = 0; actionType < Utils.NUM_ACTIONS; actionType++)
            actionTimeList.put(Utils.getActionName(actionType), 0.0f);

        //Load data from json file:
        JSON jRoot = loadJsonFromFile();
        JSON jActionList = jRoot.key("action_list");

        int actionCount = 0;

        for (int i = 0; i < jActionList.count(); i++) {
            JSON action = jActionList.index(i);
            int actionType = action.key("action_type").intValue();
            long timeStart = action.key("time_start").longValue();

            long timeEnd = -1;
            if (i < jActionList.count() - 1)
                timeEnd = jActionList.index(i + 1).key("time_start").longValue();

            //Handle last action
            if (timeEnd <= 0)
                timeEnd = new Date().getTime();
            if (!Utils.checkTimeInDay(timeStart, year, month, date) && !Utils.checkTimeInDay(timeEnd, year, month, date))
                continue;
            else if ((!Utils.checkTimeInDay(timeStart, year, month, date) && Utils.checkTimeInDay(timeEnd, year, month, date)) ||
                    (Utils.checkTimeInDay(timeStart, year, month, date) && !Utils.checkTimeInDay(timeEnd, year, month, date))) {
                //yesterday to today
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(timeEnd));
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                timeStart = calendar.getTime().getTime();
            }
            //Calculator:
            Float totalTime = actionTimeList.get(Utils.getActionName(actionType));
            float timeElapsed = Utils.calcTimeDuration(timeStart, timeEnd, "h");
            if (timeElapsed < 6) //prevent issue about long time user do not use this app
                totalTime += timeElapsed;
            actionTimeList.put(Utils.getActionName(actionType), totalTime);
            actionCount++;
            Log.i(TAG, Utils.getActionName(actionType) + ": " + totalTime);
        }
        if (actionCount > 0)
            return actionTimeList;
        else
            return null;
    }
}