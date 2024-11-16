package com.videogame_store.videogame_store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoJuegoRepository extends JpaRepository<VideoJuego, Long> {
    List<VideoJuego> findByFormato(String formato);
}
