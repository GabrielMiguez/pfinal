package com.example.gabys.notsound;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class SonidosEdicionActivity extends AppCompatActivity {

    private Sonidos sonidos;
    private int itemSeleccionado = -1;

    ImageButton botonGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonidos_edicion);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText txt_sonidoNombre = (EditText)findViewById(R.id.edtxt_sonidoNombre);

        sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        //Levanto los parametros con los datos del Sonido a Editar
        itemSeleccionado = (int) getIntent().getSerializableExtra("sonidoSeleccionado");

        if (itemSeleccionado != -1){
            //Cargo los parametros en los objetos de la pantalla

            txt_sonidoNombre.setText(sonidos.getSonido(itemSeleccionado).getNombre());
        }

        botonGuardar = (ImageButton) findViewById(R.id.btn_Guardar);
        botonGuardar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (itemSeleccionado != -1) {
                    sonidos.setSonido(itemSeleccionado,(new Sonido(txt_sonidoNombre.getText().toString(),'m')));
                    sonidos.saveSonidos(getApplicationContext());
                }
                else {
                    int indice = sonidos.sizeSonidos();
                    Sonido sonidoNuevo = new Sonido(txt_sonidoNombre.getText().toString(),'m');
                    sonidos.addSonido(getApplicationContext(), sonidoNuevo);
                }

                Intent i = new Intent(getApplicationContext(), SonidosActivity.class );
                startActivity(i);
            }
        });
    }
}
