@echo off
cls

echo Przechodzenie do katalogu .\simulation...
cd simulation
if %ERRORLEVEL% neq 0 (
    echo Błąd: Nie można przejść do katalogu .\simulation.
    exit /b 1
)

echo Instalowanie zależności za pomocą pip (biblioteka requests)...
py -m pip install requests
if %ERRORLEVEL% neq 0 (
    echo Błąd: Nie udało się zainstalować zależności.
    exit /b 1
)

echo Zależności zostały zainstalowane.
echo.

echo Uruchamianie botów symulujących giełdę...
py launch_bots.py

echo Działanie botów zostało zakończone.