package com.example.tuti;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;


public class Utils {
    public static int NUM_ACTIONS = 5;

    /*
    Get time from string, exp: 11h22 ngay 19/11/2020
     */
    public static long getTimeFromString(String strTime) {
        Calendar calendar = Calendar.getInstance();

        strTime = strTime.replaceAll("\\s+", "");
        strTime = strTime.replaceAll("ngày", " ");

        String time = spliteString(strTime, " ", "index", "front");
        String date = spliteString(strTime, " ", "index", "back");

        String dayStr = spliteString(date, "/", "index", "front");
        String monthAndYear = spliteString(date, "/", "index", "back");

        String monthStr = spliteString(monthAndYear, "/", "index", "front");
        String yearStr = spliteString(monthAndYear, "/", "index", "back");

        String hourStr = spliteString(time, ":", "index", "front");
        String minuteStr = spliteString(time, ":", "index", "back");

        int currentYear = Integer.valueOf(yearStr.trim()).intValue();
        int currentMonth = Integer.valueOf(monthStr.trim()).intValue() - 1;
        int currentDay = Integer.valueOf(dayStr.trim()).intValue();
        int currentHour = Integer.valueOf(hourStr.trim()).intValue();
        int currentMinute = Integer.valueOf(minuteStr.trim()).intValue();

        calendar.set(currentYear, currentMonth, currentDay, currentHour,
                currentMinute);
        return calendar.getTime().getTime();
    }

    public static String spliteString(String srcStr, String pattern,
                                      String indexOrLast, String frontOrBack) {
        String result = "";
        int loc = -1;
        if (indexOrLast.equalsIgnoreCase("index")) {
            loc = srcStr.indexOf(pattern);
        } else {
            loc = srcStr.lastIndexOf(pattern);
        }
        if (frontOrBack.equalsIgnoreCase("front")) {
            if (loc != -1)
                result = srcStr.substring(0, loc);
        } else {
            if (loc != -1)
                result = srcStr.substring(loc + 1, srcStr.length());
        }
        return result;
    }

    public static float calcTimeDuration(long timeStart, long timeEnd, String mode) {
        long duration = timeEnd - timeStart;
        if (mode.equals("h")) {
            return (float) duration / (1000 * 60 * 60);
        } else if (mode.equals("m")) {
            return (float) duration / (1000 * 60);
        } else if (mode.equals("s")) {
            return (float) duration / (1000);
        }
        return 0;
    }

    public static boolean checkTimeInToday(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DAY_OF_MONTH);

        return checkTimeInDay(time, year, month, date);
    }

    public static boolean checkTimeInDay(long time, int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        int yearr = calendar.get(Calendar.YEAR);

        if (year == calendar.get(Calendar.YEAR) && month == calendar.get(Calendar.MONTH) && date == calendar.get(Calendar.DAY_OF_MONTH))
            return true;
        else
            return false;
    }

    public static String getActionName(int actionType) {
        Context context = MainActivity.getMainActivityContext();
        Resources res = context.getResources();

        String strAction = "Nothing";
        switch (actionType) {
            case 0:
                return res.getString(R.string.ACTION_EAT);
            case 1:
                return res.getString(R.string.ACTION_SLEEP);
            case 2:
                return res.getString(R.string.ACTION_CRY);
            case 3:
                return res.getString(R.string.ACTION_RELAX);
            case 4:
                return res.getString(R.string.ACTION_TAKE_BATH);
            default:
                strAction = res.getString(R.string.NO_ACTION_RECORDED);
                break;
        }
        return strAction;
    }

    public static int getColorType(int actionType) {
        int color = Color.argb(255, 0, 0, 250);
        switch (actionType) {
            case 0:
                color = Color.argb(255, 205, 201, 201);
                break;
            case 1:
                color = Color.argb(255, 255, 0, 0);
                break;
            case 2:
                color = Color.argb(255, 255, 255, 0);
                break;
            case 3:
                color = Color.argb(255, 0, 0, 250);
                break;
            case 4:
                color = Color.argb(255, 78, 238, 148);
                break;
            default:
                color = Color.argb(255, 0, 0, 0);
                break;
        }
        return color;
    }

    public static Bitmap getActionBitMap(int actionType) {
        Context context = MainActivity.getMainActivityContext();
        Resources res = context.getResources();
        Bitmap bm;
        switch (actionType) {
            case 0:
                bm = BitmapFactory.decodeResource(res, R.drawable.drink);
                break;
            case 1:
                bm = BitmapFactory.decodeResource(res, R.drawable.sleep);
                break;
            case 2:
                bm = BitmapFactory.decodeResource(res, R.drawable.cry);
                break;
            case 3:
                bm = BitmapFactory.decodeResource(res, R.drawable.relax);
                break;
            case 4:
                bm = BitmapFactory.decodeResource(res, R.drawable.bath);
                break;
            default:
                bm = BitmapFactory.decodeResource(res, R.drawable.smile);
        }
        return bm;
    }

    public static JSON filterActionByToday(JSON jActionList) {
        JSON filteredAction = JSON.create(JSON.array());
        boolean isFirst = true;
        for (int i = 0; i < jActionList.count(); i++) {
            JSON jInter = jActionList.index(i);
            long timeStart = jInter.key("time_start").longValue();
            if (checkTimeInToday(timeStart)) {
                if (i > 1 && isFirst == true) {
                    isFirst = false;
                    JSON lastAction = jActionList.index(i - 1);
                    if (timeStart - lastAction.key("time_start").longValue() < 6 * 60 * 60 * 1000)
                        filteredAction.add(jActionList.index(i - 1));
                }
                filteredAction.add(jInter);
            }
        }
        return filteredAction;
    }

    public static String readFileFromAppDataFolder(String fileName) {
        Context context = MainActivity.getMainActivityContext();
        File dir = context.getExternalFilesDir(null);
        File rFile = new File(dir, fileName);

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(rFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }


    }

    public static boolean isFileOnAppDataFolderExist(String fileName) {
        Context context = MainActivity.getMainActivityContext();
        File dir = context.getExternalFilesDir(null);
        File file = new File(dir, fileName);
        return file.exists();
    }

    public static boolean saveFileToAppDataFolder(String fileName, String strData) {
        Context context = MainActivity.getMainActivityContext();
        FileOutputStream outStr = null;
        File dir = context.getExternalFilesDir(null);
        boolean success = true;
        if (!dir.exists())
            success = dir.mkdir();
        if (success) {
            File dest = new File(dir, fileName);
            try {
                outStr = new FileOutputStream(dest);
                //write here:
                outStr.write(strData.getBytes());

            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            } finally {
                if (outStr != null) {
                    try {
                        outStr.close();
                    } catch (IOException e) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void showToast(String strToast) {
        final String toast = strToast;
        final Context context = MainActivity.getMainActivityContext();
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getSettingValueFromFile( String fileName, String strField) {
        String strJson = Utils.readFileFromAppDataFolder("app_setting.json");
        JSON jRoot = new JSON(strJson);
        //Load data from json file:
        String settingValue = jRoot.key(strField).stringValue();
        if (settingValue.equals(""))
            return "unknown";
        else
            return settingValue;
    }

    public static boolean updateSettingFile( String fileName, String strField, String strValue) {
        String strJson = Utils.readFileFromAppDataFolder(fileName);
        JSON jRoot = new JSON(strJson);
        JSON target = jRoot.key(strField);
        if (target != null)
            jRoot.remove(target);
        jRoot.addEditWithKey(strField, strValue);
        //Save to file:
        return Utils.saveFileToAppDataFolder(fileName, jRoot.toString());
    }
}