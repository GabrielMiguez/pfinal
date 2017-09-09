package com.example.gabys.notsound;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class Bluetooth {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//id para otros dispositivos-no android
    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            procesarMsg(msg);
        }
    };
    MiServiceIBinder service;
    public BluetoothAdapter BA;
    private static final int MESSAGE_READ = 0;
    private ConnectThread hilocon;
    private ConnectedThread hilosms;
    public BluetoothDevice BD;
    public String buffer="",read="";

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter mArrayAdapter;

    private Spinner lstdispos;
    private Switch swB;

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

    public void conectar(String name) {
        //conexion como cliente (el arduino esclavo- esta como servidor escuchando conexion)

        //VerificarEstadoConexion();
        BA = BluetoothAdapter.getDefaultAdapter();  // tomo mi bluetooth

        try {
            if (!hilocon.isAlive()) {
                throw new RuntimeException("");
            }
            if (!hilosms.isAlive()) {
                throw new RuntimeException("");
            }
        }catch (Exception e){
            pairedDevices = BA.getBondedDevices();
            //recorro el pairedDev
            for (BluetoothDevice bt : pairedDevices) {
                if (bt.getName().contentEquals(name)) {
                    BD = bt;
                    hilocon = new ConnectThread(BD);
                    hilocon.start();
                }
            }
        }
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

    public boolean Conected(){
        try {
            if (hilosms.isAlive()) {
                return true;
            }else{
                return false;
            }

        } catch (Exception e){
            return false;
        }
    }

    public int EnviarSMS(String sms) {
        try {
            if (hilosms.isAlive()) {
                hilosms.write(sms);
            }
        }catch (Exception e){
            return -1;
        }
        return 1;
    }

    //procesa cada parte de mesaje recibido, corta, divide, etc
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

    //Aca ya tenemos el sms completo para tomar acciones
    public void reccmd(String buf) {
        //Avisarle al servicio, para que notifique
        //serviceHandler.obtainMessage(MESSAGE_READ, buf.length(), -1, buf).sendToTarget();
        service.procesarMsg(buf);
    }
    public Bluetooth(MiServiceIBinder s){
        service = s;
    }

}
