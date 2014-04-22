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
function configure_action {
  case $1 in
    comment)
    #--------------------------------------------------------------------------
    export HELP_HEADER="Adds a comment to a group of JIRA 'Samples'"
    export column_name=''
    export data_collection_cmd='check_comment'   #defined in shared-comment.sh
    export jira_cmd_line='--action addComment' 
    export jira_error_handler='check_username' #defined in functions.sh
    #--------------------------------------------------------------------------
    ;;
    move)
    #--------------------------------------------------------------------------
    export HELP_HEADER="Moves a group of JIRA 'Samples' between steps"
    export column_name='step'
    export data_collection_cmd='get_available_steps'
    export jira_cmd_line='--action progressIssue' 
    export jira_error_handler='check_workflow_steps'
    #--------------------------------------------------------------------------
    ;;
    assign)
    #--------------------------------------------------------------------------
    export HELP_HEADER="Assigns a group of JIRA 'Samples' to a single user"
    export column_name='assignee'
    #==========================================================================
    # get_assignee
    #==========================================================================
    # Queries for assignee if it hasn't been set
    # Transfers the result to $value to be inserted into the csv file
    #==========================================================================
    function get_assignee() {
      safe_read assignto \
        "Please enter the user to assign the samples/subtasks to"
      value="$assignto"
    }

    export data_collection_cmd='get_assignee'
    export jira_cmd_line='--action updateIssue' 
    #==========================================================================
    # check_assignee
    #==========================================================================
    # A function to pass parse the error file looking for messages about 
    # the assignee
    #==========================================================================
    # Environment:
    # ERROR_FILE
    #==========================================================================
    function check_assignee() {
      if [[ "`grep -E 'User .* cannot be assigned issues' $ERROR_FILE`" ]]; then
        cat $ERROR_FILE | \
        sed -n "s/^.*User '\([^']*\)' cannot be assigned issues.*$/Could not assign to \1 (invalid user?)/p"
        exit
      fi
      check_username
    }
    export jira_error_handler='check_assignee'  
    #--------------------------------------------------------------------------
    ;;
    *)
    echo "Unknown action $1"
    exit 600
  esac
}

#------------------------------------------------------------------------------
# Body
#------------------------------------------------------------------------------

#------------------------------------------------------------------------------
# How was the script called?
#------------------------------------------------------------------------------
full_script_name=`basename "$0"`
# '##' removes the longest string matching '*-' from the start of the value
# essential returns everything after the last '-'
temp="${full_script_name##*-}"
# '%%' removes the longest string matching '.*' from the end of the value
script_action="${temp%%.*}"
script_target="${full_script_name%%-*}"

#------------------------------------------------------------------------------
# Handle subtask versions
#------------------------------------------------------------------------------
if [[ "$script_target" == "subtask" ]]; then
  export set_subtask='true'
fi

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

if [[ "$script_action" == "cmd" ]]; then
  #----------------------------------------------------------------------------
  # loop through the actions listed in the '--action' parameter
  #----------------------------------------------------------------------------
  for action in $actions; do 
    configure_action "$action"
    carry_out_jira_action
    #clear the commandline comment
    comment=
  done
else
  configure_action "$script_action"
  #----------------------------------------------------------------------------
  # One command
  #----------------------------------------------------------------------------
  carry_out_jira_action
fi

