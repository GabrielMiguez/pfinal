package com.example.gabys.notsound;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class SonidosEdicionActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;

    private Sonidos sonidos;
    private int itemSeleccionado = -1;

    private String rutaFoto = "";

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

        if (itemSeleccionado != -1){
            Sonido sonido = sonidos.getSonidoByPosition(itemSeleccionado);

            //Cargo las variables auxiliares del objeto
            rutaFoto = sonido.getRutaFoto();

            //Cargo los parametros en los objetos de la pantalla
            txt_sonidoID.setText(String.valueOf(sonido.getID()));
            txt_sonidoNombre.setText(sonido.getNombre());
            chk_habilitado.setChecked(sonido.getHabilitado());
            img_imagenSonido.setImageBitmap(sonido.getImagen());
        }

        botonGuardar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int IDSonido = sonidos.getAvailableSonidoID();
                if(!txt_sonidoID.getText().toString().equals("(Desconocido)")){
                     IDSonido = Integer.parseInt(txt_sonidoID.getText().toString());
                }
                String nombreSonido = txt_sonidoNombre.getText().toString();
                Boolean estaHabilitado = chk_habilitado.isChecked();

                if (itemSeleccionado != -1) {
                    sonidos.setSonido(itemSeleccionado,(new Sonido(IDSonido,nombreSonido,rutaFoto,estaHabilitado)));
                    sonidos.saveSonidos(getApplicationContext());
                }
                else {
                    Sonido sonidoNuevo = new Sonido(IDSonido,nombreSonido,rutaFoto,estaHabilitado);
                    sonidos.addSonido(getApplicationContext(), sonidoNuevo);
                }

                Intent i = new Intent(getApplicationContext(), SonidosActivity.class );
                startActivity(i);
            }
        });

    }

    public void tomarFoto (View v){

        File dirFoto = getExternalFilesDir(null);
        String nombreFoto = txt_sonidoNombre.getText().toString();

        this.rutaFoto = dirFoto + "/" + nombreFoto; // Se guarda la ruta para despues guardarla

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File archivoFoto = new File(dirFoto, nombreFoto);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(archivoFoto));
        startActivity(i);

        //Cargo en el imageView la foto recien sacada
        //img_imagenSonido.setImageBitmap((Bitmap) BitmapFactory.decodeFile(this.rutaFoto));
    }
}
