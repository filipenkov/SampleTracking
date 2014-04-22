#!/bin/bash

export ENV=prod

# Warning Hard coded user info in a script
# BAD, but I don't know how else it can be done for now
SQSH_USER="jira_user"
SQSH_PASSWORD='jira_user99'
JIRA_USER="sampletracking"
JIRA_PASSWORD='a2c4e6g8'

host=$(hostname)
export LOGDIR="/usr/local/scratch/VIRAL/ST"
if [ ! -d "$LOGDIR" ]; then 
    mkdir ${LOGDIR} 2>/dev/null
    chmod 0777 ${LOGDIR} 2>/devl/null
fi



JIRA_CSV_INPUT_FILE="${LOGDIR}/ST-jira-taxon_id_sync-${host}-$$.csv"
JIRA_OUTPUT_FILE="${LOGDIR}/ST-jira-taxon_id_sync-${host}-$$.out"
ERROR_FILE="${LOGDIR}/ST-jira-taxon_id_sync-${host}-$$.err"

#processTaxonIds.pl carries out the logic of the operation. The rest of this script just 
#passes the results into jira_cli
###############################################################################
#Checks the NCBI Taxonomy database for the samples in the 
#'Request Taxon ID' and 'Waiting for Taxon ID' states.
#Performs appropriate checks, sets the GLK values and moves the samples.
###############################################################################
#This script is intended to be run from a cron task. 

#Environment Variables:
# JIRA_USER, JIRA_PASSWORD, JIRA_CSV_INPUT_FILE, 
# ERROR_FILE
#Files (In):
# NONE
#Files (Out):
# NONE
#Files (Temp/diagnostic):
# JIRA_CSV_INPUT_FILE -- used to pass the output of the taxon_id script to Jira
# ERROR_FILE          -- used to store any error output from jira 
#External Functions:
# jira_cli
#get the directory that this script is in, needed for all referenced files
DIR="$( cd "$( dirname "$0" )" && pwd )/.."
. $DIR/lib/functions.sh

#dbs=`get_all_databases | ${TR_CMD} ' ' ','`
#only for gsc flu so using a hand crafted list
dbs="giv,giv2,giv3,piv,swiv"

TAXON_ID_CHECK="$DIR/cron/lib/processTaxonIds.pl"

${TAXON_ID_CHECK} -v -P "$SQSH_PASSWORD" -U "$SQSH_USER" -D "$dbs" -o $JIRA_CSV_INPUT_FILE

export JIRA_USER JIRA_PASSWORD JIRA_CSV_INPUT_FILE JIRA_OUTPUT_FILE ERROR_FILE
jira_cli '--action progressIssue'
