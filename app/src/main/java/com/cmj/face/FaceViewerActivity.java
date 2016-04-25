package com.cmj.face;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.cmj.network.BasicNetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class FaceViewerActivity extends BaseAppCompatActivity {
    String fileName;
    private ImageView img;
    int sign_id, student_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        haveProgressBar = true; // can show ProgressBar
        pre = PreferenceManager.getDefaultSharedPreferences(this);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        sign_id = intent.getIntExtra("sign_id", -1);
        student_id = intent.getIntExtra("student_id", -1);
        if (sign_id == -1 || student_id == -1) {
            Toast.makeText(this, R.string.error_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bm = (Bitmap) intent.getExtras().get("face");
        img = (ImageView) findViewById(R.id.person_face);
        if (img != null) {
            img.setImageBitmap(bm);
        }
        try {
            fileName = "face" + System.currentTimeMillis() + ".png";
            BufferedOutputStream bos = new BufferedOutputStream(openFileOutput(fileName, MODE_PRIVATE));
            if (bm != null) {
                bm.compress(Bitmap.CompressFormat.PNG, 80, bos);
            }
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.face_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.submit_face:
                sendFace();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendFace() {
        if (fileName == null) {
            Toast.makeText(this, R.string.error_photo_not_found, Toast.LENGTH_SHORT).show();
        }
        try {
            showProgress(true);
            FileInputStream fis = openFileInput(fileName);
            HashMap<String, String> m = getAccessTokenMap();
            m.put("sign_id", sign_id + "");
            m.put("student_id", student_id + "");
//            m.put("course_id", "");
            httpPostFile(getString(R.string.address_sign_verify, BasicNetwork.BASIC_HOST), m, fis, 0x001, true);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_submit_photo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHttpCallBack(int status, int what, String data, String message) {
        switch (what) {
            case 0x001:
                Log.v("HHH", data);
                showProgress(false);
                try {
                    JSONObject json = new JSONObject(data);
                    if (json.getInt("status") == 200) {
                        JSONObject D = json.getJSONObject("data");
                        if (D == null) {
                            Toast.makeText(this, R.string.network_error_bad_required, Toast.LENGTH_SHORT).show();
                        } else if (D.getBoolean("status")) {
                            Intent intent = new Intent();
                            intent.putExtra("statue",true);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(this, R.string.fail_to_sign, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
