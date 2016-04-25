package com.cmj.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cmj.face.MyApplication;
import com.cmj.face.R;
import com.cmj.network.BasicNetwork;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 陈茂建 on 2016/4/19.
 */
public class BaseFragment extends Fragment {
    final static String DATA_TAG = "data";
    final static String JSON_DATA = "data";
    final static String JSON_STATUS = "status";
    final static String USER_ID = "id", ACCESS_TOKEN = "access_token";

    protected View loadEmptyView(LayoutInflater inflater, ViewGroup container, int message) {
        View rootView = inflater.inflate(R.layout.fragment_empty, container, false);
        AppCompatTextView tv = (AppCompatTextView) rootView.findViewById(R.id.error_message);
        tv.setText(message);
        return rootView;
    }

    // if you use SharedPreferences in parent Object,
    // you must init your SharedPreferences at OnCreate method
    public SharedPreferences pre;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BasicNetwork.HttpDataObject data = (BasicNetwork.HttpDataObject) msg.obj;
            if (data.STATUS == BasicNetwork.HttpDataObject.REQUEST_ERROR) {
                onHttpRequestError(msg.what);
                return;
            }
            onHttpCallBack(data.STATUS, msg.what, data.Data, data.message);
        }
    };

    private void onHttpRequestError(int what) {
        if (getGoogleProgressBar() && haveProgressBar) {
            showProgress.setVisibility(View.GONE);
        }
        Toast.makeText(getActivity(), R.string.network_error_bad_required, Toast.LENGTH_SHORT).show();
    }

    public void onHttpCallBack(int status, int what, String data, String message) {

    }

    GoogleProgressBar showProgress;
    public boolean haveProgressBar = false;

    // must ensure progress_bar exit
    public void showProgress(final boolean visible) {
        if (getGoogleProgressBar()) {
            if (visible) {
                showProgress.setVisibility(View.VISIBLE);
            } else {
                showProgress.setVisibility(View.GONE);
            }
        }
    }

    private boolean getGoogleProgressBar() {
        if (showProgress == null) {
            showProgress = (GoogleProgressBar) getActivity().findViewById(R.id.progress_bar);
            return showProgress != null;
        }
        return true;
    }

    /**
     * 该函数先检查网络是否可用,如果不可用给出错误提示
     * 否则,将HashMap转化为字符串形式(如:"id=1&name=e*age=12" ),并新建一个BasicNetwork类,
     * 该类负责进行发送httpPost请求
     * @param url url 如192.168.10.1/user/login
     * @param postParams Post请求参数
     * @param options url的附加参数,如果不为null,最后的url的形式可能是(eg:92.168.10.1/user/login?id=1&access-token=wewdf%sf)
     * @param feedback  httpPost响应的回调标识
     * @param showError 如果网络不可用,是否给出Toast错误提示
     * @return 请求是否提交出去
     */
    public boolean httpPost(String url, HashMap<String, String> postParams, HashMap<String, String> options,
                            int feedback, boolean showError) {
        if (((MyApplication) getActivity().getApplication()).CheckNetwork()) {
            BasicNetwork basicNetwork = new BasicNetwork(handler);
            if (options != null) {
                url = url + "?" + MapToString(options);
            }
            basicNetwork.httpPost(url, MapToString(postParams), feedback);
            return true;
        } else {
            showError(showError);
            return false;
        }
    }

    /**
     * 各参数值和httpPost类似
     * @param url url
     * @param getParams getParams
     * @param feedback feedback
     * @param showError showError
     * @return 请求是否提交出去
     */
    public boolean httpGet(String url, HashMap<String, String> getParams, int feedback, boolean showError) {
        if (((MyApplication) getActivity().getApplication()).CheckNetwork()) {
            BasicNetwork basicNetwork = new BasicNetwork(handler);
            basicNetwork.httpGet(url + "?" + MapToString(getParams), feedback);
            return true;
        } else {
            showError(showError);
            return false;
        }
    }

    private void showError(boolean showError) {
        if (showError) {
            Toast.makeText(getActivity(), R.string.no_network, Toast.LENGTH_SHORT).show();
        }
        if (getGoogleProgressBar() && haveProgressBar) {
            showProgress.setVisibility(View.GONE);
        }
    }

    private String MapToString(HashMap<String, String> paramMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return sb.toString();
    }

    /***
     * note: if you want to use ,you should set these code in ooCreate method
     *
     * @return Map of user id and access token,if it returns null,you should login again!
     * @code pre = PreferenceManager.getDefaultSharedPreferences(this);
     */
    public HashMap<String, String> getAccessTokenMap() {
        if (pre == null) {
            return null;
        } else {
            int id = pre.getInt(USER_ID, -1);
            String access_token = pre.getString(ACCESS_TOKEN, null);
            if (id == -1 || access_token == null) {
                return null;
            } else {
                HashMap<String, String> m = new HashMap<>();
                m.put(USER_ID, id + "");
                m.put("access-token", access_token);  // ACCESS_TOKEN = "access_token"
                return m;
            }
        }
    }

}
