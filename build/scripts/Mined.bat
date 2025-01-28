@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Mined startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and MINED_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xms2g" "-Xmx3g" "-XX:+UseG1GC" "-XX:MaxGCPauseMillis=10" "-XX:+OptimizeStringConcat" "-XX:+UseStringDeduplication"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\Mined.jar;%APP_HOME%\lib\jme3-lwjgl3-3.3.2-stable.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.2.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.2-natives-windows.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.2-natives-linux.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.2-natives-macos.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.2.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.2-natives-windows.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.2-natives-linux.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.2-natives-macos.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.2.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.2-natives-windows.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.2-natives-linux.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.3.2-natives-macos.jar;%APP_HOME%\lib\lwjgl-openal-3.3.2.jar;%APP_HOME%\lib\lwjgl-openal-3.3.2-natives-windows.jar;%APP_HOME%\lib\lwjgl-openal-3.3.2-natives-linux.jar;%APP_HOME%\lib\lwjgl-openal-3.3.2-natives-macos.jar;%APP_HOME%\lib\lwjgl-opencl-3.3.2.jar;%APP_HOME%\lib\lwjgl-3.3.2.jar;%APP_HOME%\lib\lwjgl-3.3.2-natives-windows.jar;%APP_HOME%\lib\lwjgl-3.3.2-natives-linux.jar;%APP_HOME%\lib\lwjgl-3.3.2-natives-macos.jar;%APP_HOME%\lib\jme3-bullet-native-3.3.0-stable.jar;%APP_HOME%\lib\jme3-bullet-3.3.0-stable.jar;%APP_HOME%\lib\jme3-lwjgl-3.3.2-stable.jar;%APP_HOME%\lib\jme3-desktop-3.3.2-stable.jar;%APP_HOME%\lib\jme3-effects-3.3.2-stable.jar;%APP_HOME%\lib\jme3-jogg-3.3.2-stable.jar;%APP_HOME%\lib\jme3-plugins-3.3.2-stable.jar;%APP_HOME%\lib\jme3-terrain-3.3.0-stable.jar;%APP_HOME%\lib\jme3-core-3.3.2-stable.jar;%APP_HOME%\lib\joml-1.10.0.jar;%APP_HOME%\lib\ejml-all-0.41.jar;%APP_HOME%\lib\ejml-simple-0.41.jar;%APP_HOME%\lib\ejml-fsparse-0.41.jar;%APP_HOME%\lib\ejml-fdense-0.41.jar;%APP_HOME%\lib\ejml-dsparse-0.41.jar;%APP_HOME%\lib\ejml-ddense-0.41.jar;%APP_HOME%\lib\ejml-cdense-0.41.jar;%APP_HOME%\lib\ejml-zdense-0.41.jar;%APP_HOME%\lib\ejml-core-0.41.jar;%APP_HOME%\lib\commons-math3-3.6.1.jar;%APP_HOME%\lib\vecmath-1.5.2.jar;%APP_HOME%\lib\lwjgl-2.9.3.jar;%APP_HOME%\lib\j-ogg-all-1.0.0.jar;%APP_HOME%\lib\gson-2.8.1.jar;%APP_HOME%\lib\lwjgl-platform-2.9.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-platform-2.9.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-platform-2.9.3-natives-osx.jar;%APP_HOME%\lib\jinput-2.0.5.jar;%APP_HOME%\lib\jutils-1.0.0.jar;%APP_HOME%\lib\jinput-platform-2.0.5-natives-linux.jar;%APP_HOME%\lib\jinput-platform-2.0.5-natives-windows.jar;%APP_HOME%\lib\jinput-platform-2.0.5-natives-osx.jar


@rem Execute Mined
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %MINED_OPTS%  -classpath "%CLASSPATH%" mined.Main %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable MINED_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%MINED_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
