#!/bin/bash

host=$(hostname)
LOGDIR="/usr/local/scratch/VIRAL/ST"

###############################################################################
HELP_HEADER="Creates a new subtask for each JIRA 'Sample' specified"
###############################################################################
#Parameters:
# SEE functions.set_from_parameters()
# SEE functions.functions.sh:help_message()
# There are no extra parameters that this script accepts
#
#Environment Variables:
# SEE functions.set_from_parameters()
# There are no other variables in herited from the environment
#
#Files (In):
# A tuple file in the format <db>,<collection>,<bac-id> can be specified as a 
# parameter (--file) or through an environment variable $file.
#Files (Out):
# NONE
#Files (Temp/diagnostic):
#  This same error file is used for all of the external command calls
export ERROR_FILE="${LOGDIR}/ST-error-${host}-$$"
#  This is hardcoded in the SQL files. It is used as input to the first call 
#  to jira_cli (JIRA_CSV_INPUT_FILE) and in building up the input to the 
#  second call
       SQL_OUTPUT_FILE="${LOGDIR}/ST-jira-${host}-$$.csv" 
#  The output of the first call to jira_cli. It contains information on who
#  is assigned to each of the parent tasks
       JIRA_OUTPUT_ASSIGNMENTS="${LOGDIR}/ST-jira-assignee-${host}-$$.out" 
#  The contents of JIRA_OUTPUT_ASSIGNMENTS is reformatted into this CSV file
       ASSIGNEE_CSV_FILE="${LOGDIR}/ST-jira-assignee-processed-${host}-$$.out"
#  The file used as input to the second call to jira_cli
       MERGED_CSV_INPUT_FILE="${LOGDIR}/ST-jira-merged-${host}-$$.csv"
#  The output of jira_cli when it is creating the subtasks 
       JIRA_OUTPUT_SUBTASK="${LOGDIR}/ST-jira-subtasks-${host}-$$.out"
#
#External Functions:
# set_from_parameters	Reads the commandline variables and prompts for
#                       any missing required params
# create_ST_jira        Calls the database script to get the JIRA-IDs
# safe_read             Prompts for a variable
# jira_cli		Makes calls to JIRA
#
#Internal Functions:
# append_to_lines_from_file      Add to the end of each line the  
#                                corresponding line from the input file
# get_assignee_from_jira         Find the assignee for the sample in JIRA
# create_fake_assignee_csv_file  Create a file with the same assignee for all
#                                samples
###############################################################################

export ENV=prod

#get the directory that this script is in, needed for all referenced files
DIR="$( cd "$( dirname "$0" )" && pwd )"
. $DIR/lib/functions.sh
set_from_parameters "$@"

###############################################################################
# Add to the end of each line the corresponding line from the input file
###############################################################################
#Parameters: 
# $1 The file to merge
#Note:
# Hard-coded to use ',' as the delimeter
###############################################################################
function append_to_lines_from_file() {
  paste \
  -d ',' $(: 'sets the delimeter inserted to "," instead of the default tab')\
  - $(: '"-" is a special filename indicating to use stdin')\
  $1
}

function get_assignee_from_jira() {
###############################################################################
#get_assignee_from_jira
###############################################################################
#Find the assignee for the sample in JIRA
###############################################################################
#Produces: ASSIGNEE_CSV_FILE       JIRA_OUTPUT_ASSIGNMENTS processed into
#                                  csv format
#                                  (${LOGDIR}/ST-jira-assignee-processed.out)
#Requires: SQL_OUTPUT_FILE	   Used as the CSV input for jira_cli
#                                  (${LOGDIR}/ST-jira.csv)
#Internal: JIRA_OUTPUT_ASSIGNMENTS The jira_cli results containing 
#                                  sample assignment information
#                                  (${LOGDIR}/ST-jira-assignee.out)
#Env:      ERROR_FILE              ($LOGDIR}/ST-error)
JIRA_CSV_INPUT_FILE="$SQL_OUTPUT_FILE"
JIRA_OUTPUT_FILE="$JIRA_OUTPUT_ASSIGNMENTS"
###############################################################################
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  # ASSIGNEE_CSV_FILE
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  #Head:
  head=",assignee"
  #Body: <JIRA-ID>,<User ID>
  #Example: 
  #  ST-1587,nfedorov
  #  ST-1588,nfedorov
  #Note:  A sub-part of JIRA_OUTPUT_ASSIGNMENTS
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  # JIRA_OUTPUT_ASSIGNMENTS
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  #Format: <blank line> \n Run: <command line parameters> \n Issues <Jira ID> 
  #                             has field 'assignee' with value: \n <user name>
  #Example:
  #
  #Run: --action getFieldValue --field assignee --issue "ST-1643"
  #Issue ST-1643 has field 'assignee' with value: 
  #rhalpin1~2
  #Run: --action getFieldValue --field assignee --issue "ST-1643"
  #Issue ST-1643 has field 'assignee' with value: 
  #rhalpin
  #
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  #Call jira_cli with SQL_OUTPUT_FILE and store the result as 
  #JIRA_OUTPUT_ASSIGNMENTS

  echo "Looking up samples' assignees"
  jira_cli "--action getFieldValue --field assignee"

  # process the output from JIRA (JIRA_OUTPUT_ASSIGNMENTS) into a csv file
  # $ASSIGNEE_CSV_FILE
  echo $head > $ASSIGNEE_CSV_FILE

cat $JIRA_OUTPUT_ASSIGNMENTS\
 | sed $(: 'merge the Issue and the value into one line')\
-n $(: 	   'no output (p is used to explicitly output what we want)')\
-r $(:     'normal regex')\
\
'/Issue.*assignee.*value/'$(:    'Start processing only when a line matching 
                                  this pattern is found')\
'{'$(:                           'All the directive in the braces only apply 
                                  when the pattern matched')\
's/^.*Issue ([^ ]+) .*$/\1/;'$(: 'cut out the Issue id')\
'N;'$(:                          'append the next line')\
's/\n/,/p'$(:                    'replace the new-line with a comma')\
'}'\
 | \
sort $(: 'The sort is to ensure that the SQL_OUTPUT_FILEs entries match up') \
>> $ASSIGNEE_CSV_FILE


:<<COMMENT
The original command, in case all the inline comments make it hard to read
cat $JIRA_OUTPUT_ASSIGNMENTS |\a
sed -nr '/Issue.*assignee.*value/{s/^.*Issue ([^ ]+) .*$/\1/;N;s/\n/,/p}' |\
sort\
>> $ASSIGNEE_CSV_FILE
COMMENT
}

function create_fake_assignee_csv_file() {
###############################################################################
#Create a csv file where every assignee is $assignto
###############################################################################
#Produces: ASSIGNEE_CSV_FILE       a csv file containing <JIRA-ID>,<User ID>
#                                  (${LOGDIR}/ST-jira-assignee-processed.out)
#Requires: assignto		   The user that all the samples will be 
#                                  assigned to
###############################################################################
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  # ASSIGNEE_CSV_FILE
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  head=",assignee"
  #Body: X,<User ID>
  #Example: 
  #  X,nfedorov
  #  X,nfedorov
  #Note:  A sub-part of JIRA_OUTPUT_ASSIGNMENTS
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  #create a file the same length as the SQL output file
  create_empty_file "$(number_of_lines $SQL_OUTPUT_FILE)" |\
  replace_header "$head" |\
  add_values_to_csv "$assignto" $(: 'add_values_to_csv always prepends a comma')\
                                $(: 'resulting in ",<assignee>"')\
    > "$ASSIGNEE_CSV_FILE"

}


###############################################################################
# create_empty_file
# Creates a file with the number of lines passed as the parameter
###############################################################################
# $1 The number of lines in the output file
###############################################################################
function create_empty_file() {
  for (( i = 0; i < $1; i++ ))
  do 
    echo ""
  done
}

###############################################################################
# check_subtask
###############################################################################
#function to pass to jira_cli to handle errors
###############################################################################
  check_subtask () {
    if [ ! -z "`grep 'error: Issue type' $ERROR_FILE`" ]; then
      echo "Invalid subtask name"
      exit
    fi
    check_username
  }

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------


#------------------------------------------------------------------------------
#Select Jira IDs for Samples from the GLK
#------------------------------------------------------------------------------
#Produces: SQL_OUTPUT_FILE         The output from sqsh and the sql scripts
#                                  (${LOGDIR}/ST-jira.csv)
#------------------------------------------------------------------------------
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  # SQL_OUTPUT_FILE
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  HEAD="Issue"
  #         The header only gives one field, this will cause JIRA CLI to 
  #         ignore the rest of the values
  #Body: <JIRA_ID>,Sample Tracking,<Collection ID>_<Sample Number>_
  #Example: ST-1643,Sample Tracking,BUCB_00041_
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
create_ST_jira $DIR/sql/subtasks-create-csv-file.sql "$SQL_OUTPUT_FILE"

#------------------------------------------------------------------------------
#Get the type of subtask to create
#------------------------------------------------------------------------------
#Produces: $subtask       The subtasks name
#------------------------------------------------------------------------------
safe_read subtask "Please enter the type of sub-task to create"


#------------------------------------------------------------------------------
#Get a comment
#------------------------------------------------------------------------------
#Produces: $COMMENT                The value could be empty
#------------------------------------------------------------------------------
export COMMENT
#copy the commandline variable, if there is one
COMMENT="$comment"
safe_read COMMENT "Please enter a comment"

if [ -z $assignto ]; then
  #............................................................................
  # CALL get_assignee_from_jira
  #............................................................................
  #Lookup the assigee of each Sample from JIRA
  #............................................................................
  #Produces: $ASSIGNEE_CSV_FILE    a csv file containing <JIRA-ID>,<User ID>
  #                                (${LOGDIR}/ST-jira-assignee-processed.out)
  #          $ERROR_FILE	   (${LOGDIR}/ST-error)
  #Requires: $SQL_OUTPUT_FILE	   Used as the CSV input for jira_cli
  #                                (${LOGDIR}/ST-jira.csv)
  #............................................................................
  get_assignee_from_jira
else
  #Create an ASSIGNEE_CSV_FILE where every line is the same assignee
  create_fake_assignee_csv_file
fi

#------------------------------------------------------------------------------
#Create a merged CSV file containing
# - The output of the database query ($SQL_OUTPUT_FILE)
# - The subtask information          ($summary_type and $subtask)
# - The asignee information          ($ASSIGNEE_CSV_FILE)
#------------------------------------------------------------------------------
#Produces: $ASSIGNEE_CSV_FILE      a csv file containing <JIRA-ID>,<User ID>
#                                  (${LOGDIR}/ST-jira-assignee-processed.out)
#          $ERROR_FILE	           (${LOGDIR}/ST-error)
#Requires: $SQL_OUTPUT_FILE	   Used as the CSV input for jira_cli
#                                  (${LOGDIR}/ST-jira.csv)
#Env:      $subtask                The name of the subtask to create
#          $COMMENT                The comment entered by the user
#------------------------------------------------------------------------------
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  # MERGED_CSV_INPUT_FILE
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  HEAD="Parent,Project,summary,type,comment"
  #HEAD from ASSIGNEE_CSV_FILE = ",assignee"
  #Body:<JIRA_ID of the parent sample>,Sample Tracking,
  #   <Collection ID>_<Sample Number>_<Sub-task Name (With ' ' replaced by _)>,
  #   <Sub-task Name>,<Comment>,<ignored value>,<Assignee>
  #Example: 
  #ST-1643,Sample Tracking,BUCB_00041_SISPA_task,SISPA task,'test',X,nfedorow
  #Note: Combines   SQL_OUTPUT_FILE (${LOGDIR}/ST-jira.csv)
  #		  ASSIGNEE_CSV_FILE (${LOGDIR}/ST-jira-assignee-processed.out)
  #::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

#The subtask name with spaces replaced
summary_type=$(echo "$subtask" | tr " " "_") #remove the spaces

#add the rest of the subtasks name, type and comment to the CSV file
cat $SQL_OUTPUT_FILE | \
  replace_header "$HEAD" |\
         $(: 'finish the summary field') \
  append_to_lines "$summary_type" |\
         $(: 'Add the fixed value for the subtask field')\
  add_values_to_csv "$subtask" "$COMMENT" | \
         $(: 'Merge the fields from the ASSIGNEE_CSV_FILE')\
  append_to_lines_from_file $ASSIGNEE_CSV_FILE \
  > $MERGED_CSV_INPUT_FILE

#..............................................................................
#CALL jira_cli to create the subtasks
#..............................................................................
#Produces: $JIRA_OUTPUT_SUBTASK     (${LOGDIR}/ST-jira-subtasks.out)
JIRA_OUTPUT_FILE=$JIRA_OUTPUT_SUBTASK
#Requires: $MERGED_CSV_INPUT_FILE   Used as the CSV input for jira_cli
#                                   (${LOGDIR}/ST-jira-merged.csv)
JIRA_CSV_INPUT_FILE=$MERGED_CSV_INPUT_FILE
#Env:      $ERROR_FILE	            The file to output any errors to
#                                   (${LOGDIR}/ST-error)
#..............................................................................
jira_cli "--action createIssue" check_subtask

  
