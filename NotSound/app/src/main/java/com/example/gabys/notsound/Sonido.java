package com.example.gabys.notsound;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

public class Sonido implements Serializable {
    private String nombre;
    private char genero;
    private String rutaFoto;
    private Boolean habilitado;

    public Sonido(String nombre, char genero, String rutaFoto, Boolean habilitado) {
        this.nombre=nombre;
        this.genero=genero;
        this.rutaFoto=rutaFoto;
        this.habilitado=habilitado;
    }

    public String getNombre() {
        return nombre;
    }

    public char getGenero() {
        return genero;
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public Boolean getHabilitado() {
        return habilitado;
    }

    public Bitmap getImagen() { return (Bitmap) BitmapFactory.decodeFile(this.getRutaFoto());}
}