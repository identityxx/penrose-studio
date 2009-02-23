@echo off
setlocal

set VD_SERVER_HOME=%~dp0..

set CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift

:setupArgs

if ""%1""=="""" goto doneStart
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart

set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto run

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo Error: JAVA_HOME is not defined.
goto end

:run

set LOCALLIBPATH=%JAVA_HOME%\lib\ext;%JAVA_HOME%\jre\lib\ext

cd %VD_SERVER_HOME%

"%VD_SERVER_HOME%\vd-studio.exe" %CMD_LINE_ARGS%

:end
endlocal
