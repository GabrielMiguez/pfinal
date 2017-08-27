package com.example.gabys.notsound;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;

public class MainActivity extends Menu {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        super.CreateMenu();

        // Get instance of Vibrator from current Context
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrates for 300 Milliseconds
        mVibrator.vibrate(500);

        startService(new Intent(this,ConnectionService.class));
    }


}
