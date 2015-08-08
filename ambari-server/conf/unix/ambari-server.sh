#!/usr/bin/env bash
# chkconfig: 345 95 20
# description: ambari-server daemon
# processname: ambari-server

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# /etc/init.d/ambari-server
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
export INSTALL_DIR=$DIR
export ROOT_FS_PATH=$DIR
export AMBARI_SERVER_LIB=$INSTALL_DIR/lib
export STACK_LOCATION_DEFAULT=$INSTALL_DIR/resources/stacks
export OUT_DIR=$INSTALL_DIR/logs
mkdir -p $OUT_DIR
VERSION="2.1.0"
HASH="${buildNumber}"

case "$1" in
  --version)
        echo -e $VERSION
        exit 0
        ;;
  --hash)
        echo -e $HASH
        exit 0
        ;;
esac

export AMBARI_CONF_DIR=$INSTALL_DIR/conf
export PATH=$INSTALL_DIR/lib/*:$AMBARI_CONF_DIR:$PATH

# Because Ambari rpm unpacks modules here on all systems
export PYTHONPATH=sbin:/opt/pysrc:$PYTHONPATH

if [ -a /usr/bin/python2.7 ] && [ -z "$PYTHON" ]; then
  PYTHON=/usr/bin/python2.7
fi

if [ -a /usr/bin/python2.6 ] && [ -z "$PYTHON" ]; then
  PYTHON=/usr/bin/python2.6
fi

if [ -a bin/ambari-env.sh ]; then
  bin/ambari-env.sh
fi

if [ -z "$PYTHON" ]; then
  PYTHON=/usr/bin/python
fi

if [ -z "$AMBARI_PASSPHRASE" ]; then
  AMBARI_PASSPHRASE="DEV"
fi

if [ -n "$JAVA_HOME" ]; then
  export JAVA_HOME=$JAVA_HOME
fi

export AMBARI_PASSPHRASE=$AMBARI_PASSPHRASE

# check for version
majversion=`$PYTHON -V 2>&1 | awk '{print $2}' | cut -d'.' -f1`
minversion=`$PYTHON -V 2>&1 | awk '{print $2}' | cut -d'.' -f2`
numversion=$(( 10 * $majversion + $minversion))
if (( $numversion < 26 )); then
  echo "Need python version > 2.6"
  exit 1
fi
echo "Using python " $PYTHON
ret=0
case "$1" in
  start)
        echo -e "Starting ambari-server"
        $PYTHON sbin/ambari-server.py $@
        ;;
  stop)
        echo -e "Stopping ambari-server"
        $PYTHON sbin/ambari-server.py $@
        ;;
  reset)
        echo -e "Resetting ambari-server"
        $PYTHON sbin/ambari-server.py $@
        ;;
  restart)
        echo -e "Restarting ambari-server"
        $0 stop
        $0 start
        ;;
  upgrade)
        echo -e "Upgrading ambari-server"
        $PYTHON sbin/ambari-server.py $@
        ;;
  status)
        echo -e "Ambari-server status"
        $PYTHON sbin/ambari-server.py $@
        ;;
  upgradestack)
        echo -e "Upgrading stack of ambari-server"
        $PYTHON sbin/ambari-server.py $@
        ;;
  setup)
        echo -e "Setup ambari-server"
        $PYTHON sbin/ambari-server.py $@
        ;;
  setup-jce)
        echo -e "Updating jce policy"
        $PYTHON sbin/ambari-server.py $@
        ;;
  setup-ldap)
        echo -e "Setting up LDAP properties..."
        $PYTHON sbin/ambari-server.py $@
        ;;
  sync-ldap)
        echo -e "Syncing with LDAP..."
        $PYTHON sbin/ambari-server.py $@
        ;;
  set-current)
        echo -e "Setting current version..."
        $PYTHON sbin/ambari-server.py $@
        ;;
  setup-security)
        echo -e "Security setup options..."
        $PYTHON sbin/ambari-server.py $@
        ;;
  refresh-stack-hash)
        echo -e "Refreshing stack hashes..."
        $PYTHON sbin/ambari-server.py $@
        ;;
  backup)
        echo -e "Backing up Ambari File System state... *this will not backup the server database*"
        $PYTHON sbin/ambari-server.py $@
        ;;
  restore)
        echo -e "Restoring Ambari File System state"
        $PYTHON sbin/ambari-server.py $@
        ;;
  *)
        echo "Usage: sbin/ambari-server
        {start|stop|restart|setup|setup-jce|upgrade|status|upgradestack|setup-ldap|sync-ldap|set-current|setup-security|refresh-stack-hash|backup|restore} [options]
        Use sbin/ambari-server <action> --help to get details on options available.
        Or, simply invoke ambari-server.py --help to print the options."
        exit 1
esac

exit $?
