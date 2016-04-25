package com.cmj.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.cmj.face.CourseDetailActivity;
import com.cmj.face.R;
import com.cmj.tools.TimeTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 陈茂建 on 2016/4/19.
 */
public class CourseFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    public static final int Empty_Message = R.string.main_no_course_list;
    ArrayList<HashMap<String, Object>> courseData;

    public static CourseFragment newInstance(String data) {
        CourseFragment f = new CourseFragment();
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
                    JSONArray D = json.getJSONObject(JSON_DATA).getJSONArray("items");
                    int length;
                    if (D != null && (length = D.length()) != 0) {
                        courseData = new ArrayList<>();
                        for (int i = 0; i < length; i++) {  //遍历JSONArray,一个一个添加课程
                            courseData.add(getHashMapItem(D.getJSONObject(i)));
                        }
                        return resolveData(inflater, container, courseData);  //执行形成视图,并返回
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return loadEmptyView(inflater, container, Empty_Message);
    }


    private HashMap<String, Object> getHashMapItem(JSONObject json) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            map.put("id", json.getInt("id"));
            map.put("course_name", json.getString("course_name"));
            map.put("describe", json.getString("describe"));
            map.put("student_num", json.getInt("student_num") + "人");
            map.put("create_time", "创建时间:" + TimeTools.MissileToString(json.getLong("create_time")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public View resolveData(LayoutInflater inflater, ViewGroup container, ArrayList<HashMap<String, Object>> courseData) {
        View rootView = inflater.inflate(R.layout.fragment_main_course, container, false);
        SimpleAdapter home_adapter = new SimpleAdapter(getActivity(), courseData, R.layout.listview_main_course,
                new String[]{"course_name", "describe", "student_num", "create_time"},
                new int[]{R.id.list_main_course_title, R.id.list_main_course_describe,
                        R.id.list_main_course_student_num, R.id.list_main_course_create_time});
        ListView course_list = (ListView) rootView.findViewById(R.id.listview_main_course);
        course_list.setAdapter(home_adapter);

        course_list.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> map = courseData.get(position);
        Intent course_detail = new Intent(getActivity(), CourseDetailActivity.class);
        course_detail.putExtra("c_id", (int)map.get("id"));
        course_detail.putExtra("course_name", map.get("course_name").toString());
        startActivity(course_detail);
    }
}
