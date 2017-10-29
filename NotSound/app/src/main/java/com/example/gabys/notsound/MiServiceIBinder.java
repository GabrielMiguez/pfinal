package com.example.gabys.notsound;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;
import java.util.Random;
import android.os.IBinder;

/*
'G|'->Comenzar Grabación (x milisegundos máximos)
                'P1|'<-Sonido PRE Guardado con ID

                'G1|'->Guardar Sonido ID
                'G1|'<-Sonido Guardado con ID

                'B1|'->Borrar Sonido ID
                'B1|'<-Sonido Borrado ID

                'T0|'->TEST ID
                'T0|'<-TEST ID

                'C1|'->CONFIG ID NIVEL
                'C1|'<-CONFIG ID OK

                   ‘C2|'->CONFIG ID NIVEL
                'C2|'<-CONFIG ID OK

                'NA|'<-Notificación Alerta
                   'N1|'<-Notificación ID
*/


public class MiServiceIBinder extends Service {
    static final int MSG_HOLA = 1;

    class MiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) { //recibe sms de activity y envia a bt
            switch (msg.what) {
                case MSG_HOLA:
                    String s = msg.getData().get("data").toString();
                    //Toast.makeText(getApplicationContext(), "Eviar: "+s, Toast.LENGTH_SHORT).show();
                    enviarSMS(s);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new MiHandler());


    private final IBinder iBinder = new MiBinderIBinder();
    private final Random random = new Random();
    //crear bluetooth
    private static Bluetooth bt;
    private btThread hilobt;

    public class MiBinderIBinder extends android.os.Binder {
        public MiServiceIBinder getService() {
            return MiServiceIBinder.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _onBind(intent);
        //Toast.makeText(this, "Service Star...", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //_onBind(intent);
        return mMessenger.getBinder();
    }

    private class btThread extends Thread {
        @Override
        public void run() {
            try {
                while (1 == 1) {
                    //tengo que poder conectarme al servicio mediante mi app y mediante el bluetooth
                    //mediante bluetooth se encarga el objeto blue
                    //mediante el servicio, seguramente tengo que exponer algun metodo de escucha(lo realizo con el Binder)
                    if (bt.Conected()) {
                        bluetoothConectado(); //nofif

                        Thread.sleep(2000); //espero 5 segndo, y vuelvo a intentar conectarme, solo cuando no estoy conectado.
                        //podria llevarlo a la pantalla de configuracion.
                        continue;
                    }

                    bluetoothDesconectado();//nofif

                    SharedPreferences prefe = getSharedPreferences("configuracion", Context.MODE_PRIVATE);
                    String dis = prefe.getString("dispositivo", "").toString();
                    if (dis.isEmpty()) {
                        Thread.sleep(5000); //espero 5 segndo, y vuelvo a revisar la config.
                        continue; //podria llevarlo a la pantalla de configuracion.
                    }

                    //si no esta conectado, intenta conectar cada 5 segundos
                    bt.conectar(dis);// una vez conectado, adentro se crean los hilos necesario para mentener la conexion abierta.
                    Thread.sleep(5000);
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public IBinder _onBind(Intent arg0) {

        if (bt != null) return iBinder;
        bt = new Bluetooth(this);

        hilobt = new btThread();
        hilobt.start();

        return iBinder;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service finalizado", Toast.LENGTH_SHORT).show();
        // se recomienda matar los hilos que el servicio inicio  ??????????????????????????????
    }


    public void bluetoothConectado() {
        // Instanciamos e inicializamos nuestro manager.
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext())
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setContentTitle("Estado Bluetooth")
                .setContentText("Conectado")
                //.setContentIntent(viewPendingIntent)
                .setWhen(System.currentTimeMillis())
                //.setVibrate(vibratePattern)
                .setPriority(Notification.PRIORITY_MIN) //Maxima prioridad
                .setAutoCancel(false);

        nManager.notify(123123, builder.build());

    }

    public void bluetoothDesconectado() {
        // Instanciamos e inicializamos nuestro manager.
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .setContentTitle("Estado Bluetooth")
                .setContentText("Desconectado")
                //.setContentIntent(viewPendingIntent)
                .setWhen(System.currentTimeMillis())
                //.setVibrate(vibratePattern)
                .setPriority(Notification.PRIORITY_MIN) //Maxima prioridad
                .setAutoCancel(false);

        nManager.notify(123123, builder.build());
    }

    private void sebdbroadcast(String s) {
        try {
            Intent ir = new Intent();
            ir.setAction("com.example.gabys.notsound.MyReceiver");
            ir.putExtra("sms", s);
            sendBroadcast(ir);
        } catch (Exception e) {
        }
    }

    private void Notificar(int idsound) {
        long[] vibratePattern = {0, 800};

        Intent i = new Intent(getApplicationContext(), SonidoAlertaActivity.class);
        i.putExtra("sonidoSeleccionado", idsound);

        Sonidos sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());
        Sonido sonido = sonidos.getSonidoByID(idsound);
        //Sonidos.ID_SONIDO_ALERTA_EXTERNA

        PendingIntent viewPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // Instanciamos e inicializamos nuestro manager.
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //nManager.cancel(1);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("ADVERTENCIA")
                .setContentText(sonido.getNombre())
                .setContentIntent(viewPendingIntent)
                .setWhen(System.currentTimeMillis())
                .setVibrate(vibratePattern)
                .setPriority(2) //Maxima prioridad
                .setAutoCancel(true);

        nManager.notify(1, builder.build());


    }

    public void procesarMsg(String s)//procesa sms que llega al servicio, desde el bt
    {
        try {
            if (s.equals("NA"))
                Notificar(Sonidos.ID_SONIDO_ALERTA_EXTERNA);
            else if (s.charAt(0) == 'N') {
                try {
                    int i = Integer.valueOf(s.substring(1));
                    Notificar(i);
                } catch (Exception e) {
                    Toast.makeText(this, "Error al Notificar Patron", Toast.LENGTH_SHORT).show();
                }
            }

            sebdbroadcast(s);
        } catch (Exception e) {
            Toast.makeText(this, "Error al Notificar", Toast.LENGTH_SHORT).show();
        }
    }

    public int enviarSMS(String sms) {
        return bt.EnviarSMS(sms);
    }

    public static boolean BTConected(){
        return bt.Conected();
    }
}