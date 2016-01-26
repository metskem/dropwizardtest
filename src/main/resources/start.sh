export SERVICENAME=service01
echo "dumping envvars..." 
echo "======================================================"
env
echo "======================================================"
curl --silent --show-error -XPUT http://${HOSTNAME}:8500/v1/agent/service/deregister/${SERVICENAME}
echo "PUTing to consul:"
echo ""
echo "{\"name\":\"${SERVICENAME}\",\"port\":$PORT0,\"checks\":[{\"http\":\"http://${HOSTNAME}:${PORT1}/health\",\"interval\":\"10s\"}]}"
echo ""
curl --silent --show-error -XPUT --data "{\"name\":\"${SERVICENAME}\",\"port\":$PORT0,\"checks\":[{\"http\":\"http://${HOSTNAME}:${PORT1}/healthcheck\",\"interval\":\"10s\"}]}" http://${HOSTNAME}:8500/v1/agent/service/register && \
echo "starting java app...."
java -jar dropwizardtest*.jar server helloworld.yaml
echo "deregistering service ${SERVICENAME}..."
curl --silent --show-error -XPUT http://${HOSTNAME}:8500/v1/agent/service/deregister/${SERVICENAME}
echo "ended"