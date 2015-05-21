#!/bin/sh
#
# Simple script to install a mesos slave node.
#  A slave node should run :
#    * mesos-slave
#
logDie() {

echo $1
exit 8
}


#  main :
NUMARGS=$#
if [ $NUMARGS -ne 3 ];then
  echo "Usage: $0 <mesos-master1 mesos-master2> <stage: test|prod>"
  echo "Example: $0 lsrv5257 lsrv5258 test"
  exit 8
fi

MASTER1=$1
MASTER2=$2
STAGE=$3
LOCATION=$(grep `hostname -s` /appl/info/lsrv_vms.csv|cut -d';' -f38 | cut -d"\"" -f2)
echo "current system location is: $LOCATION"

#  create filesystems
echo "creating filesystems..."
mk_fs --lv varlogmesos --vg vg.appl --size 2G --owner root --perms 755 --fstype ext4 /var/log/mesos || logDie "creating /var/log/mesos fs failed"
mk_fs --lv varlibmesos --vg vg.appl --size 3G --owner root --perms 755 --fstype ext4 /var/lib/mesos || logDie "creating /var/lib/mesos fs failed"
echo ""

#  install rpms
echo "Installing rpms..."
yum -y install java-1.8.0-openjdk || logDie "rpm install java-1.8.0 failed"
yum -y install cyrus-sasl-md5 || logDie "rpm install cyrus-sasl-md5 failed"
rpm -i rpms/mesos-0.22.0-1.0.centos65.x86_64.rpm || logDie "rpm install mesos failed"

#  copy config files
echo "writing config files"

F=/etc/mesos/zk
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
zk://${MASTER1}.linux.rabobank.nl:2181/mesos,${MASTER2}.linux.rabobank.nl:2181/mesos
EOF

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

F=/etc/default/mesos-slave
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
MASTER=\`cat /etc/mesos/zk\`
CLUSTER=cluster1
EOF

F=/etc/mesos-slave/attributes
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
stage:${STAGE};location:${LOCATION}
EOF

F=/etc/mesos-slave-credential
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
slave-user slave-passw0rd
EOF
chmod 700 /etc/mesos-slave-credential

F=/etc/mesos-slave/credential
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
file://etc/mesos-slave-credential
EOF

#F=/etc/mesos-slave/resources
#> $F
#while read line
#  do
#     echo $line >> $F
#  done <<EOF
#cpus(*):0.8; mem(*):512; ports(*):[31000-32000]
#EOF

F=/etc/mesos-slave/work_dir
> $F
while read line
  do
     echo $line >> $F
  done <<EOF
/var/lib/mesos
EOF
