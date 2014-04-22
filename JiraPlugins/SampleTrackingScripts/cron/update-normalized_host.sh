#!/bin/bash

# Warning Hard coded user info in a script
# BAD, but I don't know how else it can be done for now
SQSH_USER="jira_user"
SQSH_PASSWORD='jira_user99'
JIRA_USER="sampletracking"
JIRA_PASSWORD='a2c4e6g8'

export ENV=prod

host=$(hostname)
export LOGDIR="/usr/local/scratch/VIRAL/ST"
if [ ! -d "$LOGDIR" ]; then 
    mkdir ${LOGDIR} 2>/dev/null
    chmod 0777 ${LOGDIR} 2>/devl/null
fi


JIRA_CSV_INPUT_FILE="${LOGDIR}/ST-jira-normalized_host-${host}-$$.csv"
#JIRA_CSV_INPUT_FILE="/tmp/ST-jira-cron.csv"

ERROR_FILE="${LOGDIR}/ST-jira-normalized_host-${host}-$$.err"

###############################################################################
#update the normalized_host attribute using values from the host attribute
###############################################################################
#This script is intended to be run from a cron task. 
# - It requires no user interaction
#Parameters:
# NONE
#Environment Variables:
# SQSH_USER, SQSH_PASSWORD, JIRA_USER, JIRA_PASSWORD, JIRA_CSV_INPUT_FILE, 
# ERROR_FILE
#Files (In):
# NONE
#Files (Out):
# NONE
#Files (Temp/diagnostic):
# ERROR_FILE          -- used to store any error output from sqsh or jira 
#                        during the process
#External Functions:
# call_sqsh
#get the directory that this script is in, needed for all referenced files
DIR="$( cd "$( dirname "$0" )" && pwd )/.."
. $DIR/lib/functions.sh

###############################################################################
#Call sqsh
# The majority of the logic is in the SQL not here
###############################################################################

#Used by get_all_databases
export SQSH_USER SQSH_PASSWORD

if [[ -n "$VERBOSE" ]]; then
	echo "Calling SQSH to update the attributes"
fi

#Used by the shell for call_sqsh
export SQSH_USER SQSH_PASSWORD
#Used in the sql
call_sqsh "$SQSH_PASSWORD" "$SQSH_USER" < $DIR/cron/sql/update-normalized_host.sql > /dev/null
#The output from the update sql isn't important
#list-unknown-hosts is run to get human readable messages if needed
unknown_hosts=`$DIR/cron/list-unknown_hosts.sh`
if [[ -n "$unknown_hosts" ]]; then
	echo "WARNING Unknown host(s) found. "
	echo "Samples with this host cannot be assigned a value for normalized_host"
	echo 
	echo "host,number of occurances"
	echo $unknown_hosts
fi


