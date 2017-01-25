FROM anapsix/alpine-java:8
ADD build/libs/jzonbie-*-all.jar app.jar
CMD ["java", "-jar", "app.jar"]