#!/usr/bin/env bash
tag=$1
for imageid in $(docker images | grep $tag | awk '{print $3}'); do docker rmi -f $imageid; done