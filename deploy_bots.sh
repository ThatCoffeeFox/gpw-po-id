#!/bin/bash

set -e

echo "Przechodzenie do katalogu ./simulation..."
cd simulation

echo "Instalowanie zależności za pomocą pip (biblioteka requests)..."
python3 -m pip install requests

echo "Zależności zostały zainstalowane."
echo ""

echo "Uruchamianie botów symulujących giełdę..."
python3 launch_bots.py

echo "Działanie botów zostało zakończone."

    