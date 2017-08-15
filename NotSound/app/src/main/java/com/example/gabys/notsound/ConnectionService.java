package com.example.gabys.notsound;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

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

        new Thread(new Runnable() {

            @Override
            public void run() {

            try {
                long[] vibratePattern = {0, 800};

                Intent i = new Intent(getApplicationContext(), SonidoAlertaActivity.class);
                i.putExtra("sonidoSeleccionado", 1);

                PendingIntent viewPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_ONE_SHOT);

                // Instanciamos e inicializamos nuestro manager.
                NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        getBaseContext())
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("ADVERTENCIA")
                        .setContentText("Sonido detectado")
                        .setContentIntent(viewPendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .setVibrate(vibratePattern)
                        .setPriority(2) //Maxima prioridad
                        .setAutoCancel(true);

                Thread.sleep(5000);

                nManager.notify(12345, builder.build());

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            }
        }).start();

        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();
        Toast.makeText(this,"Service Destroyed...",Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
