package com.example.gabys.notsound;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by Gabys on 26/08/2017.
 * Clase padre para la creacion del menu en todos los activities
 */


public class Menu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    BroadcastReceiver receiver = null ;
    IntentFilter intentFilter;

    //private MiServiceIBinder mServiceIBinder;
    boolean mBound;
    //Creamos una interface ServiceConection para enlazar el servicio con el objeto mService
    // CONFIGURACION INTERFACE SERVICECONNECTION IBINDER
    /*
    private ServiceConnection sConnectionIB = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MiServiceIBinder.MiBinderIBinder binder = (MiServiceIBinder.MiBinderIBinder) service;
            mServiceIBinder = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound=false;
        }
    };
    */

    Messenger mService = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            mService = new Messenger(service);
            mBound = true;
            Log.d("estado","conectado");
        }

        public void onServiceDisconnected(ComponentName className) {
            //Cuando termina la conexión con el servicio de forma inesperada. no cuando el cliente se desenlaza
            mService = null;
            mBound = false;
        }
    };

    public void CreateMenu(){
        //Titulo
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //boton para acceder al meno de opciones de la izquierda
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // se configura quien atender el eventero selected
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_nosotros);

        try {
            intentFilter = new IntentFilter("com.example.gabys.notsound.MyReceiver");

            Log.i("DESARROLLO","DESARROLLO111 ");
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i("DESARROLLO","PROBANDO ONRECEIVE ");
                    processReceive(context, intent);
                }
            };
            Log.i("DESARROLLO","DESARROLLO1112222 ");
            registerReceiver(receiver,intentFilter);
        }catch (Exception e){
            Log.e("TAGERROR","ERROR CON CODIGO: "+ e);
        }

        //Si el serviicio no esta bindeado a este activity, lo bindeo
        //if (mServiceIBinder == null) {
            Intent intent = new Intent(Menu.this, MiServiceIBinder.class);
            //bindService(intent, sConnectionIB, Context.BIND_AUTO_CREATE);


            //nuevo, corro el servicio y lo ato al activity
            // Iniciar el servicio
            startService(intent);
            // Atar el servicio a la actividad
            bindService(intent, mConnection,this.BIND_AUTO_CREATE);

        //}


    }

    public void processReceive(Context context, Intent intent) {
        Log.i("DESARROLLO","processReceive ");
        Toast.makeText(context, "PROBANDO ", Toast.LENGTH_LONG).show();
    }

    public void sendMSGSRV() {
        if (!mBound) return;

        //Creamos y enviamos un mensaje al servicio, asignandole como dato el nombre del EditText
        Message msg = Message.obtain(null, MiServiceIBinder.MSG_HOLA, 0, 0);
        Bundle b = new Bundle();
        b.putString("data", "Hola");
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onResume(){
        Log.i("DESARROLLO","onResume ");
        super.onResume();
        registerReceiver (receiver,intentFilter);
    }
    public void onPause(){
        Log.i("DESARROLLO","onPause ");
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("DESARROLLO","onDestroy ");
        unregisterReceiver(receiver);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        /*
        if (mServiceIBinder != null) {
            String resultado = Integer.toString(mServiceIBinder.getResultado());
            Toast.makeText(this,"Service Bluetooth: " + resultado ,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"Service OFF" ,Toast.LENGTH_LONG).show();
        }
        */

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent i = new Intent(this, SonidosActivity.class );
            startActivity(i);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {
            Intent i = new Intent(this, ConfigActivity.class );
            startActivity(i);
        } else if (id == R.id.nav_manage) {
            Intent i = new Intent(this, NosotrosActivity.class );
            startActivity(i);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        sendMSGSRV();
        return true;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}
