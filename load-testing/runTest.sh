#!/bin/bash
# Må ha et gyldig token satt i miljøvariabelen TOKEN_DEV, dette kan settes på denne måten:
# export TOKEN_DEV="<gyldig token>"
siege -l -v -i -c15 -t5m --content-type "application/json" --header="Authorization: Bearer $TOKEN_DEV" -f urls-to-hit.txt
