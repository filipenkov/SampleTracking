#!/bin/bash

export ENV=prod

# Warning Hard coded user info in a script
# BAD, but I don't know how else it can be done for now
SQSH_USER="jira_user"
SQSH_PASSWORD='jira_user99'
JIRA_USER="sampletracking"
JIRA_PASSWORD='a2c4e6g8'

JIRA_CSV_INPUT_FILE="/tmp/ST-jira-field_sync.csv"
ERROR_FILE="/tmp/ST-jira-field_sync.err"

#GLK attribute
#field="batch_id"
#field="species_code"
#JIRA attribute
#11330 = species_code
#10730 = batch_id
#JIRA_FIELD=customfield_11330

#commandline overrides the environment
if [ ! -z "$1" ] && [ ! -z "$2" ]; then
  field="$1"
  JIRA_FIELD="$2"
fi


if [ -z "$field" ] || [ -z "$JIRA_FIELD" ]; then
  echo "$0 <GLK attribute name> customfield_<JIRA Custom Field Number>"
  exit 1
fi
###############################################################################
#Update values associated with a sample in JIRA using data from the GLK
###############################################################################
#This script is intended to be run from a cron task. 
# - It is not efficient
# - It requires no user interaction
#Parameters:
# -v	verbose output, otherwise output is only produced if an error occurs
#Environment Variables:
# SQSH_USER, SQSH_PASSWORD, JIRA_USER, JIRA_PASSWORD, JIRA_CSV_INPUT_FILE, 
# ERROR_FILE
#Files (In):
# NONE
#Files (Out):
# NONE
#Files (Temp/diagnostic):
# JIRA_CSV_INPUT_FILE -- used to pass the output of the sqsh script to Jira
# ERROR_FILE          -- used to store any error output from sqsh or jira 
#                        during the process
#External Functions:
# call_sqsh, get_all_databases, jira_cli
#get the directory that this script is in, needed for all referenced files
DIR="$( cd "$( dirname "$0" )" && pwd )/.."
. $DIR/lib/functions.sh

###############################################################################
#add_header
#add a row before streaming the rest of the output
###############################################################################
# Parameters: 
# $1           The value to use for the first line
###############################################################################
function add_header() {
  echo $1
  cat
}


###############################################################################
#check_options
# similar to set_from_parameters but a lot simpler
###############################################################################
# Parameters: 
# None
# Environment Variables:
# VERBOSE   -- If there is a -v on the commandline this is set
###############################################################################
unset VERBOSE
while getopts ":v" opt; do
    case "$opt" in
      v) export VERBOSE=true;;
      [?])
        echo "Unknown option" >&2 
#        help_message
        exit 2
        ;;
    esac
done

#Used by get_all_databases
export SQSH_USER SQSH_PASSWORD
#set the variable from the genomes table in common
dbs=`get_all_databases`

if [[ -n "$VERBOSE" ]]; then
	echo "Finding batch information..."
fi

#Used by the shell for call_sqsh
export SQSH_USER SQSH_PASSWORD
#Used in the sql
export DIR dbs field

call_sqsh "$SQSH_PASSWORD" "$SQSH_USER" < $DIR/cron/sql/sync-field.sql \
  sed -r 's/^ *\ ([^ ]*\ ) *\ ([^ ]*\ ) *$/\1,\2/' |\
  grep '^.*[^ ].*,' |\
  add_header "Issue,${JIRA_FIELD}" > $JIRA_CSV_INPUT_FILE

JIRA_OUTPUT_FILE="live_output"
function live_output() {
  grep -v "not changed" /tmp/batch-out | grep -v "Run:" | grep -v "^$"
}
if [[ -n "$VERBOSE" ]]; then
	echo "Updating JIRA. This may take some time."
fi

export JIRA_USER JIRA_PASSWORD JIRA_CSV_INPUT_FILE JIRA_OUTPUT_FILE ERROR_FILE
jira_cli '--action updateIssue'


