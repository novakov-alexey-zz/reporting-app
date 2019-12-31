#!/usr/bin/env bash

if [[ -z ${ISTIO_HOME+x} ]]; then
    echo "ISTIO_HOME env variable is unset. Please set it to Istio folder";
    exit 1
else
    echo "ISTIO_HOME is set to '$ISTIO_HOME'";
fi

helm install \
$ISTIO_HOME/install/kubernetes/helm/istio \
--name istio \
--namespace istio-system \
-f helm/istio-minikube-values.yaml

kubectl apply -f istio/logging-stack.yaml
sleep 5 # there is some race-condition, so waiting
kubectl apply -f istio/fluentd-istio.yaml