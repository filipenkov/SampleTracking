#This script takes the output of JIRA-CLI when run with the parameters '--action getFieldValue --field assignee'
#The output is 4 lines per query
#----------------------Example Jira-cli output---------------------
#
#Run: --action getFieldValue --field assignee --issue "ST-1326"
#Issue ST-1326 has field 'assignee' with value: 
#nfedorov
#------------------------------------------------------------------
#We only need the 3rd and 4th lines to get the data we want


#3~4 3rd line in 4 line group / line # / 4 has remainder 3
3~4 h
#h copy the line into the buffer

#4~4 4th line
4~4 {
#-------------------Merge line 3&4 to form:------------------------
#Issue ST-1326 has field 'assignee' with value: nfedorov
#------------------------------------------------------------------
#swap the current line and the buffered line
x
#add the buffer to the end of the current line
G
#remove the new line G adds
s/\n/ /
#----------Copy the Jira ID and Assignee into csv format-----------
#ST-1326,nfedorov
#------------------------------------------------------------------

#seperate the JIRA ID, the issue, and the value
#Matches 'Issue '
#Stores  '[^ ]*' 0 or more characters that aren't space
#Matches '[^:]*' 0 or more characters that aren't a colon, this is normally the blank space after the ID
#Matches ':'
#Matches ' *'    0 or more spaces
#Stores  '.*'    any characters
#Until   '$'     the end of the String
#returns '\1,\2' the two stored values with a comma seperating them
#Notes: SED can't handle '+' for some reason, but * works fine
s/^.*Issue \([^ ]*\)[^:]*: *\(.*\)$/\1,\2/
#------------------------------------------------------------------
#Done output the result
#------------------------------------------------------------------
p
#end of line 4 processing
}
