package com.example.tuti;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class WebServerConnect {

    private static final String TAG = "TuTi_Service";

    WebServerCallback callback = null;
    AlertDialog loadingDialog = null;
    int m_statisticViewMode = 1; //0: all; 1: today; 2: from_to

    public void setWebServerCallback(WebServerCallback callback) {
        this.callback = callback;
    }


    public void checkUpdateWithServer() {
        final String postUrl = "https://tuti-webserver.herokuapp.com/update";
        //final String postBodyText = Utils.readFileFromAppDataFolder("current_version.json");
        String versionName = BuildConfig.VERSION_NAME;
        String jVersionName = "{\"current_version\": \"" + versionName + "\"}";

        Thread thread = new Thread() {
            @Override
            public void run() {
                sendPostRequest(postUrl, jVersionName, false, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Something went wrong
                        Utils.showToast("Connect to update server failed! " + e.toString());
                        if (loadingDialog != null)
                            loadingDialog.dismiss();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (loadingDialog != null)
                            loadingDialog.dismiss();
                        if (response.isSuccessful()) {
                            try {
                                String strVersion = response.body().string();
                                Log.i(TAG, "Received message from update server: " + strVersion);
                                if (strVersion.equals("client version is latest")) {
                                    //no update:
                                    Utils.showToast("Bạn đang sử dụng phiên bản mới nhất của phần mềm");
                                } else {
                                    Utils.showToast("Có phiên bản mới hơn: " + strVersion);
                                    askGetUpdate(strVersion);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Request not successful
                            Log.i(TAG, "Failed connect to update server!");
                            Utils.showToast("Can not check update, Unknown error!");
                        }
                    }
                });
            }
        };
        thread.start();
        if (loadingDialog == null)
            loadingDialog = initProgressDialog();
        loadingDialog.show();
    }

    private void askGetUpdate(String newVersion) {
        final AlertDialog.Builder downloadArletDialog = new AlertDialog.Builder(MainActivity.getMainActivityContext());

        String jsonVersion = Utils.readFileFromAppDataFolder("current_version.json");
        String localVersion = BuildConfig.VERSION_NAME;

        downloadArletDialog.setMessage("Có phiên bản mới hơn của phần mềm, bạn có muốn tải về? \n" +
                "phiên bản hiện tại: " + localVersion + "\n" +
                "phiên bản mới: " + newVersion);
        downloadArletDialog.setCancelable(false);

        downloadArletDialog.setPositiveButton(
                "Chấp nhận",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Utils.showToast("Đang tải file, kiểm tra tiến trình trên thanh tác vụ");
                        String postUrl = "https://tuti-webserver.herokuapp.com/download-apk";
                        if (callback != null)
                            callback.OnRequestDownloadApk(postUrl);
                    }
                });

        downloadArletDialog.setNegativeButton(
                "Hủy bỏ",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        Handler mainHandler = new Handler(MainActivity.getMainActivityContext().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                //Show confirm dialog:
                AlertDialog alert11 = downloadArletDialog.create();
                downloadArletDialog.show();
            }
        };
        mainHandler.post(myRunnable);
    }

    /*
    parrams:
    -strRequest:
        NEW_ACTION
        INSERT_ACTION
        MODIFY_ACTION
        DELETE_ACTION
     id, type, timeStart, strDes
     insert_to: default: 0
     */
    public void sendActionRequest(String strRequest, int id, int type, long timeStart, String strDes, int insertTo) {
        final String postUrl = "https://tuti-webserver.herokuapp.com/sync-with-server";
        Formatter strFormat = new Formatter();
        strFormat.format("{ \"client\": \"tuan.na3\", " +
                "\"REQUEST\": \"%s\", " +
                "\"DATA\": { " +
                "\"user_id\": 0, " +
                "\"action_id\": %d, " +
                "\"action_type\": %d, " +
                "\"time_start\": %d, " +
                "\"description\": \"%s\", " +
                "\"insert_to\": %d } }", strRequest, id, type, timeStart, strDes == null ? "" : strDes, insertTo);

        String strJson = strFormat.toString();
        Log.i(TAG, strJson);

        Thread thread = new Thread() {
            @Override
            public void run() {
                sendPostRequest(postUrl, strJson, false, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Something went wrong
                        if (loadingDialog != null)
                            loadingDialog.dismiss();
                        Utils.showToast("Connect to sync server failed! " + e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (loadingDialog != null)
                            loadingDialog.dismiss();

                        if (response.isSuccessful()) {
                            //Utils.showToast("Sync with server completed.");
                            String strResponse = response.body().string();
                            getDataFromServerResponse(strResponse);

                        } else {
                            // Request not successful
                            Log.i(TAG, "Failed to Connect to sync server!");
                            Utils.showToast("Can not sync with server, Unknown error!");
                        }
                    }
                });
            }
        };
        thread.start();
        if (loadingDialog == null)
            loadingDialog = initProgressDialog();
        loadingDialog.show();
    }

    public void syncWithServer() {
        final String postUrl = "https://tuti-webserver.herokuapp.com/sync-with-server";
        String strLocalJson = Utils.readFileFromAppDataFolder("data_storage.json");
        long nVer = 0;
        if (strLocalJson != null) {
            JSON jRecordDaily = new JSON(strLocalJson);
            nVer = jRecordDaily.key("time_record").longValue();
        }
        Log.i(TAG, String.valueOf(nVer));

        Formatter strFormat = new Formatter();
        strFormat.format("{ \"client\": \"tuan.na3\", " +
                "\"REQUEST\": \"GET_SYNC_DATA\", " +
                "\"DATA\": {\"time_record\": %d,\"user_id\": 0, \"statistic_mode\": %d, \"time_from\": %d, \"time_to\": %d} }", nVer, m_statisticViewMode, 0, 0);
        String strJson = strFormat.toString();
        Log.i(TAG, strJson);
        Thread thread = new Thread() {
            @Override
            public void run() {
                sendPostRequest(postUrl, strJson, false, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Something went wrong
                        if (loadingDialog != null)
                            loadingDialog.dismiss();
                        Utils.showToast("Connect to sync server failed! " + e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (loadingDialog != null)
                            loadingDialog.dismiss();

                        if (response.isSuccessful()) {
                            try {
                                Log.i(TAG, "Received data from sync server!");
                                String strResponse = response.body().string();
                                getDataFromServerResponse(strResponse);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Request not successful
                            Log.i(TAG, "Failed to Connect to sync server!");
                            Utils.showToast("Can not sync with server, Unknown error!");
                        }
                    }
                });
            }
        };
        thread.start();

        if (loadingDialog == null)
            loadingDialog = initProgressDialog();
        loadingDialog.show();
    }

    void getDataFromServerResponse(String strResponse) {
        JSON jResponse = new JSON(strResponse);
        String result = jResponse.key("result").stringValue();
        if (result.equals("success")) {
            JSON jData = jResponse.key("data");
            boolean ret = Utils.saveFileToAppDataFolder("data_storage.json", jData.toString());
            if (ret) {
                long nVersion = jData.key("time_record").longValue();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(nVersion));
                String strVer = (String) android.text.format.DateFormat.format("dd/mm/yyyy HH:mm:ss", calendar.getTime());

                Log.i(TAG, "Sync with server completed: data version: " + strVer);
                Utils.showToast("Sync data from server completed. Data version: " + strVer);
                if (callback != null)
                    callback.OnWebServerResposed();

            }
        } else {
            Log.i(TAG, "SyncData completed, local  data verson is latest");
            Utils.showToast("Local  data verson is latest, not needed to sync with server");
        }
    }

    Call sendPostRequest(String url, String json, boolean reconnect, Callback callback) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, json);

        OkHttpClient client = null;
        if (reconnect == false)
            client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        else
            client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);

        return call;
    }

    public AlertDialog initProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getMainActivityContext());
        builder.setCancelable(false); // if you want user to wait for some process to finish,
        builder.setView(R.layout.loading_dialog);
        AlertDialog dialog = builder.create();
        return dialog;
    }

    /*
    statistic mode: 1: today
                    0: all
     */
    public void changeStatisticMode(int statisticViewMode) {
        m_statisticViewMode = statisticViewMode;
        syncWithServer();
    }
}
