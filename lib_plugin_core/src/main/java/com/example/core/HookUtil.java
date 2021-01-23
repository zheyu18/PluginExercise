package com.example.core;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Handler;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;


public class HookUtil {
    private static final String TAG = "HookUtil";

    public static void HookAMS(Context context){
        // 这里实现的API 28
        try {
            Class<?> aClass = Class.forName("android.app.ActivityManager");
            Field field = aClass.getDeclaredField("IActivityManagerSingleton");
            field.setAccessible(true);
            Object singletonObject = field.get(null);
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstance = singletonClass.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            Object IActivityManager = mInstance.get(singletonObject);
            // 使用动态代理替换这个IActivityManager
            Class<?> IActivityManagerInterface = Class.forName("android.app.IActivityManager");
            AMSInvocationHandler amsInvocationHandler = new AMSInvocationHandler(context,IActivityManager);
            Object amsProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{IActivityManagerInterface},
                    amsInvocationHandler);
            mInstance.set(singletonObject,amsProxy);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void hookMH(Context context){
        try {
            Class<?> aClass = Class.forName("android.app.ActivityThread");
            Field currentActivityThread = aClass.getDeclaredField("sCurrentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.get(null);

            Field mHField = aClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            Object mHObject = mHField.get(activityThread);

            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);

            MHandlerProxy handlerCallBackProxy = new MHandlerProxy();
            mCallbackField.set(mHObject,handlerCallBackProxy);


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void hookInstrumentation(){
        try{
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Field field = clazz.getDeclaredField("sCurrentActivityThread");
            field.setAccessible(true);
            Object activityThreadObj = field.get(null);

            Field mInstrumentationField = clazz.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation instrumentationObj = (Instrumentation) mInstrumentationField.get(activityThreadObj);
            mInstrumentationField.set(activityThreadObj,new MyInstrumentation(instrumentationObj));

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
