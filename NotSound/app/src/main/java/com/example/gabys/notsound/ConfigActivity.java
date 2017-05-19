package com.example.gabys.notsound;

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

public class ConfigActivity extends AppCompatActivity {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothAdapter BA;
    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            procesarMsg(msg);
        }
    };
    private static final int MESSAGE_READ = 0;
    private ConnectThread hilocon;
    private ConnectedThread hilosms;
    public BluetoothDevice BD;

    public String buffer="",read="";

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            BA.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

        public boolean isConnected() {
            return mmSocket.isConnected();
        }
    }

    private class ConnectedThread extends Thread {


        public BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()


            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, readMessage).sendToTarget();
                    //procesarMsg(msg);
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String sms) {
//            int bytes; // bytes from send()
//            bytes = sms.length();
//            byte[] buffer = new byte[bytes];  // buffer store for the stream
//            for (int i = 0; i < bytes; i++) {
//                buffer[i] = (byte) sms.charAt(i);
//            }

            write(sms.getBytes());
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }


    }

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter mArrayAdapter;
    private Spinner lstdispos;
    private Switch swB;
    private Button btnConectar;
    private FloatingActionButton btnBuscar;
    public TextView txtInfo;
    private Button btnTestcnx;
    public TextView txtEstado;
    private ImageButton btnGuardar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        swB = (Switch) findViewById(R.id.swB);
        lstdispos = (Spinner) findViewById(R.id.lstDispos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnConectar = (Button) findViewById(R.id.btnConectar);
        btnBuscar = (FloatingActionButton) findViewById(R.id.btnBuscar);
        txtInfo = (TextView) findViewById(R.id.txtInfo);
        btnTestcnx = (Button) findViewById(R.id.btnTestcnx);
        btnGuardar = (ImageButton) findViewById(R.id.btnGuardar);
        txtEstado = (TextView) findViewById(R.id.txtEstado);

        setSupportActionBar(toolbar);
        BA = BluetoothAdapter.getDefaultAdapter();  // tomo mi bluetooth


        //Verifico el estado del bluetooth
        if (!BA.isEnabled()) {
            swB.setChecked(false);
            Toast.makeText(getApplicationContext(), "Bluetooth DESACTIVADO", Toast.LENGTH_LONG).show();
        } else {
            swB.setChecked(true);
            ObtenerDispos();
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

        btnConectar.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s= lstdispos.getSelectedItem().toString();
                conectar(s);
                //conectar("HC-05");
            }
        });

        btnTestcnx.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerificarEstadoConexion();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                guardar();
            }
        });

        recuperar();
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

    public void conectar(String name) {
        //conexion como cliente (el arduino esclavo- esta como servidor escuchando conexion)

        VerificarEstadoConexion();

        try {
            if (!hilocon.isAlive()) {
                throw new RuntimeException("");
            }
            if (!hilosms.isAlive()) {
                throw new RuntimeException("");
            }
        }catch (Exception e){
            txtInfo.setText("");
            for (BluetoothDevice bt : pairedDevices) {
                if (bt.getName().contentEquals(name)) {
                    BD = bt;
                    hilocon = new ConnectThread(BD);
                    hilocon.start();
                }
            }
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

    public void manageConnectedSocket(BluetoothSocket mmSocket) {
        //metodo que es llamado una vex realzada la conexion correcta

        hilosms = new ConnectedThread(mmSocket);
        hilosms.start();

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        hilosms.write(new String("TESTS CONEXION|"));

    }

    public void reccmd(String buf) {

        buf = txtInfo.getText().toString() + ((char) 10) + ((char) 13) + buf;
        txtInfo.setText(buf);

        if (buf == "TESTS CONEXION OK") {
            //txtInfo.setText(buf);
        }

    }

    public void procesarMsg(Message msg) {
        try {
            read = (String) msg.obj;
            int i =read.indexOf('|');
            while(i != -1){ //encontre fin cmd
                buffer +=read.substring(0, i);
                reccmd(buffer);
                buffer="";
                if (read.length() > i+1)
                    read =read.substring(i+1, read.length());
                else
                    read="";
                i =read.indexOf('|');
            }
            if (i ==-1){
                buffer +=read.substring(0, read.length());
            }

        } catch (Exception e) {

        }
    }

    public void VerificarEstadoConexion() {
        txtInfo.setText("");
        try {
            if (hilosms.isAlive()) {
                Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_SHORT).show();
                txtEstado.setText("CONECTADO");
                txtInfo.setText("TESTS CONEXION|--->");
                hilosms.write(new String("TESTS CONEXION|"));
            }else{
                txtEstado.setText("DESCONECTADO");
                Toast.makeText(getApplicationContext(), "NO Conectado", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e){
            txtEstado.setText("DESCONECTADO");
            Toast.makeText(getApplicationContext(), "NO Conectado-hilo null", Toast.LENGTH_SHORT).show();
        }

    }

    public void recuperar(){
        try {
            SharedPreferences prefe = getSharedPreferences("configuracion", Context.MODE_PRIVATE);
            //et1.setText(prefe.getString("mail",""));

            boolean e = false;
            String dis = prefe.getString("dispositivo", "").toString();
            if (dis.isEmpty()) finish();
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
