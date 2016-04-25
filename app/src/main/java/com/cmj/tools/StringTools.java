package com.cmj.tools;

import com.cmj.face.R;

/**
 * Created by 陈茂建 on 2016/4/16.
 */
public class StringTools {

    public static int getActionBarTitle(int state){
        switch (state) {
            case 1:
                return R.string.left_slide_menu_home;
            case 2:
                return R.string.left_slide_menu_sign;
            case 3:
                return R.string.left_slide_menu_students;
            case 4:
                return R.string.left_slide_menu_course;
            case 5:
                return R.string.left_slide_menu_setting;
        }
        return R.string.left_slide_menu_home;
    }
}
