package com.example.gabys.notsound;

import java.io.Serializable;

public class Sonido implements Serializable {
    private String nombre;
    private char genero;

    public Sonido(String nombre, char genero) {
        this.nombre=nombre;
        this.genero=genero;
    }

    public String getNombre() {
        return nombre;
    }

    public char getGenero() {
        return genero;
    }
}