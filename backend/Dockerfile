FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Dependencias en una capa propia para reutilizar la caché si solo cambia el código
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /app/target/quarkus-app/lib/ lib/
COPY --from=build --chown=app:app /app/target/quarkus-app/*.jar ./
COPY --from=build --chown=app:app /app/target/quarkus-app/app/ app/
COPY --from=build --chown=app:app /app/target/quarkus-app/quarkus/ quarkus/
USER app

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar quarkus-run.jar"]
