FROM openjdk:17-slim AS builder

WORKDIR /app

COPY src/main/java ./src

RUN mkdir out && javac -d out src/org/example/TcpEchoServer.java src/org/example/TcpEchoClient.java

FROM openjdk:17-slim

WORKDIR /app

COPY --from=builder /app/out ./out

EXPOSE 8007

CMD ["java", "-cp", "out", "org.example.TcpEchoServer", "8007"]
