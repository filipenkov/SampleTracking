#Functions
# - get_password
# - help_message
# - set_from_parameters
# - create_ST_jira
# - jira_cli

. $DIR/lib/functions-csv.sh
. $DIR/lib/functions-utils.sh
# funtions-utils also finds and defines awk, sed, tr and wc command variables
# ${AWK_CMD} etc (wc isn't actually used in any of these functions)

SQSH_CMD="/usr/local/bin/sqsh"
JAVA_CMD="/usr/local/java/1.7.0/bin/java"
JIRA_CLI_JAR="/usr/local/devel/VIRIFX/software/SampleTracking/lib/jira-cli.jar"

export PROD_JIRA_SERVER="http://sampletracking.jcvi.org/"
export PROD_SYBASE="SYBPROD"
export DEV_JIRA_SERVER="http://sampletracking-dev.jcvi.org:8380/"
export DEV_SYBASE="SYBIL"
export JIRA_USER="sampletracking"
export JIRA_PASSWORD="a2c4e6g8"

export SQSH_USER="glk_admin"
export SQSH_PASSWORD="glk_admin99"

host=$(hostname)

export LOGDIR="/usr/local/scratch/VIRAL/ST"
if [ ! -d "$LOGDIR" ]; then
	mkdir ${LOGDIR} 2>/dev/null
	chmod 0777 ${LOGDIR} 2>/devl/null
fi

test_external_dependencies ${SQSH_CMD} ${JAVA_CMD}
if [ ! -e ${JIRA_CLI_JAR} ]; then 
  echo "Could not locate jira-cli.jar"
  exit 1
fi

function set_env_params() {
##
## If a script wants to set ENV first this will set the server correctly
##
if [ "$ENV" = "dev" ] ; then
	export JIRA_SERVER=$DEV_JIRA_SERVER
	export SYBASE_DB=${DEV_SYBASE}
else 
if [ "$ENV" = "prod" ] ; then
	export JIRA_SERVER=$PROD_JIRA_SERVER
	export SYBASE_DB=${PROD_SYBASE}
fi
fi
}

##
## If a script wants to set ENV first this will set the server correctly
##

set_env_params

###############################################################################
# get_password
###############################################################################
# Attempts to find and check the user's sqsh password
# The following sources are used
#   env  - SQSH_PASSWORD
#   env  - SQSH
#   file - .sqshrc
#   file - .sqshpasswd
#   prompting for user input
###############################################################################
#Produces: stdout           Messages about the source of the password
#          Env - password   The password
#                           If none is found then the empty string is returned
#Requires: nothing
###############################################################################
function get_password() {
  #============================================================================
  # check_password
  #============================================================================
  # connects to the database server using the password given
  #============================================================================
  #Produces: stdout           "TRUE" is output if the password works
  #                           The empty string is returned if it didn't
  #Requires: $1               The password to try
  #============================================================================
  # Use:
  # if [ ! -z "$(check_password $toCheck)" ]; then 
  #    echo "valid password"
  # fi
  #============================================================================
  function check_password() {
    #Ensure that sqsh is run non-interactively
    export password_retry=false
    check=$(
      echo "workaround" | $(: "If it is used in a pipe it doesn't prompt")\
      $SQSH_CMD \
      -b                  $(: "suppress the banner message"              )\
      -S ${SYBASE_DB}          $(: "Use the prod db"                          )\
      -P "$1"             $(: "Pass the password to sqsh test"           )\
      -U "$2"             $(: "Pass the user to sqsh test"           )\
      -i <(echo "\\quit") $(: "input file to ensure that sqsh returns"    \
                              "after loging in"                          )\
       2>&1 > /dev/null)  $(: "redirect stderr to stdout and "            \
                              "redirect old stdout to /dev/null"         )
    if [ -z "$check" ]; then 
      echo "\"TRUE\""
    fi
  }

  #============================================================================
  # password_from_sqsh_cmd
  #============================================================================
  # parses a list of sqsh commands looking for a line that contains
  # set SQSH_PASSWORD=<password>
  #============================================================================
  #Produces: stdout           The password, in quotes;
  #                           If none is found then the empty string is returned
  #Requires: stdin            The commands to parse  
  #============================================================================
  function password_from_sqsh_cmd() {
    #quiet suppresses output unless it matches the pattern
    #[[:space:]] matches whitespace within a line
    #[[:print:]] is the character class of printable non-control characters
    ${SED_CMD} --quiet -r 's/^[[:space:]]*\\set[[:space:]]+password[[:space:]]*=[[:space:]]*([[:print:]]+).*$/\1/p' |\
    tail -n1
  }
  #source is used in messages
  local source

  for (( try=1 ; ; try++ )); do
    case "$try" in
      1) #See if the variable is set and valid
        ;;
      2) #1st try: ~/.sqshrc
        source="~/.sqshrc"
        if [ -r ~/.sqshrc ]; then
          SQSH_PASSWORD=$(cat ~/.sqshrc | password_from_sqsh_cmd )
        fi #end file exists
        ;;
      3) #2nd try: ~/.sqshpasswd
        source="~/.sqshpasswd"
        if [ -r ~/.sqshpasswd ]; then
          SQSH_PASSWORD="$(cat ~/.sqshpasswd |\
                      grep -v '^[[:space:]]*$' |\
                      tail -n1 |\
                      ${TR_CMD} -d [\\n\\r])"
        fi
        ;;
      4) #None of the automatic sources worked, try prompting
        source="user"
        echo "No password found in \$SQSH, ~/.sqshrc, ~/.sqshpasswd"
        safe_read SQSH_PASSWORD "Please enter database password"        
        ;;
      *)
        #just keep asking
        safe_read SQSH_PASSWORD "Please enter database password"
        break
        ;;
    esac

    #if password is empty don't check it
    if [ ! -z "$SQSH_PASSWORD" ]; then
      if [ ! -z $(check_password "$SQSH_PASSWORD" "$SQSH_USER") ]; then
#        echo "Using password from $source"
        return
      else 
        echo "Invalid password for ($SQSH_USER) for server ${SYBASE_DB} from $source"
      fi #end check_password
    fi #end non-empty password
    #clear password for the next attempt
    SQSH_PASSWORD=
  done
}

###############################################################################
#help_message
###############################################################################
# Output the available options
###############################################################################
#Env:  HELP_HEADER   A message to print before the list of options
###############################################################################
function help_message() {
echo "$HELP_HEADER"
  cat <<EOF
Command Line Options:
-c, --comment="<comment>"        The comment to add to the sample/subtask's log
-d, --database="<db>"            The GLK database [e.g. giv]
-e, --env="<env>"                The environment (dev/prod). Defaults to prod
-f, --file="<full filename>"     A tuple file listing the samples to act on
-l, --lot="<lot id>"             The lot to import [e.g. RFH301]
-u, --user="<jira user>"         The JIRA user to use     [ALWAYS Required]
-p, --password="<jira_password>" The JIRA user's password [ALWAYS Required]

-t, --test                       No changes are made to JIRA or the GLK
-h, --help                       Display this message

For subtasks-create.sh or action "subtask" only
-s, --subtask="<subtask type>"   The subtask to create (subtasks-create only)
-g, --assignee="<jira user>"     The JIRA user to assign

If an option is not specified and has no default value it will be prompted 
for when the program is run
EOF
}

###############################################################################
#set_from_parameters
###############################################################################
# Pre-process long parameters and split parameters where equals is used
# Sets variables from commandline parameters. The parameters can be in the form
# -<char> <value>  or -<char>=<value> or 
# --<name>=<value> or --<name> <value>
###############################################################################
function set_from_parameters() {
  #Check the required functions have been loaded
  #util_functions
  required_functions="safe_read safe_password_read"
  #local
  #required_functions="${required_functions} help_message"
  #The actual test is delayed until after the function definitions

  # The two arrays short_arguments and long_arguments define which character
  # argument a name refers to. All of the entries in long_arguments should be
  # unique, but short_arguments may contain the same value multiple times if 
  # an option has multiple names.
  short_arguments=("a"      "c"       "d"        "e"           "g"        "f"    "h"    "l"   "p"        "s"       "t"    "u"    )
  long_arguments=( "action" "comment" "database" "environment" "assignee" "file" "help" "lot" "password" "subtask" "test" "user" )
  #man getopts for information on the format of the pattern
  getopts_pattern=":a:c:d:e:g:f:hl:p:s:tu:"

  #============================================================================
  # lookup_long_name
  #============================================================================
  # Replace strings that match one of the long_arguments with the corresponding 
  # character from the short_argments variable. Any that don't match are passed 
  # through unchanged.
  # The command returns its output via stdout
  # Example: echo $(lookup_long_name "$name")
  #============================================================================
  #Produces: stdin            The short version of the parameter, or the input
  #                           if no parameter is matched
  #Requires: $1               The argument to lookup
  #     Env: long_arguments   An array of values to replace with single letter
  #                           arguments
  #          short_arguments  An array of the single letters to use as 
  #                           replacements
  #============================================================================  
  function lookup_long_name() {
    for (( command_number = 0; command_number <= ${#long_arguments[@]}; command_number++ )); do
      if [[ "$1" == "--${long_arguments[$command_number]}" ]]; then
        echo "-${short_arguments[$command_number]}"
        return
      fi
    done
    #not found
    echo "$1"
  }

  #============================================================================
  # check_if_set()
  #============================================================================
  # Sets a variable only if it doesn't already have a value
  # If the variable has already been set then an error is produced
  #============================================================================
  #Requires: $1               The name of the variable to test
  #          $2               The value to set the variable to
  #          $3               The name of the commandline option 
  #                           (for the error message)
  #     Env: $1               A variable with the name taken from the first 
  #                           parameter is set to the value of the second 
  #                           parameter
  #============================================================================
  function check_if_set() {
    if [ ! -z ${!1} ]; then
      echo "Option '$3' should only be specified once"
      exit 3
    fi
    #eval is evil ;-} but convenient
    #all quotes need to be escaped correctly
    eval "export $1=\"$(echo $2 | ${SED_CMD} "s/\"/'/g")\""
  }

  #----------------------------------------------------------------------------
  # body
  #----------------------------------------------------------------------------
  test_dependencies ${required_functions}

  #Pre-process the arguments to all be in the format
  # -<single character> <value>

  # args_out is used to hold the processed parameters
  #local args_out
  #local args_out_index=0
  #for argument in "${@}"; do
#echo "ARG = ${argument}"
    ##Split parameters where equals is used
    #if [[ "${argument}" =~ ".*-.*=.*" ]]; then #test for a pattern match
      ## ${variable%%<pattern>} removes <pattern> from the end of the string
      #args_out[$args_out_index]=$(lookup_long_name "${argument%%=*}")
      #args_out_index=$[$args_out_index + 1]
      ## ${variable##<pattern>} removes <pattern> from the start of the string
      #args_out[$args_out_index]="${argument##*=}"
    #else
      #args_out[$args_out_index]=$(lookup_long_name "${argument}")
    #fi
    #args_out_index=$[$args_out_index + 1]
#echo "ARG = args_out[$args_out_index]"
  #done
#
  ## copy args back to the commandline parameters
  #set -- "${args_out[@]}" #Must quote, or it removes quotes spliting parameters

  # Use getopts to convert the commandline options into variables
  while getopts $getopts_pattern opt; do
    case "$opt" in
      a) check_if_set actions   "$OPTARG" "action"  ;;
      c) check_if_set comment   "$OPTARG" "comment" ;;
      d) check_if_set database  "$OPTARG" "database";; 
      e) export ENV="$OPTARG"; echo "ENVIRONMENT = $ENV"; set_env_params       ;;
      f) check_if_set file      "$OPTARG" "file"    ;;
      h) help_message; exit 0;;
      l) check_if_set lot       "$OPTARG" "lot"     ;;
#      p) check_if_set JIRA_PASSWORD "$OPTARG" "password";;
      s) check_if_set subtask   "$OPTARG" "subtask" ;;
      t) check_if_set test      "TRUE"    "test"    ;;
      u) check_if_set JIRA_USER "$OPTARG" "user"    ;;
      g) check_if_set assignto  "$OPTARG" "assignee";; 

      :)
        echo "Option -$OPTARG requires an argument." >&2
        exit 1
        ;;
      [?])
        echo "Unknown option" >&2 
        help_message
        exit 2
        ;;
    esac
  done

  #ensure that the username and password are set
  safe_read JIRA_USER     "Please enter the JIRA username to use"
  safe_password_read JIRA_PASSWORD \
                         "Please enter the JIRA user's password to use"

  #check 
  if [ \( ! -z "${file}" \) -a \( ! -z "${lot}" \) ];then
    echo "Only one of lot or file should be set"
    exit 5
  fi
  project="Sample Tracking"
#  project="Sample Tracking Test"
  export project
}

#----------------------------------------------------------------------------
#setup the filtering. This is a variable as it used to be set differently
#for each of the calling scripts. I've kept it as one incase we need to
#go back to that.
#----------------------------------------------------------------------------
#1d			Remove the header line. This has to be done before 
#                     blank lines are removed as the sample create script has
#                     a blank header
#/^[[:space:]]+/d	Remove white-space before the data
#/[[:space:]]+$/d	Remove white-space after the data
#/^$-*/d              Remove blank lines and lines of only hyphens
#/rows* affected/d	Remove the summary line
#                     This could also be done by $d (remove the last line)
#                     I'm not sure which is more robust though
#----------------------------------------------------------------------------
SED_DEFAULT='1d;s/^[[:space:]]+//;s/[[:space:]]+$//;/^-*$/d;/rows* affected/d'

###############################################################################
# create_ST_jira
###############################################################################
#Produces: output file $2 (normally ${LOGDIR}/ST-jira.csv)
#Requires: $1           Action Specific SQL file
#          $2           Output file
# Env:  ERROR_FILE      The file to redirect sterr to
#       HEAD            A line to put at the top of the output
###############################################################################
# This produces a csv file of sample data from the GLK/CTM. The format of
# the data is controlled by the sql file passed as the 1st parameter.
# This works with both bacid files and lot ids.
###############################################################################
function create_ST_jira() {
  #Check the required functions have been loaded
  functions_csv_required="quote_csv_values get_column get_row"
  functions_utils_required="number_of_lines to_lowercase safe_read"
  functions_local_required="list_databases get_sql"
  #The actual test is delayed until after the function definitions

  #alias $1 and $2
  local action_sql=$1
  local output_file=$2

  #============================================================================
  # list_databases
  #============================================================================
  #Produces: <stdout>         A list of databases for use with a for loop
  #Requires: $1               The csv file to locate the databases in. The file
  #                           must be formatted.
  #                           <database>,<collection>,<bacid>
  #============================================================================
  #Use:  for database in $(list_databases <file>); do ...
  #============================================================================
  function list_databases() {
    if [ ! -e $1 ]; then
      echo "No file to scan for databases" >&2
      return
    fi
    cat $1 |\
    $(: "Remove blank lines")\
    ${SED_CMD} -r "/^$/d" |\
    $(: "Format the csv to handle quoted columns")\
    quote_csv_values |\
    $(: "The database is the 1st column")\
    get_column 1 |\
    sort -u - |            $(: "-u   only return each value once (unique)   ")\
    $(: "Convert the value seperator from newline to space. So that the FOR ")\
    $(: "loop can use them")\
    ${TR_CMD} "[\n\r]" " " $(: "replace /[\n\r]/ (newline characters)       ")\
                           $(: "with " " (space)                            ")
  }

  function get_sql() {
  #============================================================================
  # get_sql
  #============================================================================
  # Creates the shared start of all of the functions SQL.
  # - Transfers variables
  # - Creates temp tables
  # - Populates a table of samples based on the csv file or lot
  #============================================================================
  # Environment (Required):
  test_environment \
    db             $(: "The database to use (needed to filter the tuple file)")\
    action_sql     $(: "The script specific sql file to use")\
  # Environment (Optional):
  # $file               The tuple file to use to populate #selected, if empty
  #                     then lot is used
  #============================================================================
  # Used by the SQL:
  #             $db     The database
  #             $lot    The lot to select samples from (only if no tuple file)
  #============================================================================

    #The query is composed of modules
    #------------------------------------------------------------------------
    # var-setup.sql           common; copy the environment variables into
    #                         sqsh. (${db}, ${project})
    #------------------------------------------------------------------------
    cat $DIR/sql/var-setup.sql
    #------------------------------------------------------------------------
    # selected-create.sql     common; setup the temp table
    #------------------------------------------------------------------------
    cat $DIR/sql/selected-create.sql

    #${LOGDIR}/ST-jira-select-from-bacid.sql
    if [ -z "$file" ]; then
      #------------------------------------------------------------------------
      # selected-from_lot.sql   if --file isn't specified. This uses ${lot} to
      #                         populate #selected
      #------------------------------------------------------------------------
      cat "$DIR/sql/selected-from_lot.sql"
    else
      #------------------------------------------------------------------------
      # $file                   The tuple file is used to generate sql that adds
      #                         the bacids it contains to #selected.
      #                         [The file is first filtered for the current 
      #                         database]
      #------------------------------------------------------------------------
      cat "$file" |\
      quote_csv_values |  $(: 'Ensure that all values are quoted to help  ')\
                          $(: 'parsing                                    ')\
      get_row 1 ${db}  |  $(: 'Only use the rows for the current db       ')\
      $(: 'AWK convert the csv file into SQL                              ')\
      $(: '-F  sets (<Start>"); (",") and ("<end>) as the seperators      ')\
      ${AWK_CMD} -F '^ *"|" *, *"|" *$' '{ 
            # The SQL:
            # #Selected   - The hash indicates that this is a temp table
            # ${db}       - This isnt resolved until the sql is run
            # \""$4"\"    - <Double Quote>; close string;
            #             \"        print quote
            #             "         end string. No "+" as awk automatically 
            #                       joins the strings
            #             $4        output the bac field (+1 as the first 
            #                       quote is counted as a seperator)
            #             "         Start a new String
            #             \"        #print quote
            #             \n        #newline followed by go to execute the sql
            #             go        go is used instead of semicolon to allow 
            #                       redirecting the output to /dev/null
            print "INSERT INTO #Selected SELECT Extent_id FROM ${db}..Extent WHERE ref_id=\""$4"\"\ngo > /dev/null";
            next;
         }'
    fi

    #------------------------------------------------------------------------
    # samples-table-create.sql  common; populate #samples using #selected
    #------------------------------------------------------------------------
    cat $DIR/sql/samples-table-create.sql
    #------------------------------------------------------------------------
    # action_sql                The script specific SQL that converts the 
    #                           samples table into CSV format
    #------------------------------------------------------------------------
    if [ ! -e "$action_sql" ]; then
      echo "Warning: file '$file' does not exist and so can't be appended to the sql" >&2
    else
      cat "$action_sql"
    fi
  }
  #----------------------------------------------------------------------------
  # body
  #----------------------------------------------------------------------------
  test_dependencies ${functions_csv_required}\
                   ${functions_utils_required}\
                   ${functions_local_required}

  #----------------------------------------------------------------------------
  #setup the filtering. This is a variable as it used to be set differently
  #for each of the calling scripts. I've kept it as one incase we need to
  #go back to that.
  #----------------------------------------------------------------------------
  SED="$SED_DEFAULT"

  #----------------------------------------------------------------------------
  #Setup output file
  #----------------------------------------------------------------------------
  #Clear any old version, as we will be appending to this file
  rm -f "$output_file"

  if [[ "$HEAD" ]]; then
    #write the header
    echo "$HEAD" > "$output_file"
  fi

  #----------------------------------------------------------------------------
  #Find the databases to use
  #----------------------------------------------------------------------------
  local databases
  #Mode
  if [ -z "$file" ]; then
    #By Lot
    echo "Selecting Samples based on Lot ID. To use a tuple file add"
    echo "--file=<filename> to the command"
    safe_read database "Please enter DB to use"
    databases="$database" #just one db to use
    safe_read lot "Please enter lot code to use"
    lot=$(to_uppercase "$lot")
  else
    #From File
    echo "Selecting Samples based on file $file."
    if [ "$database" ]; then
      echo "The database parameter will be ignored."
      echo "The value for database will be read from the file."
    fi
    #find all of the databases to use
    databases="$(list_databases $file)"
  fi

  #Passed to the scripts run by sqsh
  export db
  export project

  #Clear old errors
  rm -f $ERROR_FILE

  #Find the database password
  get_password

  #----------------------------------------------------------------------------
  #loop through the databases
  #----------------------------------------------------------------------------
  #Doing the work one database at a time is necessary to avoid having to 
  #dynamically write the join that populates the temp data table.
  #This join has to know which databases ExtentAttribute table to link with
  #which sample.
  #----------------------------------------------------------------------------
  for db in $databases; do
    db=$(to_lowercase "$db")

#debug copy of the sql
cat <(get_sql) > "${LOGDIR}/ST-sql-$db-${host}-$$"

    #Ensure that sqsh is run non-interactively
    export password_retry=false
      ${SQSH_CMD} -w 255     $(: "Width of output, 255 to avoid wrapping in ")\
                             $(: "columns in the output file                ")\
         -b                  $(: "Suppress the banner message               ")\
         -P "$SQSH_PASSWORD" $(: "Use the found and tested password         ")\
         -U "$SQSH_USER" $(: "Use the found and tested password         ")\
         -S ${SYBASE_DB}          $(: "Update prod                               ")\
         -i <(get_sql)       $(: "The script to run, see method for details ")\
          2>> $ERROR_FILE    $(: "Send errors to ERROR_FILE                 ")\
      | ${SED_CMD} -r "$SED" $(: "sed is used to reformat the output before ")\
                             $(: "it is appended to the file                ")\
      | sort                 $(: "Some of the functions require the output  ")\
                             $(: "to be ordered                             ")\
      >> "$output_file"
    #check for errors from this database
    if [ -s $ERROR_FILE ]; then # -s = file is not zero size
      #sqsh returned errors, most likely because of an invalid database
      echo "No samples were found; invalid database"
      exit 10
    fi 
  done
  #end databases loop

  #Modify the error message for create
  if [ -n "$create" ]; then # -n tests if the variable has been defined
    not=" not"
  fi
  #Test if the output contains any data
  # the double brackets indicate to treat this as an int comparison
  if (( "$(number_of_lines "$output_file")" <= "1" )); then 
    echo "No Samples were found; check that the values for Lot/Database are"
    echo "correct and that the samples have${not} already been added to jira"
    exit 11
  fi
}

function jira_cli() {
###############################################################################
#jira_cli
###############################################################################
# Environment variables are used to try and keep down the number of 
# parameters, hopefully making it easier to read the code
###############################################################################
# Parameters: 
# $1 The value for the 'common' parameter
# $2 The name of the function to use to check for errors
#       This is only called if #ERROR_FILE is not empty. Write to 
#       $ERROR_FILE before calling jira_cli if the function should always 
#       be called
#       The function called can use INPUT_FILE and ERROR_FILE to locate
#       the files to check.
#       The function should send its error message via stdout
# Environment (Required):
test_environment \
  JIRA_CSV_INPUT_FILE  $(: "A file of parameters for JIRA CLI in csv format")\
  JIRA_OUTPUT_FILE     $(: "A file to store the output of JIRA CLI,")\
                       $(: "No processing takes place on this file.")\
  ERROR_FILE           $(: "A file to write any error messages to")\
  JIRA_USER            $(: "The user to connect to JIRA using")\
  JIRA_PASSWORD        
# Environment (Optional):
# COMMENT              A comment, not containing quote marks
# test                 If this is set then the command is only simulated 
#                      and the state of JIRA is not changed
###############################################################################
# Makes no additions to the environment
###############################################################################
  #the old behaviour used a pre-defined jira user
  # JIRA_USER="sampletracking"
  # JIRA_PASSWORD="a2c4e6g8"

  #Comments are now always part of the csv file
  #if [[ "$COMMENT" ]]; then
  #  comment_cmd="--comment \"$(echo $COMMENT | ${SED_CMD} "s/\"/'/g")\""
  #else
  #  comment_cmd=""
  #fi

  if [[ $test && ! $1 =~ "--action get.*" ]]; then
    simulate="--simulate"
  else
    #requires resetting incase this has been run before
    simulate=""
  fi

  rm -f $ERROR_FILE
#BFB
   echo $JAVA_CMD -jar $JIRA_CLI_JAR \
    -a runFromCSV \
    --server $JIRA_SERVER \
    --user "$JIRA_USER" \
    --password "$JIRA_PASSWORD" \
    --file "$JIRA_CSV_INPUT_FILE" \
    --common "$1" \
    $simulate \
    --continue > $JIRA_OUTPUT_FILE 2> $ERROR_FILE

   $JAVA_CMD -jar $JIRA_CLI_JAR \
    -a runFromCSV \
    --server $JIRA_SERVER \
    --user "$JIRA_USER" \
    --password "$JIRA_PASSWORD" \
    --file "$JIRA_CSV_INPUT_FILE" \
    --common "$1" \
    $simulate \
    --continue > $JIRA_OUTPUT_FILE 2> $ERROR_FILE

  #$comment_cmd \
  #no longer needed, this was part of the jira-cli call. To use it requires
  #adding an eval to the start of the command to correctly group the parameters
  #The eval is needed owing to the optional comment
  #putting it in quotes results in jira-cli thinking that it is a
  #single unknown option
  #Not quoting it results in jira-cli thinking that each word is an option
  #Using escaped quotes (as in comment_cmd) seems to be ignored and it again
  #treats the comments as multiple options

  #check the results
  if [ -s $ERROR_FILE ]; then
    #was an error handler passed?
    if [ ! -z "$2" ]; then
      #call the error handler and echo its output
      #uniq tidies it up where the same error is reported for every sample
      message="$($2 | uniq)"
    fi
    #was a message produced?
    if [[ "$message" ]]; then
      echo "$message"      
    else
      #The summary is on the last line
      tail -1 $ERROR_FILE 
      #default / backup if the error isn't recognized
      echo "Errors occurred. View $ERROR_FILE for more information."
    fi
    exit
  else
    #No errors
    rm -f $ERROR_FILE #file could exist but be zero length
  fi 
}

check_username () {
###############################################################################
#check_username
###############################################################################
#A function to pass to jira_cli to handle errors
###############################################################################
# Environment (Required):
test_environment \
  ERROR_FILE           $(: "A file to write any error messages to")
###############################################################################
# Makes no additions to the environment
###############################################################################
  if [[ "`grep -E 'RemoteAuthenticationException' $ERROR_FILE`" ]]; then
    #try to output the message
    cat ${LOGDIR}/ST-error-${host}-$$ |\
      ${SED_CMD} --quiet 's/^.*RemoteAuthenticationException: *\([^:]*\)$/\1/p'
    #the error handler is called via $() and so this exit only exits the 
    #sub-shell
    exit
  fi
}

###############################################################################
# Process tuple file 
###############################################################################
# NOT USED
# Carries out an AWK print for each line in the tuple file.
# The variables _db, _collection, _bacid are setup using the values
# from the tuple file.
###############################################################################
#Produces: stdout           An output line per input line from the tuple
#                           formatted using the passed in print statement
#Requires: $1               The tuple file to process
#          $formatted_print The content of the print statement e.g.
#                           "insert into .... "_db"..."
#                           Note: for AWK references to variables should be
#                           made outside of quotes and without a leading $
###############################################################################
function process_tuple_file() {
  #Check the required functions have been loaded
  #csv_functions
  test_dependencies "quote_csv_values"
  test_environment formatted_print

  cat "$1" |\
  quote_csv_values |  $(: 'Ensure that all values are quoted to help  ')\
                      $(: 'parsing                                    ')\
  $(: 'AWK converts the csv file into variables                       ')\
  $(: 'and then uses print to generate the output                     ')\
  $(: '-F  sets (<Start>"); (",") and ("<end>) as the seperators      ')\
  ${AWK_CMD} -F '^ *"|" *, *"|" *$' '{
    _db=$2;
    _collection=$3;
    _bacid=$4;
    print '"${formatted_print}"';
    next;
  }'
}

###############################################################################
# get_all_databases
###############################################################################
# Uses the 'genomes' table in 'common' to get a list of all of the databases
###############################################################################
#Produces: stdout           A space seperated list of databases
#Env:      SQSH_PASSWORD
#          SQSH_USERNAME
#Example:  dbs=`get_all_databases`
###############################################################################
function get_all_databases() {
   call_sqsh < ${DIR}/sql/all-databases.sql \
         2>> /dev/null |\
         tail -n+3           $(: "cut of the first 2 lines, the header  ")|\
         head -n-2           $(: "cut of the last 2 lines, the total    ")|\
         ${SED_CMD} 's/ //g' $(: "remove all of the padding sqsh adds   ")|\
         ${TR_CMD} '\n' ' '  $(: "convert from one per line into one    ")\
                             $(: "line with spaces seperating them      ")
}

###############################################################################
# call_sqsh
###############################################################################
# Wraps the sqsh command to allow the inclusion of the various specific
# parameters and environment variables that need to be used
###############################################################################
#Produces: stdout           The normal output from the call to sqsh
#          stderr           The normal output from the call to sqsh
#Params:   stdin            The sql should be piped into the call
#          $1               The password to connect to the database with
#          $2               The user to connect to the database with
#Env:      SQSH_PASSWORD    Used if $1 isn't specified. If this isn't specified
#                           either then sqsh is called without the parameter
#          SQSH_USERNAME    Used if $2 isn't specified. If this isn't specified
#                           either then sqsh is called without the parameter
#Example:  
# call_sqsh "SQSH_USERNAME" "$SQSH_PASSWORD" < echo "\\quit" 2>> $ERROR_FILE
###############################################################################
function call_sqsh() {
    #pass user and password information via the environment
    #Note the choice of name for the environment variables used by sqsh
    #carefully selected to near guarantee a clash with any calling script 
    local old_username="$username"
    local old_password="$password"
    #clear the variables to avoid passing though the old values
    export username=
    export password=

    if [ ! -z "$1" ]; then
      export password="$1"
    else
      if [ ! -z "$SQSH_PASSWORD" ]; then
        export password="$SQSH_PASSWORD"
      fi
    fi
    if [ ! -z "$2" ]; then
      export username="$2"
    else
      if [ ! -z "$SQSH_USERNAME" ]; then
        export username="$SQSH_USERNAME"
      fi
    fi

    #Ensure that sqsh is run non-interactively
    export password_retry=false

    ${SQSH_CMD} -w 255      $(: "Width of output, 255 to avoid wrapping   ")\
                -S ${SYBASE_DB}  $(: "use the prod db                          ")\
                -U ${SQSH_USER}  $(: "use the prod db                          ")\
                -P ${SQSH_PASSWORD}  $(: "use the prod db                          ")\
                -i <(cat -) $(: "re-direct the input to here              ")

   #restore the old username/password values
   username="$old_username"
   password="$old_password"
}
