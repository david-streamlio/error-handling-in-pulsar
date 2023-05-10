#!/bin/bash

INFRA_DIR="../infrastructure"

docker compose --project-name brokers --file $INFRA_DIR/broker.yml down
