#!/bin/bash

OPTS="$*"

set -o errexit # exit if any statement returns a non-true return value

killvnc() {
    echo stopping Xvfb on $DISPLAY
    killall Xvfb >/dev/null 2>&1
}

displayEnv() {

	echo "---------------------------------------------"
	echo "Displaying Environment Variables"
	echo "---------------------------------------------"
	env
	echo "---------------------------------------------"
}

echo starting Xvfb
export DISPLAY=:23

Xvfb $DISPLAY -screen 0 1600x1200x24 &
echo Xvfb started on $DISPLAY

displayEnv

# Move the mouse pointer out of the way
# echo Moving mouse pointer to 10 10.
# xwarppointer abspos 10 10

#Make sure the VNC server is killed always. Why wont you just die!
trap killvnc INT TERM EXIT

if [ -f "${M2_HOME}/bin/mvn" ]; then
  MVN=${M2_HOME}/bin/mvn
else
  MVN=mvn
fi

echo Starting $MVN $OPTS
$MVN $OPTS
MVN_STATUS=$?

exit $MVN_STATUS
