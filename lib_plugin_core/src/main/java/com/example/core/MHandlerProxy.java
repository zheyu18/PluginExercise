package com.example.core;


import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.List;

public class MHandlerProxy implements Handler.Callback {
    public static final int EXECUTE_TRANSACTION = 159;

    @Override
    public boolean handleMessage(Message msg) {
        Log.e("MHandlerProxy", "handleMessage:msg.what=" + msg.what);
        if (msg.what == EXECUTE_TRANSACTION) {
            try {
                Class<?> aClass = Class.forName("android.app.servertransaction.ClientTransaction");
                //mActivityCallbacks
                Field mActivityCallbacksField = aClass.getDeclaredField("mActivityCallbacks");
                mActivityCallbacksField.setAccessible(true);
                Object mActivityCallbacksObj = mActivityCallbacksField.get(msg.obj);
                if (!aClass.isInstance(msg.obj)){
                    return true;
                }
                List list = (List) mActivityCallbacksObj;
                for (Object item : list) {
                    Class<?> clazz = item.getClass();
                    if (clazz.getName().equals("android.app.servertransaction.LaunchActivityItem")){
                        Field mIntentField = clazz.getDeclaredField("mIntent");
                        mIntentField.setAccessible(true);
                        Intent intent = (Intent) mIntentField.get(item);
                        Intent originalIntent = intent.getParcelableExtra("actionIntent");
                        if (originalIntent!=null){
                            // 替换回来
                            mIntentField.set(item,originalIntent);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
