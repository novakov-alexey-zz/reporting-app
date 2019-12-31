{{- define "gbVersion" -}}
{{- required "No coreVersion set!  Specify it with --set gbVersion=1234 during `helm install`" .Values.gbVersion -}}
{{- end -}}

{{- define "prefix" -}}
{{- printf "%s" .Release.Name | trunc 38 | trimSuffix "-" -}}
{{- end -}}