FROM openjdk:11.0.2-jdk-stretch
COPY build/libs/order-service-*.jar /opt/app.jar
WORKDIR /opt
CMD ["java", "-jar", "app.jar"]
