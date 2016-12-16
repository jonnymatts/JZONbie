FROM anapsix/alpine-java:8
ADD build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]