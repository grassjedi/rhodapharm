#!/bin/bash

PIDFILE=rhodapharm.pid
if [ ! -f $PIDFILE ] ; then
    echo "$PIDFILE not found"
    exit 1
fi

PID=`cat $PIDFILE`
echo "Killing ${PID}"
pkill -P $PID
status=$?
if [ "$status" == "1" ]; then
    echo "No process found, is it running?"
fi
