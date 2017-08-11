package com.example.gabys.notsound;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
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
        // Variables de la notificacion
        NotificationManager nm;
        String ns = Context.NOTIFICATION_SERVICE;
        int icon = R.drawable.ic_menu_camera;

        // Inicio el servicio de notificaciones accediendo al servicio
        nm = (NotificationManager) getSystemService(ns);

        // Realizo una notificacion por medio de un metodo hecho por mi
        Notification notif = new Notification(icon, "texto", System.currentTimeMillis());;
notif.
        notificacion(notif, icon, "titulo contenido", "texto contenido", "texto extendido");

        // Lanzo la notificacion creada en el paso anterior
        nm.notify(1, notif);

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


    public void notificacion(Notification notif, int icon, CharSequence textoEstado, CharSequence titulo, CharSequence texto) {
        // Capturo la hora del evento
        long hora = System.currentTimeMillis();

        // Definimos la accion de la pulsacion sobre la notificacion (esto es opcional)
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Defino la notificacion, icono, texto y hora

        //notif.setLatestEventInfo(context, titulo, texto, contentIntent);

        //Defino que la notificacion sea permamente
        notif.flags = Notification.FLAG_ONGOING_EVENT;
    }
}
