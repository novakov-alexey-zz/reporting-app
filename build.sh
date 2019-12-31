#!/usr/bin/env bash
# build SBT-based components
sbt clean compile stage docker:stage docker:publishLocal
# build other components
cd api-report && sh docker-image.sh