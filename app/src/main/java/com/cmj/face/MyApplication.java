/**
 *
 */
package com.cmj.face;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.cmj.network.BasicNetwork;

/**
 * @author  陈茂建
 */
public class MyApplication extends Application {
    final static String BASE_HOST = "base_host";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
        BasicNetwork.setBaseHost(pre.getString(BASE_HOST, getString(R.string.base_host_address_default_value)));
    }

    public boolean CheckNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connMgr.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }
}
