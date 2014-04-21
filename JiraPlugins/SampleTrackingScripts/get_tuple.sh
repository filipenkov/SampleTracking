#!/bin/bash
##
## get_tuple.sh
## 
## Input - an Sample Tracking JIRA ticket number, or -f <file of number>
##
## Output - Comma-delimited JIRA Ticket,db,lot,BAC ID
##
##

if [ -z "$1" ]; then
  echo "Usage: $0 <ST-nnnnn>"
  echo "Usage: $0 -f <File of ST-nnnnn>"
  exit 0
fi

DIR="$( cd "$( dirname "$0" )" && pwd )"
. $DIR/lib/functions.sh

if [ "$1" == "-f" ] ; then
while read line
do
	RESULT=`$JAVA_CMD -jar $JIRA_CLI_JAR --quiet --action getFieldValue --issue "$line" --field "Sample Id" --server $JIRA_SERVER --password a2c4e6g8 --user sampletracking 2>/dev/null`
	if [ "$?" -eq "0" ] ; then
		RESULT=`echo $RESULT | sed "s/'//g" | sed "s/_/,/g" `
		echo $line,$RESULT
	else
		echo $line,NOT FOUND
	fi
done < $2
else
	RESULT=`$JAVA_CMD -jar $JIRA_CLI_JAR --quiet --action getFieldValue --issue "$1" --field "Sample Id" --server $JIRA_SERVER --password a2c4e6g8 --user sampletracking 2>/dev/null`
	if [ "$?" -eq "0" ] ; then
		RESULT=`echo $RESULT | sed "s/'//g" | sed "s/_/,/g" `
		echo $1,$RESULT
	else
		echo $1,NOT FOUND
	fi
fi



