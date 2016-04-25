package com.cmj.face;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.cmj.network.BasicNetwork;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 陈茂建 on 2016/4/13.
 */
public class BaseAppCompatActivity extends AppCompatActivity {
    final static String DATA_TAG = "data";
    final static String JSON_DATA = "data";
    final static String JSON_STATUS = "status";
    final static String USER_ID = "id", ACCESS_TOKEN = "access_token";
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
        Toast.makeText(this, R.string.network_error_bad_required, Toast.LENGTH_SHORT).show();
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
            showProgress = (GoogleProgressBar) findViewById(R.id.progress_bar);
            return showProgress != null;
        }
        return true;
    }

    /**
     * @see com.cmj.fragment.BaseFragment
     */
    public boolean httpPost(String url, HashMap<String, String> postParams,HashMap<String, String> options, int feedback, boolean showError) {
        if (((MyApplication) getApplication()).CheckNetwork()) {
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
     * @see com.cmj.fragment.BaseFragment
     */
    public boolean httpPostFile(String url, HashMap<String, String> params,FileInputStream fis, int feedback, boolean showError) {
        if (((MyApplication) getApplication()).CheckNetwork()) {
            BasicNetwork basicNetwork = new BasicNetwork(handler);
            if (params != null) {
                url = url + "?" + MapToString(params);
            }
            basicNetwork.httpFile(url, feedback,fis);
            return true;
        } else {
            showError(showError);
            return false;
        }
    }


    /**
     * @see com.cmj.fragment.BaseFragment
     */
    public boolean httpGet(String url, HashMap<String, String> getParams, int feedback, boolean showError) {
        if (((MyApplication) getApplication()).CheckNetwork()) {
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
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
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
     * @return Map of user id and access token,if it returns null,you should login again!
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

