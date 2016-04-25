package com.cmj.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmj.face.R;
import com.cmj.face.SignCreate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class QuickSignFragment extends BaseFragment implements View.OnClickListener {
    public static final int Empty_Message = R.string.no_course_to_select;
    boolean flag;
    ArrayList<String> courseList = new ArrayList<>();
    ArrayList<Integer> courseIDList = new ArrayList<>();
    private CharSequence[] courseArray;

    public static QuickSignFragment newInstance(String data) {
        QuickSignFragment fragment = new QuickSignFragment();
        Bundle args = new Bundle();
        args.putString(DATA_TAG, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            try {
                JSONObject json = new JSONObject(getArguments().getString(DATA_TAG));
                Log.d("j", json.toString());
                if (json.getInt(JSON_STATUS) == 200) {
                    JSONArray D = json.getJSONObject(JSON_DATA).getJSONArray("items");
                    int length;
                    if (D != null && (length = D.length()) != 0) {
                        for (int i = 0; i < length; i++) {  //遍历JSONArray
                            courseList.add(D.getJSONObject(i).getString("course_name"));
                            courseIDList.add(D.getJSONObject(i).getInt("id"));
                        }
                        flag = true;
                        super.onCreate(savedInstanceState);
                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        flag = false;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (flag) {
            View view = inflater.inflate(R.layout.fragment_quick_sign, container, false);
            AppCompatButton showCourseButton = (AppCompatButton) view.findViewById(R.id.show_course_button);
            showCourseButton.setOnClickListener(this);
            showDialog();
            return view;
        } else {
            return loadEmptyView(inflater, container, Empty_Message);
        }
    }

    // just one button for OnClick
    @Override
    public void onClick(View v) {
        showDialog();
    }

    int course_witch = 0;
    private void showDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.alert_select_a_course)
                .setSingleChoiceItems(getCourseArray(), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        course_witch = which;
                    }
                })
                .setPositiveButton(R.string.alert_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int course_id = courseIDList.get(course_witch);
                        String course_name = courseList.get(course_witch);
                        Intent intent = new Intent(getActivity(), SignCreate.class);
                        intent.putExtra("c_id", course_id);
                        intent.putExtra("course_name", course_name);
                        intent.putExtra("flag", true);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.alert_cancel, null)
                .show();
    }

    public CharSequence[] getCourseArray() {
        if (courseArray == null) {
            courseArray = new CharSequence[courseList.size()];
            int i = 0;
            for (String s : courseList) {
                courseArray[i] = s;
                i++;
            }
        }
        return courseArray;
    }
}
