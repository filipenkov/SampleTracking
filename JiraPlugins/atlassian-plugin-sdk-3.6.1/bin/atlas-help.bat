@echo off
if "%OS%" == "Windows_NT" setlocal


if /I "%1"=="--verbose" goto verboseoutput
if /I "%1"=="-v" goto verboseoutput
if /I "%1"=="/verbose" goto verboseoutput
if /I "%1"=="/v" goto verboseoutput


echo.
echo Atlassian Plugin SDK Help
echo =========================
echo.
echo The following scripts are available to assist with plugin development
echo.


    

    echo atlas-unit-test
    echo     Runs the unit tests for the plugin (runs mvn test).
    echo.


    

    echo atlas-create-bamboo-plugin-module
    echo     Creates a Bamboo Plugin Module
    echo.


    

    echo atlas-run
    echo     Runs the product with your plugin installed.
    echo.


    

    echo atlas-install-plugin
    echo     Installs the plugin into a running application.
    echo.


    

    echo atlas-create-home-zip
    echo     Creates a test-resources zip of the home directory (runs mvn com.atlassian.maven.plugins:maven-amps-dispatcher-plugin:3.6.1:create-home-zip).
    echo.


    

    echo atlas-debug
    echo     Runs the product in debug mode with your plugin installed.
    echo.


    

    echo atlas-create-jira-plugin-module
    echo     Creates a JIRA Plugin Module
    echo.


    

    echo atlas-release-rollback
    echo     Rolls back a release of the plugin (runs mvn release:rollback).
    echo.


    

    echo atlas-cli
    echo     Enables a command-line interface to the plugin development kit.
    echo.


    

    echo atlas-release
    echo     Performs a release of the plugin (runs mvn com.atlassian.maven.plugins:maven-amps-dispatcher-plugin:3.6.1:release).
    echo.


    

    echo atlas-create-jira5-plugin
    echo     Creates a JIRA 5.x plugin.
    echo.


    

    echo atlas-create-crowd-plugin
    echo     Creates a Crowd plugin.
    echo.


    

    echo atlas-create-confluence-plugin
    echo     Creates a Confluence plugin.
    echo.


    

    echo atlas-mvn
    echo     Execute arbitrary Maven commands using the version of Maven bundled with the Atlassian Plugin SDK.
    echo.


    

    echo atlas-create-refapp-plugin-module
    echo     Creates a Ref App Plugin Module
    echo.


    

    echo atlas-create-confluence-plugin-module
    echo     Creates a Confluence Plugin Module
    echo.


    

    echo atlas-create-fecru-plugin
    echo     Creates a FishEye/Crucible plugin.
    echo.


    

    echo atlas-package
    echo     Packages the plugin artifacts (runs mvn package).
    echo.


    

    echo atlas-create-fecru-plugin-module
    echo     Creates a FishEye/Crucible Plugin Module
    echo.


    


    echo atlas-version
    echo     Displays version and runtime information for the Atlassian Plugin SDK.
    echo.


    

    echo atlas-compile
    echo     Compiles the sources of your project (runs mvn compile).
    echo.


    

    echo atlas-create-crowd-plugin-module
    echo     Creates a Crowd Plugin Module
    echo.


    

    echo atlas-integration-test
    echo     Runs the the integration tests for the plugin.
    echo.


    

    echo atlas-create-jira4-plugin
    echo     Creates a JIRA 4.x (or earlier) plugin.
    echo.


    

    echo atlas-run-standalone
    echo     Runs any product standalone, with no plugin project defined.
    echo.


    

    echo atlas-clean
    echo     Removes files generated during the build-time in a project's directory (runs mvn clean).
    echo.


    

    echo atlas-create-bamboo-plugin
    echo     Creates a Bamboo plugin.
    echo.


    




    echo atlas-clover
    echo     Runs Clover and reports on code coverage for this plugin. For colored output, set the MAVEN_COLOR environment variable to true. (runs mvn -Djava.awt.headless=true com.atlassian.maven.plugins:maven-clover2-plugin:3.0.2:setup verify com.atlassian.maven.plugins:maven-clover2-plugin:3.0.2:clover com.atlassian.maven.plugins:maven-clover2-plugin:3.0.2:log).
    echo.


    

    echo atlas-create-refapp-plugin
    echo     Creates a Ref App plugin.
    echo.

goto :finalmessage

:verboseoutput

    

    echo atlas-unit-test
    echo     Runs the unit tests for the plugin (runs mvn test).
    echo.

    

    

    echo atlas-create-bamboo-plugin-module
    echo     Creates a Bamboo Plugin Module
    echo.

    

    

    echo atlas-run
    echo     Runs the product with your plugin installed.
    echo.

    
            echo     The following options are available:
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the product to run (default is RELEASE).
                echo.
                                                                        echo     -c [value], --container [value]
                                                    echo         Container to run in (default is tomcat6x).
                echo.
                                                                        echo     -p [value], --http-port [value]
                                                    echo         HTTP port for the servlet container.
                echo.
                                                                        echo     --context-path [value]
                                                    echo         Application context path (include the leading forward slash).
                echo.
                                                                        echo     --server [value]
                                                    echo         Host name of the application server (default is localhost).
                echo.
                                                                        echo     --jvmargs [value]
                                                    echo         Additional JVM arguments if required.
                echo.
                                                                        echo     --log4j [value]
                                                    echo         Log4j properties file.
                echo.
                                                                        echo     --test-version [value]
                                                    echo         Version to use for test resources. DEPRECATED: use data-version instead.
                echo.
                                                                        echo     --data-version [value]
                                                    echo         Version to use for data resources (default is LATEST)
                echo.
                                                                        echo     --sal-version [value]
                                                    echo         Version of SAL to use.
                echo.
                                                                        echo     --rest-version [value]
                                                    echo         Version of the Atlassian REST module to use.
                echo.
                                                                        echo     --plugins [value]
                                                    echo         Comma-delimited list of plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --lib-plugins [value]
                                                    echo         Comma-delimited list of lib artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --bundled-plugins [value]
                                                    echo         Comma-delimited list of bundled plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --product [value]
                                                    echo         The product to launch with the plugin.
                echo.
                                                                        echo     --instanceId [value]
                                                    echo         The product instance to launch with the plugin.
                echo.
                                                                        echo     --testGroup [value]
                                                    echo         Test group whose products will be launched with the plugin.
                echo.
            

    

    

    echo atlas-install-plugin
    echo     Installs the plugin into a running application.
    echo.

    
            echo     The following options are available:
                                                                        echo     -p [value], --http-port [value]
                                                    echo         HTTP port for the servlet container.
                echo.
                                                                        echo     --context-path [value]
                                                    echo         Application context path (include the leading forward slash).
                echo.
                                                                        echo     --server [value]
                                                    echo         Host name of the application server (default is localhost).
                echo.
                                                                        echo     --username [value]
                                                    echo         Username of administrator to access the plugin system (default is admin).
                echo.
                                                                        echo     --password [value]
                                                    echo         Password of administrator to access the plugin system (default is admin).
                echo.
                                                                        echo     --plugin-key [value]
                                                    echo         Unique key identifying the plugin.
                echo.
            

    

    

    echo atlas-create-home-zip
    echo     Creates a test-resources zip of the home directory (runs mvn com.atlassian.maven.plugins:maven-amps-dispatcher-plugin:3.6.1:create-home-zip).
    echo.

    

    

    echo atlas-debug
    echo     Runs the product in debug mode with your plugin installed.
    echo.

    
            echo     The following options are available:
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the product to run (default is RELEASE).
                echo.
                                                                        echo     -c [value], --container [value]
                                                    echo         Container to run in (default is tomcat6x).
                echo.
                                                                        echo     -p [value], --http-port [value]
                                                    echo         HTTP port for the servlet container.
                echo.
                                                                        echo     --context-path [value]
                                                    echo         Application context path (include the leading forward slash).
                echo.
                                                                        echo     --server [value]
                                                    echo         Host name of the application server (default is localhost).
                echo.
                                                                        echo     --jvmargs [value]
                                                    echo         Additional JVM arguments if required.
                echo.
                                                                        echo     --log4j [value]
                                                    echo         Log4j properties file.
                echo.
                                                                        echo     --test-version [value]
                                                    echo         Version to use for test resources. DEPRECATED: use data-version instead.
                echo.
                                                                        echo     --data-version [value]
                                                    echo         Version to use for data resources (default is LATEST)
                echo.
                                                                        echo     --sal-version [value]
                                                    echo         Version of SAL to use.
                echo.
                                                                        echo     --rest-version [value]
                                                    echo         Version of the Atlassian REST module to use.
                echo.
                                                                        echo     --plugins [value]
                                                    echo         Comma-delimited list of plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --lib-plugins [value]
                                                    echo         Comma-delimited list of lib artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --bundled-plugins [value]
                                                    echo         Comma-delimited list of bundled plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --product [value]
                                                    echo         The product to launch with the plugin.
                echo.
                                                                        echo     --instanceId [value]
                                                    echo         The product instance to launch with the plugin.
                echo.
                                                                        echo     --testGroup [value]
                                                    echo         Test group whose products will be launched with the plugin.
                echo.
                                                                        echo     --jvm-debug-port [value]
                                                    echo         Port open to accept connections for remote debugging (default is 5005).
                echo.
                                                                        echo     --jvm-debug-suspend
                                                    echo         Suspend JVM until debugger connects.
                echo.
            

    

    

    echo atlas-create-jira-plugin-module
    echo     Creates a JIRA Plugin Module
    echo.

    

    

    echo atlas-release-rollback
    echo     Rolls back a release of the plugin (runs mvn release:rollback).
    echo.

    

    

    echo atlas-cli
    echo     Enables a command-line interface to the plugin development kit.
    echo.

    
            echo     The following options are available:
                                                                        echo     -p [value], --http-port [value]
                                                    echo         HTTP port for the servlet container.
                echo.
                                                                        echo     --context-path [value]
                                                    echo         Application context path (include the leading forward slash).
                echo.
                                                                        echo     --server [value]
                                                    echo         Host name of the application server (default is localhost).
                echo.
                                                                        echo     --cli-port [value]
                                                    echo         The port the CLI will listen to for commands (default is 4330).
                echo.
            

    

    

    echo atlas-release
    echo     Performs a release of the plugin (runs mvn com.atlassian.maven.plugins:maven-amps-dispatcher-plugin:3.6.1:release).
    echo.

    

    

    echo atlas-create-jira5-plugin
    echo     Creates a JIRA 5.x plugin.
    echo.

    
            echo     The following options are available:
                                                                        echo     -a [value], --artifact-id [value]
                                                    echo         Name of the project (corresponds to the Maven artifactId).
                echo.
                                                                        echo     -g [value], --group-id [value]
                                                    echo         Identifier for the logical group of artifacts associated with the project (corresponds to the Maven groupId).
                echo.
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the project (default is 1.0-SNAPSHOT).
                echo.
                                                                        echo     -p [value], --package [value]
                                                    echo         Java package that will contain the plugin source code (default is group-id value).
                echo.
                                                                        echo     --non-interactive
                                                    echo         Does not prompt the user for input. Turns off interactive mode.
                echo.
            

    

    

    echo atlas-create-crowd-plugin
    echo     Creates a Crowd plugin.
    echo.

    
            echo     The following options are available:
                                                                        echo     -a [value], --artifact-id [value]
                                                    echo         Name of the project (corresponds to the Maven artifactId).
                echo.
                                                                        echo     -g [value], --group-id [value]
                                                    echo         Identifier for the logical group of artifacts associated with the project (corresponds to the Maven groupId).
                echo.
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the project (default is 1.0-SNAPSHOT).
                echo.
                                                                        echo     -p [value], --package [value]
                                                    echo         Java package that will contain the plugin source code (default is group-id value).
                echo.
                                                                        echo     --non-interactive
                                                    echo         Does not prompt the user for input. Turns off interactive mode.
                echo.
            

    

    

    echo atlas-create-confluence-plugin
    echo     Creates a Confluence plugin.
    echo.

    
            echo     The following options are available:
                                                                        echo     -a [value], --artifact-id [value]
                                                    echo         Name of the project (corresponds to the Maven artifactId).
                echo.
                                                                        echo     -g [value], --group-id [value]
                                                    echo         Identifier for the logical group of artifacts associated with the project (corresponds to the Maven groupId).
                echo.
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the project (default is 1.0-SNAPSHOT).
                echo.
                                                                        echo     -p [value], --package [value]
                                                    echo         Java package that will contain the plugin source code (default is group-id value).
                echo.
                                                                        echo     --non-interactive
                                                    echo         Does not prompt the user for input. Turns off interactive mode.
                echo.
            

    

    

    echo atlas-mvn
    echo     Execute arbitrary Maven commands using the version of Maven bundled with the Atlassian Plugin SDK.
    echo.

    

    

    echo atlas-create-refapp-plugin-module
    echo     Creates a Ref App Plugin Module
    echo.

    

    

    echo atlas-create-confluence-plugin-module
    echo     Creates a Confluence Plugin Module
    echo.

    

    

    echo atlas-create-fecru-plugin
    echo     Creates a FishEye/Crucible plugin.
    echo.

    
            echo     The following options are available:
                                                                        echo     -a [value], --artifact-id [value]
                                                    echo         Name of the project (corresponds to the Maven artifactId).
                echo.
                                                                        echo     -g [value], --group-id [value]
                                                    echo         Identifier for the logical group of artifacts associated with the project (corresponds to the Maven groupId).
                echo.
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the project (default is 1.0-SNAPSHOT).
                echo.
                                                                        echo     -p [value], --package [value]
                                                    echo         Java package that will contain the plugin source code (default is group-id value).
                echo.
                                                                        echo     --non-interactive
                                                    echo         Does not prompt the user for input. Turns off interactive mode.
                echo.
            

    

    

    echo atlas-package
    echo     Packages the plugin artifacts (runs mvn package).
    echo.

    

    

    echo atlas-create-fecru-plugin-module
    echo     Creates a FishEye/Crucible Plugin Module
    echo.

    

    


    echo atlas-version
    echo     Displays version and runtime information for the Atlassian Plugin SDK.
    echo.

    

    

    echo atlas-compile
    echo     Compiles the sources of your project (runs mvn compile).
    echo.

    

    

    echo atlas-create-crowd-plugin-module
    echo     Creates a Crowd Plugin Module
    echo.

    

    

    echo atlas-integration-test
    echo     Runs the the integration tests for the plugin.
    echo.

    
            echo     The following options are available:
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the product to run (default is RELEASE).
                echo.
                                                                        echo     -c [value], --container [value]
                                                    echo         Container to run in (default is tomcat6x).
                echo.
                                                                        echo     -p [value], --http-port [value]
                                                    echo         HTTP port for the servlet container.
                echo.
                                                                        echo     --context-path [value]
                                                    echo         Application context path (include the leading forward slash).
                echo.
                                                                        echo     --server [value]
                                                    echo         Host name of the application server (default is localhost).
                echo.
                                                                        echo     --jvmargs [value]
                                                    echo         Additional JVM arguments if required.
                echo.
                                                                        echo     --log4j [value]
                                                    echo         Log4j properties file.
                echo.
                                                                        echo     --test-version [value]
                                                    echo         Version to use for test resources. DEPRECATED: use data-version instead.
                echo.
                                                                        echo     --data-version [value]
                                                    echo         Version to use for data resources (default is LATEST)
                echo.
                                                                        echo     --sal-version [value]
                                                    echo         Version of SAL to use.
                echo.
                                                                        echo     --rest-version [value]
                                                    echo         Version of the Atlassian REST module to use.
                echo.
                                                                        echo     --plugins [value]
                                                    echo         Comma-delimited list of plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --lib-plugins [value]
                                                    echo         Comma-delimited list of lib artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --bundled-plugins [value]
                                                    echo         Comma-delimited list of bundled plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --product [value]
                                                    echo         The product to launch with the plugin.
                echo.
                                                                        echo     --no-webapp
                                                    echo         Do not start the application.
                echo.
                                                                        echo     --skip-tests
                                                    echo         Skip the tests.
                echo.
            

    

    

    echo atlas-create-jira4-plugin
    echo     Creates a JIRA 4.x (or earlier) plugin.
    echo.

    
            echo     The following options are available:
                                                                        echo     -a [value], --artifact-id [value]
                                                    echo         Name of the project (corresponds to the Maven artifactId).
                echo.
                                                                        echo     -g [value], --group-id [value]
                                                    echo         Identifier for the logical group of artifacts associated with the project (corresponds to the Maven groupId).
                echo.
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the project (default is 1.0-SNAPSHOT).
                echo.
                                                                        echo     -p [value], --package [value]
                                                    echo         Java package that will contain the plugin source code (default is group-id value).
                echo.
                                                                        echo     --non-interactive
                                                    echo         Does not prompt the user for input. Turns off interactive mode.
                echo.
            

    

    

    echo atlas-run-standalone
    echo     Runs any product standalone, with no plugin project defined.
    echo.

    
            echo     The following options are available:
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the product to run (default is RELEASE).
                echo.
                                                                        echo     -c [value], --container [value]
                                                    echo         Container to run in (default is tomcat6x).
                echo.
                                                                        echo     -p [value], --http-port [value]
                                                    echo         HTTP port for the servlet container.
                echo.
                                                                        echo     --context-path [value]
                                                    echo         Application context path (include the leading forward slash).
                echo.
                                                                        echo     --server [value]
                                                    echo         Host name of the application server (default is localhost).
                echo.
                                                                        echo     --jvmargs [value]
                                                    echo         Additional JVM arguments if required.
                echo.
                                                                        echo     --log4j [value]
                                                    echo         Log4j properties file.
                echo.
                                                                        echo     --test-version [value]
                                                    echo         Version to use for test resources. DEPRECATED: use data-version instead.
                echo.
                                                                        echo     --data-version [value]
                                                    echo         Version to use for data resources (default is LATEST)
                echo.
                                                                        echo     --sal-version [value]
                                                    echo         Version of SAL to use.
                echo.
                                                                        echo     --rest-version [value]
                                                    echo         Version of the Atlassian REST module to use.
                echo.
                                                                        echo     --plugins [value]
                                                    echo         Comma-delimited list of plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --lib-plugins [value]
                                                    echo         Comma-delimited list of lib artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --bundled-plugins [value]
                                                    echo         Comma-delimited list of bundled plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --product [value]
                                                    echo         The product to launch with the plugin.
                echo.
            

    

    

    echo atlas-clean
    echo     Removes files generated during the build-time in a project's directory (runs mvn clean).
    echo.

    

    

    echo atlas-create-bamboo-plugin
    echo     Creates a Bamboo plugin.
    echo.

    
            echo     The following options are available:
                                                                        echo     -a [value], --artifact-id [value]
                                                    echo         Name of the project (corresponds to the Maven artifactId).
                echo.
                                                                        echo     -g [value], --group-id [value]
                                                    echo         Identifier for the logical group of artifacts associated with the project (corresponds to the Maven groupId).
                echo.
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the project (default is 1.0-SNAPSHOT).
                echo.
                                                                        echo     -p [value], --package [value]
                                                    echo         Java package that will contain the plugin source code (default is group-id value).
                echo.
                                                                        echo     --non-interactive
                                                    echo         Does not prompt the user for input. Turns off interactive mode.
                echo.
            

    

    




    echo atlas-clover
    echo     Runs Clover and reports on code coverage for this plugin. For colored output, set the MAVEN_COLOR environment variable to true. (runs mvn -Djava.awt.headless=true com.atlassian.maven.plugins:maven-clover2-plugin:3.0.2:setup verify com.atlassian.maven.plugins:maven-clover2-plugin:3.0.2:clover com.atlassian.maven.plugins:maven-clover2-plugin:3.0.2:log).
    echo.

    
            echo     The following options are available:
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the product to run (default is RELEASE).
                echo.
                                                                        echo     -c [value], --container [value]
                                                    echo         Container to run in (default is tomcat6x).
                echo.
                                                                        echo     -p [value], --http-port [value]
                                                    echo         HTTP port for the servlet container.
                echo.
                                                                        echo     --context-path [value]
                                                    echo         Application context path (include the leading forward slash).
                echo.
                                                                        echo     --server [value]
                                                    echo         Host name of the application server (default is localhost).
                echo.
                                                                        echo     --jvmargs [value]
                                                    echo         Additional JVM arguments if required.
                echo.
                                                                        echo     --log4j [value]
                                                    echo         Log4j properties file.
                echo.
                                                                        echo     --test-version [value]
                                                    echo         Version to use for test resources. DEPRECATED: use data-version instead.
                echo.
                                                                        echo     --data-version [value]
                                                    echo         Version to use for data resources (default is LATEST)
                echo.
                                                                        echo     --sal-version [value]
                                                    echo         Version of SAL to use.
                echo.
                                                                        echo     --rest-version [value]
                                                    echo         Version of the Atlassian REST module to use.
                echo.
                                                                        echo     --plugins [value]
                                                    echo         Comma-delimited list of plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --lib-plugins [value]
                                                    echo         Comma-delimited list of lib artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --bundled-plugins [value]
                                                    echo         Comma-delimited list of bundled plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
                echo.
                                                                        echo     --product [value]
                                                    echo         The product to launch with the plugin.
                echo.
                                                                        echo     --no-webapp
                                                    echo         Do not start the application.
                echo.
                                                                        echo     --skip-tests
                                                    echo         Skip the tests.
                echo.
            

    

    

    echo atlas-create-refapp-plugin
    echo     Creates a Ref App plugin.
    echo.

    
            echo     The following options are available:
                                                                        echo     -a [value], --artifact-id [value]
                                                    echo         Name of the project (corresponds to the Maven artifactId).
                echo.
                                                                        echo     -g [value], --group-id [value]
                                                    echo         Identifier for the logical group of artifacts associated with the project (corresponds to the Maven groupId).
                echo.
                                                                        echo     -v [value], --version [value]
                                                    echo         Version of the project (default is 1.0-SNAPSHOT).
                echo.
                                                                        echo     -p [value], --package [value]
                                                    echo         Java package that will contain the plugin source code (default is group-id value).
                echo.
                                                                        echo     --non-interactive
                                                    echo         Does not prompt the user for input. Turns off interactive mode.
                echo.
            

    

:finalmessage

echo For detailed information on a particular script, use the '--help' option, eg. 'atlas-run --help'.
echo For an entire list of all scripts and their parameters, run 'atlas-help --verbose'.
echo.
