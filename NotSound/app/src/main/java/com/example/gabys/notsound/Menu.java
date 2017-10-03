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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Gabys on 26/08/2017.
 * Clase padre para la creacion del menu en todos los activities
 */


public class Menu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    BroadcastReceiver receiver = null ;
    IntentFilter intentFilter;

    String ayuda_titulo1="";
    String ayuda_cuerpo1="";
    String ayuda_titulo2="";
    String ayuda_cuerpo2="";
    String ayuda_titulo3="";
    String ayuda_cuerpo3="";
    String ayuda_titulo4="";
    String ayuda_cuerpo4="";

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

            // Atar el servicio a la actividad
            bindService(intent, mConnection,this.BIND_AUTO_CREATE);

            if (this.getClass().getSimpleName().equals("MainActivity"))
                startService(intent);
        //}


    }

    public void processReceive(Context context, Intent intent) {
        Log.i("DESARROLLO","processReceive ");
        String s=intent.getExtras().getSerializable("sms").toString();
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
        this.ActionRecive(s);
        try{
            int i=-1;
            if (s.equals("NA"))
                i=99;
            else  if (s.charAt(0)=='N'){
                i=  Integer.valueOf(s.substring(1));
            }
            if (i>=0){
                //puebo si puedo abrir el activity directamete
                Intent inte = new Intent(getApplicationContext(), SonidoAlertaActivity.class);
                inte.putExtra("sonidoSeleccionado", i);
                startActivity(inte);
            }
        }catch (Exception e ){
            Toast.makeText(this, "Error al Notificar Activity Patron", Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo para Implememtar Accion en cada Activity
    public void ActionRecive(String s){

    }

    public void sendMSGSRV(String s) {
        if (!mBound) return;

        //Creamos y enviamos un mensaje al servicio, asignandole como dato el nombre del EditText
        Message msg = Message.obtain(null, MiServiceIBinder.MSG_HOLA, 0, 0);
        Bundle b = new Bundle();
        b.putString("data", s);
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
        //Log.i("DESARROLLO","onDestroy ");
        //unregisterReceiver(receiver);
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

        if (id == R.id.nav_sonidos) {
            Intent i = new Intent(this, SonidosActivity.class );
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else if (id == R.id.nav_alerta_externa) {
            Intent i = new Intent(this, SonidosEdicionActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("sonidoSeleccionado", Sonidos.POSICION_SONIDO_ALERTA_EXTERNA); // Sonido Alerta
            startActivity(i);
        } else if (id == R.id.nav_configuracion) {
            Intent i = new Intent(this, ConfigActivity.class );
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else if (id == R.id.nav_nosotros) {
            Intent i = new Intent(this, NosotrosActivity.class );
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(this.getClass().getSimpleName().equals("MainActivity") ){
                finish();
            } else {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    public void setAyudaParametros(String titulo1, String cuerpo1, String titulo2, String cuerpo2, String titulo3, String cuerpo3, String titulo4, String cuerpo4){
        this.ayuda_titulo1=titulo1;
        this.ayuda_cuerpo1=cuerpo1;

        this.ayuda_titulo2=titulo2;
        this.ayuda_cuerpo2=cuerpo2;

        this.ayuda_titulo3=titulo3;
        this.ayuda_cuerpo3=cuerpo3;

        this.ayuda_titulo4=titulo4;
        this.ayuda_cuerpo4=cuerpo4;
    }

    public void InflateAyuda(){

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View customView = inflater.inflate(R.layout.ayuda,null);


        // Initialize a new instance of popup window
        final PopupWindow mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextView t1 = (TextView) customView.findViewById(R.id.txtvw_titulo1);
        TextView c1 = (TextView) customView.findViewById(R.id.txtvw_cuerpo1);
        TextView t2 = (TextView) customView.findViewById(R.id.txtvw_titulo2);
        TextView c2 = (TextView) customView.findViewById(R.id.txtvw_cuerpo2);
        TextView t3 = (TextView) customView.findViewById(R.id.txtvw_titulo3);
        TextView c3 = (TextView) customView.findViewById(R.id.txtvw_cuerpo3);
        TextView t4 = (TextView) customView.findViewById(R.id.txtvw_titulo4);
        TextView c4 = (TextView) customView.findViewById(R.id.txtvw_cuerpo4);

        t1.setText(this.ayuda_titulo1);
        c1.setText(this.ayuda_cuerpo1);
        t2.setText(this.ayuda_titulo2);
        c2.setText(this.ayuda_cuerpo2);
        t3.setText(this.ayuda_titulo3);
        c3.setText(this.ayuda_cuerpo3);
        t4.setText(this.ayuda_titulo4);
        c4.setText(this.ayuda_cuerpo4);

        // Set a click listener for the popup window close button
        Button closeButton = (Button) customView.findViewById(R.id.btn_cerrar);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.showAtLocation((DrawerLayout) findViewById(R.id.drawer_layout), Gravity.CENTER,0,0);
    }

    public void abrirAyuda(View view){
        //this.setTextoAyuda();
        this.InflateAyuda();
    }
/*
    public void setTextoAyuda(String titulo1, String cuerpo1, String titulo2, String cuerpo2){
        TextView t1 = (TextView) findViewById(R.id.txtvw_titulo1);
        t1.setText(titulo1);
        TextView c1 = (TextView) findViewById(R.id.txtvw_cuerpo1);
        c1.setText(cuerpo1);

        TextView t2 = (TextView) findViewById(R.id.txtvw_titulo2);
        t2.setText(titulo2);
        TextView c2 = (TextView) findViewById(R.id.txtvw_cuerpo2);
        c2.setText(cuerpo2);
    }
*/
}
