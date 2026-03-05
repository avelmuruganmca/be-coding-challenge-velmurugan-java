FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY build/libs/code-challenge-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]