#!/bin/sh
exec java \
    -Djdbc.driver=org.postgresql.Driver \
    -Djdbc.url=jdbc:postgresql://localhost:5432/hellophpworld \
    -Djdbc.username=hellophpworld \
    -Djdbc.password=bI6mtPBxVjEJ \
    -jar extract-tool-*-cli.jar \
    -c /dev/null \
    -J ~/.m2/repository/org/postgresql/postgresql/42.2.8/postgresql-42.2.8.jar \
    "$(dirname "$0")/example.jsn"
