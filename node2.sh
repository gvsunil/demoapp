#!/usr/bin/env bash

#java -javaagent:/Users/mihirgore/Downloads/appagent432/ver4.3.2.0/javaagent.jar -Dappdynamics.agent.applicationName=test-application -Dappdynamics.agent.tierName=newTier -Dappdynamics.agent.nodeName=JavaNode20 -Dappdynamics.agent.accountName=customer1  -jar build/libs/demoapp-bt-0.1-all.jar server node2.yml

java -javaagent:/Users/mihirgore/Downloads/appagent432/ver4.3.2.0/javaagent.jar -Dappdynamics.agent.tierName=tier20 -Dappdynamics.agent.nodeName=Node20  -jar build/libs/demoapp-bt-0.1-all.jar server node2.yml
