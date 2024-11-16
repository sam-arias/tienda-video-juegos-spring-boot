package com.videogame_store.videogame_store;

import jakarta.persistence.Entity;

@Entity
public class VideoJuegoDigital extends VideoJuego {

    private float tamano;

    public float getTamano() {
        return tamano;
    }

    public void setTamano(float tamano) {
        setFormato("Digital");
        this.tamano = tamano;
    }

}
