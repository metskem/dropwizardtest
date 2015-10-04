# dump envvars:
env
# set current dir
cd ${0%/*}
# run app
java -jar dropwizardtest*.jar server helloworld.yaml