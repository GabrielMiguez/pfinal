package com.example.gabys.notsound;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import java.util.Random;
import android.os.IBinder;

/*
Activity -> Srv -> Arduino
Arduino -> Srv -> Activity

Opcion Sincro
                ->Comenzar Grabacion (graba 4segundos maximos)
                ->Fin Grabacion
                <-Sonido PRE Guardado con ID

                ->Guardar Sonido ID
                <-Sonido Guardado con ID

                ->Borrar Sonido ID
                <-Sonido Borrado ID

                ->TEST ID
                <-TEST ID

                ->CONFIG ID NIVEL
                <-CONFIG ID OK

                <-Notoficacion ID
*/

/*
SonidoActivity.
//Andorid servicios comunicacion bidireccional con activity
//https://miguelangellv.wordpress.com/2011/09/14/creacion-de-servicios-con-comunicacion-bidireccional-con-un-activity/
//Activity que se comunica con el servicio
    public ComenzarGrabacion(){
        //Envia SMS BLueTOOTH
        Singleton_Binder.getInstance.Send_Message(SMSBT.Grabacion);
    }
    public Gardar(){
        //Verificar si el Servicio respondio...
        if (respondio){

        }
    }

    //El servicio se comunica con la Aplicacion mediante Notificaicones
    //, y mediante BroadCastService, donde cada activitie que queria recivir un evento debe suscribirse de alguna manera al servicio.
    public onRecive(){
        respondio=true;
    }
*/

public class MiServiceIBinder extends Service {

    private final IBinder iBinder = new MiBinderIBinder();
    private final Random random = new Random();
    //crear bluetooth
    private static Bluetooth bt;

    public class MiBinderIBinder extends android.os.Binder {
        public MiServiceIBinder getService() {
            return MiServiceIBinder.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Service Star...",Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        bt= new Bluetooth(this);
        Toast.makeText(this, "Service Binder", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    while(1==1) {
                        //tengo que poder conectarme al servicio mediante mi app y mediante el bluetooth
                        //mediante bluetooth se encarga el objeto blue
                        //mediante el servicio, seguramente tengo que exponer algun metodo de escucha(lo realizo con el Binder)
                        if (bt.Conected()){
                            Thread.sleep(5000); //espero 5 segndo, y vuelvo a intentar conectarme, solo cuando no estoy conectado.
                            //podria llevarlo a la pantalla de configuracion.
                            continue;
                        }

                        SharedPreferences prefe = getSharedPreferences("configuracion", Context.MODE_PRIVATE);
                        String dis = prefe.getString("dispositivo", "").toString();
                        if (dis.isEmpty()) {
                            Thread.sleep(5000); //espero 5 segndo, y vuelvo a revisar la config.
                            continue; //podria llevarlo a la pantalla de configuracion.
                        }

                        //si no esta conectado, intenta conectar cada 5 segundos
                        bt.conectar(dis);// una vez conectado, adentro se crean los hilos necesario para mentener la conexion abierta.
                        Thread.sleep(10000);
                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

        return iBinder;
    }


    @Override
    public void onDestroy(){
        Toast.makeText(this, "Service finalizado", Toast.LENGTH_SHORT).show();
        // se recomienda, matar los hilos que el servicio inicio  ??????????????????????????????
    }

    public int enviarSMS(String sms){
        return bt.EnviarSMS(sms);
    }
    public int getResultado() {
        Notificar();
        return random.nextInt(1000);
    }

    private void Notificar(){
        long[] vibratePattern = {0, 800};

        Intent i = new Intent(getApplicationContext(), SonidoAlertaActivity.class);
        i.putExtra("sonidoSeleccionado", -1);

        PendingIntent viewPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_ONE_SHOT);

        // Instanciamos e inicializamos nuestro manager.
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("ADVERTENCIA")
                .setContentText("Sonido detectado")
                .setContentIntent(viewPendingIntent)
                .setWhen(System.currentTimeMillis())
                .setVibrate(vibratePattern)
                .setPriority(2) //Maxima prioridad
                .setAutoCancel(true);

        nManager.notify(12345, builder.build());
    }

    public void procesarMsg(String s)
    {
        try {
            Notificar();
        } catch (Exception e) {
        }
    }
}