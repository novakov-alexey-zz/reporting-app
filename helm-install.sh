#!/usr/bin/env bash

if [[ -z "$1" ]]
  then
    registryParam=""
else
    registryParam="--set dockerRegistry=$1"
    echo "\033[0;33m registry: $1 \033[0m"
fi

helm install --name gb --namespace gb $registryParam ./helm

# label gb namespace for Istio. This is needed to let Istio inject the side-card Pods as par t of the Service Mesh
kubectl label namespace gb istio-injection=enabled --overwrite