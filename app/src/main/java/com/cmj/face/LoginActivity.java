package com.cmj.face;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cmj.network.BasicNetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by 陈茂建 on 2016/4/13.
 */
public class LoginActivity extends BaseAppCompatActivity {
    // UI references.
    final static String USER_ID = "id",USER_NO = "no",USER_NAME = "name",ACCESS_TOKEN = "access_token";

    SharedPreferences pre;
    private AutoCompleteTextView userName;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pre = PreferenceManager.getDefaultSharedPreferences(this);
        if (checkUserExist()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_login);
        haveProgressBar = true;

        userName = (AutoCompleteTextView) findViewById(R.id.user_name);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击登录按钮,将用户名和密码发给服务器验证
                attemptLogin();
            }
        });
    }

    private boolean checkUserExist() {
        int id = pre.getInt(USER_ID, -1);
        String name = pre.getString(USER_NAME, null);
        String no = pre.getString(USER_NO, null);
        String access_token = pre.getString(ACCESS_TOKEN, null);
        return !(id == -1 || no == null || no.isEmpty() || name == null || name.isEmpty()
                || access_token == null || access_token.isEmpty());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, DebugSettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptLogin() {
        // Reset errors.
        boolean cancel = false;
        View focusView = null;
        String username = userName.getText().toString();
        String password = mPasswordView.getText().toString();
        userName.setError(null);
        mPasswordView.setError(null);
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            userName.setError(getString(R.string.error_field_required));
            focusView = userName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("no", username);
            postParams.put("password", password);
            //发给服务器验证
            httpPost(getString(R.string.address_login_url, BasicNetwork.BASIC_HOST), postParams,null, 0x001,true);
        }
    }

    @Override
    public void onHttpCallBack(int status, int what, String data, String message) {
        switch (what) {
            case 0x001:
                showProgress(false);
                try {
                    JSONObject json = new JSONObject(data);
                    if (json.getInt("status") == 200) {
                        JSONObject D = json.getJSONObject("data");
                        if (D == null) {
                            Toast.makeText(this, R.string.network_error_bad_required, Toast.LENGTH_SHORT).show();
                        } else if (D.getBoolean("verify")) {
                            setSuccess(D); //success
                        } else {
                            Toast.makeText(this, R.string.error_incorrect_password_or_username, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, R.string.network_error_bad_required, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setSuccess(JSONObject d) {
        try {
            int id = d.getInt("id");
            String no = d.getString("no");
            String name = d.getString("name");
            String access_token = d.getString("access_token");
            if ((id >= 0 && no != null && !no.isEmpty() && name != null && !name.isEmpty()
                    && access_token != null && !access_token.isEmpty())) {
                //如果验证通过,则将用户的ID,工号,姓名，token值存储到SharedPreferences中
                SharedPreferences.Editor editor = pre.edit();
                editor.putInt(USER_ID, id);
                editor.putString(USER_NO, no);
                editor.putString(USER_NAME, name);
                editor.putString(ACCESS_TOKEN, access_token);
                editor.apply();
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, R.string.network_error_bad_required, Toast.LENGTH_SHORT).show();

    }

}

