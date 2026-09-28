# Etapa 1: Construcción
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# RUN chmod +x ./mvnw
# RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/rcc2026_backend-0.0.1-SNAPSHOT.jar app.jar

# Crear carpeta para almacenamiento de fotos
RUN mkdir -p uploads/perfiles

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]