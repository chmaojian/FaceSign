package com.cmj.face;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cmj.network.BasicNetwork;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class SignCreate extends BaseAppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    final String START_DATE = "StartDate", END_DATE = "EndDate", START_TIME = "StartTime", END_TIME = "EndTime";
    int course_id;
    boolean flag = false;
    AppCompatTextView start_time_date, start_time_time, end_time_date, end_time_time;
    EditText remark_edit;
    Time start_time = new Time(), end_time = new Time();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        haveProgressBar = true;
        pre = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = getIntent();
        course_id = intent.getIntExtra("c_id", -1);
        flag = intent.getBooleanExtra("flag", false);
        init();
    }

    private void init() {
        remark_edit = (EditText) findViewById(R.id.remark_edit);
        start_time_date = (AppCompatTextView) findViewById(R.id.start_time_date);
        start_time_time = (AppCompatTextView) findViewById(R.id.start_time_time);
        end_time_date = (AppCompatTextView) findViewById(R.id.end_time_date);
        end_time_time = (AppCompatTextView) findViewById(R.id.end_time_time);

        start_time_date.setText(start_time.formatData());
        start_time_time.setText(start_time.formatTime());
        end_time_date.setText(end_time.formatData());
        end_time_time.setText(end_time.formatTime());

        start_time_date.setOnClickListener(this);
        start_time_time.setOnClickListener(this);
        end_time_date.setOnClickListener(this);
        end_time_time.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_create_submit:
                long start = start_time.toSecond();
                long end = end_time.toSecond();
                if (end < start) {
                    Toast.makeText(this, R.string.error_smaller, Toast.LENGTH_SHORT).show();
                    break;
                }

                showProgress(true);
                HashMap<String, String> m = new HashMap<>();
                m.put("c_id", course_id + "");
                m.put("start_time", start + "");
                m.put("end_time", end + "");
                m.put("remark", remark_edit.getText().toString());
                httpPost(getString(R.string.address_sign_create, BasicNetwork.BASIC_HOST), m, getAccessTokenMap(), 0x001, true);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    boolean isStartTime = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_time_date:
                DatePickerDialog start_dpd = DatePickerDialog.newInstance(this, start_time.year, start_time.month, start_time.day);
                start_dpd.show(getFragmentManager(), START_DATE);
                break;
            case R.id.end_time_date:
                DatePickerDialog end_dpd = DatePickerDialog.newInstance(this, end_time.year, end_time.month, end_time.day);
                end_dpd.show(getFragmentManager(), END_DATE);
                break;
            case R.id.start_time_time:
                isStartTime = true;
                TimePickerDialog start_tpd = TimePickerDialog.newInstance(this, start_time.hour, start_time.minute, true);
                start_tpd.show(getFragmentManager(), START_TIME);
                break;
            case R.id.end_time_time:
                isStartTime = false;
                TimePickerDialog end_tpd = TimePickerDialog.newInstance(this, end_time.hour, end_time.minute, true);
                end_tpd.show(getFragmentManager(), END_TIME);
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        switch (view.getTag()) {
            case START_DATE:
                start_time.setDate(year, monthOfYear, dayOfMonth);
                start_time_date.setText(start_time.formatData());
                break;
            case END_DATE:
                end_time.setDate(year, monthOfYear, dayOfMonth);
                end_time_date.setText(end_time.formatData());
                break;
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        if (isStartTime) {
            start_time.setTime(hourOfDay, minute);
            start_time_time.setText(start_time.formatTime());
        } else {
            end_time.setTime(hourOfDay, minute);
            end_time_time.setText(end_time.formatTime());
        }
    }

    public void onHttpCallBack(int status, int what, String data, String message) {
//        Log.v("tag", data);
        switch (what) {
            case 0x001:
                showProgress(false);
                try {
                    JSONObject json = new JSONObject(data);
                    if (json.getInt(JSON_STATUS) == 200) {
                        JSONObject D = json.getJSONObject(JSON_DATA);
                        if (D == null) {
                            Toast.makeText(this, R.string.error_submit_request, Toast.LENGTH_SHORT).show();
                        } else if (D.getBoolean("created")) {
                            Toast.makeText(this, R.string.sign_create_success, Toast.LENGTH_SHORT).show();
                            if(flag){ //快速签到创建完成后,直接转至签到界面
                                int id = D.getJSONObject("sign_info").getInt("id");
                                Intent intent = new Intent(this, SignActivity.class);
                                intent.putExtra("sign_id", id);
                                startActivity(intent);
                            }
                            finish();
                        } else {
                            Toast.makeText(this, R.string.error_submit_sign, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, R.string.error_submit_request, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class Time {
        int year, month, day, hour, minute;

        public Time() {
            Calendar now = Calendar.getInstance();
            year = now.get(Calendar.YEAR);
            month = now.get(Calendar.MONTH);
            day = now.get(Calendar.DAY_OF_MONTH);
            hour = now.get(Calendar.HOUR_OF_DAY);
            minute = now.get(Calendar.MINUTE);
        }

        public String formatData() {
            return year + "年" + (month + 1) + "月" + day + "日";
        }

        public String formatTime() {
            return hour + "时" + minute + "分";
        }

        public void setDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public void setTime(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public long toSecond() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute);
            return calendar.getTimeInMillis()/1000;
        }
    }
}
