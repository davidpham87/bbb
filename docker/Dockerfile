# FROM docker.io/cimg/clojure:1.10.3 AS builder

# USER circleci

# WORKDIR /home/circleci/project

# COPY --chown=circleci:circleci deps.edn .
# COPY --chown=circleci:circleci src ./src

# # Compile into a uberjar
# RUN clojure -X:depstar

FROM ghcr.io/graalvm/native-image:21.3.0 as native

USER root

WORKDIR /usr/src/app

COPY app.jar /usr/src/app/app.jar

COPY reflection-config.json .
COPY compile.sh .

RUN sh compile.sh

FROM ubuntu as native-tar

COPY --from=native /usr/src/app/app /

RUN tar -cjvf app.tar.bz2 app

FROM alpine

COPY --from=native-tar /app.tar.bz2 /
