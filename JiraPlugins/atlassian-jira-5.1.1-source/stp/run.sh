#!/bin/bash

OPTS="$*"

if [ -z "$RESULTDIR" ]; then
    RESULTDIR="target/selenium-test-reports"
fi
CAPTURE="${RESULTDIR}/capture_raw.mkv"
SUBTITLES="${RESULTDIR}/capture.srt"
CHAPTERS="${RESULTDIR}/capture.chap"
OUTPUT="${RESULTDIR}/capture.mkv"

set -o errexit # exit if any statement returns a non-true return value

killvnc() {
    if ! test -z $vncdisplay; then
        echo stopping vncserver on $DISPLAY
        vncserver -kill $vncdisplay >/dev/null 2>&1
    fi
}

createMovie() {
    if test -e "$CAPTURE" && test -e "$SUBTITLES" && test -e "$CHAPTERS"; then
        mkvmerge --default-language eng -o "$OUTPUT" --language 1:eng --default-track 1:true "$CAPTURE" \
        --title "Selenium" --sub-charset 0:utf8 --language 0:eng --default-track 0:true "$SUBTITLES" \
        --chapter-language eng --chapter-charset utf8 --chapters "$CHAPTERS"

        if test $? -eq 0; then
            rm -f "$CAPTURE"
        fi
    fi
}

displayEnv() {
	echo "---------------------------------------------"
	echo "Displaying Environment Variables"
	echo "---------------------------------------------"
	env
	echo "---------------------------------------------"
}

setVncPassword() {
    vncpasswd=$(which vncpasswd)
    if [ -z vncpasswd ]; then 
	echo "Can't find vncpasswd, unable to continue..."
	echo
	exit 2
    fi
    
    echo Setting VNC password...
    vncpasswd 2>&1 <<EOF
123456
123456
EOF
    if [ ! -e ~/.vnc/passwd ]; then
	echo "Failed to create vncpasswd file, can't continue.  VNCpasswd returns:"
	echo $vncpasswdoutput
	echo
	exit 2
    fi
}

echo Checking to see if vncserver is in our path...
vncbinary=$(which vncserver)
if [ -z "$vncbinary" ]; then
    echo No VNC binary found
    echo
    exit 2
else
    echo VNC binary found at $vncbinary
fi

if [ ! -e ~/.vnc/passwd ]; then
    echo "No VNC password file found, so I'll attempt to create one..."
    setVncPassword
fi


echo Starting vncserver...
vncdisplay=$(vncserver 2>&1 | perl -ne '/^New .* desktop is (.*)$/ && print"$1\n"')

if [ -z "$vncdisplay" ]; then
     echo "Failed to create a vncserver or get its display identifier."
     echo
     exit 2
else 
    echo "Will use VNC server with display identifier $vncdisplay ..."
fi


export DISPLAY=$vncdisplay
echo vncserver started on $DISPLAY

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

# we need to install a few more things before we can use this
#createMovie

exit $MVN_STATUS
