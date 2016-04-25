package com.cmj.face;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.cmj.network.BasicNetwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SignActivity extends BaseAppCompatActivity implements AdapterView.OnItemClickListener {
    ArrayList<HashMap<String, Object>> sign_students = new ArrayList<>();
    GridView sign_students_grid_view;
    SignStudentsAdapter sign_students_adapter;
    Toolbar toolbar;
    int sign_id, student_id, position; //用于提交标识

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        haveProgressBar = true;
        pre = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = getIntent();
        sign_id = intent.getIntExtra("sign_id", -1);

        if (sign_id == -1) { //todo
            Snackbar.make(toolbar, R.string.error_to_load_sign_list, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else {
            HashMap<String, String> m = getAccessTokenMap();
            if (m == null) {
                Snackbar.make(toolbar, R.string.error_to_load_sign_list, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return;
            }
            showProgress(true);
            m.put("sign_id", sign_id + "");
            httpGet(getString(R.string.address_sign_sign, BasicNetwork.BASIC_HOST), m, 0x001, true);
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
                            Snackbar.make(toolbar, R.string.error_to_load_sign_sign, Snackbar.LENGTH_LONG).show();
                        } else if (D.getBoolean("has") && D.getInt("count") > 0) {
                            JSONArray json_sign_list = D.getJSONArray("items");
                            int length = json_sign_list.length();
                            sign_students.clear();
                            for (int i = 0; i < length; i++) {
                                sign_students.add(getHashMapItem(json_sign_list.getJSONObject(i)));
                            }
                            sign_students_adapter = new SignStudentsAdapter(this, sign_students);
                            sign_students_grid_view = (GridView) findViewById(R.id.sign_students_grid_view);
                            sign_students_grid_view.setAdapter(sign_students_adapter);
                            sign_students_grid_view.setOnItemClickListener(this);
                        } else {
                            Snackbar.make(toolbar, R.string.error_to_no_sign_data, Snackbar.LENGTH_LONG).show();
                            // todo show no sign page!
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Snackbar.make(toolbar, R.string.error_to_load_sign_sign, Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    // id 是 学生与课程的关联ID
//    {"id":1,"sign_record_id":1,"rate":80,"student_id":1,"name":"\u9648\u8302\u5efa",
//            "no":41355048,"status":10,"picture":""}
    private HashMap<String, Object> getHashMapItem(JSONObject json) {
        HashMap<String, Object> map = new HashMap<>();
        try {
//            map.put("id", json.getInt("id"));
//            map.put("sign_record_id", json.getInt("sign_count"));
            Object record = json.get("sign_record_id");
            if (record == null || record.toString().equals("null")) {
                map.put("signed", false);
            } else {
                map.put("signed", true);
            }
            map.put("name", json.getString("name"));
            map.put("no", json.getInt("no"));
            map.put("student_id", json.getInt("student_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    //某一个学生item被点击了,如果该学生未签到，则启动相机进行人脸识别签到
    final int TAKE_PICTURE = 0x202;
    final int VIEW_FACE = 0x203;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //login judge
        HashMap<String, Object> map = sign_students.get(position);
        if ((boolean) map.get("signed")) {
            Toast.makeText(this, R.string.has_signed, Toast.LENGTH_SHORT).show();
            return;
        }
        //启动相机
        this.position = position;
        student_id = (int) map.get("student_id");
        startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), TAKE_PICTURE);
    }

    //相机拍照返回
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            //查看拍照大图
            Bundle bundle = new Bundle();
            bundle.putParcelable("face", (Bitmap) data.getExtras().get("data"));
            Intent intent = new Intent(this, FaceViewerActivity.class);
            intent.putExtras(bundle);
            intent.putExtra("sign_id", sign_id);
            intent.putExtra("student_id", student_id);
//            intent.putExtra("course_id",1);
            startActivityForResult(intent, VIEW_FACE);
        } else if (requestCode == VIEW_FACE && resultCode == RESULT_OK) {
            boolean status = data.getBooleanExtra("statue", false);
            if (status) {
                Toast.makeText(this, R.string.success_sign, Toast.LENGTH_SHORT).show();
                sign_students.get(position).put("signed", true);
                sign_students_adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, R.string.error_sign, Toast.LENGTH_SHORT).show();
            }
        }
    }

    class SignStudentsAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> sign_students;

        SignStudentsAdapter(Context context, ArrayList<HashMap<String, Object>> sign_students) {
            this.sign_students = sign_students;
        }

        @Override
        public int getCount() {
            return sign_students.size();
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
            final HashMap<String, Object> map = sign_students.get(position);
            ViewHolder vh = new ViewHolder();
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.gridview_sign, parent, false);
//                vh.Header = (CircleImageView) convertView.findViewById(R.id.sign_list_bg_image);
                vh.studentName = (AppCompatTextView) convertView.findViewById(R.id.sign_student_name);
                vh.studentNo = (AppCompatTextView) convertView.findViewById(R.id.sign_student_no);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
//            vh.signListBackground.setImageResource(getScoreColor((int) map.get("sign_count")));
            vh.studentName.setText(map.get("name").toString());
            vh.studentNo.setText(map.get("no").toString());
            if ((boolean) map.get("signed")) {
                vh.studentName.setTextColor(0xFF34A350); //green color
                vh.studentNo.setTextColor(0xFF34A350);
            }
            return convertView;
        }
    }

    class ViewHolder {
        //        CircleImageView Header;
        AppCompatTextView studentName;
        AppCompatTextView studentNo;
//        AppCompatTextView signListStartTime;
//        AppCompatTextView signListEndTime;
    }
}
