#!/bin/sh
exec java \
    -Djdbc.driver="${db:-org.postgresql.Driver}" \
    -Djdbc.url="${dburl:-jdbc:postgresql://localhost:5432/hellophpworld}" \
    -Djdbc.username="${dbuser:-hellophpworld}" \
    -Djdbc.password="${dbpass:-bI6mtPBxVjEJ}" \
    -jar extract-tool-*-cli.jar \
    -c /dev/null \
    -J ~/.m2/repository/org/postgresql/postgresql/42.2.8/postgresql-42.2.8.jar \
    "${1:-$(dirname "$0")/example.jsn}"
