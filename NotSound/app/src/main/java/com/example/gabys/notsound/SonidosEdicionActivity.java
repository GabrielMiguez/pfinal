package com.example.gabys.notsound;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SonidosEdicionActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Sonidos sonidos;
    private int itemSeleccionado;

    //private Bitmap imagen;

    private TextView txt_sonidoID;
    private EditText txt_sonidoNombre;
    private CheckBox chk_habilitado;
    private ImageView img_imagenSonido;
    private ImageButton botonGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonidos_edicion);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txt_sonidoID = (TextView)findViewById(R.id.txtvw_sonidoID);
        txt_sonidoNombre = (EditText)findViewById(R.id.edtxt_sonidoNombre);
        chk_habilitado = (CheckBox) findViewById(R.id.chk_Habilitado);
        img_imagenSonido = (ImageView)findViewById(R.id.img_ImagenSonido);
        botonGuardar = (ImageButton) findViewById(R.id.btn_Guardar);

        sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        //Levanto los parametros con los datos del Sonido a Editar
        itemSeleccionado = (int) getIntent().getSerializableExtra("sonidoSeleccionado");

        if (itemSeleccionado != SonidosActivity.SONIDO_NUEVO){
            Sonido sonido = sonidos.getSonidoByPosition(itemSeleccionado);

            txt_sonidoID.setText(String.valueOf(sonido.getID()));
            txt_sonidoNombre.setText(sonido.getNombre());
            chk_habilitado.setChecked(sonido.getHabilitado());
            if (sonido.getImagen() != null) {img_imagenSonido.setImageBitmap(sonido.getImagen());}
        }

        //Deshabilito algunos objetos para el sonidos "Alerta Externa"
        if (itemSeleccionado == Sonidos.POSICION_SONIDO_ALERTA_EXTERNA) {
            txt_sonidoNombre.setEnabled(false);
            chk_habilitado.setEnabled(false);
        }

        botonGuardar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int IDSonido = sonidos.getAvailableSonidoID();
                if(!txt_sonidoID.getText().toString().equals("(Desconocido)")){ IDSonido = Integer.parseInt(txt_sonidoID.getText().toString()); }
                String nombreSonido = txt_sonidoNombre.getText().toString();
                Boolean estaHabilitado = chk_habilitado.isChecked();
                String rutaFoto = getExternalFilesDir(null) + "/" + nombreSonido + ".png";

                img_imagenSonido.buildDrawingCache();
                Bitmap imagen = img_imagenSonido.getDrawingCache();

                saveImage(imagen, rutaFoto);

                if (itemSeleccionado != SonidosActivity.SONIDO_NUEVO) {
                    sonidos.setSonido(itemSeleccionado,(new Sonido(IDSonido,nombreSonido,rutaFoto,estaHabilitado)));
                    sonidos.saveSonidos(getApplicationContext());
                }
                else {
                    Sonido sonidoNuevo = new Sonido(IDSonido,nombreSonido,rutaFoto,estaHabilitado);
                    sonidos.addSonido(getApplicationContext(), sonidoNuevo);
                }

                if (itemSeleccionado != Sonidos.POSICION_SONIDO_ALERTA_EXTERNA) {
                    Intent i = new Intent(getApplicationContext(), SonidosActivity.class );
                    startActivity(i);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap imagen = (Bitmap) data.getExtras().get("data");
            img_imagenSonido.setImageBitmap(imagen);
        }
    }

    public void tomarFoto (View v){

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

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

}
