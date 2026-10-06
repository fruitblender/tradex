FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out
RUN javac -d out exp07-mapreduce/src/tradex/mapreduce/*.java
WORKDIR /app
CMD ["java", "-cp", "out", "tradex.mapreduce.MapReduceDemo"]
