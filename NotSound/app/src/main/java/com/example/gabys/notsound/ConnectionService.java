package com.example.gabys.notsound;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by VirtualBox on 10/08/2017.
 */

public class ConnectionService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Service Started...",Toast.LENGTH_LONG).show();

        // Get instance of Vibrator from current Context
        //Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrates for 300 Milliseconds
        //mVibrator.vibrate(300);
        //long[] pattern = { 100, 300};
        //mVibrator.vibrate(pattern , 0);

        // Instanciamos e inicializamos nuestro manager.
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getBaseContext())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("MyService")
                .setContentText("Terminó el servicio!")
                .setWhen(System.currentTimeMillis());

        try {
            // Simulamos trabajo de 10 segundos.
            Thread.sleep(15000);

            nManager.notify(12345, builder.build());
            // Get instance of Vibrator from current Context
            Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrates for 300 Milliseconds
            mVibrator.vibrate(500);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();
        Toast.makeText(this,"Service Destroyed...",Toast.LENGTH_LONG).show();

        // Get instance of Vibrator from current Context
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
