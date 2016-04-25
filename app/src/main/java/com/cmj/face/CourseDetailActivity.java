package com.cmj.face;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.cmj.network.BasicNetwork;
import com.cmj.tools.TimeTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CourseDetailActivity extends BaseAppCompatActivity implements View.OnClickListener {
    FloatingActionButton fab;
    String course_name;
    int c_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        c_id = intent.getIntExtra("c_id", -1);
        course_name = intent.getStringExtra("course_name");
        if (course_name != null) { //设置标题为学生姓名
            getSupportActionBar().setTitle(course_name);
        }

        haveProgressBar =true;
        pre = PreferenceManager.getDefaultSharedPreferences(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (c_id == -1) {
            Snackbar.make(fab, R.string.error_to_load_course_info, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else {
            HashMap<String, String> m = getAccessTokenMap();
            if (m == null) {
                Snackbar.make(fab, R.string.error_to_load_course_info, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return;
            }
            showProgress(true);
            m.put("c_id", c_id + "");
            httpGet(getString(R.string.address_course_detail, BasicNetwork.BASIC_HOST), m, 0x001, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                            Snackbar.make(fab, R.string.error_to_load_course_info, Snackbar.LENGTH_LONG).show();
                        } else if (D.getBoolean("has")) {  //这里说明操作成功
                            JSONObject course_info = D.getJSONObject("info");
                            resolveCourseInfo(course_info);
                        } else {
                            Snackbar.make(fab, R.string.error_not_student_found, Snackbar.LENGTH_LONG).show();
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Snackbar.make(fab, R.string.error_to_load_course_info, Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    //    {"id":1,"teacher_id":1,"student_num":0,"course_name":"","describe":"","remark":"","create_time":,"update_time":}
    private void resolveCourseInfo(JSONObject course_info) {
        if (course_info != null) {
            try {
                setText(R.id.course_detail_course_name, R.string.course_detail_course_name, course_info.getString("course_name"));
                setText(R.id.course_detail_describe, R.string.course_detail_describe, course_info.getString("describe"));
                setText(R.id.course_detail_student_num, R.string.course_detail_student_num, course_info.getString("student_num"));
                setText(R.id.course_detail_remark, R.string.course_detail_remark, course_info.getString("remark"));
                setText(R.id.course_detail_create_time, R.string.course_detail_create_time, TimeTools.MissileToString(course_info.getLong("create_time")));
                setText(R.id.course_detail_update_time, R.string.course_detail_update_time, TimeTools.MissileToString(course_info.getLong("update_time")));
//           设置各个按钮可以点击有效
                fab.setOnClickListener(this);
                AppCompatButton button_new = (AppCompatButton) findViewById(R.id.btn_create_new_sign);
                AppCompatButton button_history = (AppCompatButton) findViewById(R.id.btn_history_sign);
                button_new.setOnClickListener(this);
                button_history.setOnClickListener(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setText(int id, int base_text, String value) {
        AppCompatTextView textView = (AppCompatTextView) findViewById(id);
        if (textView != null) {
            textView.setText(getString(base_text, value));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:  //修改课表
                Snackbar.make(fab, "null", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btn_create_new_sign:
                Intent intent_new = new Intent(this, SignCreate.class);
                intent_new.putExtra("c_id", c_id);
                intent_new.putExtra("course_name", course_name);
                intent_new.putExtra("flag", false);
                startActivity(intent_new);
                break;
            case R.id.btn_history_sign:
                Intent intent = new Intent(this, SignList.class);
                intent.putExtra("c_id", c_id);
                intent.putExtra("course_name", course_name);
                startActivity(intent);
                break;
        }
    }
}
