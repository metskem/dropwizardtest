echo "dumping envvars..." 
echo "======================================================"
env
echo "======================================================"
java -jar dropwizardtest*.jar server helloworld.yaml