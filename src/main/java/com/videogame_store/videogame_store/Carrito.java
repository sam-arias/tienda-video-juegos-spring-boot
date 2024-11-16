package com.videogame_store.videogame_store;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private List<VideoJuego> productos = new ArrayList<>();

    public void agregarProducto(VideoJuego videojuego) {
        productos.add(videojuego);
    }

    public List<VideoJuego> getProductos() {
        return productos;
    }

    public void eliminarProducto(Long id) {
        productos.removeIf(videojuego -> videojuego.getId().equals(id));
    }
    
    public void limpiarCarrito() {
        productos.clear();
    }
    
}
