#needed for jira_cli
#           safe_read
#           check_username
. $DIR/lib/functions.sh
#needed for get_column_by_name
. $DIR/lib/functions-csv.sh

host=$(hostname)
LOGDIR="/usr/local/scratch/VIRAL/ST"

mkdir ${LOGDIR} 2>/dev/null
chmod 0777 ${LOGDIR}

function find_subtasks() {
###############################################################################
# find_subtasks
# Create a csv file using the lot or tuple file
###############################################################################
#Produces: file $1/subtask_output_file  (${LOGDIR}/ST-jira-subtask.csv)
local subtask_output_file=$1
#Parameters $1
#Env:(opt) $database         The GLK database to use    [not yet supported]
#    (opt) $lot		     The lot to use e.g. RFH301 [not yet supported]
#    (opt) $file             The file that contains the bac ids to use
#          $project          The project to search for the samples in
#                           'Sample Tracking' or 'Sample Tracking Test'
#          $ERROR_FILE       The file to output any errors to
#		             (/tmp/ST-error)
# (write)  JIRA_CSV_INPUT_FILE
# (write)  JIRA_OUTPUT_FILE
# (write)  COMMENT
# (write)  HEAD
#Note:     Either ($lot and $database) or $file should be set, but not both
###############################################################################
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  # subtask_output_file
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
   HEAD="issue"
  #Body:    "<JIRA_ID>"
  #Example: "ST-1643"
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  #----------------------------------------------------------------------------
  #Prompt for the type of subtask to look for
  #----------------------------------------------------------------------------
  #Produces: $subtask       The subtasks name
  #----------------------------------------------------------------------------
  safe_read subtask "Please enter the type of sub-task to act on"

  if [[ "$file" ]];then
    #--------------------------------------------------------------------------
    # FILE VERSION
    #--------------------------------------------------------------------------

    #..........................................................................
    # call create_subtasks_csv
    # This function carries out the actual work
    #..........................................................................
    #Produces: 
    #         $subtask_output_file (passed through)
    # Env: 
    #          file                (passed through)
    #          project             (passed through)
    #          ERROR_FILE          (passed through)
    #          subtask             The type of sub-task to search for
    # (write)  JIRA_CSV_INPUT_FILE
    # (write)  JIRA_OUTPUT_FILE
    # (write)  COMMENT
    # (write)  HEAD
    # Temp:
    JIRA_TEMP_INPUT_FILE=${LOGDIR}/ST-jira-subtask-query-${host}-$$.csv
    JIRA_TEMP_OUTPUT_FILE=${LOGDIR}/ST-jira-subtask-query-${host}-$$.raw
    #..........................................................................
    create_subtasks_csv
  else
    #--------------------------------------------------------------------------
    # LOT VERSION
    #--------------------------------------------------------------------------
    echo "Subtasks can only be selected using a tuple file, not by lot id."
    exit 505
fi
}

function create_subtasks_csv() {
###############################################################################
# create_subtasks_csv
# This produces a csv file of sample data from JIRA
###############################################################################
#Produces: $subtask_output_file  (See find_subtasks above)
# Env:
#          file                The file that contains the bac ids to use
#          project             The project to search for the samples in
#                              'Sample Tracking' or 'Sample Tracking Test'
#          subtask             The type of sub-task to search for
#          ERROR_FILE          The file to redirect sterr to
# (write)  JIRA_CSV_INPUT_FILE
# (write)  JIRA_OUTPUT_FILE
# (write)  COMMENT
# (write)  cat 
# Temp:
JIRA_TEMP_INPUT_FILE=${LOGDIR}/ST-jira-subtask-query-${host}-$$.csv
JIRA_TEMP_OUTPUT_FILE=${LOGDIR}/ST-jira-subtask-query-${host}-$$.raw
###############################################################################

  #----------------------------------------------------------------------------
  # write_out the search query
  #----------------------------------------------------------------------------  
  # Produces JIRA_TEMP_INPUT_FILE containing a list of JQL queries 
  #--------------------------------------------------------------------------
    #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    # JIRA_TEMP_INPUT_FILE (/tmp/ST-jira-subtask-query.csv)
    #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    search_head="search"
    #Body: 
    #" project='<project>' and Type = '<sub-task>' and 'bac id' = '<BAC ID>' "
    #Example: 
    #search
    #" project='Sample Tracking' and Type = 'pcr task' and 'bac id' = '38354' "
    #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  echo "$search_head" > $JIRA_TEMP_INPUT_FILE
  cat $file |\
    awk -F , '{
      print "\"'" project='$project' and Type = '${subtask}' and 'bac id' = '\"\$3\"' "'\""
    }' \
  >> $JIRA_TEMP_INPUT_FILE

  #............................................................................
  #call jira_cli
  #............................................................................
  # Parameters: 
  # 1  The value for the 'common' parameter
  # 2  The name of the function to use to check for errors
  #............................................................................
  # Environment:
    JIRA_CSV_INPUT_FILE="$JIRA_TEMP_INPUT_FILE" # Parameters for JIRA CLI
                                                # See searches generated above
    JIRA_OUTPUT_FILE="$JIRA_TEMP_OUTPUT_FILE"   # The raw output from JIRA
  # ERROR_FILE (/tmp/ST-error)                  # Output of std-err
  # JIRA_USER                                   # already set
  # JIRA_PASSWORD                               # already set
  #............................................................................
  jira_cli "--action getIssueList" subtask_find_error

  #process the results
  #Note: There could be more than one header if there was more than one search. 
  #The headers are filtered out once they have been used to select the column.
  echo $HEAD > "$subtask_output_file"
  cat $JIRA_TEMP_OUTPUT_FILE |\
  grep -E '^"' |             $(: "Only include the headers and CSV lines")\
  get_column_by_name "Key" | $(: "Only include values in the 'Key' column")\
  grep -E '^"[A-Z]+-[0-9]+'  $(: "Only include CSV lines, no headers")\
  >> "$subtask_output_file"
}

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
