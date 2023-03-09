FROM maven:3.8.5-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:17-oracle
COPY --from=build /home/app/target/formatter-*.jar /usr/local/lib/formatter.jar
ENTRYPOINT ["java", "-jar", "/usr/local/lib/formatter.jar"]
