package com.example.gabys.notsound;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class SonidoAlertaActivity extends AppCompatActivity {

    private int itemSeleccionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonido_alerta);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView txt_sonidoNombre = (TextView)findViewById(R.id.SonidoID);

        Sonidos sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        //Levanto los parametros
        itemSeleccionado = (int) getIntent().getSerializableExtra("sonidoSeleccionado");

        if (itemSeleccionado != -1){
            //Cargo los parametros en los objetos de la pantalla
            txt_sonidoNombre.setText(sonidos.getSonidoByPosition(itemSeleccionado).getNombre());
        }

    }
}
