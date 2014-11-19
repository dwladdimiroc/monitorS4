@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  s4-tools startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and S_TOOLS_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\s4-tools-0.6.0-incubating.jar;%APP_HOME%\lib\guava-13.0.1.jar;%APP_HOME%\lib\guice-3.0.jar;%APP_HOME%\lib\guice-assistedinject-3.0.jar;%APP_HOME%\lib\slf4j-api-1.6.1.jar;%APP_HOME%\lib\logback-core-0.9.29.jar;%APP_HOME%\lib\logback-classic-0.9.29.jar;%APP_HOME%\lib\commons-configuration-1.6.jar;%APP_HOME%\lib\collections-generic-4.01.jar;%APP_HOME%\lib\commons-io-2.4.jar;%APP_HOME%\lib\commons-lang-2.6.jar;%APP_HOME%\lib\jcip-annotations-1.0.jar;%APP_HOME%\lib\zookeeper-3.3.3.jar;%APP_HOME%\lib\metrics-core-2.1.3.jar;%APP_HOME%\lib\reflectasm-1.07-shaded.jar;%APP_HOME%\lib\javax.inject-1.jar;%APP_HOME%\lib\s4-base-0.6.0-incubating.jar;%APP_HOME%\lib\s4-comm-0.6.0-incubating.jar;%APP_HOME%\lib\s4-core-0.6.0-incubating.jar;%APP_HOME%\lib\jcommander-1.25.jar;%APP_HOME%\lib\zkclient-0.1.jar;%APP_HOME%\lib\gradle-base-services-1.4.jar;%APP_HOME%\lib\gradle-core-1.4.jar;%APP_HOME%\lib\gradle-tooling-api-1.4.jar;%APP_HOME%\lib\gradle-wrapper-1.4.jar;%APP_HOME%\lib\log4j-1.2.15.jar;%APP_HOME%\lib\commons-logging-1.1.1.jar;%APP_HOME%\lib\commons-collections-3.2.1.jar;%APP_HOME%\lib\aopalliance-1.0.jar;%APP_HOME%\lib\minlog-1.2.jar;%APP_HOME%\lib\gson-1.6.jar;%APP_HOME%\lib\objenesis-1.2.jar;%APP_HOME%\lib\kryo-2.20.jar;%APP_HOME%\lib\netty-3.5.11.Final.jar;%APP_HOME%\lib\asm-4.0.jar;%APP_HOME%\lib\commons-codec-1.4.jar

@rem Execute s4-tools
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %S_TOOLS_OPTS%  -classpath "%CLASSPATH%" org.apache.s4.tools.Tools %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable S_TOOLS_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%S_TOOLS_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
