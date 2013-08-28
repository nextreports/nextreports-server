#!/bin/sh

################################
# run with ". ./ant-proxy.sh"
################################

HTTP_PROXY="-Dhttp.proxyHost=192.168.16.1 -Dhttp.proxyPort=128"
HTTPS_PROXY="-Dhttps.proxyHost=192.168.16.1 -Dhttps.proxyPort=128"

export ANT_OPTS="$HTTP_PROXY $HTTPS_PROXY"