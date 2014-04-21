#!/bin/bash

# Warning to run this from a cron SQSH_USER and SQSH_PASSWORD must be set
SQSH_USER="access"
SQSH_PASSWORD='access'
SYBASE_DB="SYBIL"

###############################################################################
#Find host values that do not have a known mapping to a normalized host
###############################################################################
#Parameters:
# -v	verbose output, otherwise output is only produced if an error occurs
#Environment Variables:
# SQSH_USER, SQSH_PASSWORD
#Files (In):
# NONE
#Files (Out):
# NONE
#Files (Temp/diagnostic):
# NONE
#External Functions:
# call_sqsh
#get the directory that this script is in, needed for all referenced files
DIR="$( cd "$( dirname "$0" )" && pwd )/.."
. $DIR/lib/functions.sh

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

if [[ -n "$VERBOSE" ]]; then
	echo "Checking for unmapped hosts..."
fi

output=`call_sqsh < $DIR/cron/sql/unknown-hosts.sql |\
  sed -r -e 's/^\s*//' -e 's/\s*$//' | tee /tmp/tmp |\
  grep -v " affected" | grep -v '^$'`
#cut off the header and see if there is anything remaining
short_output=`echo "$output" | grep -v "[-]--" | grep -v "host"`
if [[ -z "$short_output" ]]; then
	echo "$output"
fi
echo "$output"
