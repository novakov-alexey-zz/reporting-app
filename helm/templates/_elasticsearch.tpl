{{- define "elasticsearch_yml" -}}
elasticsearch.yml: |
    # ======================== Elasticsearch Configuration =========================
    #
    # NOTE: Elasticsearch comes with reasonable defaults for most settings.
    #       Before you set out to tweak and tune the configuration, make sure you
    #       understand what are you trying to accomplish and the consequences.
    #
    # The primary way of configuring a node is via this file. This template lists
    # the most important settings you may want to configure for a production cluster.
    #
    # Please consult the documentation for further information on configuration options:
    # https://www.elastic.co/guide/en/elasticsearch/reference/index.html
    #
    # ---------------------------------- Cluster -----------------------------------
    #
    # Use a descriptive name for your cluster:
    #
    cluster.name: dev-k8s
    #
    # ------------------------------------ Node ------------------------------------
    #
    # Use a descriptive name for the node:
    #
    node.name: dev-k8s-1

    # Master node
    #node.master: false
    #node.data: true
    #node.ingest: false
    #
    # Add custom attributes to the node:
    #
    #node.attr.rack: r1
    #
    # ----------------------------------- Paths ------------------------------------
    #
    # Path to directory where to store the data (separate multiple locations by comma):
    #
    #path.data: /path/to/data
    #
    # Path to log files:
    #
    #path.logs: /path/to/logs
    #
    # ----------------------------------- Memory -----------------------------------
    #
    # Lock the memory on startup:
    #
    #bootstrap.memory_lock: true
    #
    # Make sure that the heap size is set to about half the memory available
    # on the system and that the owner of the process is allowed to use this
    # limit.
    #
    # Elasticsearch performs poorly when the system is swapping the memory.
    #
    # ---------------------------------- Network -----------------------------------
    #
    # Set the bind address to a specific IP (IPv4 or IPv6):
    #
    network.host: 0.0.0.0
    #
    # Set a custom port for HTTP:
    #
    http.port: 9200

    #transport.tcp.port: 9300
    #transport.publish_port: 59127
    #
    # For more information, consult the network module documentation.
    #
    # --------------------------------- Discovery ----------------------------------
    #
    # Pass an initial list of hosts to perform discovery when new node is started:
    # The default list of hosts is ["127.0.0.1", "[::1]"]
    #
    #discovery.zen.ping.unicast.hosts: ["10.0.1.36:59124", "10.0.1.36:59126"]
    #
    # Prevent the "split brain" by configuring the majority of nodes (total number of master-eligible nodes / 2 + 1):
    #
    #discovery.zen.minimum_master_nodes: 0
    #
    # For more information, consult the zen discovery module documentation.
    #
    # ---------------------------------- Gateway -----------------------------------
    #
    # Block initial recovery after a full cluster restart until N nodes are started:
    #
    #gateway.recover_after_nodes: 3
    #
    # For more information, consult the gateway module documentation.
    #
    # ---------------------------------- Various -----------------------------------
    #
    # Require explicit names when deleting indices:
    #
    #action.destructive_requires_name: true

    # This setting should be used when more than one host is running n the same machine
    #cluster.routing.allocation.same_shard.host: true
    #thread_pool.write.queue_size: 1500

    #-------------------------------- X-Pack settings
    xpack.security.enabled: {{ .Values.withXpackSecurity }}

    action.auto_create_index: .security,.monitoring*,.watches,.triggered_watches,.watcher-history*,.ml*

    {{ if .Values.withXpackSecurity }}
    xpack:
      security:
        authc:
          realms:
            ldap1:
              type: ldap
              order: 0
              url: "ldap://{{ template "prefix" . }}-reportingapp-ldap:10389"
              bind_dn: "uid=admin,ou=system"
              bind_password: secret
              user_search:
                base_dn: "ou=users,ou=system"
              group_search:
                base_dn: "ou=groups,ou=system"
              files:
                role_mapping: "/usr/share/elasticsearch/config/role_mapping.yml"
              unmapped_groups_as_roles: true
    {{ end }}
{{- end -}}
