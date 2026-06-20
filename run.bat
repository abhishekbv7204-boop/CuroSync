@echo off
echo ===================================================
echo      CuraSync - Build and Run Script
echo ===================================================
echo.

cd /d "%~dp0"

if not exist lib mkdir lib
if not exist bin mkdir bin

echo [1/3] Checking for dependencies...
if not exist "lib\sqlite-jdbc-3.42.0.0.jar" (
    echo Downloading SQLite Database Driver...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar' -OutFile 'lib\sqlite-jdbc-3.42.0.0.jar'"
)
if not exist "lib\slf4j-api-1.7.36.jar" (
    echo Downloading SLF4J required by SQLite...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar' -OutFile 'lib\slf4j-api-1.7.36.jar'"
)
if not exist "lib\flatlaf-3.4.1.jar" (
    echo Downloading FlatLaf UI Library...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4.1/flatlaf-3.4.1.jar' -OutFile 'lib\flatlaf-3.4.1.jar'"
)

echo [2/3] Compiling Java source files...
dir /s /B src\main\java\*.java > sources.txt
javac -d bin -cp "lib\*" @sources.txt
if %ERRORLEVEL% neq 0 (
    echo.
    echo COMPILATION FAILED! Please check the errors above.
    del sources.txt
    pause
    exit /b %ERRORLEVEL%
)
del sources.txt

echo [3/3] Launching CuraSync...
echo.
java --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" com.curasync.main.Main

pause
