package com.example.alarm_crash;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "AlarmCrash";

    private ListView mAlarmList;
    private AlarmListAdapter mAlarmListAdapter;
    private Alarm mCurrentAlarm;

    private final int NEW_ALARM_ACTIVITY = 0;
    private final int EDIT_ALARM_ACTIVITY = 1;
    private final int PREFERENCES_ACTIVITY = 2;
    private final int ABOUT_ACTIVITY = 3;

    private final int CONTEXT_MENU_EDIT = 0;
    private final int CONTEXT_MENU_DELETE = 1;
    private final int CONTEXT_MENU_DUPLICATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddAlarmClick(view);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ImageButton btnSetting = (ImageButton) findViewById(R.id.imageButton);
        btnSetting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(v.getContext(), "click setting", Toast.LENGTH_SHORT);
            }
        });

        TextView textCurrentTime = (TextView) findViewById(R.id.textCurrentTime);
        textCurrentTime.setText((new SimpleDateFormat("HH:mm")).format(new Date()));

        TextView textCurrentDay = (TextView) findViewById(R.id.textCurrentDay);
        textCurrentDay.setText((new SimpleDateFormat("ddd, MMM dd, yyyy")).format(new Date()));

        mAlarmList = (ListView)findViewById(R.id.alarm_list);

        mAlarmListAdapter = new AlarmListAdapter(this);
        mAlarmList.setAdapter(mAlarmListAdapter);
        mAlarmList.setOnItemClickListener(mListOnItemClickListener);
        registerForContextMenu(mAlarmList);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i(TAG, "AlarmMe.onResume()");
        mAlarmListAdapter.updateAlarms();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == NEW_ALARM_ACTIVITY)
        {
            if (resultCode == RESULT_OK)
            {
                mCurrentAlarm.fromIntent(data);
                mAlarmListAdapter.add(mCurrentAlarm);
            }
            mCurrentAlarm = null;
        }
        else if (requestCode == EDIT_ALARM_ACTIVITY)
        {
            if (resultCode == RESULT_OK)
            {
                mCurrentAlarm.fromIntent(data);
                mAlarmListAdapter.update(mCurrentAlarm);
            }
            mCurrentAlarm = null;
        }
        else if (requestCode == PREFERENCES_ACTIVITY)
        {
            mAlarmListAdapter.onSettingsUpdated();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (R.id.menu_settings == item.getItemId())
        {
            Intent intent = new Intent(getBaseContext(), Preferences.class);
            startActivityForResult(intent, PREFERENCES_ACTIVITY);
            return true;
        }
        else if (R.id.menu_about == item.getItemId())
        {
            Intent intent = new Intent(getBaseContext(), About.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if (v.getId() == R.id.alarm_list)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            menu.setHeaderTitle(mAlarmListAdapter.getItem(info.position).getTitle());
            menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, "Edit");
            menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, "Delete");
            menu.add(Menu.NONE, CONTEXT_MENU_DUPLICATE, Menu.NONE, "Duplicate");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int index = item.getItemId();

        if (index == CONTEXT_MENU_EDIT)
        {
            Intent intent = new Intent(getBaseContext(), EditAlarm.class);

            mCurrentAlarm = mAlarmListAdapter.getItem(info.position);
            mCurrentAlarm.toIntent(intent);
            startActivityForResult(intent, EDIT_ALARM_ACTIVITY);
        }
        else if (index == CONTEXT_MENU_DELETE)
        {
            mAlarmListAdapter.delete(info.position);
        }
        else if (index == CONTEXT_MENU_DUPLICATE)
        {
            Alarm alarm = mAlarmListAdapter.getItem(info.position);
            Alarm newAlarm = new Alarm(this);
            Intent intent = new Intent();

            alarm.toIntent(intent);
            newAlarm.fromIntent(intent);
            newAlarm.setTitle(alarm.getTitle() + " (copy)");
            mAlarmListAdapter.add(newAlarm);
        }

        return true;
    }

    public void onAddAlarmClick(View view)
    {
        Intent intent = new Intent(getBaseContext(), EditAlarm.class);

        mCurrentAlarm = new Alarm(this);
        mCurrentAlarm.toIntent(intent);

        startActivityForResult(intent, NEW_ALARM_ACTIVITY);
    }

    private AdapterView.OnItemClickListener mListOnItemClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Intent intent = new Intent(getBaseContext(), EditAlarm.class);

            mCurrentAlarm = mAlarmListAdapter.getItem(position);
            mCurrentAlarm.toIntent(intent);
            startActivityForResult(intent, EDIT_ALARM_ACTIVITY);
        }
    };
}
