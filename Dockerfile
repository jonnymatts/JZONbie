FROM anapsix/alpine-java:8
ADD jzonbie-*-all.jar app.jar
CMD ["java", "-jar", "app.jar"]