package com.cmj.face;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cmj.fragment.CourseFragment;
import com.cmj.fragment.HomeFragment;
import com.cmj.fragment.QuickSignFragment;
import com.cmj.fragment.StudentMessageFragment;
import com.cmj.network.BasicNetwork;
import com.cmj.tools.StringTools;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by 陈茂建 on 2016/4/13.
 */

public class MainActivity extends BaseAppCompatActivity {
    final static String USER_NO = "no", USER_NAME = "name";
    private long lastDown = 0;
    public int state;
    TextView nav_sub_header, nav_header_title;

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private View navigation_drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        haveProgressBar = true; // can show ProgressBar
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigation_drawer = findViewById(R.id.navigation_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        pre = PreferenceManager.getDefaultSharedPreferences(this);
        (findViewById(R.id.menu_home)).setOnClickListener(new ClickHand());
        (findViewById(R.id.menu_sign)).setOnClickListener(new ClickHand());
        (findViewById(R.id.menu_students)).setOnClickListener(new ClickHand());
        (findViewById(R.id.menu_course)).setOnClickListener(new ClickHand());
        (findViewById(R.id.menu_setting)).setOnClickListener(new ClickHand());
        nav_sub_header = (TextView) findViewById(R.id.nav_sub_header);
        nav_header_title = (TextView) findViewById(R.id.nav_header_title);

        getRecentSign(); //开始是首页,显示最近的签到
    }

    @Override
    public void onResume() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int drawable;
        if (hour < 6) {
            drawable = R.drawable.nav_night;
        } else if (hour < 11) {
            drawable = R.drawable.nav_morning;
        } else if (hour < 18) {
            drawable = R.drawable.nav_afternoon;
        } else {
            drawable = R.drawable.nav_night;
        }
        findViewById(R.id.nav_container).setBackgroundResource(drawable);

        //设置侧栏的用户名及工号
        String name = pre.getString(USER_NAME, null);
        String no = pre.getString(USER_NO, null);
        if (no == null || name == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            nav_header_title.setText(name);
            nav_sub_header.setText(no);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            long nowDown = System.currentTimeMillis();
            if (nowDown - lastDown > 2000) {
                Toast.makeText(MainActivity.this, R.string.press_to_exit, Toast.LENGTH_SHORT).show();
                lastDown = nowDown;
            } else {
                finish();
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void drawerToggle() {
        if (mDrawerLayout.isDrawerOpen(navigation_drawer)) {
            mDrawerLayout.closeDrawer(navigation_drawer);
            toolbar.setTitle(StringTools.getActionBarTitle(state));
        } else {
            mDrawerLayout.openDrawer(navigation_drawer);
            toolbar.setTitle(R.string.home_title);
        }
    }

    private void getRecentSign() {
        showProgress(true);
        HashMap<String, String> m = getAccessTokenMap();
        httpGet(getString(R.string.address_sign_recent, BasicNetwork.BASIC_HOST), m, 0x001, true);
    }

    public void MainClick(View view) {
        switch (view.getId()) {
            case R.id.account:
                Intent intent = new Intent(this, AccountActivity.class);
                startActivity(intent);
                break;
            case R.id.account_exit:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.logout_account_ack)
                        .setMessage(R.string.logout_account_ack_message)
                        .setNegativeButton(R.string.alert_cancel, null)
                        .setPositiveButton(R.string.alert_sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                exitAccount();
                            }
                        })
                        .show();
                break;
        }
    }

    class ClickHand implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.menu_home:
                    state = 1;
                    //抽屉关闭
                    drawerToggle();
                    //显示最近的签到情况
                    getRecentSign();
                    break;
                case R.id.menu_sign:
                    state = 2;
                    drawerToggle();
                    showProgress(true);
                    HashMap<String, String> course_select = getAccessTokenMap();
                    httpGet(getString(R.string.address_course_list, BasicNetwork.BASIC_HOST), course_select, 0x002, true);
                    break;
                case R.id.menu_students:
                    state = 3;
                    drawerToggle();
                    showProgress(true);
                    HashMap<String, String> m_course = getAccessTokenMap();
                    httpGet(getString(R.string.address_student_message, BasicNetwork.BASIC_HOST), m_course, 0x003, true);
                    break;
                case R.id.menu_course:
                    state = 4;
                    drawerToggle();
                    showProgress(true);
                    HashMap<String, String> m_student_message = getAccessTokenMap();
                    httpGet(getString(R.string.address_course_list, BasicNetwork.BASIC_HOST), m_student_message, 0x004, true);
                    break;
                case R.id.menu_setting:
                    state = 5;
                    drawerToggle();
                    break;
            }
        }
    }

    /**
     * 退出账号 前需要清除一些信息
     */
    public void exitAccount() {
        SharedPreferences.Editor editor = pre.edit();
        editor.remove(USER_ID);
        editor.remove(USER_NAME);
        editor.remove(USER_NO);
        editor.remove(ACCESS_TOKEN);
        editor.apply();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public void onHttpCallBack(int status, int what, String data, String message) {
        switch (what) {
            case 0x001:
                showProgress(false);
                HomeFragment home_fragment = HomeFragment.newInstance(data);
                getFragmentManager().beginTransaction().replace(R.id.main_container, home_fragment).commit();
                break;
            case 0x002:
                showProgress(false);
                QuickSignFragment quick_sign_fragment = QuickSignFragment.newInstance(data);
                getFragmentManager().beginTransaction().replace(R.id.main_container, quick_sign_fragment).commit();
                break;
            case 0x003:
                showProgress(false);
                StudentMessageFragment student_message_fragment = StudentMessageFragment.newInstance(data);
                getFragmentManager().beginTransaction().replace(R.id.main_container, student_message_fragment).commit();
                break;
            case 0x004:
                showProgress(false);
                CourseFragment course_fragment = CourseFragment.newInstance(data);
                getFragmentManager().beginTransaction().replace(R.id.main_container, course_fragment).commit();
                break;
        }
    }
}
