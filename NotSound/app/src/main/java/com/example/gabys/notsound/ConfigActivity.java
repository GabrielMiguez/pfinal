package com.example.gabys.notsound;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static java.lang.Thread.sleep;

public class ConfigActivity extends Menu {

    public BluetoothAdapter BA;
    public BluetoothDevice BD;

    public static int GRABACION_TIMEOUT = 15000 + 1000; // Extiendo un segundo mas el Timeout para que no coinicda con el tiempo de grabacion.

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter mArrayAdapter;
    private Spinner lstdispos;
    private Switch swB, swM;
    private FloatingActionButton btnBuscar;
    public TextView txtInfo;
    private boolean grabacionExitosa;
    ProgressDialog progress;
    Runnable progressRunnable;
    Handler pdCanceller;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        super.CreateMenu();

        String menuAyuda_titulo1="Bluetooth";
        String menuAyuda_cuerpo1="Habilite el bluetooth para que la aplicación funcione correctamente. La función de deshabilitarlo no está permitida";
        String menuAyuda_titulo2="Modo";
        String menuAyuda_cuerpo2="Intercambie los modos de funcionamiente según en el ambiente en que se encuentre.";
        String menuAyuda_titulo3="Dispositivo";
        String menuAyuda_cuerpo3="Busque el dispositivo electrónico al que quiere conectarse.";
        String menuAyuda_titulo4="Restablecer valores por defecto";
        String menuAyuda_cuerpo4="Borre los sonidos almacenados en el dispositivo electrónico y en la aplicación mobile.";

        super.setAyudaParametros(
                menuAyuda_titulo1,
                menuAyuda_cuerpo1,
                menuAyuda_titulo2,
                menuAyuda_cuerpo2,
                menuAyuda_titulo3,
                menuAyuda_cuerpo3,
                menuAyuda_titulo4,
                menuAyuda_cuerpo4);

        swB = (Switch) findViewById(R.id.swB);
        swM = (Switch) findViewById(R.id.swM);
        lstdispos = (Spinner) findViewById(R.id.lstDispos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnBuscar = (FloatingActionButton) findViewById(R.id.btnBuscar);
        txtInfo = (TextView) findViewById(R.id.txtInfo);
        txtInfo.setText("");

        //setSupportActionBar(toolbar);
        BA = BluetoothAdapter.getDefaultAdapter();  // tomo mi bluetooth

        //Verifico el estado del bluetooth

        if (BA != null) {
            if (!BA.isEnabled()) {
                swB.setChecked(false);
                Toast.makeText(getApplicationContext(), "Bluetooth DESACTIVADO", Toast.LENGTH_LONG).show();
            } else {
                swB.setChecked(true);
                ObtenerDispos();
            }
        }

        swB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Log.v("Switch State=", ""+isChecked);
                if (!isChecked) {
                    //isChecked = true;
                    swB.setChecked(true); //coloca como antes, no deja desactivar

                } else {
                    btnOnBluetooth();
                    swB.setEnabled(false); // una vez activado, se deshabilita el switch para que no pueda desactivar el bluetooth
                }
            }
        });


        swM.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

             /*
            }
        });

        swM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                */
                //Log.v("Switch State=", ""+isChecked);
                grabacionExitosa = false; // Si la grabacion es exitosa se modifica desde el metodo ActionRecive

                progress = new ProgressDialog(ConfigActivity.this);
                //progress.setTitle("Grabando");
                progress.setMessage("Configurando Dispositivo. Espere...");
                progress.setCancelable(false);
                progress.show();

                if (!swM.isChecked()) {
                    swM.setChecked(true); //coloca como antes, no deja desactivar
                    sendMSGSRV("C1|");
                } else {
                    swM.setChecked(false); //coloca como antes, no deja desactivar
                    sendMSGSRV("C2|");
                }

                // Se crea un proceso para que al superar el TIMEOUT de GRABACION se ejecute.
                // Si la grabacion no fue exitosa: se va a cerrar el ProgressDialog y va a mostrar un mensaje de error.
                progressRunnable = new Runnable() {
                    @Override
                    public void run() {

                        progress.cancel();
                        if (!grabacionExitosa) {
                            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(ConfigActivity.this);
                            dialogo1.setTitle("Atención");
                            dialogo1.setMessage("Se superó el tiempo de espera.");
                            dialogo1.setCancelable(false);
                            dialogo1.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogo1, int id) {
                                }
                            });

                            dialogo1.show();
                        }
                    }
                };

                pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, ConfigActivity.GRABACION_TIMEOUT); // Se setea el tiempo de espera antes de que llame al proceso de arriba.
            }
        });

        recuperar();
        //Obtengo el modo en el que esta el dispositivo?
    }

    //Metodo para Implememtar Accion en cada Activity
    @Override
    public void ActionRecive(String s){
        String msgInfo="";

        switch (s) {
            case "T0": msgInfo="Probando conexión"; break;
            case "C1":
                msgInfo="Conexión exitosa. Modo: EXTERIOR";
                swM.setChecked(false);
                break;
            case "C2":
                msgInfo="Conexión exitosa. Modo: INTERIOR";
                swM.setChecked(true);
                break;
            default: msgInfo="Conexión exitosa"; break;
        }
        s = txtInfo.getText().toString() + ((char) 10) + ((char) 13) + s;

        txtInfo.setText(msgInfo);
        grabacionExitosa=true;
        if (progress != null)
            progress.cancel();
    }

    public void test(View view){
        sendMSGSRV("T0|");
    }

    public void ClearEPPROM(View view){

        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(ConfigActivity.this);
        dialogo1.setTitle("Importante");
        dialogo1.setMessage("¿Está seguro que desea restablecer los valores de fábrica? " +
                            "Si confirma, perderá todos sus Sonidos guardados en su dispositivo móvil y en el dispositivo electrónico");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                if (sendMSGSRV("CE|")) {
                    // Limpio los sonidos y vuelvo a insertar el sonido de alerta externa por defecto
                    Sonidos sonidos = new Sonidos();
                    sonidos.loadSonidos(getApplicationContext());
                    sonidos.cleanSonidos(getApplicationContext());
                    if (sonidos.getSonidoByID(Sonidos.ID_SONIDO_ALERTA_EXTERNA) == null){
                        sonidos.addSonido(getApplicationContext(),new Sonido(Sonidos.ID_SONIDO_ALERTA_EXTERNA,"Alerta Externa", null, true));
                    }
                } else {
                    AlertDialog.Builder dialogoError = new AlertDialog.Builder(ConfigActivity.this);
                    dialogoError.setTitle("Error");
                    dialogoError.setMessage("El dispositivo móvil no está conectado al dispositivo electrónico.");
                    dialogoError.setCancelable(false);
                    dialogoError.setPositiveButton("OK", null);
                    dialogoError.show();
                }
            }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });

        dialogo1.show();



    }

    public void btnOnBluetooth() {
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);
        Toast.makeText(getApplicationContext(), "Activando", Toast.LENGTH_LONG).show();
        ObtenerDispos();
    }

    public void ObtenerDispos() {
        pairedDevices = BA.getBondedDevices();
        //recorro el pairedDev
        ArrayList list = new ArrayList();
        for (BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
        }

        //actualizo el adapter de mi view
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        lstdispos.setAdapter(mArrayAdapter);
        if (list.isEmpty()) {
            String[] opciones = {"-No Hay Dispositivos-"};
            mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opciones);
            lstdispos.setAdapter(mArrayAdapter);
        }
    }

    public void btnBuscar(View view) {
        Toast notificacion = Toast.makeText(this, "Buscando...", Toast.LENGTH_LONG);
        notificacion.show();

        //aceptar nuevos dispositivos encontrados y agregalos a la lista
        BA.startDiscovery();

        // Create a BroadcastReceiver for ACTION_FOUND
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    boolean e = false;
                    for (int i=0; i<mArrayAdapter.getCount() ; i++) {
                        if (device.getName().toString().equals( mArrayAdapter.getItem(i).toString())){
                            e=true;
                            break;
                        }
                    }
                    if (!e)
                        mArrayAdapter.add(device.getName());
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

    }

    public void recuperar(){
        try {
            SharedPreferences prefe = getSharedPreferences("configuracion", Context.MODE_PRIVATE);
            //et1.setText(prefe.getString("mail",""));

            boolean e = false;

            //String dis = prefe.getString("dispositivo", "").toString();
            String dis = "HC-05"; // fijo el nombre del arduino


            if (dis.isEmpty()) return;
            int i = 0;
            if (mArrayAdapter == null){
                String[] opciones = {dis};
                mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opciones);
                lstdispos.setAdapter(mArrayAdapter);
            }
            for (i=0; i<mArrayAdapter.getCount() ; i++) {
                if (dis.toString().equals( mArrayAdapter.getItem(i).toString())){
                    e=true;
                    break;
                }
            }
            if (!e)
                mArrayAdapter.add(dis);

            //selecciono el item
            lstdispos.setSelection(i,true);

            if (prefe.getString("modo", "").toString().equals("C1")){
                swM.setChecked(true);
            }else{
                swM.setChecked(false);
            }
        }catch (Exception e){

        }
    }

    public void guardar(){
        try {
            SharedPreferences preferencias=getSharedPreferences("configuracion",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferencias.edit();
            editor.putString("dispositivo", lstdispos.getSelectedItem().toString());
            if (swM.isChecked()){
                editor.putString("modo", "C1");
            }else{
                editor.putString("modo", "C2");
            }

            editor.commit();
            //Toast.makeText(this,"Datos grabados", Toast.LENGTH_LONG).show();

        }catch (Exception e){

        }

        //Intent i = new Intent(this, MainActivity.class );
        //startActivity(i);
    }

    @Override
    protected void onStop() {
        guardar();
        super.onStop();
    }
}
