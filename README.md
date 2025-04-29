## Symulacja Giełdy Papierów Wartościowych

Nasz projekt przewiduje implementację internetowej giełdy papierów wartościowych.
Komenda do uruchomienia skryptu tworzącego baze danych:

    psql -U postgres -d postgres -f db/create.sql

Komenda do uruchomienia skryptu usuwającego struktury bazy danych:

    psql -U postgres -d postgres -f db/clear.sql
