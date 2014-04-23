#!/bin/bash

if [ -z "$1" ]; then
  echo "Useage: $0 <collection_name> [<collection_name>]*"
  exit 0
fi

# Warning Hard coded user info in a script
# BAD, but using less priviledges than the real user to reduce the damage that can be done
SQSH_USER="jira_user"
SQSH_PASSWORD='jira_user99'
export ENV=prod

###############################################################################
#lookup the status of samples in a collection and summarize
###############################################################################
#Parameters:
# A space seperated list of collections
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

#half hearted attempt at avoiding SQL injection
cleaned_params="$(echo "$*" | sed -r "s/[{}()\"']//g")"
#wrap in quotes
collections="'$(echo "$cleaned_params" | sed -r "s/ /', '/g")'"


echo " Collection Status                       Count"
#Used by the shell for call_sqsh
export SQSH_USER SQSH_PASSWORD
#Used in the sql
export DIR dbs collections
call_sqsh "$SQSH_PASSWORD" "$SQSH_USER" \
< $DIR/sql/collection_status.sql |\
grep -v '^ *[(-]' | grep -v "^ *$" |\
awk '
  {
    printf $0 "\n"
    number = gensub(/^.* ([0-9]+) *$/,"\\1","x", $0)
    total += number
  } 
  END { 
    print "Total Samples=" total 
  }
'
