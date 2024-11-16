package com.videogame_store.videogame_store;

import jakarta.persistence.Entity;

@Entity
public class VideoJuegoFisico extends VideoJuego {
    private double ancho;
    private double largo;

    public double getAncho() {
        return ancho;
    }

    public void setAncho(double ancho) {
        setFormato("Fisico");
        this.ancho = ancho;
    }

    public double getLargo() {
        return largo;
    }

    public void setLargo(double largo) {
        this.largo = largo;
    }

}
