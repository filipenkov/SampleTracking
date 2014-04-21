#!/bin/bash

# Warning Hard coded user info in a script
# BAD, but using less priviledges than the real user to reduce the damage that can be done
SQSH_USER="jira_user"
SQSH_PASSWORD='jira_user99'

###############################################################################
#produces a tuple file containing all of the samples that have been published
###############################################################################
#Parameters:
# NONE
#Environment Variables:
# NONE
#Files (In):
# NONE
#Files (Out):
# NONE
#Files (Temp/diagnostic):
# NONE
#External Functions:
# call_sqsh, get_all_databases
#get the directory that this script is in, needed for all referenced files
DIR="$( cd "$( dirname "$0" )" && pwd )"
. $DIR/lib/functions.sh

#Used by get_all_databases
export SQSH_USER SQSH_PASSWORD
#set the variable from the genomes table in common
dbs=`get_all_databases`

#Used by call_sqsh
export SQSH_USER SQSH_PASSWORD
#Used in the sql
export DIR dbs collections
cat $DIR/sql/find-deprecated.sql $DIR/sql/find-all-published.sql $DIR/sql/list-all-samples.sql |\
call_sqsh "$SQSH_PASSWORD" "$SQSH_USER" |\
${SED_CMD} "${SED_DEFAULT}"|\
${TR_CMD} -d '"'|\
tail -n+2
