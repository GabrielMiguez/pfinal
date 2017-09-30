package com.example.gabys.notsound;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;

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

        String menuAyuda_titulo1="Sonidos";
        String menuAyuda_cuerpo1="Edita la lista de sonidos que podrás detectar en el modo interior.";
        String menuAyuda_titulo2="Alerta Externa";
        String menuAyuda_cuerpo2="Personaliza la notificación que recibirás en el modo exterior.";
        String menuAyuda_titulo3="Configuación";
        String menuAyuda_cuerpo3="Ajusta los parámetros de la solución.";
        String menuAyuda_titulo4="Nosotros";
        String menuAyuda_cuerpo4="Envianos dudas y sugerencias.";

        super.setAyudaParametros(
                menuAyuda_titulo1,
                menuAyuda_cuerpo1,
                menuAyuda_titulo2,
                menuAyuda_cuerpo2,
                menuAyuda_titulo3,
                menuAyuda_cuerpo3,
                menuAyuda_titulo4,
                menuAyuda_cuerpo4);

        Sonidos sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        if (sonidos.getSonidoByID(Sonidos.ID_SONIDO_ALERTA_EXTERNA) == null){
            sonidos.addSonido(getApplicationContext(),new Sonido(Sonidos.ID_SONIDO_ALERTA_EXTERNA,"Alerta Externa", null, true));
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
/*
    @Override
    public void abrirAyuda(View view) {

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View customView = inflater.inflate(R.layout.ayuda,null);


        // Initialize a new instance of popup window
        final PopupWindow mPopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );

        // Set a click listener for the popup window close button
        Button closeButton = (Button) customView.findViewById(R.id.end_data_send_button);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.showAtLocation((DrawerLayout) findViewById(R.id.drawer_layout), Gravity.CENTER,0,0);
    }
    */
}
