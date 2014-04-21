#!/bin/bash
if [ -z "$1" ]; then
  echo "$0 <batch id>"
  echo "returns a list of BAC IDs; one per line."
  exit
fi

while getopts ":e:" opt; do
  case $opt in
    e)
      ENV=$OPTARG
                shift; shift;
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

##
## Set server, etc. based on env
##
DIR="$( cd "$( dirname "$0" )" && pwd )"
. $DIR/lib/functions.sh


#setup parameters
project="ST"
batch_id="$1"
#JIRA_USER="sampletracking"
#JIRA_PASSWORD="a2c4e6g8"

echo "SERVER = $JIRA_SERVER"

/usr/local/java/1.6.0/bin/java -jar \
   /usr/local/devel/VIRIFX/software/SampleTracking/lib/jira-cli.jar \
--server $JIRA_SERVER \
--user "$JIRA_USER"  \
--password "$JIRA_PASSWORD" \
--action runFromIssueList \
--search "project='$project' and type='sample' and 'Batch Id'='$batch_id' and status != 'Unresolved' and status != 'Deprecated'" \
--common '--action getFieldValue --field "BAC ID" --issue @issue@' \
| grep "^'[^']*'$" \
| sed "s/'//g"

#grep selects only the field value from the output
#jira-cli output format:
#
#Run: --action getFieldValue --field "BAC ID" --issue <JIRA_ID> 
#Issue <JIRA_ID> has field 'BAC ID' with value: 
#'<BAC_ID'
#
#sed removes the wrapping quotes
