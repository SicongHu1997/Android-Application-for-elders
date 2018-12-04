package com.example.ecare_client.alarmclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.example.ecare_client.R;
import com.example.ecare_client.alarmclock.view.SelectRemindCyclePopup;
import com.example.ecare_client.alarmclock.view.SelectRemindWayPopup;
import com.example.ecare_client.alarmclock.clock.AlarmManagerUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmClockActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView date_tv;
    private TimePickerView pvTime;
    private RelativeLayout repeat_rl, ring_rl;
    private TextView tv_repeat_value, tv_ring_value;
    private LinearLayout allLayout;
    private Button set_btn;
    private String time;
    private int cycle;
    private int ring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        allLayout = (LinearLayout) findViewById(R.id.all_layout);
        set_btn = (Button) findViewById(R.id.set_btn);
        set_btn.setOnClickListener(this);
        date_tv = (TextView) findViewById(R.id.date_tv);
        repeat_rl = (RelativeLayout) findViewById(R.id.repeat_rl);
        repeat_rl.setOnClickListener(this);
        ring_rl = (RelativeLayout) findViewById(R.id.ring_rl);
        ring_rl.setOnClickListener(this);
        tv_repeat_value = (TextView) findViewById(R.id.tv_repeat_value);
        tv_ring_value = (TextView) findViewById(R.id.tv_ring_value);
        pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date,View v) {//选中事件回调
                time = getTime(date);
                date_tv.setText(time);
            }
        })
                .setType(new boolean[]{false, false, false, true, true, false})
                .setLabel("", "", "", "", "", "")
                .build();
        date_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pvTime.show();
            }
        });

    }

    public static String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.repeat_rl:
                selectRemindCycle();
                break;
            case R.id.ring_rl:
                selectRingWay();
                break;
            case R.id.set_btn:
                setClock();
                break;
            default:
                break;
        }
    }

    private void setClock() {
        if (time != null && time.length() > 0) {
            String[] times = time.split(":");
            if (cycle == 0) {
                com.example.ecare_client.alarmclock.clock.AlarmManagerUtil.setAlarm(this, 0, Integer.parseInt(times[0]), Integer.parseInt
                        (times[1]), 0, 0, "Alarm Clock", ring);
            } if(cycle == -1){
                com.example.ecare_client.alarmclock.clock.AlarmManagerUtil.setAlarm(this, 1, Integer.parseInt(times[0]), Integer.parseInt
                        (times[1]), 0, 0, "Alarm Clock", ring);
            }else {
                String weeksStr = parseRepeat(cycle, 1);
                String[] weeks = weeksStr.split(",");
                for (int i = 0; i < weeks.length; i++) {
                    com.example.ecare_client.alarmclock.clock.AlarmManagerUtil.setAlarm(this, 2, Integer.parseInt(times[0]), Integer
                            .parseInt(times[1]), i, Integer.parseInt(weeks[i]), "Alarm Clock", ring);
                }
            }
            Toast.makeText(this, "Alarm clock set successfully", Toast.LENGTH_LONG).show();
        }

    }


    public void selectRemindCycle() {
        final SelectRemindCyclePopup fp = new SelectRemindCyclePopup(this);
        fp.showPopup(allLayout);
        fp.setOnSelectRemindCyclePopupListener(new SelectRemindCyclePopup
                .SelectRemindCyclePopupOnClickListener() {

            @Override
            public void obtainMessage(int flag, String ret) {
                switch (flag) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        int repeat = Integer.valueOf(ret);
                        tv_repeat_value.setText(parseRepeat(repeat, 0));
                        cycle = repeat;
                        fp.dismiss();
                        break;
                    case 8:
                        tv_repeat_value.setText("Everyday");
                        cycle = 0;
                        fp.dismiss();
                        break;
                    case 9:
                        tv_repeat_value.setText("Once");
                        cycle = -1;
                        fp.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
    }


    public void selectRingWay() {
        SelectRemindWayPopup fp = new SelectRemindWayPopup(this);
        fp.showPopup(allLayout);
        fp.setOnSelectRemindWayPopupListener(new SelectRemindWayPopup
                .SelectRemindWayPopupOnClickListener() {

            @Override
            public void obtainMessage(int flag) {
                switch (flag) {
                    case 0:
                        tv_ring_value.setText("Vibrate");
                        ring = 0;
                        break;
                    case 1:
                        tv_ring_value.setText("Ring");
                        ring = 1;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public static String parseRepeat(int repeat, int flag) {
        String cycle = "";
        String weeks = "";
        if (repeat == 0) {
            repeat = 127;
        }
        if (repeat % 2 == 1) {
            cycle = "Monday";
            weeks = "1";
        }
        if (repeat % 4 >= 2) {
            if ("".equals(cycle)) {
                cycle = "Tuesday";
                weeks = "2";
            } else {
                cycle = cycle + "," + "Tuesday";
                weeks = weeks + "," + "2";
            }
        }
        if (repeat % 8 >= 4) {
            if ("".equals(cycle)) {
                cycle = "Wednesday";
                weeks = "3";
            } else {
                cycle = cycle + "," + "Wednesday";
                weeks = weeks + "," + "3";
            }
        }
        if (repeat % 16 >= 8) {
            if ("".equals(cycle)) {
                cycle = "Thursday";
                weeks = "4";
            } else {
                cycle = cycle + "," + "Thursday";
                weeks = weeks + "," + "4";
            }
        }
        if (repeat % 32 >= 16) {
            if ("".equals(cycle)) {
                cycle = "Friday";
                weeks = "5";
            } else {
                cycle = cycle + "," + "Friday";
                weeks = weeks + "," + "5";
            }
        }
        if (repeat % 64 >= 32) {
            if ("".equals(cycle)) {
                cycle = "Saturday";
                weeks = "6";
            } else {
                cycle = cycle + "," + "Saturday";
                weeks = weeks + "," + "6";
            }
        }
        if (repeat / 64 == 1) {
            if ("".equals(cycle)) {
                cycle = "Sunday";
                weeks = "7";
            } else {
                cycle = cycle + "," + "Sunday";
                weeks = weeks + "," + "7";
            }
        }

        return flag == 0 ? cycle : weeks;
    }

}
