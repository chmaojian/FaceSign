package com.cmj.face;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.cmj.network.BasicNetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class StudentInfoActivity extends BaseAppCompatActivity {

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        int s_id = intent.getIntExtra("s_id", -1);
        String student_name = intent.getStringExtra("student_name");
        if (student_name != null) { //设置标题为学生姓名
            getSupportActionBar().setTitle(student_name);
        }

        haveProgressBar =true;
        pre = PreferenceManager.getDefaultSharedPreferences(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (s_id == -1) {
            Snackbar.make(fab, R.string.error_to_load_student_info, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else {
            HashMap<String, String> m = getAccessTokenMap();
            if (m == null) {
                Snackbar.make(fab, R.string.error_to_load_student_info, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return;
            }
            showProgress(true);
            m.put("s_id", s_id + "");
            httpGet(getString(R.string.address_student_info, BasicNetwork.BASIC_HOST), m, 0x001, true);
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
        Log.v("tag", data);
        switch (what) {
            case 0x001:
                showProgress(false);
                try {
                    JSONObject json = new JSONObject(data);
                    if (json.getInt(JSON_STATUS) == 200) {
                        JSONObject D = json.getJSONObject(JSON_DATA);
                        if (D == null) {
                            Snackbar.make(fab, R.string.error_to_load_student_info, Snackbar.LENGTH_LONG).show();
                        } else if (D.getBoolean("has")) {  //这里说明操作成功
                            JSONObject student_info = D.getJSONObject("student_info");
                            resolveStudentInfo(student_info);
                        } else {
                            Snackbar.make(fab, R.string.error_not_student_found, Snackbar.LENGTH_LONG).show();
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Snackbar.make(fab, R.string.error_to_load_student_info, Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    // {"id":1,"name":"ABC","s_no":1234567,"sex":"","tel":"","picture":"","major":"","school":"","email":123@12.comm"}
    private void resolveStudentInfo(JSONObject student_info) {
        if (student_info != null) {
            final int LENGTH = 7;
            int[] ids = new int[]{R.id.student_info_name, R.id.student_info_s_no, R.id.student_info_sex,
                    R.id.student_info_tel, R.id.student_info_email, R.id.student_info_major, R.id.student_info_school};
            int[] base_text = new int[]{R.string.student_info_label_name, R.string.student_info_label_s_no,
                    R.string.student_info_label_sex, R.string.student_info_label_tel, R.string.student_info_label_email,
                    R.string.student_info_label_major, R.string.student_info_label_school};
            String[] tags = new String[]{"name", "s_no", "sex", "tel", "email", "major", "school"};

            AppCompatTextView tv;
            for (int i = 0; i < LENGTH; i++) {
                tv = ((AppCompatTextView) findViewById(ids[i]));
                if (tv != null) {
                    try {
                        tv.setText(getString(base_text[i], student_info.getString(tags[i])));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
