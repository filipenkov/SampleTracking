#!/bin/bash
DIR="$( cd "$( dirname "$0" )" && pwd )/.."

export ENV=prod

# Warning Hard coded user info in a script
# BAD, but I don't know how else it can be done for now
#SQSH_USER="jira_user"
#SQSH_PASSWORD='jira_user99'
JIRA_USER="sampletracking"
JIRA_PASSWORD='a2c4e6g8'


host=$(hostname)
export LOGDIR="/usr/local/scratch/VIRAL/ST"
if [ ! -d "$LOGDIR" ]; then 
    mkdir ${LOGDIR} 2>/dev/null
    chmod 0777 ${LOGDIR} 2>/devl/null
fi




JIRA_CSV_INPUT_FILE="${LOGDIR}/cron/sql/recently-created-samples-${host}-$$.jql"
JIRA_OUTPUT_FILE="${LOGDIR}/ST-jira-new-samples-${host}-$$.out"
ERROR_FILE="${LOGDIR}/ST-jira-new-samples-${host}-$$.err"
###############################################################################
#Compare a list of recently created samples with the samples in the GLK to
#find any that have been created in JIRA but not in the GLK
###############################################################################
#This script is intended to be run periodically, possibly as part of a cron
# - It is not efficient
# - It requires no user interaction
#Parameters:
# -v	verbose output, otherwise output is only produced if missing samples
#       are found
#Environment Variables:
# SQSH_USER, SQSH_PASSWORD, JIRA_USER, JIRA_PASSWORD, JIRA_CSV_INPUT_FILE, 
# ERROR_FILE
#Files (In):
# NONE
#Files (Out):
# NONE
#Files (Temp/diagnostic):
# JIRA_CSV_INPUT_FILE -- used to pass the query to Jira
# JIRA_OUTPUT_FILE    -- used to hold the list of samples
# ERROR_FILE          -- used to store any error output from sqsh or jira 
#                        during the process
#External Functions:
# call_sqsh, get_all_databases, jira_cli
#get the directory that this script is in, needed for all referenced files

. $DIR/lib/functions.sh

###############################################################################
# subtask_find_error
# A function for error handle handling when using JIRA CLI to move issues
###############################################################################
# Environment:
# ERROR_FILE
###############################################################################
function subtask_find_error() {
  if [ ! -z "`grep -E 'The value .* does not exist for the field .Type.' $ERROR_FILE`" ]
  then
    cat $ERROR_FILE | \
    sed -n 's/^.*The value \(.*\) does not exist for the field .Type.*$/\1 is not a valid Sub-task type/p'
    #the error handler is called via $() and so this exit only exits the 
    #sub-shell
    exit
  fi
  check_username
}

jira_cli "--action getIssueList" subtask_find_error
echo "The script is incomplete. It queries JIRA to get a list of JIRA IDs created in the last two weeks."
echo "The remaining part is to create SQL from the result to check that all of the JIRA IDs are in the"
echo "GLK. Complications arise as they could be in any database."
