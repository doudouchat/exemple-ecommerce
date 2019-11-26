# exemple-ecommerce

## maven

execute with cargo and cassandra
mvn clean verify -Pwebservice,it

execute without cargo and cassandra
mvn clean verify -Pit -Dauthorization.port=8084 -Dapplication.port=8080

## Docker

. docker build -t exemple-ecommerce-api exemple-ecommerce-api
. docker build -t exemple-ecommerce-authorization exemple-ecommerce-authorization
. docker build -t exemple-ecommerce-db exemple-ecommerce-api-integration

. docker-compose up -d zookeeper
. docker-compose up -d cassandra
. docker-compose exec cassandra cqlsh --debug -f /usr/local/tmp/cassandra/schema.cql
. docker-compose exec cassandra cqlsh --debug -f /usr/local/tmp/cassandra/exec.cql
. docker-compose up -d web
. docker-compose up -d authorization



docker-compose exec web cat logs/localhost.2018-08-24.log

keytool -genkeypair -alias mytest -keyalg RSA -keypass mypass -keystore mytest.jks -storepass mypass
