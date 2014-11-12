#!/bin/bash
SCRIPT_PATH="${BASH_SOURCE[0]}";
if([ -h "${SCRIPT_PATH}" ]) then
  while([ -h "${SCRIPT_PATH}" ]) do SCRIPT_PATH=`readlink "${SCRIPT_PATH}"`; done
fi
pushd . > /dev/null
cd "`dirname "${SCRIPT_PATH}"`" > /dev/null
SCRIPT_PATH=`pwd`;
popd  > /dev/null

script_dir=`cygpath -w "${SCRIPT_PATH}" || echo "${SCRIPT_PATH}"`

exec java -XX:+UseSerialGC -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:+AggressiveOpts -XX:MaxPermSize=512m -XX:ReservedCodeCacheSize=128m -Xmx1000M -Xss2M -da -XX:-DontCompileHugeMethods -server -Dfile.encoding=utf-8 -Dinput.encoding=cp1252 $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
