package com.example.gabys.notsound;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SonidosEdicionActivity extends Menu {
    private static final int CAMERA_REQUEST = 1888;
    private static int RESULT_LOAD_IMAGE = 1;

    private Boolean grabacionExitosa = false;

    private Sonidos sonidos;
    private int itemSeleccionado;
    private int IDSonido_grabado=-1;

    private TextView txt_sonidoID_texto;
    private TextView txt_sonidoID;
    private EditText txt_sonidoNombre;
    private Switch chk_habilitado;
    private TextView txt_imagen_texto;
    private ImageView img_imagenSonido;
    private FloatingActionButton fab_grabarAudio;

    AlertDialog.Builder dialogo1;
    ProgressDialog progress;
    Runnable progressRunnable;
    Handler pdCanceller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonidos_edicion);

        super.CreateMenu();

        ImageView img = (ImageView) findViewById(R.id.img_ImagenSonido);
        // set a onclick listener for when the button gets clicked
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContextMenu(v);
            }
        });
        registerForContextMenu(img);

        //Levanto los parametros con los datos del Sonido a Editar
        itemSeleccionado = (int) getIntent().getSerializableExtra("sonidoSeleccionado");

        //Seteo el texto del menu de ayuda
        String menuAyuda_titulo1="";
        String menuAyuda_cuerpo1="";
        String menuAyuda_titulo2="";
        String menuAyuda_cuerpo2="";
        String menuAyuda_titulo3="";
        String menuAyuda_cuerpo3="";
        String menuAyuda_titulo4="";
        String menuAyuda_cuerpo4="";

        if (itemSeleccionado == Sonidos.POSICION_SONIDO_ALERTA_EXTERNA) {
            menuAyuda_titulo1="Imagen";
            menuAyuda_cuerpo1="Presione el recuadro de imagen que identifique al alerta externa. Al detectarse un alerta externa se mostrará esta imagen como notificación.";
        } else {
            menuAyuda_titulo1="ID de Sonido";
            menuAyuda_cuerpo1="Informa el número que identifica al Sonido.";
            menuAyuda_titulo2="Habilitado";
            menuAyuda_cuerpo2="Habilite o deshabilite la detección de este Sonido.";
            menuAyuda_titulo3="Imagen";
            menuAyuda_cuerpo3="Presione el recuadro de imagen que identifique al alerta externa. Al detectarse un alerta externa se mostrará esta imagen como notificación.";
            menuAyuda_titulo4="Grabar Sonido";
            menuAyuda_cuerpo4="Presione el botón para iniciar la grabación del Sonido en el dispositivo electrónico.";
        }

        super.setAyudaParametros(
                menuAyuda_titulo1,
                menuAyuda_cuerpo1,
                menuAyuda_titulo2,
                menuAyuda_cuerpo2,
                menuAyuda_titulo3,
                menuAyuda_cuerpo3,
                menuAyuda_titulo4,
                menuAyuda_cuerpo4);

        txt_sonidoID_texto = (TextView)findViewById(R.id.txtvw_sonidoID_texto);
        txt_sonidoID = (TextView)findViewById(R.id.txtvw_sonidoID);
        txt_sonidoNombre = (EditText)findViewById(R.id.edtxt_sonidoNombre);
        chk_habilitado = (Switch) findViewById(R.id.sw_Habilitado);
        txt_imagen_texto = (TextView)findViewById(R.id.txtvw_imagen_texto);
        img_imagenSonido = (ImageView) findViewById(R.id.img_ImagenSonido);
        fab_grabarAudio = (FloatingActionButton) findViewById(R.id.fab_GrabarAudio);

        sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        if (itemSeleccionado != SonidosActivity.SONIDO_NUEVO){
            Sonido sonido = sonidos.getSonidoByPosition(itemSeleccionado);

            txt_sonidoID.setText(String.valueOf(sonido.getID()));
            txt_sonidoNombre.setText(sonido.getNombre());
            chk_habilitado.setChecked(sonido.getHabilitado());
            if (sonido.getImagen() != null) {img_imagenSonido.setImageBitmap(sonido.getImagen());}
        }

        //Oculto o muestro algunos objetos para el sonidos "Alerta Externa"
        if (itemSeleccionado == Sonidos.POSICION_SONIDO_ALERTA_EXTERNA) {
            txt_sonidoID_texto.setVisibility(View.INVISIBLE);
            txt_sonidoID.setVisibility(View.INVISIBLE);
            txt_sonidoNombre.setVisibility(View.INVISIBLE);
            chk_habilitado.setVisibility(View.INVISIBLE);
            fab_grabarAudio.setVisibility(View.INVISIBLE);

            txt_imagen_texto.setVisibility(View.VISIBLE);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // Al iniciar, abro el teclado para editar el nombre del Sonido
    }

    @Override
    public void onBackPressed() {
        if (itemSeleccionado != Sonidos.POSICION_SONIDO_ALERTA_EXTERNA) {
            Intent i = new Intent(this, SonidosActivity.class );
            startActivity(i);
        } else {
            Intent i = new Intent(this, MainActivity.class );
            startActivity(i);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.img_ImagenSonido) {
            getMenuInflater().inflate(R.menu.menu_ctx_imagen, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.CtxOpTomarFoto:
                tomarFoto();
                break;
            case R.id.CtxOpCargarImagen:
                cargarImagen();
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap imagen = (Bitmap) data.getExtras().get("data");
            img_imagenSonido.setImageBitmap(imagen);
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            img_imagenSonido.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    public void guardarSonidoEdicion (View v){
        int IDSonido = sonidos.getAvailableSonidoID();
        if(!txt_sonidoID.getText().toString().equals("(Desconocido)")){ IDSonido = Integer.parseInt(txt_sonidoID.getText().toString()); }
        String nombreSonido = txt_sonidoNombre.getText().toString();
        Boolean estaHabilitado = chk_habilitado.isChecked();
        String rutaFoto = getExternalFilesDir(null) + "/" + Integer.toString(IDSonido) + "_" + nombreSonido + ".png";

        img_imagenSonido.buildDrawingCache();
        Bitmap imagen = img_imagenSonido.getDrawingCache();


        if (itemSeleccionado != SonidosActivity.SONIDO_NUEVO) {
            if (IDSonido_grabado != -1)
                IDSonido=IDSonido_grabado;
            if (sonidos.getSonidoByPosition(itemSeleccionado).getRutaFoto() != null){
                File file = new File(sonidos.getSonidoByPosition(itemSeleccionado).getRutaFoto());
                file.delete();
            }
            sonidos.setSonido(itemSeleccionado,(new Sonido(IDSonido,nombreSonido,rutaFoto,estaHabilitado)));
            sonidos.saveSonidos(getApplicationContext());
        }
        else {
            if (IDSonido_grabado != -1)
                IDSonido=IDSonido_grabado;
            Sonido sonidoNuevo = new Sonido(IDSonido,nombreSonido,rutaFoto,estaHabilitado);
            sonidos.addSonido(getApplicationContext(), sonidoNuevo);
        }

        saveImage(imagen, rutaFoto);
        this.onBackPressed();
    }

    public void tomarFoto (){

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

    }

    public void cargarImagen (){

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);

    }

    public boolean saveImage(Bitmap image, String fullPathFile) {

        try {

            OutputStream os = new FileOutputStream(fullPathFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();

            return true;
        } catch (Exception e) {
            Log.e("saveToInternalStorage()", e.getMessage());
            return false;
        }
    }

    public void grabarAudio (View v) {

        // Cambia el icono cuando empieza a grabar
        fab_grabarAudio.setImageResource(R.drawable.ic_stop_black_24dp);
        fab_grabarAudio.setColorFilter(Color.rgb(218,0,0));

        //Metodo para Implememtar Accion en cada Activity
        //sendMSGSRV("G|");
        if (itemSeleccionado != SonidosActivity.SONIDO_NUEVO) {
            int IDSonido = sonidos.getAvailableSonidoID();
            if(!txt_sonidoID.getText().toString().equals("(Desconocido)")){ IDSonido = Integer.parseInt(txt_sonidoID.getText().toString()); }
            if (IDSonido==0) {
                if (!sendMSGSRV("G|"))
                    Toast.makeText(this, "ERROR: NO HAY CONEXION BT", Toast.LENGTH_SHORT).show();
            }
            else
                if (!sendMSGSRV("R" + Integer.toString(IDSonido) + "|"))
                    Toast.makeText(this, "ERROR: NO HAY CONEXION BT", Toast.LENGTH_SHORT).show();
        }
        else{
            if (!sendMSGSRV("G|"))
                Toast.makeText(this, "ERROR: NO HAY CONEXION BT", Toast.LENGTH_SHORT).show();
        }
        grabacionExitosa = false; // Si la grabacion es exitosa se modifica desde el metodo ActionRecive

        progress = new ProgressDialog(this);
        //progress.setTitle("Grabando");
        progress.setMessage("Grabación en proceso. Espere...");
        progress.setCancelable(false);
        progress.show();

        // Se crea un proceso para que al superar el TIMEOUT de GRABACION se ejecute.
        // Si la grabacion no fue exitosa: se va a cerrar el ProgressDialog y va a mostrar un mensaje de error.
        progressRunnable = new Runnable() {
            @Override
            public void run() {

                progress.cancel();
                if (!grabacionExitosa) {
                    AlertDialog.Builder dialogo1 = new AlertDialog.Builder(SonidosEdicionActivity.this);
                    dialogo1.setTitle("Atención");
                    dialogo1.setMessage("El Sonido no fue grabado debido a que se superó el tiempo de espera.");
                    dialogo1.setCancelable(false);
                    dialogo1.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                        }
                    });

                    dialogo1.show();

                    // Cambia el icono si el guardado NO fue exitoso
                    fab_grabarAudio.setImageResource(R.drawable.ic_mic_black_24dp);
                    fab_grabarAudio.setColorFilter(Color.rgb(0,0,0));
                }
            }
        };

        pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, ConfigActivity.GRABACION_TIMEOUT); // Se setea el tiempo de espera antes de que llame al proceso de arriba.

        /* CODIGO PARA PROBAR LA INTERRUPCION DEL BLUETOOTH. BORRAR!!!*/
        /* INICIO */
        /*
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SonidosEdicionActivity.this.ActionRecive("G1");
            }
        }, 5000);
        */
        /* FIN */
    };

    @Override
    public void ActionRecive(String s){

        //'G1|'<-Sonido Guardado con ID 1
        if (s.charAt(0)=='G'){
            s=s.substring(1);
            IDSonido_grabado =  Integer.valueOf(s);

            // Seccion de validacion del ID del sonido
            grabacionExitosa = true;
            progress.cancel(); // Se cierra el ProgressDialog porque el bluetooth le devolvio el ID del sonido

            // Cambia el icono si el guardado fue exitoso
            fab_grabarAudio.setImageResource(R.drawable.ic_mic_black_24dp);
            fab_grabarAudio.setColorFilter(Color.rgb(0,0,0));
            Toast.makeText(this, "Grabación exitosa", Toast.LENGTH_SHORT).show();
        }

    }

}
