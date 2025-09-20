# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# If you use the Maven wrapper, ensure it's executable locally first (chmod +x mvnw)
RUN ./mvnw -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
