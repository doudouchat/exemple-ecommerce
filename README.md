# exemple-ecommerce

## maven

<p>execute with cargo and cassandra <code>mvn clean verify -Pwebservice,it</code></p>

<p>execute without cargo and cassandra <code>mvn clean verify -Pit -Dauthorization.port=8084 -Dapplication.port=8080</code></p>

## Docker

<ol>
<li>docker build -t exemple-ecommerce-api exemple-ecommerce-api</li>
<li>docker build -t exemple-ecommerce-authorization exemple-ecommerce-authorization</li>
<li>docker build -t exemple-ecommerce-db exemple-ecommerce-api-integration</li>
</ol>

<ol>
<li>docker-compose up -d zookeeper</li>
<li>docker-compose up -d cassandra</li>
<li>docker-compose exec cassandra cqlsh --debug -f /usr/local/tmp/cassandra/schema.cql</li>
<li>docker-compose exec cassandra cqlsh --debug -f /usr/local/tmp/cassandra/exec.cql</li>
<li>docker-compose up -d web</li>
<li>docker-compose up -d authorization</li>
</ol>

docker-compose exec web cat logs/localhost.2018-08-24.log

## Certificate

keytool -genkeypair -alias mytest -keyalg RSA -keypass mypass -keystore mytest.jks -storepass mypass
