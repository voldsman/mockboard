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

ARG USER_ID=1000
ARG GROUP_ID=1000

RUN addgroup -g ${GROUP_ID} mbgroup && \
    adduser -u ${USER_ID} -G mbgroup -s /bin/sh -D mbuser

COPY --from=builder /backend/target/*.jar mb.jar

RUN mkdir -p /backend/data /backend/logs && \
    chown -R mbuser:mbgroup /backend

USER mbuser

# ownership verification commands
#docker exec -it mockboard-mockboard_dev-1 id mbuser
#out: uid=1000(mbuser) gid=1000(mbgroup) groups=1000(mbgroup),1000(mbgroup)

#docker exec -it mockboard-mockboard_dev-1 ls -la /backend
#total 43136
#drwxr-xr-x 1 mbuser mbgroup     4096 Jan 23 19:27 .
#drwxr-xr-x 1 root   root        4096 Jan 23 19:29 ..
#drwxr-xr-x 2 mbuser mbgroup     4096 Jan 23 19:29 data
#drwxr-xr-x 2 mbuser mbgroup     4096 Jan 23 19:27 logs
#-rw-r--r-- 1 mbuser mbgroup 44150326 Jan 23 19:27 mb.jar

#docker exec -it mockboard-mockboard_dev-1 ls -ln /backend
#total 43128
#drwxr-xr-x 2 1000 1000     4096 Jan 23 19:29 data
#drwxr-xr-x 2 1000 1000     4096 Jan 23 19:27 logs
#-rw-r--r-- 1 1000 1000 44150326 Jan 23 19:27 mb.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport"
EXPOSE 8000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar mb.jar"]
