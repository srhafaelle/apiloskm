# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el .jar generado
COPY --from=build /app/target/*.jar api-cvm.jar

# Exponemos el puerto de Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api-cvm.jar"]