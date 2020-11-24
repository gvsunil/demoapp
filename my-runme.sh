# java -javaagent:/Users/mihirgore/Downloads/AppServerAgent/ver4.2.1.6/javaagent.jar -jar build/libs/demoapp-java
# .1-all.jar server
# java -javaagent:/Users/gvsunil/agents/AppServerAgent-4.2.8.1/ver4.2.8.1/javaagent.jar -jar build/libs/demoapp-0.1-all.jar server
#java -javaagent:/Users/gvsunil/ad_repos/cart-tmp/TIER2TOMCAT/appagent/ver4.5.0.0/javaagent.jar -jar build/libs/demoapp-0.1-all.jar server
java -javaagent:/Users/gvsunil/agents/AppServerAgent-4.2.8.1/ver4.2.8.1/javaagent.jar -Dappdynamics.agent.applicationName=Test -Dappdynamics.agent.nodeName=JavaNode2 -Dappdynamics.agent.tierName=tier3 -jar build/libs/demoapp-0.1-all.jar server config.yml
