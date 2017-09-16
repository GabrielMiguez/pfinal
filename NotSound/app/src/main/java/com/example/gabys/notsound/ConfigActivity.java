package com.example.gabys.notsound;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class ConfigActivity extends Menu {

    public BluetoothAdapter BA;
    public BluetoothDevice BD;

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter mArrayAdapter;
    private Spinner lstdispos;
    private Switch swB;
    private FloatingActionButton btnBuscar;
    public TextView txtInfo;
    private Button btnTestcnx;
    private ImageButton btnGuardar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        super.CreateMenu();

        swB = (Switch) findViewById(R.id.swB);
        lstdispos = (Spinner) findViewById(R.id.lstDispos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnBuscar = (FloatingActionButton) findViewById(R.id.btnBuscar);
        txtInfo = (TextView) findViewById(R.id.txtInfo);
        btnTestcnx = (Button) findViewById(R.id.btnTestcnx);
        btnGuardar = (ImageButton) findViewById(R.id.btnGuardar);

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

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnBuscar();
            }
        });

        swB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Log.v("Switch State=", ""+isChecked);
                if (!isChecked) {
                    //isChecked = true;
                    swB.setChecked(true); //coloca como antes, no deja desactivar
                } else
                    btnOnBluetooth();
            }
        });


        btnGuardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                guardar();
            }
        });

        btnTestcnx.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                test();
            }
        });
        recuperar();
    }

    public void test(){
        sendMSGSRV("TC|");
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

    public void btnBuscar() {
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
            String dis = prefe.getString("dispositivo", "").toString();
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

        }catch (Exception e){

        }
    }

    public void guardar(){
        try {
            SharedPreferences preferencias=getSharedPreferences("configuracion",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferencias.edit();
            editor.putString("dispositivo", lstdispos.getSelectedItem().toString());
            editor.commit();
            Toast.makeText(this,"Datos grabados", Toast.LENGTH_LONG).show();
        }catch (Exception e){

        }

    }
}
