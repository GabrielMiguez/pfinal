package com.example.gabys.notsound;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SonidoAlertaActivity extends Menu {

    private int itemSeleccionado;
    private TextView txt_sonidoNombre;
    private ImageView img_imagenSonido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonido_alerta);
        super.CreateMenu();

        String menuAyuda_titulo1="Alerta de Sonido";
        String menuAyuda_cuerpo1="Se ha detectado un Sonido.";

        super.setAyudaParametros(
                menuAyuda_titulo1,
                menuAyuda_cuerpo1,
                null,
                null,
                null,
                null,
                null,
                null);

        txt_sonidoNombre = (TextView)findViewById(R.id.txtvw_sonidoNombre);
        img_imagenSonido = (ImageView)findViewById(R.id.img_ImagenSonido);

        Sonidos sonidos = new Sonidos();
        sonidos.loadSonidos(getApplicationContext());

        //Levanto los parametros
        itemSeleccionado = (int) getIntent().getSerializableExtra("sonidoSeleccionado");

        try {
            Sonido sonido = sonidos.getSonidoByID(itemSeleccionado);
            //Cargo los parametros en los objetos de la pantalla
            txt_sonidoNombre.setText(sonido.getNombre());
            if (sonido.getImagen() != null) {img_imagenSonido.setImageBitmap(sonido.getImagen());}
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "error:"+e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }

    }
}
