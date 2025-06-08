#!/bin/bash

set -e

echo "Przechodzenie do katalogu ./simulation..."
cd simulation

echo "Uruchamianie botów symulujących giełdę..."
python3 launch_bots.py

echo "Działanie botów zostało zakończone."