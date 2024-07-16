package com.aluracursos.challenge_literalura.repository;

import com.aluracursos.challenge_literalura.model.Autor;
import com.aluracursos.challenge_literalura.model.Lenguaje;
import com.aluracursos.challenge_literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Libro l JOIN l.autor a WHERE a.nombre LIKE %:nombre%")
    Optional<Autor> buscarAutorPorNombre(String nombre);

    @Query("SELECT l FROM Libro l JOIN l.autor a WHERE l.titulo LIKE %:nombre%")
    Optional<Libro> buscarLibroPorNombre(String nombre);

    @Query("SELECT l FROM Libro l JOIN l.autor a")
    List<Libro> buscarTodosLosLibros();

    @Query("SELECT a FROM Autor a WHERE a.nacimiento <= :fecha AND a.fallecimiento > :fecha  ")
    List<Autor> buscarAutoresVivos(Integer fecha);

    @Query("SELECT l FROM Autor a JOIN a.libros l WHERE l.lenguaje = :idioma ")
    List<Libro> buscarLibrosPorIdioma(Lenguaje idioma);

    @Query("SELECT l FROM Autor a JOIN a.libros l ORDER BY l.descarga DESC LIMIT 5")
    List<Libro> top5Libros();
}
