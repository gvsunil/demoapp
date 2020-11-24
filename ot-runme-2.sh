java -javaagent:/Users/gvsunil/Downloads/opentelemetry-javaagent-all.jar -Dotel.exporter=zipkin -Dotel.zipkin.service.name=service2 -jar build/libs/demoapp-0.1-all.jar server config2.yml
