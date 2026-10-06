FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out
RUN javac -d out exp06-load-balancing/src/tradex/loadbalancer/*.java
WORKDIR /app/out
