# MovieChecklist – Dokumentacja projektu

## Opis aplikacji

MovieChecklist to aplikacja Android napisana w Kotlin z użyciem Jetpack Compose. Pozwala użytkownikowi zarządzać listą filmów i seriali – dodawać je do planowanych, oznaczać jako obejrzane, oceniać gwiazdkami (1–5) oraz wyszukiwać nowe tytuły przez API TMDB. Dane są przechowywane lokalnie (Room) i synchronizowane z chmurą (Firebase Firestore). Uwierzytelnianie odbywa się przez Firebase Auth.

---

## Architektura

Projekt stosuje architekturę **MVVM (Model–View–ViewModel)** z warstwą repozytorium:

```
UI (Composable Screens)
        ↓ obserwuje StateFlow
ViewModels (logika UI)
        ↓ wywołuje metody
Repository (jedna prawda o danych)
        ↓                    ↓
Room (lokalna baza)    TMDB API + Firebase Firestore (zdalne źródła)
```

Wstrzykiwanie zależności realizuje **Hilt**.

---

## Struktura plików

```
app/src/main/java/com/project/moviechecklist/
│
├── MainActivity.kt
├── MovieChecklistApp.kt
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── MovieDao.kt
│   │   ├── MovieEntity.kt
│   │   ├── MovieStatus.kt
│   │   └── TypeConverters.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── MovieApiService.kt
│   │   └── dto/
│   │       ├── MovieDetailDto.kt
│   │       └── MovieResultDto.kt
│   └── repository/
│       ├── AuthRepository.kt
│       ├── AuthRepositoryImpl.kt
│       ├── MovieRepository.kt
│       └── MovieRepositoryImpl.kt
│
├── di/
│   ├── AppModule.kt
│   ├── AuthModule.kt
│   ├── DatabaseModule.kt
│   ├── FirebaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
│
├── ui/
│   ├── navigation/
│   │   ├── AppNavigation.kt
│   │   └── Screen.kt
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── AuthScreen.kt
│   │   │   └── AuthViewModel.kt
│   │   ├── common/
│   │   │   ├── BottomNavBar.kt
│   │   │   ├── RatingPromptDialog.kt
│   │   │   └── StarRatingInput.kt
│   │   ├── detail/
│   │   │   ├── MovieDetailScreen.kt
│   │   │   └── MovieDetailViewModel.kt
│   │   ├── planned/
│   │   │   ├── PlannedScreen.kt
│   │   │   └── PlannedViewModel.kt
│   │   ├── search/
│   │   │   ├── SearchScreen.kt
│   │   │   └── SearchViewModel.kt
│   │   └── watched/
│   │       ├── WatchedScreen.kt
│   │       └── WatchedViewModel.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── util/
│   ├── ConnectivityObserver.kt
│   ├── Constants.kt
│   └── Resource.kt
│
└── worker/
    └── ReminderWorker.kt
```

---

## Opis każdego pliku

### Główne pliki aplikacji

**`MainActivity.kt`**
Jedyna Activity w aplikacji. Przy starcie prosi o uprawnienie do powiadomień (Android 13+). Ustawia Compose jako zawartość ekranu – tworzy `NavController`, `Scaffold` z `BottomNavBar` na dole i `AppNavigation` jako treść. W metodzie `onStop()` (użytkownik wychodzi z apki) zleca `WorkManager` wysłanie powiadomienia po 15 sekundach. W `onStart()` (powrót do apki) anuluje to powiadomienie.

**`MovieChecklistApp.kt`**
Klasa `Application` z adnotacją `@HiltAndroidApp` – punkt startowy dla Hilt. Implementuje `Configuration.Provider` żeby dostarczyć `HiltWorkerFactory` do WorkManagera (wymagane dla `@HiltWorker`). Przy starcie anuluje wszystkie zakolejkowane zadania WorkManager, żeby nie było duplikatów powiadomień.

---

### `data/local/` – lokalna baza danych (Room)

**`MovieEntity.kt`**
Klasa danych reprezentująca jeden rekord w bazie. Przechowuje: `id` (TMDB), `userId` (Firebase UID), `title`, `overview`, `posterPath`, `backdropPath`, `releaseDate`, `voteAverage`, `genres` (lista stringów), `status` (PLANNED/WATCHED/null), `userRating` (1–5 lub null), `mediaType` ("movie" lub "tv"). Klucz główny to para `(id, userId)`, dzięki czemu różni użytkownicy na jednym urządzeniu mają oddzielne biblioteki. Posiada bezparametrowy konstruktor wymagany przez Firestore.

**`MovieStatus.kt`**
Enum z dwoma wartościami: `PLANNED` i `WATCHED`.

**`MovieDao.kt`**
Interfejs DAO (Data Access Object) dla Room. Udostępnia operacje:
- `insertMovie` – wstawia lub nadpisuje rekord
- `updateMovie` – aktualizuje istniejący rekord
- `deleteMovie` – usuwa rekord
- `getMovieById(id, userId)` – zwraca `Flow<MovieEntity?>` dla konkretnego filmu
- `getMoviesByStatus(status, userId)` – zwraca `Flow<List<MovieEntity>>` dla danego statusu (używane przez Watched i Planned)
- `searchLocalMovies(query, userId)` – wyszukiwanie po tytule w lokalnej bazie
- `getAllMovies(userId)` – zwraca wszystkie filmy użytkownika

**`AppDatabase.kt`**
Klasa bazy danych Room, wersja 3. Rejestruje `MovieEntity` jako tabelę i `TypeConverters` do obsługi `List<String>` (gatunki). Udostępnia `movieDao()`.

**`TypeConverters.kt`**
Konwertery Room – zamienia `List<String>` na JSON string i z powrotem (Room nie umie natywnie przechować list). Używane dla pola `genres` w `MovieEntity`.

---

### `data/remote/` – komunikacja z API TMDB

**`MovieApiService.kt`**
Interfejs Retrofit z trzema endpointami TMDB:
- `searchMulti(query, page)` – wyszukiwanie filmów i seriali jednocześnie (endpoint `/search/multi`)
- `getMovieDetails(movieId)` – szczegóły filmu (`/movie/{id}`)
- `getTvShowDetails(tvId)` – szczegóły serialu (`/tv/{id}`)

API key jest wczytywany z `BuildConfig.TMDB_API_KEY` (zdefiniowany w `local.properties`, nie commitowany do repo).

**`MovieResultDto.kt`**
DTO (Data Transfer Object) dla jednego wyniki wyszukiwania. Zawiera pola z JSON odpowiedzi TMDB: `id`, `title` (filmy), `name` (seriale), `overview`, `posterPath`, `mediaType` itd.

**`MovieDetailDto.kt`** (nie czytany osobno, ale używany)
DTO dla szczegółów konkretnego tytułu – zawiera dodatkowo `genres` (lista obiektów `{id, name}`), `backdropPath`, `voteAverage`, `firstAirDate` (seriale).

---

### `data/repository/` – warstwa repozytorium

**`MovieRepository.kt`**
Interfejs definiujący kontrakt repozytorium dla filmów. ViewModels zależą tylko od interfejsu, nie od konkretnej implementacji – ułatwia testowanie.

**`MovieRepositoryImpl.kt`**
Implementacja repozytorium. Zarządza trzema źródłami danych:
- **Room** – lokalny cache, główne źródło dla list
- **Firestore** – synchronizacja w chmurze (każdy film zapisywany pod `users/{uid}/movies/{filmId}`)
- **TMDB API** – pobieranie danych wyszukiwania i szczegółów

Kluczowe metody:
- `addMovieToLibrary` / `updateMovieInLibrary` / `deleteMovieFromLibrary` – zapis do Room i synchronizacja do Firestore
- `syncWithFirestore()` – przy logowaniu pobiera dane z Firestore i wgrywa do lokalnej bazy (i odwrotnie)
- `searchRemoteMovies(query)` – wywołuje TMDB API, filtruje wyniki (tylko "movie" i "tv", bez "person")
- `getRemoteMovieDetails(movieId, mediaType)` – pobiera szczegóły z odpowiedniego endpointu
- `mapMovieDetailDtoToEntity` / `mapMovieResultDtoToEntity` – przekształca odpowiedzi API na `MovieEntity`
- `getLibraryMoviesMap()` – zwraca `Map<Int, MovieStatus?>` używaną przez SearchScreen do oznaczania, które filmy są już w bibliotece

**`AuthRepository.kt`**
Interfejs uwierzytelniania.

**`AuthRepositoryImpl.kt`**
Implementacja z Firebase Auth. Metody:
- `login(email, password)` – loguje użytkownika, zwraca `Flow<Resource<FirebaseUser>>`
- `register(email, password)` – rejestracja, zwraca `Flow<Resource<FirebaseUser>>`
- `logout()` – wylogowanie (`FirebaseAuth.signOut()`)
- `isUserLoggedIn()` – sprawdza czy `currentUser != null`

---

### `di/` – wstrzykiwanie zależności (Hilt)

**`AppModule.kt`**
Dostarcza `ConnectivityObserver` (singleton) – obserwator stanu sieci.

**`AuthModule.kt`**
Dostarcza `AuthRepository` jako binding do `AuthRepositoryImpl`.

**`DatabaseModule.kt`**
Dostarcza `AppDatabase` i `MovieDao` jako singletony Room.

**`FirebaseModule.kt`**
Dostarcza `FirebaseAuth` i `FirebaseFirestore` jako singletony.

**`NetworkModule.kt`**
Konfiguruje i dostarcza Retrofit z OkHttp jako singleton. Ustawia base URL TMDB (`https://api.themoviedb.org/3/`), serializację Gson i interceptory (np. logowanie requestów).

**`RepositoryModule.kt`**
Dostarcza `MovieRepository` jako binding do `MovieRepositoryImpl`.

---

### `ui/navigation/` – nawigacja

**`Screen.kt`**
Sealed class definiująca wszystkie ekrany (route'y nawigacji):
- `Auth` → `"auth_screen"`
- `Watched` → `"watched_screen"`
- `Planned` → `"planned_screen"`
- `Search` → `"search_screen"`
- `MovieDetail` → `"movie_detail_screen/{movieId}/{mediaType}"` z pomocniczą funkcją `createRoute(id, type)`

**`AppNavigation.kt`**
Composable konfigurujący `NavHost` z wszystkimi composable destinations. Przy starcie sprawdza przez `AuthViewModel.isUserLoggedIn()` czy użytkownik jest zalogowany i ustawia odpowiedni `startDestination` (`Watched` albo `Auth`). Po zalogowaniu przez `AuthScreen` nawiguje do `Watched` i usuwa `Auth` ze stosu. `MovieDetail` przyjmuje argumenty `movieId: Int` i `mediaType: String`.

---

### `ui/screens/common/` – współdzielone komponenty UI

**`BottomNavBar.kt`**
Dolny pasek nawigacji z czterema pozycjami: Watched, Planned, Search, Logout. Ukrywa się na ekranie Auth. Klikając tab już aktywny lub wracając z podekranu – zawsze tworzy świeżą instancję ekranu (reset do góry listy). Logika nawigacji: dla zakładki Watched czyści stos inclusive (łącznie z samym Watched), dla pozostałych zakładek czyści stos powyżej Watched.

**`StarRatingInput.kt`**
Wiersz 5 gwiazdek do oceniania. Przyjmuje `currentRating: Int` i callback `onRatingChange`. Wypełnione gwiazdki = ocena, puste = reszta.

**`RatingPromptDialog.kt`**
Dialog oceniania używany w dwóch miejscach: po oznaczeniu jako obejrzane z ekranu szczegółów ORAZ z listy Planned. Zawiera tytuł "Rate this Movie/Series", komponent `StarRatingInput`, przycisk "Confirm" (zapisuje wybraną ocenę lub null jeśli 0) i "Skip Rating" (zapisuje null).

---

### `ui/screens/auth/`

**`AuthScreen.kt`**
Ekran logowania/rejestracji. Zawiera pola email i hasło, toggle między trybem logowania a rejestracji, przycisk akcji i komunikaty błędów. Obserwuje `authState` z ViewModelu – po sukcesie wywołuje `onAuthSuccess()` przekazywany z `AppNavigation`.

**`AuthViewModel.kt`**
ViewModel dla Auth. Przechowuje `authState: StateFlow<Resource<FirebaseUser?>>`. Metody: `login`, `register`, `logout`, `isUserLoggedIn`, `resetAuthState`.

---

### `ui/screens/watched/`

**`WatchedScreen.kt`**
Lista wszystkich filmów/seriali oznaczonych jako obejrzane. Każdy element (WatchedMovieItem) wyświetla poster, tytuł, datę premiery, typ, ocenę gwiazdkami i dwa przyciski: gwiazdka (przejście do szczegółów do zmiany oceny) i lista (przeniesienie z powrotem do Planned). Przy pustej liście wyświetla komunikat.

**`WatchedViewModel.kt`**
Pobiera `watchedMovies` jako `StateFlow<List<MovieEntity>>` z repozytorium. Metody:
- `updateUserRating(movie, rating)` – aktualizuje ocenę
- `moveToPlanned(movie)` – zmienia status na PLANNED i zeruje ocenę

---

### `ui/screens/planned/`

**`PlannedScreen.kt`**
Lista filmów/seriali zaplanowanych do obejrzenia. Każdy element (PlannedMovieItem) ma dwa przyciski akcji: ptaszek (oznacz jako obejrzany – otwiera `RatingPromptDialog`) i kosz (usuń z listy). Kliknięcie w kartę przechodzi do szczegółów. Przy pustej liście wyświetla instrukcję.

**`PlannedViewModel.kt`**
Pobiera `plannedMovies` jako `StateFlow`. Metody:
- `moveToWatched(movie, rating)` – zmienia status na WATCHED i zapisuje ocenę (opcjonalną)
- `removeFromPlanned(movie)` – usuwa film z bazy

---

### `ui/screens/search/`

**`SearchScreen.kt`**
Ekran wyszukiwania online przez TMDB. Zawiera pole tekstowe z przyciskiem czyszczenia, filtry chipów (Wszystkie / Filmy / Seriale) i listę wyników. Każdy wynik (SearchResultItem) pokazuje poster, tytuł, rok, typ i przycisk dodania – lub ikonę statusu jeśli film jest już w bibliotece (zielony ptaszek = obejrzane, zakładka = zaplanowane). Wyszukiwanie startuje po wpisaniu minimum 3 znaków, z debounce 500ms.

**`SearchViewModel.kt`**
Zarządza stanem wyszukiwania:
- `searchQuery` – bieżące zapytanie
- `searchResults: StateFlow<Resource<SearchResponseDto>>` – wyniki z API
- `mediaTypeFilter` – aktywny filtr
- `libraryMoviesMap: StateFlow<Map<Int, MovieStatus?>>` – mapa wszystkich filmów w bibliotece (do oznaczania statusu w wynikach)
- `onSearchQueryChanged` – debounce 500ms przed wywołaniem API
- `addMovieToPlanned(movie)` – dodaje film do biblioteki, a następnie asynchronicznie pobiera pełne szczegóły (z gatunkami) i nadpisuje rekord

---

### `ui/screens/detail/`

**`MovieDetailScreen.kt`**
Ekran szczegółów konkretnego tytułu. Wyświetla backdrop/poster (w jakości HD jeśli WiFi aktywne lub SD na komórkowym), tytuł, typ, datę premiery, ocenę TMDB, gatunki, opis i aktualny status w bibliotece. Jeśli film jest obejrzany – pokazuje `StarRatingInput` do zmiany oceny. Zawiera przyciski akcji (`AddOrChangeStatusButtons`):
- Film nie w bibliotece → "Add to Planned List"
- Film zaplanowany → "Mark as Watched & Rate" (otwiera `RatingPromptDialog`)
- Film obejrzany → "Move to Planned"

Zawiera przycisk Share (systemowy sharesheet z linkiem TMDB) i ikonę WiFi/HD w nagłówku.

**`MovieDetailViewModel.kt`**
Odbiera `movieId` i `mediaType` z `SavedStateHandle`. Strategia ładowania danych:
1. Subskrybuje lokalną bazę (`getMovieFromLibrary`)
2. Jeśli film jest w bazie ale brakuje gatunków → pobiera szczegóły z TMDB i scala z lokalnym statusem/oceną
3. Jeśli film nie jest w bazie → pobiera wyłącznie z TMDB (stan tymczasowy, niepersistowany)

Obserwuje `isWifiConnected` z `ConnectivityObserver` do przełączania jakości obrazów. Metody: `addCurrentMovieToPlanned`, `markAsWatched(rating)`, `markAsPlanned`, `updateRating(newRating)`.

---

### `util/`

**`Resource.kt`**
Sealed class opakowująca stany asynchronicznych operacji:
- `Loading` – trwa ładowanie
- `Success(data)` – sukces z danymi
- `Error(message, data?, code?)` – błąd, opcjonalnie z częściowymi danymi i kodem HTTP

**`ConnectivityObserver.kt`**
Interfejs `ConnectivityObserver` i jego implementacja `NetworkConnectivityObserver`. Używa `ConnectivityManager` do:
- `observe()` – emituje statusy sieci (Available/Unavailable/Losing/Lost)
- `observeWifiStatus()` – emituje `Boolean` (true = połączono przez WiFi)
- `isWifiConnected()` – synchroniczne sprawdzenie bieżącego stanu WiFi

**`Constants.kt`**
Stałe aplikacji: URL-e do obrazków TMDB w różnych rozdzielczościach (`TMDB_IMAGE_BASE_URL` dla standardowej jakości, `TMDB_IMAGE_BASE_URL_HIGH` i `TMDB_IMAGE_BASE_URL_LOW` dla przełączania wg WiFi).

---

### `worker/`

**`ReminderWorker.kt`**
Worker WorkManager z adnotacją `@HiltWorker` (obsługuje Hilt w workerach). Przy uruchomieniu pobiera losowy film z listy Planned i wyświetla powiadomienie systemowe "Time for a movie! Why not watch '{tytuł}' today?". Kliknięcie powiadomienia otwiera `MainActivity`. Uruchamiany przez `MainActivity.onStop()` z opóźnieniem 15 sekund, anulowany w `onStart()`.

---

## Flow aplikacji

### 1. Start i uwierzytelnianie

```
App start
    → MainActivity.onCreate()
    → checkNotificationPermission() (prosi o uprawnienie na Android 13+)
    → setContent() – tworzy NavController i Scaffold
    → AppNavigation sprawdza isUserLoggedIn()
         ├── TAK → startDestination = WatchedScreen
         └── NIE → startDestination = AuthScreen
```

Na `AuthScreen` użytkownik wpisuje email i hasło. `AuthViewModel` wywołuje `AuthRepositoryImpl.login()` / `register()` przez Firebase Auth. Po sukcesie `AppNavigation` nawiguje do `WatchedScreen` i usuwa `AuthScreen` ze stosu nawigacji.

---

### 2. Przeglądanie listy Watched

```
WatchedScreen
    ← WatchedViewModel.watchedMovies (StateFlow z Room)
    → wyświetla listę MovieEntity ze statusem WATCHED

Kliknięcie karty:
    → navController.navigate(MovieDetail)

Kliknięcie przycisku gwiazdki:
    → navController.navigate(MovieDetail) (do zmiany oceny)

Kliknięcie przycisku listy (przenieś do Planned):
    → WatchedViewModel.moveToPlanned(movie)
    → Room: status = PLANNED, userRating = null
    → Firestore sync
    → film znika z listy Watched, pojawia się w Planned
```

---

### 3. Przeglądanie listy Planned

```
PlannedScreen
    ← PlannedViewModel.plannedMovies (StateFlow z Room)
    → wyświetla listę MovieEntity ze statusem PLANNED

Kliknięcie karty:
    → navController.navigate(MovieDetail)

Kliknięcie przycisku kosza:
    → PlannedViewModel.removeFromPlanned(movie)
    → Room: DELETE, Firestore: DELETE

Kliknięcie przycisku ptaszka (oznacz jako obejrzane):
    → otwiera RatingPromptDialog
    → użytkownik ocenia (1-5) lub klika Skip Rating
    → PlannedViewModel.moveToWatched(movie, rating?)
    → Room: status = WATCHED, userRating = rating
    → Firestore sync
    → film przenosi się z Planned do Watched
```

---

### 4. Wyszukiwanie filmów

```
SearchScreen – użytkownik wpisuje zapytanie (min. 3 znaki)
    → debounce 500ms
    → SearchViewModel.performRemoteSearch()
    → MovieRepositoryImpl.searchRemoteMovies() → TMDB API /search/multi
    → filtrowanie: tylko mediaType == "movie" lub "tv"
    → opcjonalne filtrowanie chipem (Filmy / Seriale)
    → wyświetla wyniki z oznaczeniem statusu (libraryMoviesMap)

Kliknięcie "+" (dodaj do Planned):
    → SearchViewModel.addMovieToPlanned(movie)
    → mapMovieResultDtoToEntity() → MovieEntity(status=PLANNED)
    → Room: INSERT
    → Firestore sync
    → asynchronicznie: fetchAndStoreFullDetails() – pobiera pełne szczegóły
      (z gatunkami) i nadpisuje rekord w bazie
    → ikona przy filmie zmienia się na zakładkę (PLANNED)
```

---

### 5. Szczegóły filmu

```
MovieDetailScreen (movieId, mediaType)
    → MovieDetailViewModel.loadMovieDetails()

Strategia ładowania:
    1. Nasłuchuje Room (getMovieFromLibrary)
       ├── Film w bazie + ma gatunki → Resource.Success (dane lokalne)
       ├── Film w bazie + brak gatunków → fetchRemoteAndMerge()
       │       → pobiera z TMDB, scala z lokalnym statusem i oceną
       │       → Resource.Success (dane wzbogacone)
       └── Brak w bazie → fetchRemoteDetailsOnly()
               → pobiera z TMDB, tworzy tymczasowy MovieEntity (status=null)
               → Resource.Success (dane tymczasowe)

Akcje użytkownika:
    [Film nie w bibliotece]
    "Add to Planned List" → addCurrentMovieToPlanned() → Room INSERT

    [Film zaplanowany]
    "Mark as Watched & Rate" → otwiera RatingPromptDialog
        → markAsWatched(rating?) → Room UPDATE (status=WATCHED, userRating=rating)

    [Film obejrzany]
    StarRatingInput → updateRating(newRating) → Room UPDATE
    "Move to Planned" → markAsPlanned() → Room UPDATE (status=PLANNED, userRating=null)

    [Każdy film]
    Share → systemowy sharesheet z tytułem, opisem i linkiem TMDB
    WiFi aktywne → obrazy w jakości HD (w3.tmdb.org/original)
    WiFi nieaktywne → obrazy w jakości SD (w3.tmdb.org/w185)
```

---

### 6. Powiadomienia

```
Użytkownik wychodzi z apki (onStop)
    → WorkManager.enqueue(ReminderWorker, delay=15s)

Użytkownik wraca do apki (onStart)
    → WorkManager.cancelAllWork()

ReminderWorker.doWork():
    → pobiera listę Planned z Room dla bieżącego userId
    → jeśli lista niepusta: losuje jeden film
    → wyświetla powiadomienie "Time for a movie! Why not watch '{tytuł}' today?"
    → kliknięcie powiadomienia → otwiera MainActivity
```

---

### 7. Nawigacja dolnym paskiem (BottomNavBar)

Kliknięcie zakładki zawsze resetuje ekran do stanu początkowego (góra listy):
- Zakładka **Watched**: czyści cały stos nawigacji łącznie z Watched i tworzy nową instancję
- Zakładki **Planned / Search**: czyści stos powyżej Watched i tworzy nową instancję wybranego ekranu
- Zakładka **Logout**: wylogowuje przez `AuthViewModel.logout()`, czyści cały stos i nawiguje do Auth

---

## Zewnętrzne technologie i biblioteki

| Biblioteka | Zastosowanie |
|---|---|
| **Jetpack Compose** | Cały interfejs użytkownika |
| **Navigation Compose** | Nawigacja między ekranami |
| **Hilt** | Wstrzykiwanie zależności |
| **Room** | Lokalna baza danych SQLite |
| **Retrofit + Gson** | Komunikacja z TMDB API |
| **Firebase Auth** | Uwierzytelnianie użytkowników |
| **Firebase Firestore** | Synchronizacja danych w chmurze |
| **WorkManager** | Planowanie powiadomień |
| **Coil** | Ładowanie i cachowanie obrazków |
| **Kotlin Coroutines + Flow** | Asynchroniczność i reaktywne strumienie danych |
| **StateFlow** | Reaktywny stan UI w ViewModelach |
