@echo off
set PATH=%cd%;%PATH%
set SETTINGSFILE=settings.xml
set LOCALREPO=localrepo
call mvn2.bat clean install -f "atlassian-secure-random/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "sal-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "sal-spi/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "sal-core/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-template-renderer-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-template-renderer-velocity-common/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-template-renderer-velocity16-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "com.atlassian.jersey-library/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-rest-common/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-rest-doclet/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-rest-module/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-rest-common/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "embedded-crowd-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-ip/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-integration-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-cookie-tools/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-integration-client-common/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-core/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-integration-seraph25/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "streams-gadget-resources/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira4-adapter/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-util-concurrent/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-event/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-spring/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-server-common/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-rest-application-management/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-password-encoder/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "activeobjects-spi/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "activeobjects-dbex/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "embedded-crowd-spi/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-password-encoders/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-events/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-persistence/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-integration-client-rest/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-remote/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-ldap/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-core/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "embedded-crowd-core/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "icu4j/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "streams-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "streams-spi/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "streams-inline-actions-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-mail/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-shared/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-spi/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "json/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-dashboard-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-ical-feed/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "analytics-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-issue-nav-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-embedded-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-directory-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "fecru-host-license-provider/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "streams-jira-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-voorhees/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "applinks-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "applinks-spi/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "applinks-host/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-trusted-apps-core/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "applinks-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "modz-detector/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "confluence-host-license-provider/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira5-adapter/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-json-rpc-components/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-json-rpc-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-bamboo-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-monitoring-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-instrumentation-core/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-welcome-plugin/pom.xml" -pl jira-welcome-plugin -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "soy-template-renderer-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-workflow-gui-model/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "workflow-designer-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "sisyphus-scanner-tools/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "stp/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-scheduler/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-trusted-apps-seraph-integration/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-johnson/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "bamboo-host-license-provider/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira4-compat/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-admin-quicksearch-api/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "soy-template-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "streams-core-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "crowd-rest-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-publisher-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-oauth-service-provider-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-velocity/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "streams-jira-inline-actions-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "activeobjects-core/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "activeobjects-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "activeobjects-jira-spi/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "nekohtml/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-gadgets-opensocial-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-host-license-provider/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-quick-edit-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-config/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "embedded-crowd-admin-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-bot-killer/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-embedded-crowd-ofbiz/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-admin-quicksearch-core/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "spi/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "upm-common/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "refapp-host-license-provider/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-universal-plugin-manager-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "hipchat-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-admin-quicksearch-jira/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-tzdetect-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-importers-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-help-tips/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-annotations/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-project/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "jira-issue-collector-plugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "atlassian-renderer/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f "auiplugin/pom.xml" -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean install -f jira-project/jira-components/jira-webapp/pom.xml -Dmaven.test.skip -Pbuild-from-source-dist -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
call mvn2.bat clean package -Dmaven.test.skip -f jira-project/jira-distribution/jira-webapp-dist/pom.xml -Dmaven.test.skip -s %SETTINGSFILE% -Dmaven.repo.local=%cd%\%LOCALREPO% %* 
if %errorlevel% neq 0 exit /b %errorlevel%
