#!/usr/bin/env bash
 docker run -v $PWD:/volume --rm -t clux/muslrust:nightly cargo build --release
 docker build -f Dockerfile_debian -t api-report:0.1.0-SNAPSHOT .