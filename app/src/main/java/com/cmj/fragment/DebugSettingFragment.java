package com.cmj.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;
import com.cmj.face.R;
import com.cmj.network.BasicNetwork;

/**
 * Created by 陈茂建 on 2016/4/13.
 */

public class DebugSettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * @see com.cmj.face.MyApplication#onCreate
     * @see com.cmj.face.MyApplication#BASE_HOST
     */
    final static String BASE_HOST = "base_host";
    MaterialEditTextPreference base_host;

    public DebugSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.debug_preferences);
        base_host = (MaterialEditTextPreference) getPreferenceScreen().findPreference(BASE_HOST);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(BASE_HOST)) {
            setSummary();
        }
    }

    private void setSummary() {
        if (base_host.getText().isEmpty()) {
            base_host.setSummary(R.string.base_host_address_default_value);
            BasicNetwork.setBaseHost(getActivity().getString(R.string.base_host_address_default_value));
        } else {
            String text = base_host.getText();
            base_host.setSummary(text);
            BasicNetwork.setBaseHost(text);
        }
    }

}
