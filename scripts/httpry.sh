#!/bin/bash
# Monitor HTTP requests to the app using httpry. This needs to be run by root.
# You can look at any HTTP header field. See:
# https://github.com/jbittel/httpry/blob/master/doc/format-string

fields=$1
if [ "$fields" == "" ] ; then
    fields=timestamp,x-forwarded-for,direction,method,request-uri,status-code,reason-phrase,cookie
fi

httpry -i lo -f $fields 'tcp port 8092'
