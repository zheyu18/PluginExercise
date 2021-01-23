package com.example.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 *  思路：
 *  1、加载插件包中的class
 *  2、直接通过hook住系统的Activity启动流程来实现
 *  3、解决Resources冲突，或者怎样加载插件中的资源
 */
public class PluginManager {
    private static final String TAG = "PluginManager";
    private static PluginManager instance;

    private Context mBase;

    private Resources mResources;

    private PluginManager(Context context) {
        mBase = context;
    }

    public static PluginManager getInstance(Context context) {
        if (instance == null){
            instance = new PluginManager(context);
        }
        return instance;
    }

    public void init(){
        loadApk();
        loadAsset();
        HookUtil.HookAMS(mBase);
        HookUtil.hookMH(mBase);
        HookUtil.hookInstrumentation();
    }

    /**
     *  加载插件
     */
    private void loadApk() {
        try {
            String apkPath = mBase.getExternalFilesDir(null).getAbsolutePath()+"/pluginapp-debug.apk";
            String cachePath = mBase.getDir("plugin_cache",Context.MODE_PRIVATE).getAbsolutePath();
            DexClassLoader apkDexClassLoader = new DexClassLoader(apkPath,cachePath,cachePath,mBase.getClassLoader());
            //1、通过反射区获取插件包加载器的dexElements
            Class<?> myDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader");
            Field myPathListField = myDexClassLoader.getDeclaredField("pathList");
            myPathListField.setAccessible(true);
            Object myPathListObject = myPathListField.get(apkDexClassLoader);
            //PathList.class
            Class<?> myPathListClass = myPathListObject.getClass();
            Field myDexElements = myPathListClass.getDeclaredField("dexElements");
            myDexElements.setAccessible(true);
            Object myElements = myDexElements.get(myPathListObject);

            // 2、通过反射宿主App中的dexElements
            PathClassLoader pathClassLoader = (PathClassLoader) mBase.getClassLoader();
            Class<?> baseDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListFiled = baseDexClassLoader.getDeclaredField("pathList");
            pathListFiled.setAccessible(true);
            Object pathListObject = pathListFiled.get(pathClassLoader);
            //PathList.class
            Class<?> pathListClass = pathListObject.getClass();
            Field dexElementsField = pathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object elements = dexElementsField.get(pathListObject);

            //3、合并插件包和宿主app的类加载器的elements
            int length = Array.getLength(elements);
            int myLength = Array.getLength(myElements);
            Class<?> sigleElementClazz = elements.getClass().getComponentType();
            int newSysteLength = myLength + length;
            Object newElementsArray = Array.newInstance(sigleElementClazz, newSysteLength);
            for (int i = 0; i < newSysteLength; i++) {
                // 先融合 插件的Elements
                if (i < myLength) {
                    Array.set(newElementsArray, i, Array.get(myElements, i));
                } else {
                    Array.set(newElementsArray, i, Array.get(elements, i - myLength));
                }
            }

            //4、重新设置属性的值
            dexElementsField.set(pathListObject,newElementsArray);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void loadAsset() {
        try {
            String apkPath = mBase.getExternalFilesDir(null).getAbsolutePath() + "/pluginapp-debug.apk";
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager,apkPath);
            mResources = new Resources(assetManager,mBase.getResources().getDisplayMetrics(),
                    mBase.getResources().getConfiguration());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Resources getResources() {
        return mResources;
    }
}
