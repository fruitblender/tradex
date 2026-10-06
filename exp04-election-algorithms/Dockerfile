FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out
RUN javac -d out exp04-election-algorithms/src/tradex/election/bully/*.java exp04-election-algorithms/src/tradex/election/ring/*.java
WORKDIR /app/out
