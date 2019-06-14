#!/bin/sh
exec java \
    -Djdbc.driver=org.postgresql.Driver \
    -Djdbc.url=jdbc:postgresql://localhost:5432/hellophpworld \
    -Djdbc.username=hellophpworld \
    -Djdbc.password=somepassword \
    -jar extract-tool-*-cli.jar \
    -c /dev/null \
    -J ~/.m2/repository/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar \
    example.jsn
