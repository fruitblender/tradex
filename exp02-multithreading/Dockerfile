FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out
RUN javac -d out exp02-multithreading/src/tradex/multithread/*.java
WORKDIR /app/out
