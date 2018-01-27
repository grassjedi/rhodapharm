#!/usr/bin/env bash
# Don't invoke this directly. Use start.sh to start the application or stop.sh to stop the application.
# This is called from _run.sh when the jvm exits with am OutOfMemoryException

# Send a mail to notify people that restarting the app due to an OutOfMemoryException could not be done

APP_NAME=rhodapharm
APP_UID=1002
APP_PORT=8092

sendMailStopFailed() {
      host=`hostname`
      date | mail -s "Could not restart carrot after running out of memory on $host" root@$host
}

# Send a mail to notify people that the app was restarted due to an OutOfMemoryException
sendMailRestarted() {
      host=`hostname`
      date | mail -s "Restarted carrot after running out of memory on $host" root@$host
}

# try and force kill the app and then restart the app or notify people of the problem
forceKill() {
    kill -9 $PID

    # sleep for 30 seconds
    # Was originally 5 seconds, but would often not have given a process enough time
    # to forceably quit.
    sleep 30

    # is the process running?
    ps cax | grep $PID > /dev/null
    if [ $? -eq 0 ]; then
      echo "carrot is still running after a force kill, not going to try and recover - sending mail"
      sendMailStopFailed
    else
      echo "carrot has been force stopped, Restarting carrot..."
      ./start.sh
      sendMailRestarted
    fi
}

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
    echo "timeout waiting for ${APP_NAME} port ${APP_PORT} to stop listening in ${port_wait_time}s"
  else
    echo "${APP_NAME} stopped listening on port ${APP_PORT} in ${port_wait_time}s"
  fi
}

#populate the PID
PIDFILE=carrot.pid
if [ ! -f $PIDFILE ] ; then
    echo "$PIDFILE not found"
    exit 1
fi

PID=`cat $PIDFILE`

# Try and stop the process gracefully
./stop.sh

# sleep for 10 seconds
sleep 10

waitListeningPortGone

# is the process running?
ps cax | grep $PID > /dev/null
if [ $? -eq 0 ]; then
  echo "carrot is still running after a graceful stop going to try and kill -9 " + $PID
  forceKill
else
  echo "carrot has stopped gracefully. Restart carrot..."
  ./start.sh
  sendMailRestarted
fi