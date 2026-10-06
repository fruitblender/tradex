FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN mkdir -p out
# Compile common models and RMI classes together
RUN javac -d out common/src/tradex/model/*.java exp01-rmi/src/tradex/rmi/*.java
WORKDIR /app/out
CMD ["java", "tradex.rmi.RmiServer"]
