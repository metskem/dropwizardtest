#!/bin/sh
# dump envvars:
env
# cd to dir, but not to current dir
cd $(find . -type d ! -name ".")
# run app
java -jar dropwizardtest*.jar server helloworld.yaml