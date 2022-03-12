FROM docker.io/cimg/clojure:1.10.3 AS builder

USER circleci

WORKDIR /home/circleci/project

COPY --chown=circleci:circleci deps.edn .
COPY --chown=circleci:circleci src ./src

# Compile into a uberjar
RUN clojure -X:depstar

FROM ghcr.io/graalvm/native-image:latest as native

USER root

WORKDIR /usr/src/app

COPY --from=builder /home/circleci/project/app.jar /usr/src/app/app.jar

# COPY resource-config.json .
COPY reflectconfig.json .

# Compile into a native binary
COPY _devops/compile.sh .

RUN sh compile.sh
RUN tar -cjvf app.tar.bz2 app

FROM gcr.io/distroless/base

COPY --from=native-tar /app.tar.bz2 /
