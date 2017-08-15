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

/**
 * Created by VirtualBox on 10/08/2017.
 */

public class ConnectionService extends Service {

    private ArrayList<Sonido> sonidos;
    private String ser;

    public void recuperarSonidos() {
        ser = SerializeObject.ReadSettings(this, "notas.dat");
        if (ser != null && !ser.equalsIgnoreCase("")) {
            this.sonidos = (ArrayList<Sonido>)SerializeObject.stringToObject(ser);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sonidos = new ArrayList<Sonido>();
        recuperarSonidos();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this,"Service Started...",Toast.LENGTH_LONG).show();
        Toast.makeText(this,Integer.toString(sonidos.size()),Toast.LENGTH_LONG).show();


        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    long[] vibratePattern = {0, 800};

                    // Build intent for notification content
                    //Intent viewIntent = new Intent(getApplicationContext(), SonidoAlertaActivity.class);
                    //viewIntent.putExtra("sonidos", sonidos);
                    //viewIntent.putExtra("sonidoSeleccionado", 1);
                    //PendingIntent viewPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, viewIntent, 0);
/*
                    Intent notificationIntent = new Intent(getApplicationContext(), SonidoAlertaActivity.class);
                    // set intent so it does not start a new activity
                    notificationIntent.putExtra("sonidoSeleccionado", "1");
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent viewPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                            notificationIntent, 0);
*/
                    Intent i = new Intent(getApplicationContext(), SonidoAlertaActivity.class);
                    i.putExtra("sonidoSeleccionado", 28);

                    PendingIntent viewPendingIntent = PendingIntent.getActivity(getApplicationContext(), FLAG_CANCEL_CURRENT, i, 0);

                    // Instanciamos e inicializamos nuestro manager.
                    NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(
                            getBaseContext())
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle("Notificacion 2!!")
                            .setContentText("Notificacion Servicio!")
                            .setContentIntent(viewPendingIntent)
                            .setWhen(System.currentTimeMillis())
                            .setVibrate(vibratePattern)
                            .setPriority(2); //Maxima prioridad

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
