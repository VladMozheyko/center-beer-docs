FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

## Прикрепляем локальные настройки Maven (если есть)
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src

## Собираем артефакт
ARG JAR_FILE=doc-multi-lang-app.jar
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy

## Упаковка под не-root пользователя
RUN groupadd -r app && useradd -r -g app app
USER app
WORKDIR /home/app

## Прописываем ARG чтобы COPY знало, какой JAR копировать
ARG JAR_FILE=doc-multi-lang-app.jar
COPY --from=build --chown=app /app/target/${JAR_FILE} app.jar

## Порт
ENV SERVER_PORT=8443

## Memory/Options (можно переопределить через JAVA_OPTS)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8443

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
