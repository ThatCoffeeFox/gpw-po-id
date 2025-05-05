## Symulacja Giełdy Papierów Wartościowych

Nasz projekt przewiduje implementację internetowej giełdy papierów wartościowych.
Do projektu użyte zostały frameworki Spring Boot oraz Vaadin z bazą w postgresql.
Komenda do uruchomienia skryptu tworzącego relacje:

    psql -U postgres -d <NAZWA_BAZY> -f db/create.sql

lub

    psql < db/create.sql

Komenda do uruchomienia skryptu usuwającego struktury bazy danych:

    psql -U postgres -d postgres -f db/clear.sql

lub

    psql < db/clear.sql
