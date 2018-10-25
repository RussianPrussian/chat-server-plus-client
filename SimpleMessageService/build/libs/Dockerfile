FROM openjdk:8u181-jdk-slim-stretch
ADD SimpleMessageService-all.jar app.jar
ENTRYPOINT [ "java", "-jar", "app.jar" ]
EXPOSE 8091