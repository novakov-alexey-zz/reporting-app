#!/usr/bin/env bash

if [[ -z ${ISTIO_HOME+x} ]]; then
    echo "ISTIO_HOME env variable is unset. Please set it to Istio folder";
    exit 1
else
    echo "ISTIO_HOME is set to '$ISTIO_HOME'";
fi

kubectl delete -f $ISTIO_HOME/install/kubernetes/helm/istio/templates/crds.yaml -n istio-system

helm delete --purge istio

kubectl delete -f istio/logging-stack.yaml