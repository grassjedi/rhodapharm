#!/bin/bash
# Don't invoke this directly. Use start.sh to start the application.
# This runs java in a loop and restarts it if it exited with 42.
# This also restarts the app if the jvm runs out of memory see -XX:OnOutOfMemoryError="./_restart.sh"

APP_NAME=rhodapharm
APP_UID=1002
APP_PORT=8181

war=$1

JAVA_OPTS="-Xmx3072m -Xms3072m -XX:MaxPermSize=160m -XX:OnOutOfMemoryError=\"./_restart.sh\" -server -XX:+UseCompressedOops  -Dmaster=true -DserverURL=https://carrot-bilbo.brandseye.com -Dsun.net.inetaddr.ttl=5 -Dsun.net.http.allowRestrictedHeaders=true -Dstringchararrayaccessor.disabled=true -Dspring.profiles.active=production"

WAR_ARGS=""

waitListeningPortGone() {
  port_wait_time=0
  while [ "${port_wait_time}" -lt "60" ]; do
    # first
    port_up=$(netstat --tcp --listening --numeric --extend | grep ${APP_UID} | grep -c ${APP_PORT})
    if [ "${port_up}" == "1" ]; then
      echo "${APP_NAME} port ${APP_PORT} is still listening..."
    else
      echo "${APP_NAME} port ${APP_PORT} has stopped listening."
      break
    fi
    sleep 10
    port_wait_time=$((port_wait_time+10))
  done
  if [ "${port_wait_time}" -ne "0" ]; then
    echo "timeout waiting for ${APP_NAME} port ${APP_PORT} to stop listening"
  else
    echo "${APP_NAME} stopped listening on port ${APP_PORT} in ${port_wait_time}s"
  fi
}

# Ensure that we do not have any leakage of unwanted locale information
# upsetting either the JVM or our apps. This can effect, for
# instance, the encoding of non-ascii plain unicode
# characters (breaking them).
unset LC_ALL

# Detect when we are running on jdk 1.8 so that we can replace the MaxPermSize with MaxMetaspaceSize.
# I decided to do this because radish stopped starting up because it was running out of java heap space.
JAVA_VERSION=`echo "$(java -version 2>&1)" | grep "java version" | awk '{ print substr($3, 2, length($3)-2); }'`
echo "Java version is: ${JAVA_VERSION}"

case $JAVA_VERSION in 1.8.0*)
  echo "We are using Java 8 replacing MaxPermSize with MaxMetaspaceSize"
  JAVA_OPTS="${JAVA_OPTS/MaxPermSize/MaxMetaspaceSize}"
esac

status=42
while [ "$status" = "42" ] ; do
  java $JAVA_OPTS -jar $war $WAR_ARGS
  status=$?
  echo `date` java exited with $status
  if [ "$status" = "42" ] ; then
    sleep 5
    waitListeningPortGone
    host=`hostname`
    echo "restarting"
    date | mail -s "carrot restarting on $host" root@$host
  fi
done
