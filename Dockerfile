FROM maven:3.8.6-eclipse-temurin-11-alpine as build

WORKDIR /app

COPY . .


RUN ["mvn", "clean", "install", "-DskipTests"]

FROM  tomcat:9-jre11 as run


RUN ["apt-get", "update"] 

RUN ["apt-get", "install", "-y", "vim"]

COPY --from=build /app/betCoreServer/target/  /usr/local/tomcat/webapps/

CMD ["catalina.sh", "run"]

EXPOSE 8080




