package com.cmj.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.cmj.face.R;
import com.cmj.face.StudentInfoActivity;
import com.cmj.network.BasicNetwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 学生添加课程请求列表
 */
public class StudentMessageFragment extends BaseFragment {

    public static final int Empty_Message = R.string.no_students_massage;
    ArrayList<HashMap<String, Object>> student_messages = new ArrayList<>();
    ListView student_message_list;
    MessageAdapter message_adapter;

    /**
     * @param data json string
     * @return A new instance of fragment StudentMessageFragment.
     */
    public static StudentMessageFragment newInstance(String data) {
        StudentMessageFragment fragment = new StudentMessageFragment();
        Bundle args = new Bundle();
        args.putString(DATA_TAG, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pre = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            try {
                JSONObject json = new JSONObject(getArguments().getString(DATA_TAG));
                if (json.getInt(JSON_STATUS) == 200) {
                    JSONArray D = json.getJSONObject(JSON_DATA).getJSONArray("items");
                    int length;
                    if (D != null && (length = D.length()) != 0) {
                        student_messages.clear();
                        for (int i = 0; i < length; i++) {  //遍历JSONArray,一个一个添加课程
                            student_messages.add(getHashMapItem(D.getJSONObject(i)));
                        }
                        return resolveData(inflater, container, student_messages);  //执行形成视图,并返回
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
            map.put("student_id", json.getInt("student_id"));
            map.put("course_name", json.getString("course_name"));
            map.put("student_name", json.getString("student_name"));
            map.put("student_name_show", json.getString("student_name") + "(" + json.getString("student_no") + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public View resolveData(LayoutInflater inflater, ViewGroup container, ArrayList<HashMap<String, Object>> signData) {
        View rootView = inflater.inflate(R.layout.fragment_main_course, container, false);
        message_adapter = new MessageAdapter(getActivity(), signData);
        student_message_list = (ListView) rootView.findViewById(R.id.listview_main_course);
        student_message_list.setAdapter(message_adapter);
        return rootView;
    }

    //如果某一项被点击了,则进入查看该学生信息界面
    private void onItemClicked(View v, HashMap<String, Object> m, int position) {
        Intent intent = new Intent(getActivity(), StudentInfoActivity.class);
        intent.putExtra("s_id", (int) m.get("student_id"));
        intent.putExtra("student_name", m.get("student_name").toString());
        getActivity().startActivity(intent);
    }

    //点击了每一项的同意请求触发的事件
    private void actionAccept(final Map<String, Object> map) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.student_action_accept_ack)
                .setMessage(R.string.student_action_accept_ack_message)
                .setNegativeButton(R.string.alert_cancel, null)
                .setPositiveButton(R.string.alert_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, String> postParams = new HashMap<>();
                        postParams.put("cs_id", (int) map.get("id") + "");
                        httpPost(getString(R.string.address_student_accept, BasicNetwork.BASIC_HOST),
                                postParams, getAccessTokenMap(), 0x001, true);
                    }
                })
                .show();
    }

    //点击某一项的拒绝请求触发的事件
    private void actionRefuse(final Map<String, Object> map) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.student_action_refuse_ack)
                .setMessage(R.string.student_action_refuse_ack_message)
                .setNegativeButton(R.string.alert_cancel, null)
                .setPositiveButton(R.string.alert_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, String> postParams = new HashMap<>();
                        postParams.put("cs_id", (int) map.get("id") + "");
                        httpPost(getString(R.string.address_student_refuse, BasicNetwork.BASIC_HOST),
                                postParams, getAccessTokenMap(), 0x002, true);
                    }
                })
                .show();
    }

    public void onHttpCallBack(int status, int what, String data, String message) {
        Log.v("tag", data);
        switch (what) {
            case 0x001: //如果操作成功将返回: {"status":200,"data":{"accepted":true,id=1 },"message":"success"}
                handleAndRefresh(data, "accepted", R.string.action_accept_fail, R.string.action_accept_success);
                break;
            case 0x002: //如果操作成功将返回: {"status":200,"data":{"refused":true,id=1 },"message":"success"}
                handleAndRefresh(data, "refused", R.string.action_refuse_fail, R.string.action_refuse_success);
                break;
        }
    }

    private void handleAndRefresh(final String data, final String TAG, int fail_message, int success_message) {
        try {
            JSONObject json = new JSONObject(data);
            if (json.getInt(JSON_STATUS) == 200) {
                JSONObject D = json.getJSONObject(JSON_DATA);
                if (D == null) {
                    Snackbar.make(student_message_list, R.string.network_error_bad_required, Snackbar.LENGTH_LONG).show();
                } else if (D.getBoolean(TAG)) {  //这里说明操作成功
                    int index = 0, id = D.getInt("id");
                    for (HashMap<String, Object> m : student_messages) {
                        if ((int) m.get("id") == id) {
                            student_messages.remove(index);
                            message_adapter.notifyDataSetChanged();
                            break;
                        }
                        index++;
                    }
                    Snackbar.make(student_message_list, success_message, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(student_message_list, fail_message, Snackbar.LENGTH_LONG).show();
                }
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Snackbar.make(student_message_list, R.string.network_error_bad_required, Snackbar.LENGTH_LONG).show();
    }

    class MessageAdapter extends BaseAdapter {
        Context context;
        ArrayList<HashMap<String, Object>> messageData;

        public MessageAdapter(Context context, ArrayList<HashMap<String, Object>> messageData) {
            this.context = context;
            this.messageData = messageData;
        }

        @Override
        public int getCount() {
            return messageData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //生成列表的每一项的视图
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final HashMap<String, Object> map = messageData.get(position);
            ViewHolder vh = new ViewHolder();
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listview_student_message, parent, false);
                vh.student_name = (AppCompatTextView) convertView.findViewById(R.id.student_name);
                vh.course_name = (AppCompatTextView) convertView.findViewById(R.id.course_name);
                vh.accept_button = (AppCompatButton) convertView.findViewById(R.id.action_accept);
                vh.refuse_button = (AppCompatButton) convertView.findViewById(R.id.action_refuse);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.student_name.setText(map.get("student_name_show").toString());
            vh.course_name.setText((map.get("course_name")).toString());
            //设置接受请求的点击事件
            vh.accept_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionAccept(map);
                }
            });
            //设置拒绝请求的点击事件
            vh.refuse_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionRefuse(map);
                }
            });
            //设置该项的点击事件(除了上面两个按钮的其他地方)
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClicked(v, map, position);
                }
            });
            return convertView;
        }
    }

    class ViewHolder {
        AppCompatTextView student_name;
        AppCompatTextView course_name;
        AppCompatButton accept_button;
        AppCompatButton refuse_button;
    }
}
