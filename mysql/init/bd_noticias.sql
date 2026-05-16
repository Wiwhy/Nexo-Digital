CREATE DATABASE IF NOT EXISTS nexo_digital;
USE nexo_digital;

CREATE TABLE IF NOT EXISTS administradores (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100)
);

INSERT INTO administradores (usuario, password_hash, nombre_completo)
VALUES ('admin', 'admin123', 'Valentin Penna');

CREATE TABLE IF NOT EXISTS noticias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    contenido_noticia TEXT NOT NULL,
    nombre_archivo_imagen VARCHAR(255),
    autor VARCHAR(150),
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);