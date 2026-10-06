FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out
RUN javac -d out exp08-primary-backup/src/tradex/primarybackup/*.java
WORKDIR /app/out
