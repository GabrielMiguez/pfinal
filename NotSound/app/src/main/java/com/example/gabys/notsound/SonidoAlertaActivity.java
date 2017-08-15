package com.example.gabys.notsound;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SonidoAlertaActivity extends AppCompatActivity {

    private ArrayList<Sonido> sonidos;
    private int itemSeleccionado = -1;
    private String ser;

    public void recuperarSonidos() {
        ser = SerializeObject.ReadSettings(this, "notas.dat");
        if (ser != null && !ser.equalsIgnoreCase("")) {
            this.sonidos = (ArrayList<Sonido>)SerializeObject.stringToObject(ser);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonido_alerta);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView txt_sonidoNombre = (TextView)findViewById(R.id.SonidoID);

        sonidos = new ArrayList<Sonido>();
        recuperarSonidos();

        //Levanto los parametros
        itemSeleccionado = (int) getIntent().getSerializableExtra("sonidoSeleccionado");

        //Toast.makeText(this,Integer.toString(itemSeleccionado),Toast.LENGTH_LONG).show();
        //Toast.makeText(this,Integer.toString(sonidos.size()),Toast.LENGTH_LONG).show();
        //Toast.makeText(this,sonidos.get(itemSeleccionado).getNombre(),Toast.LENGTH_LONG).show();

        if (itemSeleccionado != -1){
            //Cargo los parametros en los objetos de la pantalla
            txt_sonidoNombre.setText(sonidos.get(itemSeleccionado).getNombre());
        }

    }
}
