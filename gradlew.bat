@rem Gradle startup script for Windows

@if "%DEBUG%"=="" @echo off

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

set JAVACMD=java

"%JAVACMD%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
