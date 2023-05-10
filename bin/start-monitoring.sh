#!/bin/bash

INFRA_DIR="../infrastructure"

docker compose --project-name monitoring --file $INFRA_DIR/monitoring.yml up -d