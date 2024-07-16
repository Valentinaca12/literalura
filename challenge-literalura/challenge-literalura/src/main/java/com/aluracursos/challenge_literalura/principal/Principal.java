package com.aluracursos.challenge_literalura.principal;

import com.aluracursos.challenge_literalura.model.*;
import com.aluracursos.challenge_literalura.repository.AutorRepository;
import com.aluracursos.challenge_literalura.service.ConsumoAPI;
import com.aluracursos.challenge_literalura.service.ConvierteDatos;


import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private String URL_BASE = "https://gutendex.com/books/";
    private AutorRepository repositorio;


    public Principal(AutorRepository repository){
        this.repositorio = repository;
        System.out.println("\n¡BIENVENID@ A LITERALURA!");
    }
    public void mostrarMenu() {
        var opcion = -1;
        var menu = """
                 
                ************************************
                *          MENÚ PRINCIPAL          *
                ************************************
                
                Elija la opción de la consulta que desea realizar:
                
                1 - Buscar libro por título
                2 - Listar libros registrados
                3 - Listar autores registrados
                4 - Listar autores vivos en un determinado año
                5 - Listar libros por idioma
                6 - Top 5 libros
                7 - Buscar autor por nombre
                
                0 - Salir
               
                """;
        while (opcion != 0) {
            System.out.println(menu);
            try {
                opcion = Integer.valueOf(teclado.nextLine());
                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        listarAutoresVivos();
                        break;
                    case 5:
                        listarLibrosPorIdioma();
                        break;
                    case 6:
                       top5Libros();
                        break;
                    case 7:
                        buscarAutorPorNombre();
                        break;
                    case 0:
                        System.out.println("Finalizando el programa.");
                        System.out.println("¡Gracias por usar nuestros servicios!");
                        break;
                    default:
                        System.out.println("Opción inválida!");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Opción  inválida: " + e.getMessage());

            }
        }
    }

    public void buscarLibroPorTitulo(){
        System.out.println("Usted ha elegido la opción: BUSCAR LIBRO POR TÍTULO\n");
        System.out.println("Introduce el nombre del libro que deseas buscar:\n");
        var nombre = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ","+"));
        var datos = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibro> libroBuscado = datos.libros().stream()
                .findFirst();
        if(libroBuscado.isPresent()){
            System.out.println(
                            "\nTitulo: " + libroBuscado.get().titulo() +
                            "\nAutor: " + libroBuscado.get().autores().stream()
                            .map(a -> a.nombre()).limit(1).collect(Collectors.joining())+
                            "\nIdioma: " + libroBuscado.get().lenguajes().stream().collect(Collectors.joining()) +
                            "\nNumero de descargas: " + libroBuscado.get().descarga()
            );
            try{
                List<Libro> libroEncontrado = libroBuscado.stream().map(a -> new Libro(a)).collect(Collectors.toList());
                Autor autorAPI = libroBuscado.stream().
                        flatMap(l -> l.autores().stream()
                                .map(a -> new Autor(a)))
                        .collect(Collectors.toList()).stream().findFirst().get();
                Optional<Autor> autorBD = repositorio.buscarAutorPorNombre(libroBuscado.get().autores().stream()
                        .map(a -> a.nombre())
                        .collect(Collectors.joining()));
                Optional<Libro> libroOptional = repositorio.buscarLibroPorNombre(nombre);
                if (libroOptional.isPresent()) {
                    System.out.println("El libro ya está guardado en la base de datos.");
                } else {
                    Autor autor;
                    if (autorBD.isPresent()) {
                        autor = autorBD.get();
                        System.out.println("EL autor ya esta guardado en la base de datos");
                    } else {
                        autor = autorAPI;
                        repositorio.save(autor);
                    }
                    autor.setLibros(libroEncontrado);
                    repositorio.save(autor);
                }
            } catch(Exception e) {
                System.out.println("Advertencia! " + e.getMessage());
            }
        } else {
            System.out.println("\nLo siento, no pude encontrar el libro que buscabas");
        }
    }

    public void listarLibrosRegistrados(){
        System.out.println("Usted ha elegido la opción: LISTAR LIBROS REGISTRADOS\n");
        List<Libro> libros = repositorio.buscarTodosLosLibros();
        repositorio.findAll();
        List<String> librosObtenidos = libros.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                        .map(Libro::getTitulo).collect(Collectors.toList());
        System.out.println(librosObtenidos);

    }

    public void listarAutoresRegistrados(){
        System.out.println("Usted ha elegido la opción: LISTAR AUTORES REGISTRADOS\n");
        List<Autor> autores = repositorio.findAll();
        List<Autor> autoresOrdenados = autores.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .collect(Collectors.toList());
        autoresOrdenados.forEach(l -> {
            List<String> titulosLibros = l.getLibros().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.toList());

            System.out.println("Autor: " + l.getNombre() +
                    "\nFecha de nacimiento: " + l.getNacimiento() +
                    "\nFecha de fallecimiento: " + l.getFallecimiento() +
                    "\nLibros: " + titulosLibros + "\n");
        });
    }

    public void listarAutoresVivos(){
        System.out.println("Usted ha elegido la opción: LISTAR AUTORES VIVOS EN UN DETERMINADO AÑO\n");
        System.out.println("Introduce el año a partir del cual deseas buscar autores vivos:");
        try{
            var fecha = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = repositorio.buscarAutoresVivos(fecha);
            if(!autores.isEmpty()){
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "Autor: " + a.getNombre() +
                                "\nFecha de nacimiento: " + a.getNacimiento() +
                                "\nFecha de fallecimiento: " + a.getFallecimiento() +
                                "\nLibros: " + a.getLibros().stream()
                                .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
                ));
            } else{
                System.out.println("No hay autores vivos en ese año registrados en la base de datos");
            }
        } catch(NumberFormatException e){
            System.out.println("introduce un año válido " + e.getMessage());
        }
    }

    public void listarLibrosPorIdioma(){
        System.out.println("Usted ha elegido la opción: LISTAR LIBROS POR IDIOMA\n");
        var menu = """
                Ingrese el idioma con el cual desea buscar los libros:
                es - español
                en - inglés
                fr - francés
                pt - portugués
                """;
        System.out.println(menu);
        var idioma = teclado.nextLine();
        if(idioma.equalsIgnoreCase("es") || idioma.equalsIgnoreCase("en") ||
                idioma.equalsIgnoreCase("fr") || idioma.equalsIgnoreCase("pt")){
            Lenguaje lenguaje = Lenguaje.fromString(idioma);
            List<Libro> libros = repositorio.buscarLibrosPorIdioma(lenguaje);
                System.out.println();
                libros.forEach(l -> System.out.println(
                                "\nTitulo: " + l.getTitulo() +
                                "\nAutor: " + l.getAutor().getNombre() +
                                "\nIdioma: " + l.getLenguaje().getIdioma() +
                                "\nNumero de descargas: " + l.getDescarga()
                ));

        } else{
            System.out.println("Introduce un idioma en el formato válido, (usando sólo el cógigo) Ej: es");
        }
    }

    public void top5Libros(){
        System.out.println("Usted ha elegido la opción: TOP 5 LIBROS\n");
        List<Libro> libros = repositorio.top5Libros();
        System.out.println(libros);
        libros.forEach(l -> System.out.println(
                        "\nTitulo: " + l.getTitulo() +
                        "\nAutor: " + l.getAutor().getNombre() +
                        "\nIdioma: " + l.getLenguaje().getIdioma() +
                        "\nNumero de descargas: " + l.getDescarga()
        ));
    }

    public void buscarAutorPorNombre(){
        System.out.println("Usted ha elegido la opción: BUSCAR AUTOR POR NOMBRE");
        System.out.println("Ingrese el nombre del autor que desea buscar:");
        var nombre = teclado.nextLine();
        Optional<Autor> autor = repositorio.buscarAutorPorNombre(nombre);
        if(autor.isPresent()){
            System.out.println(
                    "\nAutor: " + autor.get().getNombre() +
                            "\nFecha de nacimiento: " + autor.get().getNacimiento() +
                            "\nFecha de fallecimiento: " + autor.get().getFallecimiento() +
                            "\nLibros: " + autor.get().getLibros().stream()
                            .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
            );
        } else {
            System.out.println("El autor no existe en la base de datos");
        }
    }
}
