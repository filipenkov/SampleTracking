

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
echo Usage: atlas-create-jira4-plugin [options]
echo.
echo Creates a JIRA 4.x (or earlier) plugin.
    echo.
    echo The following options are available:
                                        echo -a [value], --artifact-id [value]
                            echo     Name of the project (corresponds to the Maven artifactId).
        echo.
                                        echo -g [value], --group-id [value]
                            echo     Identifier for the logical group of artifacts associated with the project (corresponds to the Maven groupId).
        echo.
                                        echo -v [value], --version [value]
                            echo     Version of the project (default is 1.0-SNAPSHOT).
        echo.
                                        echo -p [value], --package [value]
                            echo     Java package that will contain the plugin source code (default is group-id value).
        echo.
                                        echo --non-interactive
                            echo     Does not prompt the user for input. Turns off interactive mode.
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
    if /I "%1"=="--artifact-id" (
                    set MVN_PARAMS=%MVN_PARAMS% -DartifactId=%2
            shift
            shift
                goto loopstart
    )  else (
            if /I "%1"=="-a" (
                                    set MVN_PARAMS=%MVN_PARAMS% -DartifactId=%2
                    shift
                    shift
                                goto loopstart
            )  else (
    if /I "%1"=="--group-id" (
                    set MVN_PARAMS=%MVN_PARAMS% -DgroupId=%2
            shift
            shift
                goto loopstart
    )  else (
            if /I "%1"=="-g" (
                                    set MVN_PARAMS=%MVN_PARAMS% -DgroupId=%2
                    shift
                    shift
                                goto loopstart
            )  else (
    if /I "%1"=="--version" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dversion=%2
            shift
            shift
                goto loopstart
    )  else (
            if /I "%1"=="-v" (
                                    set MVN_PARAMS=%MVN_PARAMS% -Dversion=%2
                    shift
                    shift
                                goto loopstart
            )  else (
    if /I "%1"=="--package" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dpackage=%2
            shift
            shift
                goto loopstart
    )  else (
            if /I "%1"=="-p" (
                                    set MVN_PARAMS=%MVN_PARAMS% -Dpackage=%2
                    shift
                    shift
                                goto loopstart
            )  else (
    if /I "%1"=="--non-interactive" (
                    set MVN_PARAMS=%MVN_PARAMS% -DinteractiveMode=false
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
    :loopend


rem ---------------------------------------------------------------
rem Executing Maven
rem ---------------------------------------------------------------

echo Executing: %MAVEN_EXECUTABLE% com.atlassian.maven.plugins:maven-jira-plugin:3.6.1:create_v4 %MVN_PARAMS%
%MAVEN_EXECUTABLE% com.atlassian.maven.plugins:maven-jira-plugin:3.6.1:create_v4 %MVN_PARAMS%

:end



