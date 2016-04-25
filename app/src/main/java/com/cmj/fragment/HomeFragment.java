package com.cmj.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.cmj.face.R;
import com.cmj.face.SignActivity;
import com.cmj.tools.TimeTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 陈茂建 on 2016/4/16.
 */
public class HomeFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    public static final int Empty_Message = R.string.home_no_recent_sign;
    ArrayList<HashMap<String, Object>> signData = new ArrayList<>();

    public static HomeFragment newInstance(String data) {
        HomeFragment f = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(DATA_TAG, data);
        f.setArguments(args);
        return f;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            try {
                JSONObject json = new JSONObject(getArguments().getString(DATA_TAG));
                if (json.getInt(JSON_STATUS) == 200) {
                    JSONArray D = json.getJSONArray(JSON_DATA);
                    int length;
                    if (D != null && (length = D.length()) != 0) {
                        for (int i = 0; i < length; i++) {  //遍历JSONArray
                            signData.add(getHashMapItem(D.getJSONObject(i)));
                        }
                        return resolveData(inflater, container, signData);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return loadEmptyView(inflater, container, Empty_Message);
    }

//eg:
// course_id:1 course_name:"计算机组成原理" course_remark:"硬" course_student_num: 0
// describe:"计算机专业课程" end_time:1461686400 sign_count:0 sign_create_time:1460390239
// sign_id:1 sign_remark:"eee" sign_update_time:1460390239 start_time:1459180800

    private HashMap<String, Object> getHashMapItem(JSONObject json) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            map.put("sign_id", json.getInt("sign_id"));  //签到ID
            map.put("course_name", json.getString("course_name"));
            map.put("sign_count", json.getInt("sign_count") + "人签到");
            map.put("sign_date", TimeTools.MissileToString(json.getLong("start_time")) + " To " + TimeTools.MissileToString(json.getLong("end_time")));
//            map.put("end_time", );
//            map.put("sign_create_time", TimeTools.MissileToString(json.getLong("sign_create_time")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    //    home_no_recent_sign
    public View resolveData(LayoutInflater inflater, ViewGroup container, ArrayList<HashMap<String, Object>> signData) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        SimpleAdapter home_adapter = new SimpleAdapter(getActivity(), signData, R.layout.listview_home,
                new String[]{"course_name", "sign_count", "sign_date"},
                new int[]{R.id.home_course_title, R.id.home_sign_count, R.id.home_sign_date});
        ListView home_list = (ListView) rootView.findViewById(R.id.home_sign_list);
        home_list.setAdapter(home_adapter);

        home_list.setOnItemClickListener(this);
        return rootView;
    }

    //某一项签到被点击了
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final HashMap<String, Object> map = signData.get(position);
        Intent intent = new Intent(getActivity(), SignActivity.class);
        intent.putExtra("sign_id", (int) map.get("sign_id"));
        startActivity(intent);
    }
}
