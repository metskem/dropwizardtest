#!/usr/bin/env bash
# dump envvars:
env
# set current dir
cd ${0%/*}
# run app
java -javaagent:/appl/hc/consul/bin/consul-registrator-1.0.0-SNAPSHOT.jar=logger=debug -jar dropwizardtest*.jar server helloworld.yaml