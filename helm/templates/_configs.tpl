{{- define "api_search_conf" -}}
api-search.conf: |
  server {
    host = 0.0.0.0
    https-port = 9000
    http-port = 9080
  }
  es {
    host = {{ template "prefix" . }}-elasticsearch
    port = 80
  }
{{- end -}}