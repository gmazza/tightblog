#!/bin/bash

# $CATALINA_HOME/bin/shutdown.sh

# Stop and remove the container
docker rm -f tightblog-tightblog-web-1 tightblog-tightblog-db-1

# Remove the web image
docker rmi tightblog-tightblog-web

# Optionally prune unused volumes and networks
docker volume prune -f
# docker network prune -f

# Rebuild and start the containers using docker-compose
docker-compose -p tightblog up -d
