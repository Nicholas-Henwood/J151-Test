# syntax=docker/dockerfile:1

# ---- Stage 1: build the WAR with Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache dependencies first (only re-runs when pom.xml changes)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build the WAR (tests skipped for the deployment image)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Stage 2: run the WAR on Payara Micro (Jakarta EE 10 / JDK 17) ----
# Payara 6 == Jakarta EE 10, matching the frontend's jakartaee 10.0.0 dependency.
# Do NOT bump to payara/micro:7 / :latest (Jakarta EE 11 on JDK 25).
FROM payara/micro:6.2024.10-jdk17

# Serve at the domain root. WEB-INF/glassfish-web.xml pins <context-root>/</context-root>,
# and ROOT.war reinforces the root context.
COPY --from=build /build/target/*.war $DEPLOY_DIR/ROOT.war

EXPOSE 8080
