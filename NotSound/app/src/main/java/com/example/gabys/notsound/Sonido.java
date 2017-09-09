package com.example.gabys.notsound;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

public class Sonido implements Serializable {
    private String nombre;
    private int ID;
    private String rutaFoto;
    private Boolean habilitado;

    public Sonido(int ID, String nombre, String rutaFoto, Boolean habilitado) {
        this.ID=ID;
        this.nombre=nombre;
        this.rutaFoto=rutaFoto;
        this.habilitado=habilitado;
    }

    public int getID() {
        return ID;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public Boolean getHabilitado() {
        return habilitado;
    }

    public Bitmap getImagen() { return (Bitmap) BitmapFactory.decodeFile(this.getRutaFoto());}
}