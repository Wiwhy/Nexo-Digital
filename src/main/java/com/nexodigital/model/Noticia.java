// ============================================================
// PAQUETE: Indica en qué "carpeta lógica" vive esta clase.
// Java usa paquetes para organizar el código, igual que
// carpetas en el ordenador. "com.nexodigital.model" significa
// que esta clase pertenece a la capa MODEL (datos) del proyecto.
// ============================================================
package com.nexodigital.model;

// ============================================================
// CLASE: Noticia
//
// Esta clase es un "molde" (o plantilla) que representa UNA noticia.
// Es lo que en programación se llama un POJO (Plain Old Java Object)
// o también "entidad" o "modelo".
//
// Piénsalo así: igual que en el mundo real una noticia tiene
// título, texto, autor, etc., aquí en código tenemos una clase
// que guarda exactamente esa información.
//
// IMPORTANTE: Esta clase NO hace nada, solo ALMACENA datos.
// Es como un formulario en papel: tiene huecos (campos) para
// rellenar información.
// ============================================================
public class Noticia {

    // ----------------------------------------------------------
    // ATRIBUTOS (también llamados "campos" o "variables de instancia")
    //
    // Cada atributo es un HUECO donde se guardará un dato concreto.
    // La palabra "private" significa que nadie de fuera puede
    // acceder directamente a este campo; solo se puede acceder
    // a través de los métodos get/set que están más abajo.
    // Esto se llama ENCAPSULACIÓN: ocultamos los datos y
    // los protegemos de accesos directos no controlados.
    // ----------------------------------------------------------

    // "int" es un tipo de dato para números enteros (sin decimales).
    // "id" es el identificador único de la noticia en la base de datos.
    // Cada noticia en la base de datos tiene un número diferente (1, 2, 3...).
    private int id;

    // "String" es el tipo de dato para texto (cadena de caracteres).
    // "titulo" guarda el título de la noticia, ej: "El Real Madrid gana la Liga"
    private String titulo;

    // "contenido" guarda el texto completo del artículo de la noticia.
    private String contenido;

    // "nombreImagen" guarda el NOMBRE DEL ARCHIVO de la imagen,
    // por ejemplo: "abc123-foto.jpg". No guarda la imagen en sí,
    // solo el nombre del archivo para saber dónde buscarlo.
    private String nombreImagen;

    // "autor" guarda el nombre de quien escribió la noticia,
    // por ejemplo: "María García"
    private String autor;

    // "fechaPublicacion" guarda cuándo se publicó la noticia,
    // por ejemplo: "2024-05-19 18:00:00"
    private String fechaPublicacion;


    // ----------------------------------------------------------
    // CONSTRUCTOR VACÍO
    //
    // Un "constructor" es un método especial que se llama
    // automáticamente cuando creas un objeto nuevo con "new Noticia()".
    // Este constructor está vacío, lo que significa que crea un
    // objeto Noticia con TODOS los campos a null/0.
    // Es útil cuando quieres crear una noticia y rellenarla
    // campo por campo después usando los métodos set.
    // Ejemplo de uso: Noticia n = new Noticia();
    // ----------------------------------------------------------
    public Noticia() {
    }

    // ----------------------------------------------------------
    // CONSTRUCTOR CON PARÁMETROS
    //
    // Este segundo constructor permite crear una Noticia ya
    // con datos cargados de golpe.
    // Ejemplo de uso:
    //   Noticia n = new Noticia("Título", "Contenido...", "foto.jpg", "Juan");
    //
    // La palabra "this" hace referencia al objeto actual.
    // "this.titulo = titulo" significa: "asigna el valor del
    // parámetro 'titulo' (lo que pasó quien llamó al constructor)
    // al campo 'titulo' de ESTE objeto".
    // Se usa "this" para distinguir el atributo privado (this.titulo)
    // del parámetro recibido (titulo).
    // ----------------------------------------------------------
    public Noticia(String titulo, String contenido, String nombreImagen, String autor) {
        this.titulo = titulo;           // Guarda el título pasado como argumento
        this.contenido = contenido;     // Guarda el contenido pasado como argumento
        this.nombreImagen = nombreImagen; // Guarda el nombre de imagen pasado como argumento
        this.autor = autor;             // Guarda el autor pasado como argumento
    }

    // ----------------------------------------------------------
    // MÉTODOS GETTER Y SETTER
    //
    // Como los atributos son "private" (privados), no se puede
    // acceder a ellos directamente desde fuera de esta clase.
    // Para leer un valor se usa un GETTER (método que devuelve el valor).
    // Para cambiar un valor se usa un SETTER (método que asigna el valor).
    //
    // Convención de nombres:
    //   - Getter: "get" + NombreCampo  →  getId(), getTitulo()...
    //   - Setter: "set" + NombreCampo  →  setId(), setTitulo()...
    //
    // "public" significa que cualquiera puede llamar a estos métodos.
    // "return" significa "devuelve este valor al que llamó".
    // ----------------------------------------------------------

    // GETTER para "id": devuelve el id de la noticia.
    // "int" antes de "getId" indica qué tipo de dato devuelve.
    public int getId() {
        return id; // Devuelve el valor almacenado en el campo "id"
    }

    // SETTER para "id": permite asignar un valor al campo "id".
    // "void" significa que no devuelve nada.
    public void setId(int id) {
        this.id = id; // Asigna el valor recibido al campo "id" de este objeto
    }

    // GETTER para "titulo"
    public String getTitulo() {
        return titulo;
    }

    // SETTER para "titulo"
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // GETTER para "contenido"
    public String getContenido() {
        return contenido;
    }

    // SETTER para "contenido"
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    // GETTER para "nombreImagen"
    public String getNombreImagen() {
        return nombreImagen;
    }

    // SETTER para "nombreImagen"
    public void setNombreImagen(String nombreImagen) {
        this.nombreImagen = nombreImagen;
    }

    // GETTER para "autor"
    public String getAutor() {
        return autor;
    }

    // SETTER para "autor"
    public void setAutor(String autor) {
        this.autor = autor;
    }

    // GETTER para "fechaPublicacion"
    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    // SETTER para "fechaPublicacion"
    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

}