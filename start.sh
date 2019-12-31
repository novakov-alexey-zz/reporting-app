#!/usr/bin/env bash
rm -rf composed/esdata
mkdir -p composed/esdata
chmod a+w composed/esdata
docker-compose -f composed/docker-compose.yml up -d