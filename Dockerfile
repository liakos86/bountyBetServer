FROM maven:3.8.6-eclipse-temurin-11-alpine as build

# Ensure the logs directory exists inside the container
RUN mkdir -p /app/logs

# Expose a volume for logs
VOLUME [ "/app/logs" ]

# Set environment variable for log4j (optional)
#ENV LOG_DIR=/app/logs

WORKDIR /app

COPY . .


RUN ["mvn", "clean", "install", "-DskipTests"]

FROM  tomcat:9-jre11 as run


RUN ["apt-get", "update"] 

RUN ["apt-get", "install", "-y", "vim"]

COPY --from=build /app/betCoreServer/target/betCoreServer-0.0.1-SNAPSHOT.war  /usr/local/tomcat/webapps/fantasyTips.war


#NOTE to self : docker run -d -p 8080:8080 -v C:\devTools\dockerApplicationLogs:/app/logs --name betserverinstance bountybetserver
CMD ["catalina.sh", "run"]

EXPOSE 8080




