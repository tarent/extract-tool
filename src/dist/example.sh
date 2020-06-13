#!/bin/sh
exec java \
    -Djdbc.driver="${db:-org.postgresql.Driver}" \
    -Djdbc.url="${dburl:-jdbc:postgresql://localhost:5432/hellophpworld}" \
    -Djdbc.username="${dbuser:-hellophpworld}" \
    -Djdbc.password="${dbpass:-P68ntEvJQbhI}" \
    -jar extract-tool-*-cli.jar \
    -c /dev/null \
    -J ~/.m2/repository/org/postgresql/postgresql/42.2.14/postgresql-42.2.14.jar \
    "${1:-$(dirname "$0")/example.jsn}"
