-- ============================================================
-- ARCHIVO: bd_noticias.sql
-- 
-- Este archivo SQL se ejecuta AUTOMÁTICAMENTE cuando el contenedor
-- de MySQL arranca por primera vez.
-- Docker Compose monta la carpeta mysql/init/ como el punto de
-- inicialización de MySQL. MySQL ejecuta todos los .sql que
-- encuentre en esa carpeta al arrancar por primera vez.
-- Si la base de datos ya existe (en ejecuciones posteriores),
-- NO vuelve a ejecutar este archivo.
-- ============================================================


-- Creamos la base de datos si no existe.
-- "IF NOT EXISTS" evita un error si ya existe; simplemente la ignora.
-- "nexo_digital" es el nombre que elegimos para esta base de datos.
CREATE DATABASE IF NOT EXISTS nexo_digital;

-- Le decimos a MySQL que trabaje con esta base de datos de ahora en adelante.
-- Es como "entrar" en esa base de datos.
USE nexo_digital;


-- ============================================================
-- TABLA: administradores
--
-- Almacena los usuarios que pueden iniciar sesión como administradores.
-- En este proyecto solo hay uno, pero la tabla permite más de uno.
-- ============================================================
CREATE TABLE IF NOT EXISTS administradores (
    -- id_admin: identificador único de cada administrador.
    -- INT = número entero.
    -- AUTO_INCREMENT = MySQL asigna el número automáticamente (1, 2, 3...).
    -- PRIMARY KEY = esta columna es la clave primaria: no puede repetirse ni ser null.
    id_admin INT AUTO_INCREMENT PRIMARY KEY,

    -- usuario: el nombre de usuario para el login.
    -- VARCHAR(50) = texto de hasta 50 caracteres.
    -- NOT NULL = este campo NO puede estar vacío.
    -- UNIQUE = no puede haber dos administradores con el mismo usuario.
    usuario VARCHAR(50) NOT NULL UNIQUE,

    -- password_hash: la contraseña (en este proyecto en texto plano por simplicidad).
    -- En producción real: se guardaría el HASH de la contraseña (con bcrypt, etc.).
    -- VARCHAR(255) = hasta 255 caracteres (suficiente para hashes largos).
    password_hash VARCHAR(255) NOT NULL,

    -- nombre_completo: el nombre real del administrador (opcional).
    -- VARCHAR(100) = hasta 100 caracteres.
    -- No tiene NOT NULL, así que puede ser NULL (campo opcional).
    nombre_completo VARCHAR(100)
);

-- Insertamos el único administrador del sistema.
-- Este INSERT crea el usuario "admin" con contraseña "admin123".
-- En producción esto sería peligroso (contraseña en texto plano y débil).
INSERT INTO administradores (usuario, password_hash, nombre_completo)
VALUES ('admin', 'admin123', 'Valentin Penna');


-- ============================================================
-- TABLA: noticias
--
-- Almacena todas las noticias del portal.
-- Esta es la tabla principal del proyecto.
-- ============================================================
CREATE TABLE IF NOT EXISTS noticias (
    -- id: identificador único de cada noticia.
    -- Número entero, autoincremental, clave primaria.
    id INT AUTO_INCREMENT PRIMARY KEY,

    -- titulo: el título de la noticia.
    -- NOT NULL: toda noticia debe tener título.
    titulo VARCHAR(255) NOT NULL,

    -- contenido_noticia: el texto completo del artículo.
    -- TEXT: tipo de dato para textos largos (hasta 65.535 caracteres).
    -- NOT NULL: toda noticia debe tener contenido.
    contenido_noticia TEXT NOT NULL,

    -- nombre_archivo_imagen: el nombre del archivo de imagen subido.
    -- Ejemplo: "a3f2c8d1-7e45-foto.jpg"
    -- Puede ser NULL (sin comillas, sin NOT NULL) → la imagen es opcional.
    nombre_archivo_imagen VARCHAR(255),

    -- autor: el nombre de quien escribió la noticia (opcional).
    autor VARCHAR(150),

    -- fecha_publicacion: cuándo se publicó la noticia.
    -- TIMESTAMP: tipo de dato para fechas con hora (ej: "2024-05-19 18:00:00").
    -- DEFAULT CURRENT_TIMESTAMP: si no se especifica, MySQL pone la fecha/hora actual.
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Nota: No se insertan noticias de ejemplo aquí.
-- Las noticias se crean desde el panel de administración de la web.