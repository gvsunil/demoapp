#java -javaagent:/Users/mihirgore/Downloads/AppServerAgent/ver4.2.1.6/javaagent.jar -jar build/libs/demoapp-bt-0.1-all.jar server
#java  -jar build/libs/demoapp-bt-0.1-all.jar server

//java -javaagent:/Users/mihirgore/Downloads/appagent432/ver4.3.2.0/javaagent.jar -Dappdynamics.agent.nodeName=JavaNode1 -Dappdynamics.agent.tierName=tier2 -jar build/libs/demoapp-bt-0.1-all.jar server config.yml
//java -javaagent:/Users/mihirgore/Downloads/appagent432/ver4.3.2.0/javaagent.jar -Dappdynamics.agent.reuse.nodeName=false -Dappdynamics.agent.reuse.nodeName.prefix=javaNode -Dappdynamics.agent.tierName=tier2 -jar build/libs/demoapp-bt-0.1-all.jar server config.yml

java -javaagent:/Users/mihirgore/Downloads/appagent432/ver4.3.2.0/javaagent.jar -Dappdynamics.agent.tierName=tier2  -jar build/libs/demoapp-bt-0.1-all.jar server config.yml
