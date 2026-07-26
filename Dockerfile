FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :server:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/server/build/install/server ./
ENV PORT=8080
EXPOSE 8080
CMD ["./bin/server"]
