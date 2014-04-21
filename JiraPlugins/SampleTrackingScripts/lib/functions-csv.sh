#Functions
# - quote_csv_values
# - remove_header
# - get_column
# - get_row

#These functions have no dependencies of their own

#############################################################################
# Normalizes a CSV file to be in the format
# "xxx","xxx","xxx"
# Where every field is wrapped by quotes
#############################################################################
# A Stream command, acts on stdin and returns its output on stdout. 
# Use:	cat x.csv | quote_csv_values() | ...
# Note: This is only for valid CSV files, an unmatched quote can cause errors
#############################################################################
function quote_csv_values() {
  #This doesnt actually use awks column splitting. It just uses awk to
  #apply multiple regexs per line.
  awk '
    #This function adds quotes around the contents of a column.
    #It needs to be called twice as the pattern "claims" the commas
    #at the ends of the column and so the search/replace doesnt start
    #until it reaches the next comma. This leaves every other column
    #unquoted, but running it twice fixes this. 
    function wrap_csv_value(csv_line) {
      #gensub is a gnu extension that allows search and replace on the
      #contents of a variable or string seperate to the input.
      return gensub(/(^|,) *([^",]+) *(,|$)/,
                   "\\1\"\\2\"\\3",
                   "g",
                   csv_line);
      #Search Pattern = /(^|,) *([^",]+) *(,|$)/
      #(^|,)    - A column starts with the start of the string or a comma. 
      #           Store which it was for use in the replacement string
      # *       - Remove the spaces around the commas
      #([^",]+) - Only match colums that dont contain quotes and stop 
      #           matching if there is a comma. Store the contents for the
      #           replacement string
      # *       - Remove the spaces around the commas
      #(,|$)    - A column ends with a comma or the end of the string. 
      #           Store for later
      #Replacement Pattern = "\\1\"\\2\"\\3",
      #\\1      - The first stored part; start of line / comma
      #\\2      - The contents of the collumn
      #\\3      - The end of the column; comma / end of line
      #Options = "g"
      #         - Global search and replace; dont stop after just one 
      #           replacement
      #The string to act on = csv_line (The functions parameter)
    }

    #For every line
    {
      line = $0;                     #$0 is the whole lines contents
      #remove whitespace before quoted columns
      line = gensub(/(^|,) *"/,      
                    "\\1\"",
                    "g",
                    line);
      #Search Pattern = /(^|,) *"/
      #(^|,)    - A column starts with the start of the string or a comma. 
      #           Store which it was for use in the replacement string
      # *       - Remove the spaces before the first column, if it is in 
      #           quotes. Remove the spaces between each comma and the 
      #           quoted column after it
#This may remove whitespace in a column (if it ends with a comma followed by
#spaces e.g. "xxx,  " -> "xxx,"
      #Replacement Pattern = "\\1\"",
      #\\1      - The stored seperator; start of line / comma
      #\"       - The opening quote
      #Options = "g"
      #The string to act on = line 

      #remove whitespace after quoted columns
      line = gensub(/" *(,|$)/,
                    "\"\\1",
                    "g",
                    line);
      #Search Pattern = /" *(,|$)/,
      #"        - The closing quote
      # *       - Remove the spaces between the closing quote and the next 
      #           column
      #(,$)     - A column ends with the end of the string or a comma. 
      #           Store which it was for use in the replacement string
#This may remove whitespace in a column (if it starts with whitespace followed
#by a comma e.g. "  ,xxx" -> ",xxx"
      #Replacement Pattern = ""\\1\",
      #\"       - The closing quote
      #\\1      - The stored seperator; comma / end of line
      #Options = "g"
      #The string to act on = line 

      #Wrap un-quoted columns (the odd numbered columns)
      newline = wrap_csv_value(line);
      #Replace the even numbered columns and print the result to stdout
      print wrap_csv_value(newline);
      #Stop processing before the original line is output
      next;
    }
  '
}

###############################################################################
#quote_for_csv
#removes un-quoted white-space
#any existing wrapping quotes
#replaces double quotes with single quotes
# Only used by shared-action.sh
###############################################################################
# Parameters: 
# $1 The values to be prepaired; e.g. "$step"
###############################################################################
function quote_for_csv() {
  local output_value
  output_value="$(echo "$1" \
    $(: "Remove any newline characters, just to be safe") | \
    tr '[\n\r]' '[  ]' | \
    sed -r \
      $(: "Remove unwrapped whitespace") \
      -e "s/^[[:space:]]*//;" \
      -e "s/[[:space:]]*$//;" \
      $(: "Remove existing wrapping quotes."\
          "The two 'guard' patterns ensure that the opening and closing" \
          "quotes match as the search and replace can't use back references" \
          "The guards are both tested against the same input, using two" \
          "seperate commands could result in 'double stripping'" \
          "e.g. if it was wrapped in single and then double quotes") \
      -e "/'.*'/, /\".*\"/ { s/^['\"](.*)['\"]$/\1/ };" \
      $(: "Convert internal double quotes into singles") \
      -e                    "s/\"/'/g"\
)"
  #add the wrapping quotes
  if [[ "$output_value" ]]; then
    echo "\"$output_value\""
  fi 
}

###############################################################################
#add_values_to_csv
#Processes the parameters to make them suitable for insertion into a CSV
#file
###############################################################################
# Parameters: 
# $1 -> $n The values to quote and then add to the piped CSV file
# Env:
# $NO_HEADER   NOT USED. The first line is copied without appending the value 
#              unless this is set
###############################################################################
function add_values_to_csv() {
  export NO_HEADER=
  local rest_of_line
  rest_of_line=
  for value in "$@"; do
#    echo "$(quote_for_csv "$value")"
    rest_of_line="$rest_of_line, $(quote_for_csv "$value")"
  done
  append_to_lines "$rest_of_line"
}

  #============================================================================
  # get_column()
  #============================================================================
  #Produces: <stdout>         A newline seperated list of values from the 
  #                           requested column
  #Requires: $1               The number of the column to select
  #          <stdin>          The csv file to read the data from
  #                           In the format "xx","xxx"...
  #============================================================================
  #The returned values are in quotes
  #============================================================================
  function get_column() {
    column=$1
    #-F sets the column seperator. 
    #(^ *")        Is a quote at the start of the line, this is used to remove 
    #              any leading quote from the value
    #(" *, *")     Is a comma surrounded by quotes
    #(" *$)        Is a quote at the end of the line
    awk -F '^ *"|" *, *"|" *$' '{
      print "\""$'$[column+1]'"\""  #print just one column
      #"\""..."\""     Wrap the output in quotes to avoid problems with spaces
      #$[column+1]     Is done in the shell and outputs the value in column + 1
      #                This is needed because matching the opening quote 
      #                increments the number used for all of the columns
      #$'$[column+1]'  becomes $N and in awk is replaced by the value of 
      #                column N
    }'
  } #end get_column

  #============================================================================
  # get_column_by_name()
  #============================================================================
  #Produces: <stdout>         A newline seperated list of values from the 
  #                           requested column
  #Requires: $1               The name, or part of, the column to select
  #          <stdin>          The csv file to read the data from
  #                           In the format "xx","xxx"...
  #============================================================================
  #The returned values are in quotes
  #============================================================================
  function get_column_by_name() {
    awk -F '^ *"|" *, *"|" *$' -v "colname=$1" '{
      if(NR==1) {            #If the row number (NR) is 1 = first row
        for(i=1;i<=NF;i++) { #Loop through the columns (NF) is number of columns
          if($i==colname) {   #If the columns value contains the colname
            colnum=i;        #store the columns position
            break;           #stop looking
          } 
        }
      } else {               #For all but the first row
                             #Wrap in quotes if it isnt already
        sub(/^[^\"].*$/,"\"&\"",$colnum);
        print $colnum        #Output $<column number> if we found the column
                             #Output $ if we didnt find the column 
                             # ($) = the whole row
      }
    }'
  }

  #============================================================================
  # get_columns_by_name()
  #============================================================================
  #Produces: <stdout>         A newline seperated list of values from the 
  #                           requested column
  #Requires: $1               A comma seperated list of names
  #          <stdin>          The csv file to read the data from
  #                           In the format "xx","xxx"...
  #============================================================================
  #The returned values are in quotes
  #============================================================================
  #This is more complex and probably not needed and so just use 
  #get_column_by_name for now It has been tested though and does appear to work
  function get_columns_by_name() {
    awk -F '^ *"|" *, *"|" *$' -v "colnames=$1" '{
      split(colnames, nameArray, ",");
      if(NR==1) {                #If the row number (NR) is 1 = first row
        for(name in nameArray) { #Loop through the columns we are looking for
          print nameArray[name];
          for(i=1;i<=NF;i++) {   #Loop through checking the column headers of  
                                 #the input. (NF) is number of columns
                                 #If the columns value contains the name
            if($i==nameArray[name]) {   
                                 #store the columns position
              outputColumns[++currentOutputColumn]=i; 
              break;             #stop looking for this name
            } 
          }                      #end heading column loop
        }                        #end columnNames loop
      } else {                   #For all but the first row
        first=1;
        for(col in outputColumns) {
          if (first) {
            first=0;
          } else {
            printf ",";
          }
          printf "\"%s\"",$outputColumns[col]; 
        }
        printf "\n"
      }
    }'
  }

  #============================================================================
  # get_row()
  #============================================================================
  # returns only rows that match the value given in $2 in the column passed as
  # $1
  #============================================================================
  #Produces: <stdout>         
  #Requires: $1               The number of the column to select
  #          $2               The value for the column
  #          <stdin>          The csv file to read the data from
  #                           In the format "xx","xxx"...
  #============================================================================
  function get_row() { 
    column=$1
    awk -F '^ *"|" *, *"|" *$' '
      ($'$[column+1]'=='$2') #Guard to the next block. Only use if 
                             #column $1 = $2     
    '   
  } #end get_row
