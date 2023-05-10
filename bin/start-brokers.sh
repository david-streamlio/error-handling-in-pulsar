#!/bin/bash

INFRA_DIR="../infrastructure"

docker compose --project-name brokers --file $INFRA_DIR/broker.yml up -d

################################################
# Wait 5 seconds for Pulsar to start
################################################
sleep 5