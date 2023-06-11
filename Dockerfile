FROM amazoncorretto:17-alpine-jdk AS builder

COPY . /build
WORKDIR /build

RUN ./mvnw clean install
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM amazoncorretto:17-alpine

COPY --from=builder /build/target/dependency/BOOT-INF/lib /app/lib
COPY --from=builder /build/target/dependency/META-INF /app/META-INF
COPY --from=builder /build/target/dependency/BOOT-INF/classes /app
ENTRYPOINT ["java", "-Dspring-boot.run.profiles=prod", "-cp", "app:app/lib/*", "com.filipegeniselli.backendtechassignment.BackendTechAssignmentApplication"]


