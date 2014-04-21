#!/bin/bash

# script to run a vmware image and test the JIRA windows installer in it
#
# USAGE:

# vmTestInstaller -i <Installer.exe> -v </full/path/to/vmfile.vmx>   -m <maven arguments>



# process args

function usage() {
    echo
    echo "You must supply these flags with values: virtual machine (-v) and maven arguments (-m)"
    echo $0 "-v </full/path/to/vmfile.vmx> -m <maven arguments>"
    echo
}

function check_options() {
	if [[ -z "$vm" || -z "$maven_args" ]]; then
		usage
		exit 2
	fi
}

# constants

GUEST_USERNAME="atlassian"
GUEST_PASSWORD="atlassian"
SNAPSHOT_NAME="selenium-test-baseline"
TEST_DIR="jira-selenium-test"
INSTALLER_CONFIG="JIRA_windows.varfile"
TEST_SCRIPT="runSeleniumTests.sh"
TEST_RESULT_TXT="TEST-com.atlassian.jira.webtests.AcceptanceTestHarness.txt"
TEST_RESULT_TXT="TEST-com.atlassian.jira.webtests.AcceptanceTestHarness.xml"
BASE="$(pwd)/subprojects/selenium-tests"
#/subprojects/selenium-tests"


# global variables
vncdisplay="" # dynamically determined


# prepares the vm by copying selenium server to it
function prep_vm() {

    echo "prepping vm"
    set -e # turn on error hypersensitivity

    echo "creating $TEST_DIR directory"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "createDirectoryInGuest"  "$vm" "c:\\$TEST_DIR"

    echo "copying test scripts"
    echo "copying test scripts  $BASE/$TEST_SCRIPT"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "copyFileFromHostToGuest"  "$vm" $BASE/$TEST_SCRIPT "c:\\$TEST_DIR\\$TEST_SCRIPT"

    # Get maven to retrieve the selenium-server jars
    maven -d subprojects/selenium-tests/ prepare-selenium-server-jars

    echo "copying selenium server jar"
    for selServerFile in $( ls $BASE/target/seleniumServerJars/*.jar ); do
	echo "Copying $selServerFile to c:\\$TEST_DIR\\$( basename $selServerFile )"
        vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "copyFileFromHostToGuest"  "$vm" $selServerFile "c:\\$TEST_DIR\\$( basename $selServerFile )"
    done

    echo "copied selenium jar to server"
    set +e # turn off error hypersensitivity
}


function start_vm_at_snapshot() {
    set -e
    echo "resetting vm state"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "revertToSnapshot"  "$vm" $SNAPSHOT_NAME
    echo "starting vm"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "start"  "$vm"
    set +e
}

# shutdown
function stop_vm() {
    echo "stopping vm"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "stop"  "$vm" hard
}

function vnc_server_startup() {
	echo starting vncserver
    vncdisplay=$(vncserver 2>&1 | perl -ne '/^New .* desktop is (.*)$/ && print"$1\n"')
    if [[ -z "$vncdisplay" ]]; then
        echo "failed to create a vncserver or get its display identifier"
        exit 2
    fi                                                                                                 
    export DISPLAY=$vncdisplay
    echo vncserver started on $DISPLAY
}

function vnc_server_shutdown() {
    echo stopping vncserver on $DISPLAY
    vncserver -kill $vncdisplay >/dev/null 2>&1
}

function run_guest_script() {

    echo "running guest script to run selenium server in the VMWare instance"
    vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "runProgramInGuest"  "$vm" \
        'C:\windows\system32\cmd.exe' \
        "/c c:\\cygwin\\bin\\bash.exe --login c:\\$TEST_DIR\\runSeleniumTests.sh"

}
function run_tests() {
    echo "Starting the selenium tests"
    $MAVEN_HOME/bin/maven clean jar:install war:webapp jira:ui-func-tests \
    -Dmaven.test.skip=true -Dedition=enterprise \
    -Djira.build.rpcplugin=false \
    $maven_args
}

function get_file() {
	vmrun -gu $GUEST_USERNAME -gp $GUEST_PASSWORD "copyFileFromGuestToHost"  "$vm" "$1" "$2"
}

# get the results of the tests out of the guest
function get_results() {
    echo "not necesary to get results"
####        for i in results.txt wget.response wget.log error.log; do
####            get_file "c:\\$TEST_DIR\\$i" "$BASE/target/$i"
####        done
####        get_file "c:\\$TEST_DIR\\func_tests\\target\\test-reports\\$TEST_RESULT_TXT" "$BASE/target/$TEST_RESULT_TXT"
####        get_file "c:\\$TEST_DIR\\func_tests\\target\\test-reports\\$TEST_RESULT_XML" "$BASE/target/$TEST_RESULT_XML"
####
####    	# now get all log files that may have rolled over
####    	suffix=""
####    	logfile=atlassian-jira.log
####    	x=1
####    	while get_file "c:\\$TEST_DIR\\${logfile}${suffix}" "$BASE/target/${logfile}${suffix}" ; do
####    		suffix=$((x++))
####    	done

}


function run_vmware_selenium_tests() {
    start_time=`date +%s`

	check_options

    echo testing installer in vmware

    vnc_server_startup

    start_vm_at_snapshot

    prep_vm

	run_guest_script

	run_tests

	get_results

	stop_vm

    vnc_server_shutdown

    # analyse the results
#    echo result:
#    if [[ -e $BASE/target/results.txt ]]; then
#		dos2unix $BASE/target/results.txt
#        cat $BASE/target/results.txt
#        grep "maven func test success" $BASE/target/results.txt >/dev/null
#        RETURN_CODE=$?
#    else
#        echo ERROR: cannot find the results.txt file
#        RETURN_CODE=1
#    fi


    now=`date +%s`
    duration=`expr \( $now - $start_time \) / 60`
    echo "completed in installer test in $duration minutes"
    return $RETURN_CODE
}

##########
# MAIN
##########

while getopts "m:v:" OPTION; do
	case $OPTION in
		m) maven_args="$OPTARG" ;;
		v) vm="$OPTARG" ;;
	esac
done

echo "will run selenium tests using:"
echo "maven arguments=$maven_args"
echo "vm=$vm"

run_vmware_selenium_tests
exit $?
