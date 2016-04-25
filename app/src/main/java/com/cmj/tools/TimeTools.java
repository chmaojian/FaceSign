package com.cmj.tools;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by 根深 on 2016/4/18.
 */
public class TimeTools {
    public static String MissileToString(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);
        return formatter.format(time*1000);
    }
}
