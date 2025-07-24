@echo off
echo Compilando proyecto Invoice Extract Backend...

REM Buscar Java en ubicaciones comunes
set "JAVA_PATHS=C:\Program Files\Java\jdk-17;C:\Program Files\Java\jdk-11;C:\Program Files\Java\jdk1.8.0_*;C:\Program Files (x86)\Java\jdk-17;C:\Program Files (x86)\Java\jdk-11"

for %%p in (%JAVA_PATHS%) do (
    if exist "%%~p\bin\java.exe" (
        set "JAVA_HOME=%%~p"
        echo Encontrado Java en: "%%~p"
        goto :found_java
    )
)

echo No se encontro Java. Por favor instala Java 11 o superior.
pause
exit /b 1

:found_java
echo Usando JAVA_HOME: "%JAVA_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Compilando con Maven Wrapper...
call .\mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilacion exitosa!
    echo Para ejecutar la aplicacion usa: call .\mvnw.cmd spring-boot:run
) else (
    echo.
    echo Error en la compilacion. Revisa los errores arriba.
)

pause
