## Reporting app

## api-search
REST API 

### 1. Query

POST /api/v1/query/all

POST /api/v1/query/some?ds=&lt;datasource1>[,&lt;datasource2>]

request:
```json
{
  "query": {
    "term": "Jonas, Hamburg, gmx.de, M.A.",
    "and_token": ","
  },
  "script_property": [
    {
      "property": "old",
      "script": "if (doc.containsKey('update_date')) {  doc['update_date'].value.getMillis()  < params.maxDate; } else false;",
      "params": {
        "maxDate": 1506117600000
      }
    }
  ],
  "highlights": [
    {
      "type": "similarities",
      "properties": [
        "first_name.keyword",
        "last_name.keyword",
        "city.keyword",
        "post_code",
        "address.keyword",
        "private_phone.keyword",
        "private_mobile.keyword",
        "academic_degree.keyword",
        "private_email.keyword",
        "birth_day"
      ]
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10
  }
}
```

- **and_token** parameter is _optional_. Contains a string to be used as logical AND token. 

- **script_property** section is _optional_. It allows to call Elasticsearch Scripted Fields functionality.

 - "property": value is up to user, it will be used to name property in the result

 - "script": is 1 to 1 ES painless script expression

 - "params":  it is a map of key/value to bind parameters in the script value

 - "maxDate": 1506117600000 is time in milliseconds (from example above) 
 
 - "highlights" is a section to request a list of properties to be highlighted. An example with type="similarities" 
 has a list of properties to be analyzed for highlighting. All text properties are put with ".keyword" as per 
 Elasticsearch keyword type. Names of the properties needs to be specified by user. 
 
 - **pagination** section is _optional_. Default page is 0 and size is 10.
 Maximum offset is 10000, i.e. page + size should not be greater than Maximum offset. Otherwise, page and size values 
 will automatically adjusted to last possible page which will be lower than Maximum offset.

response:
```json
{
  "metadata": {
    "count": 1990,
    "count_by_source": [
      {
        "contact": 1688
      },
      {
        "itusage": 302
      }
    ]
  },
  "rows": [
   {
      "first_name": "Jonas",
      "client_id": 18253,
      "city": "Kiel",
      "post_code": 19055,
      "academic_degree": "M.A.",
      "address": "Impasse dos Santos 5",
      "old": true,
      "datasource": "contact",
      "private_mobile": "+49-147-7204336",
      "birth_day": "1985-05-12T11:44:05.856",
      "private_email": "jonas.flewelling@gmx.de",
      "salutation": "Herr",
      "private_phone": "+49-147-0000001",
      "last_name": "Flewelling",
      "update_date": "2004-07-07T02:59:13.356",
      "highlights": [
        "first_name",
        "city",
        "private_phone",
        "academic_degree"
      ]
    }    
  ]
}     
```

### 2. Meta

GET /api/v1/meta/datasources

response:

```json
{
   "list": [
        "itusage",
        "contact",
        "bills",
        "banking"
    ]
}
```

### 3. Correlation

POST /api/v1/correlation/search

request:

```json
{
	"query": {
		"term": "10000"
	},
	"datasource_from": "contact",
	"datasource_to": ["bills"],
	"properties": ["client_id"],
	"pagination": {
		"page": 0,
		"size": 10
	}
}
```

- _datasource_to_ is optional parameter. If it is omitted, then api will search all other datasources except the one in "datasource_from"
- _properties_ is optional parameter. Properties are used to apply the query term to them. If it is omitted, then **query.term** will be applied to all the datasource properties

response format is the same as before, i.e. search query:

```json
{
  "metadata": {
    "count": 2
  },
  "rows": [
    {
      "client_id": 10000,
      "amount": 40.0,
      "datasource": "bills",
      "id": "63610171#001"
    },
    {
      "client_id": 10000,
      "amount": 1568.9793510517648,
      "datasource": "bills",
      "id": "99999999#001"
    }
  ]
}
```

POST /api/v1/correlation/stats

request:

```json
{
	"query": {
		"term": "10000"
	},
	"datasource_from": "contact",	
	"properties": ["client_id"],
	"pagination": {
		"page": 0,
		"size": 10
	}
}
```

response:

```json
{
  "buckets": [
    {
      "key": "10000",
      "count": 5,
      "subBuckets": [
        {
          "key": "banking",
          "count": 2,
          "subBuckets": []
        },
        {
          "key": "bills",
          "count": 2,
          "subBuckets": []
        },
        {
          "key": "itusage",
          "count": 1,
          "subBuckets": []
        }
      ]
    }
  ]
}
```
Response represents nested structure to show the aggregation by query.term value. First level of buckets are 
aggregations by query.term value(s). Also, every bucket has "count" property which is number of records found with 
the same query.term. Second level of buckets is aggregation by datasource name. Potentially, sub-bucketing can go 
infinitely deep, but it is not implemented yet.


## gen-data
Demo data generator for web-search/api-search. Data is generated in runtime and stored immediately at Elasticsearch

### Run gen-data as container locally

```bash
docker run -e ES_HOST=<put host or IP here> gen-data:0.1.0-SNAPSHOT
```
_Note that gen-data application is removing all the indices before generating the data_

## api-report
Report generation microservice

### Running without Docker
This service is using wkhtmltopdf binary. Install it on your OS:

Ubuntu: 
```bash
sudo apt-get update
sudo apt-get install xvfb libfontconfig wkhtmltopdf
```

OSX:
```bash
brew install Caskroom/cask/wkhtmltopdf
``` 

### 1. Generate PDF report

request:
See request example in api-report/samples/request.json

response:
PDF file with "Content-Type	application/pdf" HTTP header

## Dockerize components

```bash
sbt stage
sbt docker:stage
sbt docker:publishLocal
```

## Build environment using SBT

To build all components including Docker images run:
  - build.sh

## Start environment using Docker Compose

To start and remove the docker-compose environment, use
- start.sh
- remove.sh

*Elasticsearch* will be persisted and stored at composed/esdata

## TLS and certificates

At the moment we use self-signed certificates for services behind the Apache (gateway)

One of the way to create Certificate and Private Key:

```bash
keytool -keystore keystore.jks  -keyalg RSA -genkeypair -alias server -dname 'CN=<service name>,L=Frankfurt,ST=Hessen,C=GE' -validity 360 -keysize 2048 -storepass <password here>

```

See more on how to import Let's Encrypt certificate into java keystore: https://bitbucket.org/alexey-novakov/....


## Start environment in Microk8s on Ubuntu

**Note:** microk8s kubernetes is single node Kubernetes cluster which work only on Ubuntu. It does not require any Virtual Machine. It runs directly on the host.

### Install Microk8s

Install via snap:

```bash
snap install microk8s --classic
```

See more details on how to install it here: https://microk8s.io/docs/

Then, enable required moduels:

```bash
microk8s.enable dns 
microk8s.enable registry
microk8s.enable ingress
```

Optionally, make an alias for kubectl of microk8s:

```bash
snap alias microk8s.kubectl kubectl
```

### Run Reporting-app in Microk8s

Make sure you have all images on the host, otherwise build using script:

```bash
sh build.sh
```

Then push images to Microk8s registry:

```sh
./push-2-k8s localhost:32000
```

To start:
```bash
./gb-helm-install.sh
```

To remove:
```bash
./gb-helm-delete.sh
```


#### Access Web UI in Minikube using NGINX Ingress Controller 

Make sure that you have minikube in /etc/hosts file as "ip-host" record. See below. 

Replace it with IP of 'minikube ip' command
```text
<minikube ip> minikube
```

```text
For example, I have below minikube IP and the mapping in hosts file looks like this:
192.168.99.100 minikube	
```

Open Browser at https://minikube


#### Access Web UI in minikube using Istio Ingress Gateway

1. Forward HTTPS port of Istio gateway: 
```bash
sudo kubectl port-forward service/istio-ingressgateway -n istio-system 443:443
```

Open Browser at https://localhost

### Upload Docker images to Minikube

1. First, create docker-registry inside the K8s cluster
    ```bash
    kubectl create -f https://gist.githubusercontent.com/coco98/b750b3debc6d517308596c248daf3bb1/raw/6efc11eb8c2dce167ba0a5e557833cc4ff38fa7c/kube-registry.yaml
    ```
1. Open port via port-forward:
    ```bash
    kubectl port-forward --namespace kube-system \
    $(kubectl get po -n kube-system | grep kube-registry-v0 | \
    awk '{print $1;}') 5000:5000
    ```
1. Create a mapping from `minikube ip` to `minikube` hostname in /etc/hosts file:
`192.168.99.100  minikube` 

1. Push images using `docker push` or existing script:
    ```bash
    ./push-2-k8s minikube:5000
    ```
### Istio Service Mesh

Istio is a service mesh, which enables several useful functionality to monitor microservice-based application:

Install:
```bash
./istio-install.sh
```

Remove:
```bash
./istio-remove.sh
```

### Istio Tools

#### Kiali for Application Graph Visualisation

```bash
kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=kiali -o jsonpath='{.items[0].metadata.name}') 20001:20001
```

#### Grafana for Service Metrics

```bash
kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=grafana -o jsonpath='{.items[0].metadata.name}') 3000:3000
```

#### Kibana for Service and Application Stdout Logs

```bash
kubectl -n logging port-forward $(kubectl -n logging get pod -l app=kibana -o jsonpath='{.items[0].metadata.name}') 5601:5601
```    