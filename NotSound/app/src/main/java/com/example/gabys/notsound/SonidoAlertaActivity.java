package com.example.gabys.notsound;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

public class SonidoAlertaActivity extends AppCompatActivity {

    private int itemSeleccionado = -1;
    private TextView txt_sonidoNombre;
    private ImageView img_imagenSonido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonido_alerta);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txt_sonidoNombre = (TextView)findViewById(R.id.txtvw_sonidoNombre);
        img_imagenSonido = (ImageView)findViewById(R.id.img_ImagenSonido);

        Sonidos sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        //Levanto los parametros
        itemSeleccionado = (int) getIntent().getSerializableExtra("sonidoSeleccionado");

        if (itemSeleccionado != -1){
            Sonido sonido = sonidos.getSonidoByPosition(itemSeleccionado);

            //Cargo los parametros en los objetos de la pantalla
            txt_sonidoNombre.setText(sonido.getNombre());
            if (sonido.getImagen() != null) {img_imagenSonido.setImageBitmap(sonido.getImagen());}
        }
    }
}
