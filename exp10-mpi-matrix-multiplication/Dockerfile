FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install wget and tar
RUN apt-get update && apt-get install -y wget tar

# Download MPJ Express (v0.44)
RUN wget -q -O mpj.tar.gz https://sourceforge.net/projects/mpjexpress/files/releases/mpj-v0.44.tar.gz/download \
    && tar -xzf mpj.tar.gz \
    && mv mpj-v0.44 /opt/mpj \
    && rm mpj.tar.gz

ENV MPJ_HOME=/opt/mpj
ENV PATH=$MPJ_HOME/bin:$PATH

COPY . .

# Compile
RUN mkdir -p out
RUN javac -cp "$MPJ_HOME/lib/mpj.jar" -d out exp10-mpi-matrix-multiplication/src/tradex/mpi/*.java

WORKDIR /app
