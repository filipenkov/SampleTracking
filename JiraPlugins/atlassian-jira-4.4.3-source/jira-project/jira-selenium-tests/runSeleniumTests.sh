#!/bin/bash

# script for testing the JIRA installer
# designed to run on Cygwin inside the guest OS of VMware

#run the selenium server, using wildcard because we don't know exactly which version will be there
cd /cygdrive/c/jira-selenium-test/
java -jar  selenium-server-* -port 4444 -debug &

