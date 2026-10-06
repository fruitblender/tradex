FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out
RUN javac -d out exp03-clock-synchronization/src/tradex/clock/*.java
WORKDIR /app/out
CMD ["java", "tradex.clock.ClockSyncDemo"]
