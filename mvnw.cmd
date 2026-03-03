@echo off
REM ----------------------------------------------------------------------------
REM Maven Wrapper script for Windows
REM ----------------------------------------------------------------------------

setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

REM Resolve Java
if not "%JAVA_HOME%"=="" (
  set JAVACMD=%JAVA_HOME%\bin\java.exe
) else (
  set JAVACMD=java.exe
)

REM Read wrapperUrl (fallback)
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
if exist "%WRAPPER_PROPERTIES%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%WRAPPER_PROPERTIES%") do (
    if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
  )
)

REM Download wrapper jar if missing
if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper jar...
  if exist "%SystemRoot%\System32\curl.exe" (
    "%SystemRoot%\System32\curl.exe" -fsSL "%WRAPPER_URL%" -o "%WRAPPER_JAR%"
  ) else (
    echo ERROR: curl not found to download Maven Wrapper jar. >&2
    exit /b 1
  )
)

"%JAVACMD%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
