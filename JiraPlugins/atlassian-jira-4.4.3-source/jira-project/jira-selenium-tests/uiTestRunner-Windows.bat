set JAVA_HOME=C:\Progra~1\Java\jdk1.6.0_12
cd subprojects\func_tests
call "%MAVEN_HOME%\bin\maven" clean jar:install -Dmaven.test.skip
cd ..\..\
cd subprojects\selenium-tests\
call "%MAVEN_HOME%\bin\maven" clean -Dmaven.test.skip
cd ..\..\
call "%MAVEN_HOME%\bin\maven" clean jira:disablesecurity jar:install war:webapp  jira:ui-func-tests -Dmaven.test.skip=true -Dedition=enterprise -Djira.build.rpcplugin=false %*
