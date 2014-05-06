#!/bin/bash


#ENV=dev
export ENV=prod

#get the directory that this script is in, needed for all referenced files
DIR="$( cd "$( dirname "$0" )" && pwd )"
#load the libs
for library in $DIR/lib/*.sh; do
  source $library
done

#############################################################################
# Set the env variables for 'carry_out_jira_action'
#############################################################################
# Produces:
#  $HELP_HEADER
#  $column_name
#  $jira_cmd_line
#  $jira_error_handler
#############################################################################
##todo: call before loading parameters

    #--------------------------------------------------------------------------
export HELP_HEADER="Moves a group of JIRA 'Samples' between steps"
export column_name='step'
export data_collection_cmd='get_available_steps'
export jira_cmd_line='--action progressIssue' 
export jira_error_handler='check_workflow_steps'
    #--------------------------------------------------------------------------

#------------------------------------------------------------------------------
# Body
#------------------------------------------------------------------------------

#------------------------------------------------------------------------------
# How was the script called?
#------------------------------------------------------------------------------
script_action="move"
script_target="sample"


#..............................................................................
# Call set_from_parameters to parse the command line
#..............................................................................
#Produces: $comment
#          $database
#          $file
#          $lot
#          $test
#          $action
#          $project
#Note:     The actual parameters needed vary according to which action(s) are
#          used.
#..............................................................................
set_from_parameters "$@"

export again="Y"
while  [ "$again"  == "Y" ]; do
carry_out_jira_action
export again=""
export step=""
safe_read again "Move them again (Y/N) ?"
again="$(tr [a-z] [A-Z] <<< "$again")" 
done


