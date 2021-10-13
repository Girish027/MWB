## KOPF
how to open Kopf   
http://localhost:9400/_plugin/kopf/#!/cluster 

### SEARCH

Searches for intents added by username

* open up kopf
* under rest
* /nltools/_search
* POST
```
{
  "query": {
    "bool": {
      "must": [
        {
          "term": {
            "documentType": "intent-added"
          }
        },
        {
          "term": {
            "projectId": 3
          }
        },
        {
          "exists": {
            "field": "intent"
          }
        }
      ]
    }
  },
  "fields": [
    "transcriptionHash",
    "intent",
    "taggedBy"
  ]
}
```

### Add Ingestion Guide

[API on Confluence](https://247inc.atlassian.net/wiki/display/APT/APIs) See "Adding Tagging Guide"

* open up kopf
* under rest
* set the url as /intentsguide/
* method as PUT
* send
```
{
  "settings": {
    "number_of_shards": 2,
    "number_of_replicas": 1,
    "index.requests.cache.enable": true
  },
  "mappings": {
    "topic_goal": {
      "properties": {
        "intent": {
          "type": "string",
          "index": "not_analyzed",
          "doc_values": true,
          "omit_norms": true,
          "ignore_above": 256
        },
        "description": {
          "type": "string",
          "index": "not_analyzed",
          "doc_values": true,
          "omit_norms": true,
          "ignore_above": 512
        }
      }
    }
  }
}
```