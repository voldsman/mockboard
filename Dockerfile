FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /backend
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline -B

# skip frontend
COPY src/main/java src/main/java
COPY src/main/resources src/main/resources
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /backend

RUN addgroup -S mbgroup && adduser -S mbuser -G mbgroup

COPY --from=builder /backend/target/*.jar mb.jar

RUN mkdir -p /backend/data /backend/logs && chown -R mbuser:mbgroup /backend

USER mbuser

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport"
EXPOSE 8888

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar mb.jar"]
