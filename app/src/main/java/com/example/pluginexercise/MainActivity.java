package com.example.pluginexercise;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Class<?> aClass = Class.forName("com.example.pluginapp.MainActivity");
            Method good = aClass.getDeclaredMethod("good");
            good.invoke(aClass.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickView(View view) {
        Intent intent = new Intent();
        intent.setClassName("com.example.pluginapp",
                "com.example.pluginapp.MainActivity");
        intent.putExtra("isPlugin", true);
        startActivity(intent);
    }
}
