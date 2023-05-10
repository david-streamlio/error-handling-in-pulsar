#!/bin/bash

INFRA_DIR="../infrastructure"

docker compose --project-name control-plane --file $INFRA_DIR/control-plane.yml down