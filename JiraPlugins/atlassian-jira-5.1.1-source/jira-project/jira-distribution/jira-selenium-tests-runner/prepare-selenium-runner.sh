#!/bin/bash

unzip -qo *.zip
mkdir -p jira-distribution/jira-selenium-tests-runner/target/generated-sources/jira-selenium-tests
unzip -qo jira-distribution/jira-selenium-tests-runner/PassedArtifacts/jira-selenium-tests-*-jar-with-dependencies.jar xml/* com/atlassian/jira/* -d jira-distribution/jira-selenium-tests-runner/target/generated-sources/jira-selenium-tests
