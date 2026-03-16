FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -q -DskipTests clean package

FROM tomcat:10.1-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

CMD ["sh", "-c", "PORT_TO_USE=${PORT:-8080}; sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT_TO_USE}\\\"/\" /usr/local/tomcat/conf/server.xml && catalina.sh run"]
