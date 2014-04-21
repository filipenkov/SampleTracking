#!/bin/bash

RESULTDIR="subprojects/selenium-tests/target/test-reports"
CAPTURE="${RESULTDIR}/capture_raw.mkv"
SUBTITLES="${RESULTDIR}/capture.srt"
CHAPTERS="${RESULTDIR}/capture.chap"
OUTPUT="${RESULTDIR}/capture.mkv"

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

echo starting vncserver
vncdisplay=$(vncserver $2 2>&1 | perl -ne '/^New .* desktop is (.*)$/ && print"$1\n"')
if test -z "$vncdisplay"; then
     echo "failed to create a vncserver or get its display identifier"
     exit 2
fi
export DISPLAY=$vncdisplay
echo vncserver started on $DISPLAY

# Move the mouse pointer out of the way
echo Moving mouse pointer to 10 10.
xwarppointer abspos 10 10

#Make sure the VNC server is killed always. Why wont you just die!
trap killvnc INT TERM EXIT

# Command failure will be fatal. No need to run the selenium tests if any of these work.
set -e
cd subprojects/func_tests/
$MAVEN_HOME/bin/maven clean jar:install -Dmaven.test.skip
cd ../../
cd subprojects/selenium-tests/
$MAVEN_HOME/bin/maven clean -Dmaven.test.skip
cd ../../

# Command failure no longer fatal. We want to continue if selenium fails.
set +e
$MAVEN_HOME/bin/maven clean jira:disablesecurity jar:install war:webapp jira:ui-func-tests \
-Dmaven.test.skip=true -Dedition=enterprise \
-Djira.build.rpcplugin=false \
$1

result=$?
createMovie
exit $result
