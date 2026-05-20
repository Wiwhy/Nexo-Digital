# ============================================================
# DOCKERFILE
#
# Un Dockerfile es un script de instrucciones para construir
# una IMAGEN Docker. Una imagen es una "fotografía" del entorno
# de ejecución de la aplicación: sistema operativo, dependencias,
# código compilado, etc.
#
# CONCEPTO CLAVE: Imagen vs. Contenedor
#   - IMAGEN: la plantilla (como una clase en Java).
#   - CONTENEDOR: una instancia en ejecución de esa imagen (como un objeto).
#
# MULTI-STAGE BUILD (Construcción en dos etapas):
# Usamos dos FROM seguidos para separar la etapa de CONSTRUCCIÓN
# de la etapa de EJECUCIÓN. Esto reduce el tamaño de la imagen final
# porque la imagen final solo incluye lo necesario para ejecutar
# (no necesita Maven, el código fuente, etc.).
# ============================================================


# ============================================================
# ETAPA 1: Construcción (Builder)
#
# En esta etapa compilamos el código Java y generamos el .war.
# Un .war (Web Application Archive) es el archivo empaquetado
# que contiene toda la aplicación Java lista para desplegar.
# ============================================================

# FROM: indica la imagen base desde la que partimos.
# "maven:3.9.9-eclipse-temurin-21" es una imagen oficial de Docker Hub que
# incluye Maven (herramienta de build de Java) y Java 21 (JDK Temurin de Eclipse).
# "AS builder" le da un nombre a esta etapa para referenciarla después.
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# WORKDIR: establece el directorio de trabajo dentro del contenedor.
# Todos los comandos siguientes se ejecutarán desde /app.
# Es como hacer "mkdir /app && cd /app".
WORKDIR /app

# COPY: copia archivos desde el contexto de build (tu ordenador)
# al sistema de archivos del contenedor.
# Primero copiamos solo el pom.xml (la configuración de Maven/dependencias).
# Esto es una optimización: Docker cachea capas. Si el pom.xml no cambia,
# Docker usa la caché del paso siguiente (descargar dependencias) sin repetirlo.
COPY pom.xml .

# Copiamos todo el código fuente (carpeta src/) al contenedor.
COPY src ./src

# RUN: ejecuta un comando en el contenedor durante la construcción de la imagen.
# "mvn clean package" → compila el código Java y empaqueta la aplicación en un .war.
#   - "clean": elimina cualquier build anterior.
#   - "package": compila y empaqueta.
# "-DskipTests": omite los tests para agilizar el build (no tenemos tests aquí).
# El .war resultante se crea en /app/target/ROOT.war (el nombre "ROOT" viene del pom.xml).
RUN mvn clean package -DskipTests


# ============================================================
# ETAPA 2: Servidor (Tomcat)
#
# En esta etapa creamos la imagen final que se ejecutará en producción.
# Solo copiamos el .war compilado, no el código fuente ni Maven.
# ============================================================

# FROM: imagen base de Tomcat con Java 21.
# "tomcat:10.1-jdk21-temurin" es la imagen oficial de Tomcat.
# Tomcat es el servidor de aplicaciones Java que ejecutará nuestros Servlets.
FROM tomcat:10.1-jdk21-temurin

# Eliminamos la aplicación por defecto de Tomcat (la página de bienvenida).
# /usr/local/tomcat/webapps/ es donde Tomcat busca aplicaciones web.
# Con esto nos aseguramos de que solo exista nuestra aplicación.
RUN rm -rf /usr/local/tomcat/webapps/*

# COPY --from=builder: copia un archivo de la ETAPA 1 (builder) a la ETAPA 2.
# Copiamos el .war compilado al directorio de webapps de Tomcat.
# Al llamarlo "ROOT.war", Tomcat lo despliega en la URL raíz "/" (sin prefijo).
# Si se llamara "miapp.war", estaría disponible en "/miapp/".
COPY --from=builder /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# Creamos la carpeta /datos_persistentes y le damos permisos de lectura/escritura para todos.
# "mkdir -p" crea la carpeta y también las carpetas intermedias si no existen.
# "chmod 777" da permisos totales (lectura, escritura, ejecución) a todos los usuarios.
# En Railway, esta carpeta será sobreescrita por el volumen persistente real.
# En local, sirve para que Tomcat no falle si intenta escribir en ella.
RUN mkdir -p /datos_persistentes && chmod 777 /datos_persistentes

# EXPOSE: documenta que el contenedor usa el puerto 8080.
# Tomcat escucha en el puerto 8080 por defecto.
# EXPOSE no abre el puerto automáticamente; es solo documentación.
# El puerto real se mapea en docker-compose.yml con "ports: 8080:8080".
EXPOSE 8080

# CMD: el comando que se ejecuta cuando el contenedor arranca.
# "catalina.sh run" inicia el servidor Tomcat en primer plano.
# "run" vs "start": "start" ejecuta Tomcat en segundo plano y el contenedor terminaría;
# "run" lo mantiene en primer plano, que es lo que necesitamos en Docker.
CMD ["catalina.sh", "run"]