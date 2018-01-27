#!/bin/bash

if [ -f rhodapharm.pid ] ; then
    oldpid=`cat rhodapharm.pid`
    if pgrep -P $oldpid -u rhodapharm java ; then
        echo "Looks like rhodapharm is already running with PID ${oldpid}"
        exit 1
    fi
fi

set -e

WAR=`ls -t *.jar | head -n 1`

if [ "$WAR" == "" ] ; then
    echo "No war file found, run ./build.sh"
    exit 1
fi

filename=$(basename "$WAR")
extension="${filename##*.}"

[ -d deployed ] || mkdir deployed
current_md5=$(md5sum $WAR | cut -d ' ' -f 1) # md5sum but, only the hash field
# has the current jar been backed up before?, only get the first match
current_deployed=$(ls -t deployed/*.${extension} | head -5 | xargs md5sum | grep ${current_md5} | cut -d ' ' -f 3 | head -1)

dateStr=$(date +'%Y%m%d-%H%M')

if [ "${current_deployed}" == "" ]; then
  war="deployed/rhodapharm-${dateStr}.${extension}"
  cp -v $WAR $war
  ls -t deployed/*.${extension}| tail -n+6 |xargs rm -f # keep the last 5 deployed jars/wars
  echo "${dateStr}: Starting with new .${extension} ${war}" >> deployed/deployments.log
  echo "Starting with new .${extension} ${war}"
else
   war=${current_deployed}
   echo "${dateStr}: Starting with existing lib ${current_deployed} ${current_md5}" >> deployed/deployments.log
   echo "Starting with existing lib ${current_deployed} ${current_md5}"
   # ensure deployed/deployments.log stays reasonably sized
   orig_ifs=${IFS}; IFS=
   lines_to_retain=$(tail -100 deployed/deployments.log)
   echo $lines_to_retain > deployed/deployments.log
   IFS=$orig_ifs
fi

nohup /bin/bash ./_run.sh $war >> rhodapharm.log 2>&1 &
PID=$!

echo $PID > rhodapharm.pid

echo "rhodapharm PID ${PID}, tail -f rhodapharm.log to check for successful startup"
