#!/bin/bash

export ENV=prod

# Uses a readonly user
SQSH_USER="jira_user"
SQSH_PASSWORD='jira_user99'
###############################################################################
#Runs the Flu Samples by Country,State and Type SQL and then parses it
#into map files
###############################################################################
#This script is intended to be run from a cron task. 
# - It is not efficient
# - It requires no user interaction
#Parameters:
# a list of databases to query
#Environment Variables:
# NONE
#Files (In):
# NONE
#Files (Out):
# NONE
#Files (Temp/diagnostic):
# /tmp/ST-create_map_KML.csv
#External Functions:
# NONE
#get the directory that this script is in, needed for all referenced files
#.. appended to get out of the cron directory
DIR="$( cd "$( dirname "$0" )" && pwd )/.."
. $DIR/lib/functions.sh

if [ "$#" == "0" ]; then
  echo "Useage $0 <db>+"
  echo "A space seperated list of databases to include in the process is required"
  exit 1
fi

#clear the variable, just to be paranoid
dbs=
#loop until there are no more cmd line parameters
while (( "$#" )); do
  dbs="$dbs $1"
  shift
done
#convert the csv file into SQL

#pass in the require 'parameters' for the sql
export DIR
export dbs
export unknown_district_file="/tmp/ST-create_map_unknown_states.csv"
#build up the sql
cat $DIR/cron/etc/district-to-state.csv | \
sed 's/^"\([^"]*\)","\([^"]*\)"$/INSERT INTO #state_map(district, state) values ("\1","\2")/' |\
cat $DIR/cron/sql/district-to-state-table.sql - $DIR/cron/sql/flu-samples-by-country-state-type.sql |\
/usr/local/bin/sqsh -w 255 \
   -U "$SQSH_USER" \
   -P "$SQSH_PASSWORD" \
   -S SYBPROD
