#!/bin/sh
#
# Simple script to install a mesos master node.
#  A master node should run :
#    * mesos-master
#    * marathon
#    * zookeeper
#
logDie() {

echo $1
exit 8
}


#  main :
NUMARGS=$#
if [ $NUMARGS -ne 3 ];then
  echo "Usage: $0 <mesos-master1 mesos-master2> <zookeeper id>"
  echo "Example: $0 lsrv5257 lsrv5258 1"
  exit 8
fi

MASTER1=$1
MASTER2=$2
ZOOKEEPERID=$3


#  create filesystems
echo "creating filesystems..."
mk_fs --lv varlogmesos --vg vg.appl --size 2G --owner root --perms 755 --fstype ext4 /var/log/mesos || logDie "creating /var/log/mesos fs failed"
mk_fs --lv varlibmesos --vg vg.appl --size 3G --owner root --perms 755 --fstype ext4 /var/lib/mesos || logDie "creating /var/lib/mesos fs failed"
# zookeeper does not need a separate fs :
mkdir /var/lib/zookeeper && chmod 755 /var/lib/zookeeper
mkdir /usr/local/zookeeper && chmod 755 /usr/local/zookeeper
echo ""

#  install rpms
echo "Installing rpms..."
yum -y install java-1.8.0-openjdk || logDie "rpm install java-1.8.0 failed"
yum -y install cyrus-sasl-md5 || logDie "rpm install cyrus-sasl-md5 failed"
rpm -i rpms/mesos-0.22.0-1.0.centos65.x86_64.rpm || logDie "rpm install mesos failed"
rpm -i --nodeps rpms/marathon-0.8.1-1.0.171.el6.x86_64.rpm || logDie "rpm install marathon failed"

# install zookeeper
echo "installing zookeeper..."
OLDCURDIR=`pwd`
cd /usr/local
#  this somehow appears after install of mesos/marathon, we remove it, we want a symlink
rm -rf zookeeper
tar -xf $OLDCURDIR/rpms/zookeeper-3.4.6.tar || logDie "install of zookeeper failed"
# reclaim some space:
rm -rf zookeeper-3.4.6/src zookeeper-3.4.6/contrib zookeeper-3.4.6/bin/*.cmd
chown -R root.root zookeeper*
chmod -R 755 zookeeper*
ln -s "zookeeper-3.4.6" "zookeeper" || logDie "install of zookeeper failed"
echo "$ZOOKEEPERID" > /var/lib/zookeeper/myid
cd -

#  copy config files
echo "writing config files"
F=/etc/cron.daily/cleanupmesoslogs
> $F
while read line
  do
     echo $line >> $F
  done <<EOF 
find /var/log/mesos -type f -mtime +5 -exec rm -f {} \;
EOF

F=/etc/default/mesos
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
LOGS=/var/log/mesos
ULIMIT="-n 8192"
EOF

F=/etc/default/mesos-master
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
PORT=5050
ZK=\`cat /etc/mesos/zk\`
CLUSTER=cluster1
EOF

F=/etc/mesos-master/authenticate_slaves
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
true
EOF

F=/etc/mesos-master-credentials
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
slave-user slave-passw0rd
EOF
chmod 700 /etc/mesos-master-credentials

F=/etc/mesos-master/credentials
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
file://etc/mesos-master-credentials
EOF

F=/etc/mesos-master/quorum
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
1
EOF

F=/etc/mesos-master/work_dir
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
/var/lib/mesos
EOF

F=/etc/mesos/zk
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
zk://${MASTER1}.linux.rabobank.nl:2181/mesos,${MASTER2}.linux.rabobank.nl:2181/mesos
EOF

F=/usr/local/zookeeper/conf/zoo.cfg
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just
# example sakes.
dataDir=/var/lib/zookeeper
# the port at which the clients will connect
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1
server.1=${MASTER1}.linux.rabobank.nl:2182:2183
server.2=${MASTER2}.linux.rabobank.nl:2182:2183
EOF
