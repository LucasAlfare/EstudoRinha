FROM openjdk:17
LABEL authors="Francisco Lucas"
RUN mkdir /app
COPY ./build/libs/EstudoRinha-1.0-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]