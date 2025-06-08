@echo off
cls

echo --- Konfiguracja Bazy Danych ---
set /p DB_USER="Podaj nazwę użytkownika PostgreSQL: "
set /p DB_NAME="Podaj nazwę bazy danych: "
set /p DB_PASS="Podaj hasło do bazy danych: "
echo ---------------------------------
echo.

echo Przechodzenie do katalogu .\db...
cd db
if %ERRORLEVEL% neq 0 (
    echo Błąd: Nie można przejść do katalogu .\db.
    exit /b 1
)

echo Uruchamianie skryptu create.sql dla użytkownika "%DB_USER%" w bazie "%DB_NAME%"...
psql -U "%DB_USER%" -d "%DB_NAME%" -f create.sql
if %ERRORLEVEL% neq 0 (
    echo Błąd: Nie udało się wykonać skryptu create.sql.
    exit /b 1
)

echo Struktura bazy danych zostala utworzona.
echo.

echo Przechodzenie do katalogu .\app...
cd ..\app
if %ERRORLEVEL% neq 0 (
    echo Błąd: Nie można przejść do katalogu .\app.
    exit /b 1
)

echo Tworzenie pliku konfiguracyjnego .env...

(
    echo POSTGRESQL_USER=%DB_USER%
    echo POSTGRESQL_PASSWORD=%DB_PASS%
    echo DATABASE_URL=jdbc:postgresql://127.0.0.1:5432/%DB_NAME%
) > .env

echo Budowanie aplikacji (mvnw clean package)... To moze potrwac kilka minut.
CALL mvnw.cmd clean package -Pproduction
if %ERRORLEVEL% neq 0 (
    echo Błąd: Budowanie aplikacji nie powiodło się.
    exit /b 1
)

echo Budowanie zakonczone.
echo.

echo Uruchamianie aplikacji...
java -jar target\origin-0.0.1-SNAPSHOT.jar

echo Aplikacja zostala zatrzymana.
pause