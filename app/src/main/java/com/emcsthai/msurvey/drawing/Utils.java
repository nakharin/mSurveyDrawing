package com.emcsthai.msurvey.drawing;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
import android.widget.Toast;

/**
 * Created by nakharin on 21/3/2018 AD.
 */

public class Utils {

    public static int[] getDrawableArray(Context context, @ArrayRes int id) {

        TypedArray ar = context.getResources().obtainTypedArray(id);

        int len = ar.length();
        int[] resIds = new int[len];

        for (int i = 0; i < len; i++) {
            resIds[i] = ar.getResourceId(i, 0);
        }

        ar.recycle();

        return resIds;
    }

    public static void ToastSaveComplete(Context context) {
            try {
                Toast.makeText(context, "บันทึกเรียบร้อย", Toast.LENGTH_SHORT)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
