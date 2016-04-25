package com.cmj.face;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.cmj.network.BasicNetwork;
import com.cmj.tools.TimeTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignList extends BaseAppCompatActivity implements AdapterView.OnItemClickListener {
    ArrayList<HashMap<String, Object>> sign_list = new ArrayList<>();
    ListView sign_list_view;
    SignListAdapter sign_list_adapter;
    FloatingActionButton fab;
    String course_name;
    int c_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        c_id = intent.getIntExtra("c_id", -1);
        course_name = intent.getStringExtra("course_name");
        if (course_name != null) { //设置标题为学生姓名
            getSupportActionBar().setTitle(getString(R.string.sign_list_title_format, course_name));
        }

        haveProgressBar = true;
        pre = PreferenceManager.getDefaultSharedPreferences(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (c_id == -1) {
            Snackbar.make(fab, R.string.error_to_load_sign_list, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else {
            HashMap<String, String> m = getAccessTokenMap();
            if (m == null) {
                Snackbar.make(fab, R.string.error_to_load_sign_list, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return;
            }
            showProgress(true);
            m.put("c_id", c_id + "");
            httpGet(getString(R.string.address_sign_list, BasicNetwork.BASIC_HOST), m, 0x001, true);
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
                            Snackbar.make(fab, R.string.error_to_load_sign_list, Snackbar.LENGTH_LONG).show();
                        } else if (D.getInt("count") > 0) {  //当前课程的创建的签到数大于0,是有签到的
                            JSONArray json_sign_list = D.getJSONArray("items");
                            int length = json_sign_list.length();
                            sign_list.clear();
                            for (int i = 0; i < length; i++) {  //遍历JSONArray,一个一个添加签到
                                sign_list.add(getHashMapItem(json_sign_list.getJSONObject(i)));
                            }
                            //执行形成列表视图
                            sign_list_adapter = new SignListAdapter(this, sign_list);
                            sign_list_view = (ListView) findViewById(R.id.listview_sign_list);
                            sign_list_view.setAdapter(sign_list_adapter);
                            sign_list_view.setOnItemClickListener(this);
                        } else {
                            Snackbar.make(fab, R.string.error_not_sign_list_found, Snackbar.LENGTH_LONG).show();
                            // todo show no sign page!
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Snackbar.make(fab, R.string.error_to_load_sign_list, Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    //eg:json  {"id":2,"course_id":2,"teacher_id":1,"start_time":1459699200,"end_time":1461772800,
    // "sign_count":0,"remark":"eee","create_time":1460390284,"update_time":1460390284}
    private HashMap<String, Object> getHashMapItem(JSONObject json) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            map.put("id", json.getInt("id"));
            map.put("sign_count", json.getInt("sign_count"));
            map.put("remark", json.getString("remark"));
            map.put("create_time", json.getLong("create_time"));
            map.put("start_time", json.getLong("start_time"));
            map.put("end_time", json.getLong("end_time"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    //当列表的某一项被点击时调用
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final HashMap<String, Object> map = sign_list.get(position);
        int sign_id = (int) map.get("id");
        Intent intent = new Intent(this, SignActivity.class);
        intent.putExtra("sign_id", sign_id);
        startActivity(intent);
    }

    //每一项列表的BaseAdapter;
    class SignListAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> sign_list;

        public SignListAdapter(Context context, ArrayList<HashMap<String, Object>> sign_list) {
            this.sign_list = sign_list;
        }

        @Override
        public int getCount() {
            return sign_list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final HashMap<String, Object> map = sign_list.get(position);
            ViewHolder vh = new ViewHolder();
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_sign_list, parent, false);
                vh.signListBackground = (CircleImageView) convertView.findViewById(R.id.sign_list_bg_image);
                vh.signListCreateTime = (AppCompatTextView) convertView.findViewById(R.id.sign_list_create_time);
                vh.signListSignCount = (AppCompatTextView) convertView.findViewById(R.id.sign_list_sign_count);
                vh.signListStartTime = (AppCompatTextView) convertView.findViewById(R.id.sign_list_start_time);
                vh.signListEndTime = (AppCompatTextView) convertView.findViewById(R.id.sign_list_end_time);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.signListBackground.setImageResource(getScoreColor((int) map.get("sign_count")));
            vh.signListCreateTime.setText(getString(R.string.sign_list_item_label_create_time,
                    TimeTools.MissileToString((long) map.get("create_time"))));
            vh.signListSignCount.setText(map.get("sign_count") + "");
            vh.signListStartTime.setText(getString(R.string.sign_list_item_label_start_time,
                    TimeTools.MissileToString((long) map.get("start_time"))));
            vh.signListEndTime.setText(getString(R.string.sign_list_item_label_end_time,
                    TimeTools.MissileToString((long) map.get("end_time"))));
            return convertView;
        }
    }

    int[] score_color = {R.color.red, R.color.yellow, R.color.green, R.color.blue, R.color.purple};

    private int getScoreColor(int sign_count) {
        if (sign_count >= 120) {
            return score_color[4];
        } else if (sign_count >= 90) {
            return score_color[3];
        } else if (sign_count >= 60) {
            return score_color[2];
        } else if (sign_count >= 30) {
            return score_color[1];
        }
        return score_color[0];
    }

    class ViewHolder {
        CircleImageView signListBackground;
        AppCompatTextView signListCreateTime;
        AppCompatTextView signListSignCount;
        AppCompatTextView signListStartTime;
        AppCompatTextView signListEndTime;
    }

}
