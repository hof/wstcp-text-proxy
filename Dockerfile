FROM maven:3-jdk-8 as builder

WORKDIR /workspace
RUN git clone https://github.com/hof/wstcp-text-proxy.git .
RUN mvn install

FROM openjdk:8

WORKDIR /app
COPY --from=builder /workspace/target/wstcp-text-proxy-1.1.1-dist.jar /app

CMD ["java", "-jar", "wstcp-text-proxy-1.1.1-dist.jar"]