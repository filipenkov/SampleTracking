

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
echo Usage: atlas-cli [options]
echo.
echo Enables a command-line interface to the plugin development kit.
    echo.
    echo The following options are available:
                                        echo -p [value], --http-port [value]
                            echo     HTTP port for the servlet container.
        echo.
                                        echo --context-path [value]
                            echo     Application context path (include the leading forward slash).
        echo.
                                        echo --server [value]
                            echo     Host name of the application server (default is localhost).
        echo.
                                        echo --cli-port [value]
                            echo     The port the CLI will listen to for commands (default is 4330).
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
    if /I "%1"=="--cli-port" (
                    set MVN_PARAMS=%MVN_PARAMS% -Dcli.port=%2
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
    :loopend


rem ---------------------------------------------------------------
rem Executing Maven
rem ---------------------------------------------------------------

echo Executing: %MAVEN_EXECUTABLE% com.atlassian.maven.plugins:maven-amps-dispatcher-plugin:3.6.1:cli %MVN_PARAMS%
%MAVEN_EXECUTABLE% com.atlassian.maven.plugins:maven-amps-dispatcher-plugin:3.6.1:cli %MVN_PARAMS%

:end



