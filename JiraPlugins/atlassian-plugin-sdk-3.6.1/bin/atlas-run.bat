

@echo off
if "%OS%" == "Windows_NT" setlocal enabledelayedexpansion



rem ---------------------------------------------------------------
rem Check for help command
rem ---------------------------------------------------------------

if /I "%1"=="help" goto showhelp
if /I "%1"=="-?" goto showhelp
if /I "%1"=="-h" goto showhelp
if /I "%1"=="-help" goto showhelp
if /I "%1"=="--help" goto showhelp
if /I "%1"=="/?" goto showhelp
if /I "%1"=="/h" goto showhelp
if /I "%1"=="/help" goto showhelp

goto continue

:showhelp
echo.
echo Usage: atlas-run [options]
echo.
echo Runs the product with your plugin installed.
    echo.
    echo The following options are available:
                                        echo -v [value], --version [value]
                            echo     Version of the product to run (default is RELEASE).
        echo.
                                        echo -c [value], --container [value]
                            echo     Container to run in (default is tomcat6x).
        echo.
                                        echo -p [value], --http-port [value]
                            echo     HTTP port for the servlet container.
        echo.
                                        echo --context-path [value]
                            echo     Application context path (include the leading forward slash).
        echo.
                                        echo --server [value]
                            echo     Host name of the application server (default is localhost).
        echo.
                                        echo --jvmargs [value]
                            echo     Additional JVM arguments if required.
        echo.
                                        echo --log4j [value]
                            echo     Log4j properties file.
        echo.
                                        echo --test-version [value]
                            echo     Version to use for test resources. DEPRECATED: use data-version instead.
        echo.
                                        echo --data-version [value]
                            echo     Version to use for data resources (default is LATEST)
        echo.
                                        echo --sal-version [value]
                            echo     Version of SAL to use.
        echo.
                                        echo --rest-version [value]
                            echo     Version of the Atlassian REST module to use.
        echo.
                                        echo --plugins [value]
                            echo     Comma-delimited list of plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
        echo.
                                        echo --lib-plugins [value]
                            echo     Comma-delimited list of lib artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
        echo.
                                        echo --bundled-plugins [value]
                            echo     Comma-delimited list of bundled plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be ommitted, defaulting to LATEST.
        echo.
                                        echo --product [value]
                            echo     The product to launch with the plugin.
        echo.
                                        echo --instanceId [value]
                            echo     The product instance to launch with the plugin.
        echo.
                                        echo --testGroup [value]
                            echo     Test group whose products will be launched with the plugin.
        echo.
    goto end

:continue

rem ---------------------------------------------------------------
rem Find absolute path to the program
rem ---------------------------------------------------------------

set PRGDIR=%~dp0
set CURRENTDIR=%cd%
cd /d %PRGDIR%..
set ATLAS_HOME=%cd%
cd /d %CURRENTDIR%


rem ---------------------------------------------------------------
rem Identify Maven location relative to script
rem ---------------------------------------------------------------

set M2_HOME=%ATLAS_HOME%\apache-maven
set MAVEN_EXECUTABLE="%M2_HOME%\bin\mvn.bat"


rem Check that the target executable exists

if not exist "!MAVEN_EXECUTABLE!" (
	echo Cannot find %MAVEN_EXECUTABLE%
	echo This file is needed to run this program
	goto end
)



rem ---------------------------------------------------------------
rem Transform Parameters into Maven Parameters
rem
rem NOTE: in DOS, all the 'else' statements must be on the same
rem line as the closing bracket for the 'if' statement.
rem ---------------------------------------------------------------

set MAVEN_OPTS=-Xmx768M -XX:MaxPermSize=256M %ATLAS_OPTS%
set MVN_PARAMS=
:loopstart
if "%1"=="" goto loopend
    if /I "%1"=="--version" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dproduct.version=%2
            shift
            shift
                goto loopstart
    )  else (
            if /I "%1"=="-v" (
                                    set MVN_PARAMS=%MVN_PARAMS% -Dproduct.version=%2
                    shift
                    shift
                                goto loopstart
            )  else (
    if /I "%1"=="--container" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dcontainer=%2
            shift
            shift
                goto loopstart
    )  else (
            if /I "%1"=="-c" (
                                    set MVN_PARAMS=%MVN_PARAMS% -Dcontainer=%2
                    shift
                    shift
                                goto loopstart
            )  else (
    if /I "%1"=="--http-port" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dhttp.port=%2
            shift
            shift
                goto loopstart
    )  else (
            if /I "%1"=="-p" (
                                    set MVN_PARAMS=%MVN_PARAMS% -Dhttp.port=%2
                    shift
                    shift
                                goto loopstart
            )  else (
    if /I "%1"=="--context-path" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dcontext.path=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--server" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dserver=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--jvmargs" (
                    set MVN_PARAMS=%MVN_PARAMS% -Djvmargs=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--log4j" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dlog4jproperties=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--test-version" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dtest.resources.version=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--data-version" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dproduct.data.version=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--sal-version" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dsal.version=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--rest-version" (
                    set MVN_PARAMS=%MVN_PARAMS% -Drest.version=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--plugins" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dplugins=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--lib-plugins" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dlib.plugins=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--bundled-plugins" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dbundled.plugins=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--product" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dproduct=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--instanceId" (
                    set MVN_PARAMS=%MVN_PARAMS% -DinstanceId=%2
            shift
            shift
                goto loopstart
    )  else (
    if /I "%1"=="--testGroup" (
                    set MVN_PARAMS=%MVN_PARAMS% -DtestGroup=%2
            shift
            shift
                goto loopstart
    )  else (

set MVN_PARAMS=%MVN_PARAMS% %1
shift
goto loopstart

        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
        )
    :loopend


rem ---------------------------------------------------------------
rem Executing Maven
rem ---------------------------------------------------------------

echo Executing: %MAVEN_EXECUTABLE% com.atlassian.maven.plugins:maven-amps-dispatcher-plugin:3.6.1:run %MVN_PARAMS%
%MAVEN_EXECUTABLE% com.atlassian.maven.plugins:maven-amps-dispatcher-plugin:3.6.1:run %MVN_PARAMS%

:end



