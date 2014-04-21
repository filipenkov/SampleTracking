. $DIR/lib/functions.sh
#needed for 
# check_username
# jira_cli
# safe_read

LOGDIR="/usr/local/scratch/VIRAL/ST"
mkdir ${LOGDIR} 2>/dev/null
chmod 0777 ${LOGDIR}

host=$(hostname)

##
## If a script wants to set ENV first this will set the server correctly
##

set_env_params

function get_available_steps_list() {
  echo "Annotate"
  echo "Assemble"
  echo "Close Sample"
  echo "Collaborator Review"
  echo "Create"
  echo "Deliver Data"
  echo "Lab Processing"
  echo "Received Sample"
  echo "Request Taxon ID"
  echo "Review Assembly"
  echo "Sample Published"
  echo "Submitted to GenBank"
  echo "Taxon ID Hold"
  echo "Validate"
  echo "Waiting for Taxon ID"

  echo "Deprecated"
  echo "Unresolved"
  echo "Move"


  #----------------------------------------------------------------------------
  #get step from the user
  #----------------------------------------------------------------------------
  #Produces: $step
  #----------------------------------------------------------------------------
  safe_read step    "Please enter step to move samples to"
  export value="$step"
}

function get_available_steps() {
###############################################################################
# get_available_steps
# Find the states that the samples can transition to
# This version is based on querying JIRA to get the list
###############################################################################
#Produces: step             The variable containing the step selected
#
#Env:  $CSV_LOOKUP_FILE     A file containing a header and identifiers needed
#                           to locate the samples.
#      $ERROR_FILE          The file to output any errors to
#		            (/usr/local/scratch/VIRAL/ST/ST-error)
#Temp:
SHORT_JIRA_CSV="${LOGDIR}/ST-jira-short-${host}-$$.csv" #  A trimmed version of 
#                           CSV_LOOKUP_FILE that only contains the 
#                           header and the last sample.
JIRA_OUTPUT_STEPS="${LOGDIR}/ST-jira-steps-${host}-$$.out" # The full output from
#                           calling Jira
###############################################################################
# Note: only the last sample in the file is used
###############################################################################

  #----------------------------------------------------------------------------
  #Create a shortened version of ST-jira.csv to avoid having to lookup
  #lots of samples
  #----------------------------------------------------------------------------
  #Produces: SHORT_JIRA_CSV          (/usr/local/scratch/VIRAL/ST/ST-jira-short.csv)
  #Requires: $CSV_LOOKUP_FILE    (/usr/local/scratch/VIRAL/ST/ST-jira.csv or /usr/local/scratch/VIRAL/ST/ST-jira-subtask.csv)
  #----------------------------------------------------------------------------
    #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    #SHORT_JIRA_CSV
    #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    #Head: Issue
    #Body: <JIRA_ID>
    #Example: ST-1643
    #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  #sed -n only output if a 'p' command is given
  #    -r regex
  #    1p '1' first line - 'p' output (header)
  #    $p '$' last line  - 'p' output
  sed -n -r '1p;$p' $CSV_LOOKUP_FILE > $SHORT_JIRA_CSV

  echo "Searching for Available Steps"

  #............................................................................
  #call jira_cli
  #............................................................................
  # Parameters: 
  # 1  The value for the 'common' parameter
  # 2  The name of the function to use to check for errors
  #............................................................................
  # Environment:
  export JIRA_CSV_INPUT_FILE=$SHORT_JIRA_CSV # Parameters for JIRA CLI
                                             # described above
                                             # (/usr/local/scratch/VIRAL/ST/ST-jira-short.csv)
  export JIRA_OUTPUT_FILE=$JIRA_OUTPUT_STEPS # The raw output from jira_cli
                                             # (/usr/local/scratch/VIRAL/ST/ST-jira-steps.out)
  # ERROR_FILE (/usr/local/scratch/VIRAL/ST/ST-error-${host}-$$)               # (passed through)
  # JIRA_USER                                # (passed through)
  # JIRA_PASSWORD                            # (passed through)
  #............................................................................
  # no error check function
  jira_cli "--action getAvailableSteps"

  #----------------------------------------------------------------------------
  #Display the available steps and get the users selection
  #----------------------------------------------------------------------------
  #Requires: JIRA_OUTPUT_STEPS       The raw output from jira_cli
  #                                  (/usr/local/scratch/VIRAL/ST/ST-jira-steps.out)
  #Temp:     OUTPUT_STEPS_FILE       A copy of the list of steps displayed
  #                                  to the user
  #                                  ("/usr/local/scratch/VIRAL/ST/ST-jira-steps")
  #----------------------------------------------------------------------------
  echo "Available Steps:"
  rm -f $OUTPUT_STEPS_FILE
  #process the output
  cat $JIRA_OUTPUT_STEPS |\
    grep "^[\"'0-9]*," |\
    sort -ur |\
    tee $OUTPUT_STEPS_FILE

  #check if any steps were given
  if [ ! -s $OUTPUT_STEPS_FILE ]; then
    echo "No valid steps available"
    exit 2
  fi

  #----------------------------------------------------------------------------
  #get step from the user
  #----------------------------------------------------------------------------
  #Produces: $step
  #----------------------------------------------------------------------------
  safe_read step    "Please enter step to move samples to (number or name)"
  export value="$step"
}

###############################################################################
# invalid_workflow_steps
# A function to pass to jira_cli to handle errors when using a move command
###############################################################################
# Environment:
# ERROR_FILE
###############################################################################
function check_workflow_steps() {
  if [ ! -z "`grep -E 'Workflow step.*is not valid for issue:' $ERROR_FILE`" ]
  then
    cat $ERROR_FILE | \
    sed -n 's/^.*Workflow step.*is not valid for issue: \([a-zA-Z0-9-]*\).*$/Could not move \1 (invalid transition)/p'
    echo "Not all of the Samples/Sub-tasks could be transitioned"
  fi
  check_username
}
