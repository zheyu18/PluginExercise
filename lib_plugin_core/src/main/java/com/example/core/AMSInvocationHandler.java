package com.example.core;

import android.content.Context;
import android.content.Intent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AMSInvocationHandler implements InvocationHandler {
    private final Context context;

    private final Object baseIActivityManager;

    public AMSInvocationHandler(Context context, Object baseIActivityManager) {
        this.context = context;
        this.baseIActivityManager = baseIActivityManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())) {
            Intent intent = null;
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Intent) {
                    intent = (Intent) arg;
                    Intent intentNew = new Intent(context, StubActivity.class);
                    intentNew.putExtra("actionIntent", intent);
                    args[i] = intentNew;
                    break;
                }
            }
        }

        return method.invoke(baseIActivityManager,args);
    }
}
