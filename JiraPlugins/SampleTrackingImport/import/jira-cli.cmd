/usr/local/java/1.6.0_23/bin/java -jar \
/usr/local/devel/VIRIFX/software/SampleTracking/lib/jira-cli.jar \
-a runFromCSV \
--server http://pep-dev:8081/ \
--user "sampletracking" \
--password "xxxxx" \
--file "/tmp/fields" \
--common '--action updateIssue' \
--simulate
--continue
