FROM openjdk:alpine

COPY application*.yml /
COPY build/libs/entity-funct-*-all.jar /entity-funct.jar

CMD ["java", "-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector", "-Xmx1500M", "-Xms1500M", "-jar", "/entity-funct.jar"]