package com.example.gabys.notsound;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Menu {

    protected void _onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        super.CreateMenu();

        // Get instance of Vibrator from current Context
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrates for 300 Milliseconds
        mVibrator.vibrate(500);

        Intent intent = new Intent(this, ConnectionService.class);

        // con starservice siempre quedara el servicio activo
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        super.CreateMenu();

        Sonidos sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        if (sonidos.getSonidoByID(0) == null){
            sonidos.addSonido(getApplicationContext(),new Sonido(0,"Alerta Externa", null, true));
        }


        //todos los activities que hereden de Menu, tiene el servicio bindeado, desde aca lo creo normal para que no muera nunca
        //Intent intent = new Intent(this, MiServiceIBinder.MiBinderIBinder.class);
        // con starservice siempre quedara el servicio activo
        //startService(intent);

/*
        if (mServiceIBinder != null) {
            mServiceIBinder.stopForeground(true);
            unbindService(sConnectionIB);
            mServiceIBinder = null;
        }
*/
    }

}
