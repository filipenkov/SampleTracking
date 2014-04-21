#Functions:
# - to_lowercase
# - to_uppercase
# - number_of_lines
# - safe_read
# - safe_password_read
# - append_to_lines
# - remove_header
# - replace_header
# - test_dependencies
# - test_external_dependencies
# - test_environment

#These functions have no functional dependencies of their own
#They do rely on the external commands:
# - awk
# - sed
# - tr
# - wc
AWK_CMD=$(command -v awk )
SED_CMD=$(command -v sed )
TR_CMD=$(command -v tr )
WC_CMD=$(command -v wc )

if [ -z "${AWK_CMD}" ] || [ -z "${SED_CMD}" ] || [ -z "${TR_CMD}" ] || [ -z "${WC_CMD}" ]; then
  echo "Could not find one or more of: awk, sed, tr, wc"
  exit 1
fi

#These are tested for executability at the end of the file (after the function
#used has been defined)


###############################################################################
#to_lowercase
###############################################################################
#Converts the input string into lowercase and returns it via stout use as:
#echo $(to_lowercase "string")
###############################################################################
# Parameters: 
# $1 The string to convert
###############################################################################
function to_lowercase() {
  echo $1 | $TR_CMD '[A-Z]' '[a-z]'
}

###############################################################################
#to_uppercase
###############################################################################
#Converts the input string into uppercase and returns it via stout.
#Example: echo $(to_uppercase "string")
###############################################################################
# Parameters: 
# $1 The string to convert
###############################################################################
function to_uppercase() {
  echo $1 | $TR_CMD '[a-z]' '[A-Z]'
}

###############################################################################
#number_of_lines
###############################################################################
#Parses the line count from wc and returns the result to stdout.
#If the file doesnt exist, or is empty, 0 is returned.
#Example: echo $(number_of_lines "${LOGDIR}/file")
###############################################################################
# Parameters: 
# $1 file to check
###############################################################################
function number_of_lines() {
  #check the file exists and is readable
  if [ ! -r "$1" ]
  then
    echo 0
    return
  fi
  $WC_CMD -l $1 | $AWK_CMD '{ print $1 }'
}

###############################################################################
#safe_read
###############################################################################
#get a value from the user, if the variable hasnt already been set
###############################################################################
# Parameters: 
# $1              The variable to set
# $2              The message to display
# $password_echo  If set then the input is not echoed back on the screen
###############################################################################
# Produces: $read The value returned, after it has been cleaned.
###############################################################################
function safe_read() {
  variable=$1
  if [ -z "${!variable}" ]; then
    echo "$2"
    if [[ "$password_echo" ]]; then
      read -s read
    else    
      read read
    fi
    #escape quotes for the eval
    local read=$(echo $read | $SED_CMD 's/["]/\\"/g')
    eval "export $variable=\"$read\""
  fi
}

###############################################################################
#safe_password_read
###############################################################################
#get a value from the user, if the variable hasnt already been set
# NO ECHO
###############################################################################
# Parameters: 
# $1 The variable to set
# $2 The message to display
###############################################################################
# Produces: $read The value returned, after it has been cleaned.
###############################################################################
function safe_password_read() {
  password_echo="true"
  safe_read "$1" "$2"
  password_echo=
}

###############################################################################
#append_to_lines
###############################################################################
#Add the given parameter to the end of the lines being piped through.
#The value is added as is, with no processing
###############################################################################
# Parameters: 
# $1           The values to add to each line; e.g. ",$step,\'$comment\'"
# Env:
# $NO_HEADER   NOT USED. The first line is copied without appending the value 
#              unless this is set
###############################################################################
function append_to_lines() {
  NO_HEADER=
  #awk is used instead of sed because we can pass the string to append as 
  #a variable. Using sed requires using shell substitution to pass the params
  #into the sed command. This would require further escaping to ensure that
  #quotes and slashes were properly handled.
  $AWK_CMD -v NO_HEADER="$NO_HEADER" \
     -v line_end="$1" \
    '#Dont append to the first line if HAS_HEADER isnt empty (NR=line number)
     NR == 1 &&  !NO_HEADER {print}; 
     #All lines after the first one, or all if HAS_HEADER is empty
     #print the full line followed by the contents of the line_end
     #variable
     NR > 1  || NO_HEADER {print $0 line_end}'
}

###############################################################################
#remove_header
###############################################################################
#This acts as a filter, removing the first line of a stream
#Example: cat INPUT | remove_header | <other stuff> > OUTPUT
###############################################################################
# Parameters: None
###############################################################################
function remove_header() {
  #Tail for non-obvious reasons uses the number of the line to start at, 
  #counting from 1. So to skip the first line requires -n+2
  tail -n+2
}

###############################################################################
#replace_header
###############################################################################
#This acts as a filter, replacing the first line of a stream with the value
#passed as the first parameter
# cat INPUT | replace_header "$HEAD" | <other stuff> > OUTPUT
###############################################################################
# Parameters: $1 The new value for the first line
###############################################################################
function replace_header() {
  echo "$1"
  remove_header
}

##TODO: test_dependencies, test_external_dependencies and test_environment could
#be rewriten for better code reuse

###############################################################################
#test_dependencies
###############################################################################
#Loops through a list of functions checking that they all exist. If any
#do not exist then an error message is output listing the missing
#functions and the script is exited.
###############################################################################
# Parameters: $1-%$N functions that should exist to pass this test
###############################################################################
function test_dependencies() {
  #========================================================================
  #test_function_exists
  #========================================================================
  #This checks if a function with the name passed as the first parameter
  #exists. It outputs to stout if the function exists. If the function is
  #not found then nothing is output.
  #Example: if [[ "$(test_function_exists "function_name")" ]]; then
  #========================================================================
  # Parameters: $1 The name of the function
  #========================================================================
  function test_function_exists() {
    type "$1" 2> /dev/null | grep '^'"$1"' is a function$'
  }

  local missing_dependencies="";
  local dependency
  for dependency in ${@}; do
    if [ -z "$(test_function_exists ${dependency})" ]; then
      #Note it adds a newline to the end
      missing_dependencies="${missing_dependencies}${dependency}"'
' 
    fi
  done
  if [ -n "${missing_dependencies}" ]; then
    echo "The script couldn't find all of it's required functions."
    echo "The script requires:"
    echo "${missing_dependencies}"
    exit 1
  fi
}

###############################################################################
#test_external_dependencies
###############################################################################
#Loops through a list of programs checking that they all exist. If any
#do not exist then an error message is output listing the missing
#functions and the script is exited.
###############################################################################
# Parameters: $1-%$N functions that should exist to pass this test
###############################################################################
function test_external_dependencies() {
  local missing_dependencies="";
  local non_exec_dependencies="";
  local dependency
  for dependency in ${@}; do
    if [ ! -e "${dependency}" ]; then
      #Note it adds a newline to the end
      missing_dependencies="${missing_dependencies}${dependency}"'
'
    else
      if [ ! -x "${dependency}" ]; then
      non_exec_dependencies="${non_exec_dependencies}${dependency}"'
'
      fi
    fi
  done
  if [ -n "${missing_dependencies}" ] || [ -n "${non_exec_dependencies}" ]; then
    echo "The script couldn't find all of it's required external commands."
  
    if [ -n "${missing_dependencies}" ]; then
      echo "No file found for"
      echo "${missing_dependencies}"
    fi
    if [ -n "${non_exec_dependencies}" ]; then
      echo "You do not have execute permission for"
      echo "${non_exec_dependencies}"
    fi
echo "calling exit"
    exit 1
  fi
}

###############################################################################
#test_environment
###############################################################################
#Loops through a list of required variables checking that they all exist. 
#If any do not exist then an error message is output listing the missing
#variables and the script is exited.
###############################################################################
# Parameters: $1-%$N variables that are required to pass this test
###############################################################################
function test_environment() {
  local missing_variables="";
  local variable
  for variable in ${@}; do
    #double de-reference
    eval a=\$$variable
    if [ -z "$a" ]; then
      #Note it adds a newline to the end
      missing_variables="${missing_variables}${variable}"'
'
    fi
  done
  if [ -n "${missing_variables}" ]; then
    echo "The following required environment variables were not set:"
    echo "${missing_variables}"
    exit 1
  fi
}

#test for these just once for all of the above functions
test_external_dependencies "${AWK_CMD}" "${SED_CMD}" "${TR_CMD}" "${WC_CMD}"
