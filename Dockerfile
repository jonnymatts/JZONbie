FROM anapsix/alpine-java:8
WORKDIR app/
ADD build/libs/*.jar libs/
CMD ["java", "-cp", "libs/*", "com.jonnymatts.jzonbie.App"]