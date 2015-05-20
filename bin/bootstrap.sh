#!/bin/sh
#
#  bootstrap script to start an application.
#  The script is normally invoked by the mesos executor, it downloads stuff from a location and runs it.
#
DOWNLOAD_LOCATION=metskeh@lsrv4069.linux.rabobank.nl:vamp/apps

# not anymore, APP should be defined as an envvar
#APP=`echo $MARATHON_APP_ID |cut -c2-99`
echo ""
echo "bootstrapping application $APP from ${DOWNLOAD_LOCATION}/$APP with the following envvars:"
env
echo ""

scp -i /home/metskeh/.ssh/id_rsa_mesos -r ${DOWNLOAD_LOCATION}/$APP . && cd $APP && ./start.sh
# and clean up the mess afterwards:
cd .. && rm -rvf $APP
