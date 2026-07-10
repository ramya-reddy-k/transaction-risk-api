FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY . .

RUN chmod +x mvnw &&     ./mvnw --batch-mode --no-transfer-progress     -DskipTests clean package


FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd --system     --uid 10001     --create-home     --shell /usr/sbin/nologin     appuser

COPY --from=build /workspace/target/*.jar /app/app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
