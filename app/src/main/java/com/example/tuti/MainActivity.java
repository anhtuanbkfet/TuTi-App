package com.example.tuti;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TuTi";
    private static final String m_dataFileName = "data_storage.json";
    private static final String m_settingFileName = "app_setting.json";
    private Resources res;
    private static Context mContext;
    private DownloadManager m_downloadManager;

    private AlertDialog m_actionsDialog;
    private AlertDialog m_actionModifyDialog;
    private Boolean m_isDialogShow = false;
    private int m_lastActionID = 0;

    private ArrayAdapter<TimelineRow> m_Adapter;
    private ListView m_ListView;
    private ArrayList<TimelineRow> m_timelineRowsList = new ArrayList<TimelineRow>();
    private JSON m_jActionList = new JSON(null);
    private StatisticMode m_statisticViewMode = StatisticMode.Today; //0: all; 1: only today


    public static Context getMainActivityContext() {
        return mContext;
    }

    public class GenericFileProvider extends FileProvider {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        res = this.getResources();

        getSupportActionBar().setDisplayUseLogoEnabled(true);

        createTimeLine();
        createJsonDataFile(m_dataFileName);
        createJsonDataFile(m_settingFileName);

        String strMode = Utils.getSettingValueFromFile(m_settingFileName, "statistic_mode");
        if (strMode.equals("All")) {
            m_statisticViewMode = StatisticMode.All;

        } else{
            m_statisticViewMode = StatisticMode.Today;
            Utils.updateSettingFile(m_settingFileName, "statistic_mode", "Today");
        }


        //Sync with server:
        WebServerConnect webService = new WebServerConnect();
        webService.setWebServerCallback(new WebServerCallback() {
            @Override
            public void OnWebServerResposed() {
                Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //Update listView:
                        loadJsonData(m_dataFileName);
                        updateListViewFromJsonList(m_jActionList);
                    }
                };
                mainHandler.post(myRunnable);
            }

            @Override
            public boolean OnRequestDownloadApk(String linkFile) {
                return false;
            }
        });
        webService.syncWithServer(m_statisticViewMode);

        loadJsonData(m_dataFileName);
        updateListViewFromJsonList(m_jActionList);

        //Download manager:
        m_downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.option_statistic_mode);
        if (m_statisticViewMode == StatisticMode.All) {
            item.setTitle(res.getString(R.string.OPTION_STATISTIC_TODAY));
        } else {
            item.setTitle(res.getString(R.string.OPTION_STATISTIC_ALL));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.option_view_chart: {

                //Get data if needed:
                String strMode = Utils.getSettingValueFromFile(m_settingFileName, "statistic_mode");
                if (!strMode.equals("All")) {
                    WebServerConnect webService = new WebServerConnect();
                    webService.setWebServerCallback(new WebServerCallback() {
                        @Override
                        public void OnWebServerResposed() {
                            Intent intent = new Intent(MainActivity.this, SumaryActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public boolean OnRequestDownloadApk(String linkFile) {
                            return false;
                        }
                    });
                    webService.syncWithServer(StatisticMode.All); //get all data
                }
                else{
                    Intent intent = new Intent(MainActivity.this, SumaryActivity.class);
                    startActivity(intent);
                }

            }
            break;
            case R.id.option_sync_data: {
                //menu sync
                WebServerConnect webService = new WebServerConnect();
                webService.setWebServerCallback(new WebServerCallback() {
                    @Override
                    public void OnWebServerResposed() {
                        Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                //Update listView:
                                loadJsonData(m_dataFileName);
                                updateListViewFromJsonList(m_jActionList);
                            }
                        };
                        mainHandler.post(myRunnable);
                    }

                    @Override
                    public boolean OnRequestDownloadApk(String linkFile) {
                        return false;
                    }
                });
                webService.syncWithServer(m_statisticViewMode);
            }
            break;
            case R.id.option_check_update: {
                WebServerConnect webService = new WebServerConnect();
                webService.setWebServerCallback(new WebServerCallback() {
                    @Override
                    public void OnWebServerResposed() {

                    }

                    @Override
                    public boolean OnRequestDownloadApk(String linkFile) {
                        MainActivity.this.registerReceiver(new BroadcastReceiver() {
                            public void onReceive(Context ctxt, Intent intent) {
                                // Open after download:
                                File file = new File(MainActivity.this.getExternalFilesDir(null), "Download/app-release.apk");
                                if (file.exists()) {
                                    Uri fileURI = FileProvider.getUriForFile(MainActivity.this,
                                            MainActivity.this.getApplicationContext().getPackageName() + ".provider", file);
                                    Log.i(TAG, "Install apk downloaded file: " + file.getPath());
                                    intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.setDataAndType(fileURI, "application/vnd.android.package-archive");
                                    startActivity(intent);
                                } else {
                                    Log.i(TAG, "File not found: " + file.getPath());
                                }

                            }
                        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(linkFile));
                        // Setting title of request
                        request.setTitle(res.getString(R.string.DOWNLOAD_UPDATE_TITLE));
                        request.setDescription(res.getString(R.string.DOWNLOAD_UPDATE_DESCRIPTION));
                        File file = new File(MainActivity.this.getExternalFilesDir(null), "Download/app-release.apk");
                        if (file.exists())
                            file.delete();
                        request.setDestinationUri(Uri.fromFile(file));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        // Enqueue download and save into referenceId
                        m_downloadManager.enqueue(request);
                        return true;
                    }
                });
                webService.checkUpdateWithServer();
            }
            break;
            case R.id.option_statistic_mode: {
                //Menu change statistic mode
                if (item.getTitle().equals(res.getString(R.string.OPTION_STATISTIC_ALL))) {
                    item.setTitle(res.getString(R.string.OPTION_STATISTIC_TODAY));
                    m_statisticViewMode = StatisticMode.All;
                } else {
                    item.setTitle(res.getString(R.string.OPTION_STATISTIC_ALL));
                    m_statisticViewMode = StatisticMode.Today;
                }

                Utils.updateSettingFile(m_settingFileName, "statistic_mode", m_statisticViewMode == StatisticMode.All?"All":"Today");

                //Sen request to server:
                WebServerConnect webService = new WebServerConnect();
                webService.setWebServerCallback(new WebServerCallback() {
                    @Override
                    public void OnWebServerResposed() {
                        Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                //Update listView:
                                loadJsonData(m_dataFileName);
                                updateListViewFromJsonList(m_jActionList);
                            }
                        };
                        mainHandler.post(myRunnable);
                    }

                    @Override
                    public boolean OnRequestDownloadApk(String linkFile) {
                        return false;
                    }
                });
                webService.syncWithServer(m_statisticViewMode);

            }
            break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void updateListViewFromJsonList(JSON jActionList) {
        m_timelineRowsList.clear();

        if (jActionList.count() == 0)
            insertNewActionToListView(makeActionRow(m_lastActionID, -1, -1, -1, null));
        else {
            for (int i = 0; i < jActionList.count(); i++) {
                JSON current_action = jActionList.index(i);
                int actionID = current_action.key("action_id").intValue();
                int actionType = current_action.key("action_type").intValue();
                long timeStart = current_action.key("time_start").longValue();
                long timeEnd = -1;
                if (jActionList.count() > 1 && i < jActionList.count() - 1) {
                    JSON next_action = jActionList.index(i + 1);
                    timeEnd = next_action.key("time_start").longValue();
                }

                String description = current_action.key("description").stringValue();
                insertNewActionToListView(makeActionRow(actionID, actionType, timeStart, timeEnd, description.equals("") ? null : description));
            }
        }
    }

    private void loadJsonData(String strFileName) {
        String strJson = Utils.readFileFromAppDataFolder(strFileName);
        JSON jRecordDaily = new JSON(strJson);
        //Load data from json file:
        m_jActionList = jRecordDaily.key("action_list");
        if (m_jActionList.count() > 0)
            m_lastActionID = m_jActionList.index(m_jActionList.count() - 1).key("action_id").intValue();
    }

    private void createJsonDataFile(String fileName) {
        boolean isExist = Utils.isFileOnAppDataFolderExist(fileName);
        if (!isExist) {
            boolean isFileCreated = Utils.saveFileToAppDataFolder(fileName, "{}");
            if (isFileCreated) {
                //proceed with storing the first todo  or show ui
                Toast.makeText(MainActivity.this, res.getString(R.string.FILE_CREATED, fileName), Toast.LENGTH_SHORT).show();
            } else {
                //show error or try again.
                Toast.makeText(MainActivity.this, res.getString(R.string.FILE_CREATED_ERROR, fileName), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(MainActivity.this, "File existed: " + fileName, Toast.LENGTH_SHORT).show();
        }
    }


    int getBellowLineSize(long timeStart, long timeEnd) {
        long size = 1;
        int maxSize = 25;
        long duration = (timeEnd - timeStart) / 1000; //count in sec
        size = duration * maxSize / (3 * 60 * 60); //1h: max size.
        return (int) (size == 0 ? 1 : (size > maxSize ? maxSize : size));
    }

    private TimelineRow updateActionRow(TimelineRow myRow, int actionType, long timeStart, long timeEnd, String strDescription) {
        myRow.setTimeBegin(timeStart);
        myRow.setTimeEnd(timeEnd);
        myRow.setTitle(Utils.getActionName(actionType));
        myRow.setDescription(strDescription);
        myRow.setImage(Utils.getActionBitMap(actionType));
        myRow.setBellowLineColor(Utils.getColorType(actionType));
        myRow.setBellowLineSize(getBellowLineSize(timeStart, timeEnd));
        myRow.setImageSize(40);
        myRow.setBackgroundColor(Utils.getColorType(actionType));
        myRow.setBackgroundSize(60);
        myRow.setDateColor(Color.argb(255, 0, 0, 0));
        myRow.setTitleColor(Color.argb(255, 0, 0, 0));
        myRow.setDescriptionColor(Color.argb(255, 0, 0, 0));
        return myRow;
    }

    private TimelineRow makeActionRow(int actionID, int actionType, long timeStart, long timeEnd, String strDescription) {
        TimelineRow myRow = new TimelineRow(actionID, actionType);
        myRow = updateActionRow(myRow, actionType, timeStart, timeEnd, strDescription);
        return myRow;
    }

    private void insertNewActionToListView(TimelineRow item) {

        m_timelineRowsList.add(item);
        m_Adapter.notifyDataSetChanged();
    }

    private void createTimeLine() {
// Create the Timeline Adapter
        m_Adapter = new TimelineViewAdapter(this, 0, m_timelineRowsList, false);
// Get the ListView and Bind it with the Timeline Adapter
        m_ListView = (ListView) findViewById(R.id.timeline_listView);
        m_ListView.setAdapter(m_Adapter);

        m_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (position == m_timelineRowsList.size() - 1)
                    insertNewActionDialog();
            }
        });
        m_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PopupMenu popup = new PopupMenu(getMainActivityContext(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.actions_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.pmnuEdit:
                                displayModifyActionDialog(position, 0);
                                break;
                            case R.id.pmnuInsert: {
                                JSON jAction = m_jActionList.index(position);
                                displayModifyActionDialog(position, 1);
                                break;
                            }
                            case R.id.pmnuDelete: {
                                //Delete last action:
                                AlertDialog.Builder deleteArletDialog = new AlertDialog.Builder(MainActivity.this);
                                deleteArletDialog.setMessage(res.getString(R.string.DELETE_ACTION_CONFIRM));
                                deleteArletDialog.setCancelable(true);

                                deleteArletDialog.setPositiveButton(
                                        res.getString(R.string.BTN_OK),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                deleteAction(position);
                                            }
                                        });

                                deleteArletDialog.setNegativeButton(
                                        res.getString(R.string.BTN_CANCEL),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alert11 = deleteArletDialog.create();
                                deleteArletDialog.show();
                                break;
                            }
                        }
                        return false;
                    }
                });
                popup.show();
                return true;
            }
        });
    }

    private void insertNewAction(int actionType, long timeStart, String strDescription, int insertTo) {
        //Submit to server:
        WebServerConnect webService = new WebServerConnect();
        webService.setWebServerCallback(new WebServerCallback() {
            @Override
            public void OnWebServerResposed() {
                Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //Update listView:
                        loadJsonData(m_dataFileName);
                        updateListViewFromJsonList(m_jActionList);
                    }
                };
                mainHandler.post(myRunnable);
            }

            @Override
            public boolean OnRequestDownloadApk(String linkFile) {
                return false;
            }
        });
        if (strDescription == null)
            strDescription = "";
        webService.sendActionRequest("INSERT_ACTION", 0, actionType, timeStart, strDescription, insertTo);
        //Toast.makeText(MainActivity.this, res.getString(R.string.ACTION_ADDED, Utils.getActionName(actionType)), Toast.LENGTH_SHORT).show();
    }

    private void insertNewAction(int actionType) {

        if (m_isDialogShow) {
            m_isDialogShow = false;
            m_actionsDialog.dismiss();
        }
        //Modify last action:
        long actionTime = new Date().getTime();
        insertNewAction(actionType, actionTime, "", 0);
    }

    private void showDateTimePickerDialog(EditText targetEditText) {

        String strInitTime = targetEditText.getText().toString();
        if (strInitTime == null)
            strInitTime = res.getString(R.string.DATE_AND_TIME,
                    (String) android.text.format.DateFormat.format(" HH:mm", new Date()),
                    (String) android.text.format.DateFormat.format("dd/MM/yyyy", new Date()));
        DateTimePickDialogUtil dateTimePicker = new DateTimePickDialogUtil(
                MainActivity.this, strInitTime);
        dateTimePicker.dateTimePickDialog(targetEditText);
    }

    void deleteAction(int position) {
        JSON jAction = m_jActionList.index(position);

        //Submit to server:
        WebServerConnect webService = new WebServerConnect();
        webService.setWebServerCallback(new WebServerCallback() {
            @Override
            public void OnWebServerResposed() {
                Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //Update listView:
                        loadJsonData(m_dataFileName);
                        updateListViewFromJsonList(m_jActionList);
                    }
                };
                mainHandler.post(myRunnable);
            }

            @Override
            public boolean OnRequestDownloadApk(String linkFile) {
                return false;
            }
        });
        webService.sendActionRequest("DELETE_ACTION", jAction.key("action_id").intValue(), 0, 0, null, 0);
    }

    /*
    - position: selected index
    - mode:   0: modify
            1: insert
     */
    public void displayModifyActionDialog(int position, int modifyMode) {
        LayoutInflater inflater = getLayoutInflater();
        View modifyActionLayout = inflater.inflate(R.layout.modify_action, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);


        final EditText tvTimeStart = (EditText) modifyActionLayout.findViewById(R.id.et_timeStart);
//        final EditText tvTimeEnd = (EditText) modifyActionLayout.findViewById(R.id.et_timeEnd);
        final EditText tvDescription = (EditText) modifyActionLayout.findViewById(R.id.et_description);
        final Spinner spnAction = (Spinner) modifyActionLayout.findViewById(R.id.spnAction);
        final Button btnModify = (Button) modifyActionLayout.findViewById(R.id.btn_modify);
        final Button btnCancel = (Button) modifyActionLayout.findViewById(R.id.btn_cancel);

        final TimelineRow curAction = m_timelineRowsList.get(position);

        long timeStart = curAction.getTimeBegin();
        long timeEnd = curAction.getTimeEnd();
        if (timeEnd <= 0)
            timeEnd = new Date().getTime();

        String strTimeStart = res.getString(R.string.DATE_AND_TIME,
                (String) android.text.format.DateFormat.format(" HH:mm", new Date(timeStart)),
                (String) android.text.format.DateFormat.format("dd/MM/yyyy", new Date(timeStart)));
//        String strTimeEnd = res.getString(R.string.DATE_AND_TIME,
//                (String) android.text.format.DateFormat.format(" HH:mm", new Date(timeEnd)),
//                (String) android.text.format.DateFormat.format("dd/MM/yyyy", new Date(timeEnd)));
        String strDes = curAction.getDescription();

        tvTimeStart.setText(strTimeStart);
//        tvTimeEnd.setText(strTimeEnd);
        tvDescription.setText(strDes);

        List<String> actionList = new ArrayList<String>();
        for (int i = 0; i < Utils.NUM_ACTIONS; i++)
            actionList.add(Utils.getActionName(i));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, actionList);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spnAction.setAdapter(adapter);

        int actionType = curAction.getType();
        spnAction.setSelection(actionType);

        tvTimeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerDialog(tvTimeStart);
            }
        });

//        tvTimeEnd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDateTimePickerDialog(tvTimeEnd);
//            }
//        });

        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeStart = Utils.getTimeFromString(tvTimeStart.getText().toString());
//                long timeEnd = Utils.getTimeFromString(tvTimeEnd.getText().toString());
                String strDes = tvDescription.getText().toString();

                try {
                    if (modifyMode == 0) {
                        //Modify action
                        modifyAction(curAction.getId(), (int) spnAction.getSelectedItemId(), timeStart, strDes);
                    } else {
                        //insert new action:
                        insertNewAction((int) spnAction.getSelectedItemId(), timeStart, strDes, curAction.getId());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                m_actionModifyDialog.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_actionModifyDialog.dismiss();
            }
        });
        if (modifyMode == 0)
            alert.setTitle(res.getString(R.string.MODIFY_ACTION));
        else
            alert.setTitle(res.getString(R.string.CHOOSE_ACTION_TO_INSERT));
        alert.setView(modifyActionLayout);
        alert.setCancelable(true);

        m_actionModifyDialog = alert.create();
        m_actionModifyDialog.show();
    }

    private void modifyAction(int actionID, int actionType, long timeStart, String strDescription) throws JSONException {
        //sync to server:
        WebServerConnect webService = new WebServerConnect();
        webService.setWebServerCallback(new WebServerCallback() {
            @Override
            public void OnWebServerResposed() {
                Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //Update listView:
                        loadJsonData(m_dataFileName);
                        updateListViewFromJsonList(m_jActionList);
                    }
                };
                mainHandler.post(myRunnable);
            }

            @Override
            public boolean OnRequestDownloadApk(String linkFile) {
                return false;
            }
        });
        webService.sendActionRequest("MODIFY_ACTION", actionID, actionType, timeStart, strDescription, 0);
    }

    public void insertNewActionDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.choose_action, null);

        final Button btnEat = (Button) alertLayout.findViewById(R.id.btn_eat);
        final Button btnSleep = (Button) alertLayout.findViewById(R.id.btn_sleep);
        final Button btnCry = (Button) alertLayout.findViewById(R.id.btn_cry);
        final Button btnRelax = (Button) alertLayout.findViewById(R.id.btn_relax);
        final Button btnTakeBath = (Button) alertLayout.findViewById(R.id.btn_takebath);

        btnEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewAction(0);
            }
        });

        btnSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewAction(1);
            }
        });

        btnCry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewAction(2);
            }
        });

        btnRelax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewAction(3);
            }
        });

        btnTakeBath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewAction(4);
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(res.getString(R.string.CHOOSE_ACTION));
        alert.setView(alertLayout);
        alert.setCancelable(true);

        m_actionsDialog = alert.create();
        m_isDialogShow = true;
        m_actionsDialog.show();
    }
}
