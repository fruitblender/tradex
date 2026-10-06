FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out
RUN javac -d out exp05-consistency-replication/src/tradex/replication/*.java
WORKDIR /app/out
