FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.tar.gz \
    | tar -xz -C /opt \
    && ln -s /opt/apache-maven-3.9.11/bin/mvn /usr/local/bin/mvn
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home billbox \
    && mkdir -p /data/storage \
    && chown billbox:billbox /data/storage
WORKDIR /app
COPY --from=build /src/target/bill-box-api-*.jar /app/app.jar
USER billbox
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=50s --retries=12 \
    CMD curl -fsS http://127.0.0.1:8080/api/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
