/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.ecare_client.checklist.CheckListActivity;
import com.example.ecare_client.magnifier.MainActivity;

public class ToolboxActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tool_list);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
        Intent intent = getIntent();
        TitleLayout titleLayout = (TitleLayout) findViewById(R.id.tool_box_title);
        titleLayout.setTitleText("ToolBox");



        ImageView magnifier = (ImageView) findViewById(R.id.btnOpenMagnifier);
        magnifier.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        openMagnifier();
                }
                return false;

            }
        });

        ImageView userSetting = (ImageView) findViewById(R.id.btnPersonalInfo);
        userSetting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        openPersonalInfo();
                }
                return false;

            }
        });

        ImageView checkList = (ImageView) findViewById(R.id.btnOpenChecklist);
        checkList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        openToDoActivity();
                }
                return false;

            }
        });

        ImageView alarmClock = (ImageView) findViewById(R.id.btnOpenAlarmClock);
        alarmClock.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        openAlarmClock();
                }
                return false;

            }
        });
    }
    private void openPersonalInfo() {
            Intent personalInfo = new Intent(getApplicationContext(), com.example.ecare_client.settings.PersonalInfoActivity.class);
            startActivity(personalInfo);
        }
    private void openToDoActivity() {
        Intent ChecklistActivity = new Intent(getApplicationContext(), CheckListActivity.class);
        startActivity(ChecklistActivity);
    }
    private void openAlarmClock() {
        Intent AlarmClockActivity = new Intent(getApplicationContext(), com.example.ecare_client.alarmclock.AlarmClockActivity.class);
        startActivity(AlarmClockActivity);
    }
    private void openMagnifier() {
        Intent MagActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(MagActivity);
    }


}