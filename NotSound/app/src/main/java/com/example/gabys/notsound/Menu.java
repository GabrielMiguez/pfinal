package com.example.gabys.notsound;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by Gabys on 26/08/2017.
 * Clase padre para la creacion del menu en todos los activities
 */


public class Menu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private MiServiceIBinder mServiceIBinder;
    boolean mBound;

    //Creamos una interface ServiceConection para enlazar el servicio con el objeto mService
    // CONFIGURACION INTERFACE SERVICECONNECTION IBINDER
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

        //Si el serviicio no esta bindeado a este activity, lo bindeo
        if (mServiceIBinder == null) {
            Intent intent = new Intent(Menu.this, MiServiceIBinder.class);
            bindService(intent, sConnectionIB, Context.BIND_AUTO_CREATE);
        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        if (mServiceIBinder != null) {
            String resultado = Integer.toString(mServiceIBinder.getResultado());
            //texto.setText("Su resuldato es: " + resultado);
            Toast.makeText(this,"Service Bluetooth: " + resultado ,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"Service OFF" ,Toast.LENGTH_LONG).show();
        }

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