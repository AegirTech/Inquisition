FROM openjdk:11-jre-slim
MAINTAINER DazeCake

COPY build/libs/*.jar /Inquisition.jar

EXPOSE 2000

CMD java -jar /Inquisition.jar