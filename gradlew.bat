@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot
set ANDROID_HOME=C:\Users\Quentin\AppData\Local\Android\Sdk

java -version
if %errorlevel% neq 0 (
    echo Java non trouve
    exit /b 1
)

java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain %* 