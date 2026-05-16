# ETAPA 1: Construcción (Builder)
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPA 2: Servidor (Tomcat)
FROM tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiamos la aplicación correcta
COPY --from=builder /app/target/gestion-noticias.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]