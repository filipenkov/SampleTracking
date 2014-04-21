###############################################################################
#get comment from the user AND check that it isn't empty
###############################################################################
#Produces: $COMMENT
###############################################################################
function check_comment() {
  #check that a comment has been read
  if [ -z "$COMMENT" ]
  then
    echo "No comment to add"
    exit
  fi
}
