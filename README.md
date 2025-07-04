## Symulacja Giełdy Papierów Wartościowych

Nasz projekt implementuje internetową giełdę papierów wartościowych przy użyciu Spring Boot'a oraz Vaadin'a wraz z bazą danych w PostrgeSQL.

### 1. Funkcjonalności

W naszej aplikacji użytkownik jest w stanie bez rejestracji przeglądać spółki na giełdzie oraz sprawdzać ich obecne informacje - w tym interaktywny wykres.

Po zalogowaniu się lub zarejestrowaniu się użytkownik ma dostęp do swojego profilu i portfeli.
Portfel jest prostym w użyciu kontenerem do organizacji funduszy oraz przechowywania akcji danej firmy. W widoku portfela widoczne są ostatnie transakcje oraz możliwość dodania funduszy przez zewnętrzny "bank".

![Widok portfela](https://i.imgur.com/TVpv6G5.png)

Użytkownik przechodząc teraz na stronę z informacjami danej firmy zobaczy ekran pokazujący ostatnie transakcje, swoje aktywne zlecenia oraz formularz do dodania nowego.
Wszystkie informacje aktualizują się w czasie rzeczywistym, przez co używanie giełdy jest płynne.

![Widok Firmy](https://i.imgur.com/9E23ETE.png)

Jeśli użytkownik posiada uprawnienia administratora, to dostanie on dostęp do paneli administracyjnych pozwalających na edycję i przegląd szczegółów dotyczących użytkowników i firm. Może on również rozpocząć emisję nowych akcji dla danej firmy.

![Widok Paneli](https://i.imgur.com/l4sp7rb.png)

### 2. Rozpoczęcie aplikacji

Aby rozpocząć aplikację, musisz mieć stworzoną bazę danych w PostgreSQL, zainstalowaną Javę w wersji 24+ oraz
zainstalowanego najnowszego Mavena.
Następnie odpal skrypt

    .\build.bat

dla użytkowników Windows lub

    .\build.sh

dla użytkowników Linuxa.
Do uruchomienia aplikacji po budowie należy używać komendy

    java -jar target\origin-0.0.1-SNAPSHOT.jar

Aby usunąć strukturę bazy danych należy uruchomić:

    cd .\db\
    psql -U <NAZWA_UŻYTKOWNIKA> -d <NAZWA_BAZY> -f .\clear.sql

Do użytku są również dostępne boty aby przedstawić symulację giełdy. Aby ich użyć należy zainstalować Python'a w wersji 3+ oraz zainstalowana biblioteka requests, i następnie uruchomić skrypt:

    .\deploy_bots.bat

dla użytkowników Windowsa lub

    .\deploy_bots.sh

dla użytkowników Linuxa.

W środku app/ znajduje się plik .env w którym można zmienić zmienne środowiskowe.

    POSTGRESQL_USER=${DB_USER}
    POSTGRESQL_PASSWORD=postgres
    DATABASE_URL=jdbc:postgresql://127.0.0.1:5432/${DB_NAME}

### 3. API Aplikacji

Aplikacja udostępnia REST API, które pozwala na interakcję z systemem giełdowym w sposób programistyczny. Jest ono wykorzystywane głównie przez boty symulujące ruch na giełdzie, ale może być używane przez dowolne zewnętrzne narzędzia. Wszystkie endpointy znajdują się pod bazowym adresem `/api`.

#### Zarządzanie Zleceniami

| Metoda   | Endpoint                                        | Opis                                                                               | Ciało żądania (Body)         | Odpowiedź sukcesu (200 OK) | Odpowiedź błędu (400 Bad Request) |
| :------- | :---------------------------------------------- | :--------------------------------------------------------------------------------- | :--------------------------- | :------------------------- | :-------------------------------- |
| `POST`   | `/order`                                        | Składa nowe zlecenie kupna lub sprzedaży.                                          | Obiekt JSON typu `OrderDTO`. | Pusta odpowiedź.           | Komunikat błędu.                  |
| `DELETE` | `/order/{orderId}`                              | Anuluje istniejące zlecenie o podanym ID.                                          | Brak.                        | Pusta odpowiedź.           | Komunikat błędu.                  |
| `DELETE` | `/wallets/{walletId}/{companyId}/active_orders` | Anuluje **wszystkie** aktywne zlecenia dla danej spółki w obrębie danego portfela. | Brak.                        | Pusta odpowiedź.           | Komunikat błędu.                  |

#### Informacje o Portfelach i Spółkach

| Metoda | Endpoint                                        | Opis                                                                                                       | Parametry               | Odpowiedź sukcesu (200 OK)                 | Odpowiedź błędu (400 Bad Request) |
| :----- | :---------------------------------------------- | :--------------------------------------------------------------------------------------------------------- | :---------------------- | :----------------------------------------- | :-------------------------------- |
| `GET`  | `/wallets/{walletId}/{companyId}`               | Pobiera zagregowane informacje o stanie portfela w kontekście jednej spółki (np. ilość posiadanych akcji). | `walletId`, `companyId` | Obiekt JSON typu `WalletCompanyDTO`.       | Komunikat błędu.                  |
| `GET`  | `/wallets/{walletId}/{companyId}/active_orders` | Zwraca listę wszystkich aktywnych zleceń dla danej spółki w obrębie danego portfela.                       | `walletId`, `companyId` | Lista obiektów JSON typu `ActiveOrderDTO`. | Komunikat błędu.                  |

#### Informacje o Spółkach

| Metoda | Endpoint                              | Opis                                                | Parametry                                       | Odpowiedź sukcesu (200 OK)                 | Odpowiedź błędu (400 Bad Request) |
| :----- | :------------------------------------ | :-------------------------------------------------- | :---------------------------------------------- | :----------------------------------------- | :-------------------------------- |
| `GET`  | `/companies/{companyId}/transactions` | Zwraca listę ostatnich transakcji dla danej spółki. | `companyId`, opcjonalny `limit` (domyślnie 10). | Lista obiektów JSON typu `TransactionDTO`. | Komunikat błędu.                  |
