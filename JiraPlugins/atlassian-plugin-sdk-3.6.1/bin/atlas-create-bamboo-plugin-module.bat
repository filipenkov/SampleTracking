

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
echo Usage: atlas-create-bamboo-plugin-module [options]
echo.
echo Creates a Bamboo Plugin Module
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

set MVN_PARAMS=%MVN_PARAMS% %1
shift
goto loopstart

:loopend


rem ---------------------------------------------------------------
rem Executing Maven
rem ---------------------------------------------------------------

echo Executing: %MAVEN_EXECUTABLE% com.atlassian.maven.plugins:maven-bamboo-plugin:3.6.1:create-plugin-module %MVN_PARAMS%
%MAVEN_EXECUTABLE% com.atlassian.maven.plugins:maven-bamboo-plugin:3.6.1:create-plugin-module %MVN_PARAMS%

:end



