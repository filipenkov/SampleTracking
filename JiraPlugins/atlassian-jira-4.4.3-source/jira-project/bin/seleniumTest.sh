#!/bin/bash

if [ $# == 0 ]
then
  printf "\nUsage: <jira version>\n\n"
  exit -1;
fi


# First of all - set the right names for files and versions to deploy

export DEV_VERSION="$1";


#
# Do the original func test 1.6 song and dance here
# NB: jar:install and war:install are needed for the deployment further on
#

maven clean jar:install war:webapp war:install -Dmaven.test.skip=true -Dedition=enterprise -Djira.build.rpcplugin=false

result=$?
echo "Result from test: ${result}"

# Only deploy the jar if the selenium tests actually pass. It appears that maven actually returns 0 only when the tests pass.
if test ${result} -ne 0; then
	exit ${result}
fi

#
# Change into func tests sub directory
#
cd subprojects/func_tests/;

#
# Install the func tests JAR file as well now
#
maven jar:install -Dmaven.test.skip=true -Djira.build.rpcplugin=false;

cd ../..

#
# Change into selenium tests sub directory
#
cd subprojects/selenium-tests/;

#
# Install the selenium tests JAR file as well now
#
maven jar:install -Dmaven.test.skip=true -Djira.build.rpcplugin=false;

#
# Deploy the selenium-test JAR file
#
mvn deploy:deploy-file -DgroupId=com.atlassian.jira -DartifactId=jira-selenium-tests -Dversion=$DEV_VERSION -Dpackaging=jar -Dfile=target/selenium-tests-$DEV_VERSION.jar -DrepositoryId=atlassian-private-snapshot -Durl=davs://maven.atlassian.com/private-snapshot -DpomFile=pom.xml

