FROM eclipse-temurin:21-jdk
EXPOSE 8081
COPY target/movie-review-project.jar app.jar
ENTRYPOINT ["java", "-jar", "/movie-review-project.jar"]