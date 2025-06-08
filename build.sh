#!/bin/bash

set -e

echo "--- Konfiguracja Bazy Danych ---"
read -p "Podaj nazwę użytkownika PostgreSQL: " DB_USER
read -p "Podaj hasło do użytkownika bazy danych: " DB_PASS
read -p "Podaj nazwę bazy danych: " DB_NAME
echo "---------------------------------"
echo ""

echo "Przechodzenie do katalogu ./db..."
cd db

echo "Uruchamianie skryptu create.sql dla użytkownika '$DB_USER' w bazie '$DB_NAME'..."
psql -U "$DB_USER" -d "$DB_NAME" -f create.sql -W

echo "Struktura bazy danych została utworzona."
echo ""

echo "Przechodzenie do katalogu ./app..."
cd ../app

echo "Tworzenie pliku konfiguracyjnego .env..."

cat > .env <<EOF
POSTGRESQL_USER=${DB_USER}
POSTGRESQL_PASSWORD=${DB_PASS}
DATABASE_URL=jdbc:postgresql://127.0.0.1:5432/${DB_NAME}
EOF

echo "Plik .env został utworzony."
echo ""

echo "Budowanie aplikacji (mvnw clean package)... To może potrwać kilka minut."
mvn clean package -Pproduction

echo "Budowanie zakończone."
echo ""

echo "Uruchamianie aplikacji..."
java -jar target/origin-0.0.1-SNAPSHOT.jar

echo "Aplikacja została zatrzymana."