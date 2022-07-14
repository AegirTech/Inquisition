FROM openjdk:11-jre-slim
MAINTAINER DazeCake

COPY build/libs/*.jar /Inquisition.jar
COPY src/main/resources/application.yml /config/application.yml

EXPOSE 2000

CMD java -jar /Inquisition.jar