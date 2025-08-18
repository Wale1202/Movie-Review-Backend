FROM openjdk:21
EXPOSE 8081
ADD target/movie-review-project.jar movie-review-project.jar
ENTRYPOINT ["java", "-jar", "/movie-review-project.jar"]