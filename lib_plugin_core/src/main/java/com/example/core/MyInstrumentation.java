package com.example.core;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MyInstrumentation extends Instrumentation {
    private final Instrumentation mBase;
    private static final String TAG = "MyInstrumentation";

    public MyInstrumentation(Instrumentation mBase) {
        this.mBase = mBase;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {

        ActivityResult result = null;
        try {
            Class<?> aClass = Class.forName("android.app.Instrumentation");
            Method method = aClass.getDeclaredMethod("execStartActivity", Context.class, IBinder.class, IBinder.class,
                    Activity.class, Intent.class, int.class, Bundle.class);
            method.setAccessible(true);
            result = (ActivityResult) method.invoke(mBase,who, contextThread, token, target, intent, requestCode, options);

        } catch (Exception e) {
            e.printStackTrace();
        }
       return result;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Activity activity = mBase.newActivity(cl, className, intent);
        reflectActivity(activity, intent);
        if (activity!=null){
            Log.i("newActivity", "activity: "+activity.toString());
        }
        return activity;
    }

    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws IllegalAccessException, InstantiationException {
        Activity activity = mBase.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
        reflectActivity(activity, intent);
        Log.i(TAG, "newActivity: 2");
        return activity;
    }


    private void reflectActivity(Activity activity, Intent intent) {
        if (!intent.getBooleanExtra("isPlugin",false))return;
        try{
            Resources resources = PluginManager.getInstance(activity).getResources();
            Class<?> clazz = Class.forName("androidx.appcompat.app.AppCompatActivity");
            Field mResourcesField = clazz.getDeclaredField("mResources");
            mResourcesField.setAccessible(true);
            mResourcesField.set(activity,resources);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
