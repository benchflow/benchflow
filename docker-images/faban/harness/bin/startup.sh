#!/bin/sh
# -----------------------------------------------------------------------------
# Start Script for the CATALINA Server
#
# Adapted for Docker, from:
# $Id: startup.sh,v 1.8 2007/11/13 07:17:02 akara Exp $
# -----------------------------------------------------------------------------

export JAVA_HOME

# resolve links - $0 may be a softlink
PRGDIR=`dirname $0`

if [ -n "$PRGDIR" ]
then
   PRGDIR=`cd $PRGDIR > /dev/null 2>&1 && pwd`
fi

JAVA_OPTS="-Xms64m -Xmx1024m -Djava.awt.headless=true"

export JAVA_OPTS

EXECUTABLE=catalina.sh

# Check that target executable exists
if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
  echo "Cannot find $PRGDIR/$EXECUTABLE"
  echo "This file is needed to run this program"
  exit 1
fi

# Added by Ramesh and Akara

HOST=`hostname`

echo "Starting Faban Server"

exec "$PRGDIR"/"$EXECUTABLE" run "$@"
