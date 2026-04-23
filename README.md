# Poke CODE.ID

A production-grade Pokémon browser for Android built with modern Jetpack libraries, Clean Architecture, and Jetpack Compose. The app lets users browse, search, and inspect Pokémon pulled from [PokéAPI](https://pokeapi.co/), with local-only authentication and offline-capable paging.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Full Tech Stack](#3-full-tech-stack)
4. [Module & Layer Breakdown](#4-module--layer-breakdown)
5. [Screens & UI — Widget-Level Detail](#5-screens--ui--widget-level-detail)
6. [Data Flow Diagrams](#6-data-flow-diagrams)
7. [Database Schema](#7-database-schema)
8. [API Integration](#8-api-integration)
9. [Authentication & Security](#9-authentication--security)
10. [Dependency Injection](#10-dependency-injection)
11. [Navigation](#11-navigation)
12. [State Management](#12-state-management)
13. [Paging Strategy](#13-paging-strategy)
14. [Error Handling](#14-error-handling)
15. [Theme & Design System](#15-theme--design-system)
16. [Build Configuration](#16-build-configuration)
17. [Testing](#17-testing)
18. [Known Limitations & Trade-offs](#18-known-limitations--trade-offs)
19. [Future Improvements & Roadmap](#19-future-improvements--roadmap)
20. [Tech Stack Alternatives Analysis](#20-tech-stack-alternatives-analysis)

---

## 1. Project Overview

| Property | Value |
|---|---|
| App Name | Poke CODE.ID |
| Package | `code.id.poke` |
| Min SDK | 26 (Android 8.0 Oreo) |
| Target / Compile SDK | 36 |
| Language | Kotlin 2.2.10 |
| JVM Target | Java 11 |
| Architecture | MVVM + Clean Architecture |
| UI Toolkit | Jetpack Compose |
| Data Source | [PokéAPI v2](https://pokeapi.co/api/v2/) |

The app provides:
- Paginated list of all Pokémon (900+) with offline-first caching
- Detailed view per Pokémon: image, abilities, height, weight
- Full-text search backed by Room FTS4 with an API fallback
- Local-only user authentication (register / login / logout)
- Type-safe navigation with `@Serializable` route objects

---

## 2. Architecture

### Pattern: MVVM + Clean Architecture

The codebase is split into three strict layers that enforce a unidirectional dependency rule: **UI → Domain ← Data**.

```
┌─────────────────────────────────────────────────┐
│                    UI Layer                      │
│  Compose Screens  ←  ViewModels  ←  UseCases    │
└───────────────────────────┬─────────────────────┘
                            │ depends on (interfaces)
┌───────────────────────────▼─────────────────────┐
│                  Domain Layer                    │
│  Repository Interfaces  |  Domain Models         │
│  AppError sealed class  |  Use Cases             │
└───────────────────────────┬─────────────────────┘
                            │ implements
┌───────────────────────────▼─────────────────────┐
│                   Data Layer                     │
│  PokeRepositoryImpl  |  UserRepositoryImpl        │
│  Room DAOs / Entities  |  Retrofit Service       │
│  RemoteMediator  |  Mappers  |  SessionManager   │
└─────────────────────────────────────────────────┘
```

**Why Clean Architecture?**

- **Testability**: Domain and data layers can be tested independently, without Android instrumentation.
- **Replaceability**: Swapping Retrofit for GraphQL, or Room for SQLite, touches only the Data layer.
- **Scalability**: Adding a new feature means adding a use case and repository method — not tangling business logic into a ViewModel.

**Why MVVM?**

- First-class support in the Android Architecture Components (`ViewModel`, `StateFlow`).
- Compose observes `StateFlow` natively — no `LiveData` conversion boilerplate.
- Google's recommended pattern for Compose apps.

**Trade-offs:**

| Advantage | Cost |
|---|---|
| Clear separation of concerns | More files / indirection for simple features |
| Easy to unit-test domain rules | Mapper classes required at every boundary |
| Data layer is replaceable | Initial setup (interfaces, modules, mappers) is verbose |

---

## 3. Full Tech Stack

### Language & Runtime

| Technology | Version | Purpose |
|---|---|---|
| **Kotlin** | 2.2.10 | Primary language — null-safety, coroutines, sealed classes |
| **KSP** (Kotlin Symbol Processing) | 2.2.10-2.0.2 | Compile-time annotation processing for Room, Moshi |
| **Kotlin Serialization** | 1.8.0 | Serializing route objects for type-safe navigation |
| **Kotlin Coroutines** | 1.10.1 | Async/non-blocking IO, `Flow`, `StateFlow` |

### UI

| Technology | Version | Purpose |
|---|---|---|
| **Jetpack Compose BOM** | 2025.12.00 | Bill of materials — ensures all Compose libraries are version-compatible |
| **Compose UI** | (BOM-managed) | Declarative UI toolkit |
| **Compose Foundation** | (BOM-managed) | `LazyColumn`, `LazyRow`, gesture handling |
| **Compose Material3** | (BOM-managed) | Material Design 3 components (Cards, TopAppBar, etc.) |
| **Navigation Compose** | 2.9.7 | Type-safe, back-stack-aware screen navigation |
| **Coil** | 3.4.0 | Async image loading with OkHttp integration |
| **Activity Compose** | (BOM-managed) | `setContent {}` Compose entry point |
| **Material3 Adaptive Navigation Suite** | (BOM-managed) | `NavigationSuiteScaffold` — auto-adapts bottom nav / side nav |

### Networking

| Technology | Version | Purpose |
|---|---|---|
| **Retrofit** | 2.11.0 | HTTP client with coroutine suspend support |
| **Moshi** | 1.15.2 | JSON serialization/deserialization with KSP codegen |
| **OkHttp** | 5.3.2 | HTTP transport layer, connection pooling, timeout config |
| **OkHttp Logging Interceptor** | 5.3.2 | Body-level request/response logging in debug builds |

### Persistence

| Technology | Version | Purpose |
|---|---|---|
| **Room** | 2.8.4 | SQLite ORM with KSP annotation processing |
| **Room Paging** | 2.8.4 | First-class Paging 3 `PagingSource` from Room queries |
| **SharedPreferences** | (platform) | Lightweight key-value store for session tokens |

### Pagination

| Technology | Version | Purpose |
|---|---|---|
| **Paging 3 Runtime** | 3.3.6 | Paged data loading with `PagingSource`, `PagingData` |
| **Paging 3 Compose** | 3.3.6 | `collectAsLazyPagingItems()` for Compose integration |

### Dependency Injection

| Technology | Version | Purpose |
|---|---|---|
| **Koin Android** | 4.0.2 | Service locator / DI for Android components |
| **Koin Compose** | 4.0.2 | `koinViewModel()` injection inside composables |

### Build System

| Technology | Version | Purpose |
|---|---|---|
| **Android Gradle Plugin** | 9.1.0 | Android build toolchain |
| **Gradle Version Catalog** | (toml) | Centralized dependency version management |

---

## 4. Module & Layer Breakdown

### `PokeApplication.kt`

Entry point of the app's DI container. Extends `Application` and calls `startKoin {}` with three Koin modules.

```
PokeApplication
  └── startKoin {
        androidContext(this)
        modules(NetworkModule, DatabaseModule, AppModule)
      }
```

**Why a custom Application class?** Koin (and most DI frameworks) require a single initialization point that outlives any Activity. `Application.onCreate()` is the canonical place for this.

---

### `di/` — Dependency Injection Modules

#### `NetworkModule.kt`

Provides all network-related singletons in a specific construction order:

1. `Moshi` — JSON adapter with `KotlinJsonAdapterFactory` (reflectionless codegen via KSP)
2. `OkHttpClient` — configured with:
   - 30-second connect / read / write timeouts
   - `HttpLoggingInterceptor` at `BODY` level (logs full request and response bodies)
   - `retryOnConnectionFailure(true)`
3. `Retrofit` — base URL `https://pokeapi.co/api/v2/`, `MoshiConverterFactory`
4. `PokeService` — Retrofit-generated implementation

**Certificate Pinning Note:** The code contains a commented-out `CertificatePinner` block. If enabled, it would pin the TLS certificate for `pokeapi.co`, preventing MITM attacks even from trusted CAs. Currently disabled to avoid breaking on certificate rotation.

#### `DatabaseModule.kt`

Provides:
1. `AppDatabase` — `Room.databaseBuilder("poke_database")` with `fallbackToDestructiveMigration`
2. `PokemonDao`, `UserDao`, `RemoteKeyDao` — extracted from the database instance

**`fallbackToDestructiveMigration` implication:** If the schema version increases without a migration script, Room will drop and recreate all tables. This is acceptable in development (and for a free public API) but would be unacceptable if the database held user-generated content that cannot be re-fetched.

#### `AppModule.kt`

Provides all application-level singletons:

| Provided | Type | Depends On |
|---|---|---|
| `SessionManager` | `single` | `androidContext()` |
| `PokeRepositoryImpl` | `single<PokeRepository>` | `PokeService`, `AppDatabase`, DAOs |
| `UserRepositoryImpl` | `single<UserRepository>` | `UserDao` |
| `GetPokemonListUseCase` | `single` | `PokeRepository` |
| `GetPokemonDetailUseCase` | `single` | `PokeRepository` |
| `SearchPokemonUseCase` | `single` | `PokeRepository` |
| `LoginUseCase` | `single` | `UserRepository` |
| `RegisterUseCase` | `single` | `UserRepository` |
| `PokemonListViewModel` | `viewModel` | `GetPokemonListUseCase`, `SearchPokemonUseCase` |
| `PokemonDetailViewModel` | `viewModel` | `GetPokemonDetailUseCase` |
| `AuthViewModel` | `viewModel` | `LoginUseCase`, `RegisterUseCase`, `SessionManager` |
| `MainViewModel` | `viewModel` | `SessionManager` |

---

### `domain/` — Business Logic Layer

This layer has **zero Android framework imports**. It defines contracts and models that both UI and Data layers depend on.

#### `domain/model/`

| Model | Fields | Purpose |
|---|---|---|
| `Pokemon` | `id: Int, name: String, imageUrl: String` | Lightweight list item |
| `PokemonDetail` | `id, name, height, weight, abilities: List<String>, imageUrl` | Full detail |
| `User` | `id, name, email` | Authenticated user (no password) |
| `UserCredentials` | `passwordHash: String, salt: String` | Password verification payload |

#### `domain/repository/`

```kotlin
interface PokeRepository {
    fun getPokemonPager(): Flow<PagingData<Pokemon>>
    suspend fun getPokemonDetail(name: String): Result<PokemonDetail>
    suspend fun searchPokemon(query: String): Result<List<Pokemon>>
}

interface UserRepository {
    suspend fun register(name: String, email: String, passwordHash: String, salt: String): Result<Unit>
    suspend fun getUserCredentials(email: String): Result<UserCredentials>
    suspend fun getUserByEmail(email: String): Result<User>
}
```

**Why interfaces?** The UI layer never imports `PokeRepositoryImpl`. This enables mocking in tests and swapping implementations without touching ViewModels or UseCases.

#### `domain/usecase/`

| Use Case | Input | Output | Logic |
|---|---|---|---|
| `GetPokemonListUseCase` | — | `Flow<PagingData<Pokemon>>` | Delegates to `repo.getPokemonPager()` |
| `GetPokemonDetailUseCase` | `name: String` | `Result<PokemonDetail>` | Delegates to `repo.getPokemonDetail()` |
| `SearchPokemonUseCase` | `query: String` | `Result<List<Pokemon>>` | Delegates to `repo.searchPokemon()` |
| `LoginUseCase` | `email, password` | `Result<User>` | Fetches credentials, verifies hash, returns user |
| `RegisterUseCase` | `name, email, password` | `Result<Unit>` | Generates salt, hashes password, persists user |

**Trade-off:** In this project the use cases are thin pass-throughs. In a larger app they would contain meaningful business rules (e.g., rate limiting retries, combining multiple repository calls). Some developers argue thin use cases add unnecessary indirection — the counter-argument is that they establish the seam for future logic without refactoring ViewModels.

#### `domain/error/AppError.kt`

```kotlin
sealed class AppError : Exception() {
    data class NetworkError(val statusCode: Int) : AppError()
    object ServerError : AppError()
    object ClientError : AppError()
    object TimeoutError : AppError()
    object ParseError : AppError()
    object DatabaseError : AppError()
    object UnknownError : AppError()
}
```

Every repository method returns `Result<T>` wrapping either a domain model or an `AppError` subtype. The UI layer switches on the error type to show contextually appropriate messages (e.g., "Check your connection" for `TimeoutError` vs. "Something went wrong" for `UnknownError`).

---

### `data/` — Implementation Layer

#### `data/remote/PokeService.kt`

Retrofit interface with two suspend functions:

```kotlin
@GET("pokemon")
suspend fun getPokemonList(
    @Query("limit") limit: Int,
    @Query("offset") offset: Int
): PokemonListResponse

@GET("pokemon/{name}")
suspend fun getPokemonDetail(
    @Path("name") name: String
): PokemonDetailResponse
```

**Response DTOs (nested inside the file):**

```
PokemonListResponse
  └── results: List<PokemonResult>
        ├── name: String
        └── url: String (e.g. "https://pokeapi.co/api/v2/pokemon/1/")

PokemonDetailResponse
  ├── id: Int
  ├── name: String
  ├── height: Int      (in decimetres — divide by 10 for metres)
  ├── weight: Int      (in hectograms — divide by 10 for kilograms)
  ├── abilities: List<AbilitySlot>
  │     └── ability: AbilityName { name: String }
  └── sprites: Sprites
        ├── front_default: String?
        └── other: OtherSprites
              └── official_artwork: OfficialArtwork { front_default: String? }
```

**Image URL selection logic:** Prefers `sprites.other.official_artwork.front_default` (high-res artwork). Falls back to `sprites.front_default` (in-game sprite). Falls back to `""` (blank, Coil shows placeholder).

#### `data/remote/ApiCall.kt`

`safeApiCall()` extension / helper function. Wraps a suspend lambda in a `try/catch` and maps exceptions:

| Exception | Mapped AppError |
|---|---|
| `HttpException(4xx)` | `ClientError` |
| `HttpException(5xx)` | `ServerError` |
| `SocketTimeoutException` | `TimeoutError` |
| `IOException` | `NetworkError(0)` |
| `JsonDataException` | `ParseError` |
| Anything else | `UnknownError` |

Returns `Result.success(value)` or `Result.failure(appError)`.

#### `data/remote/PokemonRemoteMediator.kt`

The backbone of offline-first paging. Implements `RemoteMediator<Int, PokemonEntity>`.

**`load()` method logic:**

```
LoadType.REFRESH:
  → delete all remote_keys rows
  → delete all pokemon rows
  → fetch page 0 from API
  → insert remote_keys (prevKey=null, nextKey=pageSize)
  → insert pokemon entities
  → return MediatorResult.Success(endOfPaginationReached = results.isEmpty())

LoadType.APPEND:
  → get last remote_key from DB
  → if nextKey == null → endOfPaginationReached = true
  → fetch API at offset = nextKey
  → insert new keys + pokemon
  → return success

LoadType.PREPEND:
  → always return MediatorResult.Success(endOfPaginationReached = true)
  (PokéAPI is append-only pagination; prepend is not supported)
```

All DB operations are wrapped in a `withTransaction {}` block to ensure atomicity — either all inserts succeed or none are committed.

**Image URL construction in mediator:**

```kotlin
val id = result.url.trimEnd('/').split('/').last().toInt()
val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
```

This avoids an extra network call per Pokémon for the list image — sprites are hosted on GitHub and the ID is derivable from the list API URL.

#### `data/local/AppDatabase.kt`

```kotlin
@Database(
    entities = [PokemonEntity::class, PokemonFtsEntity::class, UserEntity::class, RemoteKeyEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase()
```

Version 6 with `fallbackToDestructiveMigration()` — schema changes nuke all local data and re-fetch from API.

#### `data/local/` — Entities

**`PokemonEntity`**

```kotlin
@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val height: Int = 0,
    val weight: Int = 0
)
```

**`PokemonFtsEntity`**

```kotlin
@Entity(tableName = "pokemon_fts")
@Fts4(contentEntity = PokemonEntity::class)
data class PokemonFtsEntity(
    val name: String
)
```

FTS4 (Full-Text Search version 4) creates a virtual table that enables `MATCH` queries — orders of magnitude faster than `LIKE '%query%'` on large datasets. The `contentEntity` links it to the main `pokemon` table so Room auto-manages inserts/deletes.

**`UserEntity`**

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String
)
```

Passwords are **never stored in plaintext**. The entity stores SHA-256 HMAC output and the random salt used to produce it.

**`RemoteKeyEntity`**

```kotlin
@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val pokemonId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
```

Stores the pagination cursor for each loaded Pokémon, enabling the `RemoteMediator` to resume from any position.

#### `data/local/` — DAOs

**`PokemonDao`**

```kotlin
@Query("SELECT * FROM pokemon WHERE name LIKE '%' || :query || '%' ORDER BY id ASC")
fun searchPokemon(query: String): PagingSource<Int, PokemonEntity>

@Query("SELECT pokemon.* FROM pokemon JOIN pokemon_fts ON pokemon.name = pokemon_fts.name WHERE pokemon_fts MATCH :query")
suspend fun searchByFts(query: String): List<PokemonEntity>

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertAll(pokemon: List<PokemonEntity>)

@Query("DELETE FROM pokemon")
suspend fun clearAll()
```

**`UserDao`**

```kotlin
@Insert(onConflict = OnConflictStrategy.ABORT)
suspend fun insertUser(user: UserEntity)

@Query("SELECT * FROM users WHERE email = :email LIMIT 1")
suspend fun getUserByEmail(email: String): UserEntity?

@Query("SELECT passwordHash, salt FROM users WHERE email = :email LIMIT 1")
suspend fun getUserCredentials(email: String): UserCredentials?
```

**`RemoteKeyDao`**

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertAll(keys: List<RemoteKeyEntity>)

@Query("SELECT * FROM remote_keys WHERE pokemonId = :pokemonId")
suspend fun getRemoteKey(pokemonId: Int): RemoteKeyEntity?

@Query("DELETE FROM remote_keys")
suspend fun clearAll()
```

#### `data/mapper/`

**`PokemonMapper.kt`**

```
PokemonEntity      →  Pokemon (domain)
PokemonDetailResponse → PokemonDetail (domain)
  (decimetres → metres, hectograms → kg, ability list flattening)
```

**`UserMapper.kt`**

```
UserEntity → User (domain)
UserCredentialRow → UserCredentials (domain)
```

Mappers prevent domain models from ever being coupled to Room annotations or Retrofit `@Json` annotations.

---

### `util/PasswordHasher.kt`

```kotlin
object PasswordHasher {
    fun generateSalt(): String = UUID.randomUUID().toString()

    fun hash(password: String, salt: String): String {
        val input = salt + password
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash
}
```

**Security note:** SHA-256 with a random salt is far better than unsalted SHA-256 (prevents rainbow table attacks) but is still a general-purpose hash function — it is fast by design, which is bad for passwords. `bcrypt`, `scrypt`, or `argon2` are purpose-built password hashing algorithms that are intentionally slow (configurable cost factor). For a production app with a real backend, `argon2` would be the recommendation.

---

## 5. Screens & UI — Widget-Level Detail

### `LoginScreen.kt`

**Purpose:** Authenticate an existing user with email + password.

**State:** Driven by `AuthViewModel.authState: StateFlow<AuthState>`

```
LoginScreen
└── Box (fillMaxSize, background = MaterialTheme.colorScheme.background)
    └── Column (verticalArrangement = Center, horizontalAlignment = CenterHorizontally, padding 32dp)
        ├── Text("Poke CODE.ID") — headlineLarge, bold, color = primary
        ├── Spacer(16dp)
        ├── OutlinedTextField (email)
        │     label = "Email"
        │     leadingIcon = Icons.Default.Email
        │     keyboardType = Email
        │     imeAction = Next
        │     singleLine = true
        ├── Spacer(8dp)
        ├── OutlinedTextField (password)
        │     label = "Password"
        │     leadingIcon = Icons.Default.Lock
        │     visualTransformation = PasswordVisualTransformation OR None (toggle)
        │     trailingIcon = IconButton(visibility toggle)
        │       → Icons.Default.Visibility / VisibilityOff
        │     keyboardType = Password
        │     imeAction = Done
        │     singleLine = true
        ├── Spacer(8dp)
        ├── [if AuthState.Error] Text(errorMessage) — color = MaterialTheme.colorScheme.error
        ├── Spacer(16dp)
        ├── Button(onClick = loginAction, enabled = email.isNotBlank() && password.isNotBlank(), fillMaxWidth)
        │     [if AuthState.Loading] CircularProgressIndicator(size=20dp, color=onPrimary, strokeWidth=2dp)
        │     [else] Text("Login")
        └── TextButton(onClick = navigateToRegister)
              Text("Don't have an account? Register Now")
```

**UX highlights:**
- Login button is disabled while either field is blank, preventing empty-credential API calls.
- Password field shows a visibility toggle so users can verify what they typed.
- Error text appears inline (no Toast/Snackbar) so it persists until the user corrects their input.
- `CircularProgressIndicator` replaces button label during loading — prevents double-tap submission.

**Navigation triggers:**
- `AuthState.Success` → `navController.navigate(MainRoute) { popUpTo(LoginRoute) { inclusive = true } }`
- `TextButton` → `navController.navigate(RegisterRoute)`

---

### `RegisterScreen.kt`

**Purpose:** Create a new local account.

**State:** Driven by `AuthViewModel.authState: StateFlow<AuthState>`

**Validation (client-side, in ViewModel):**
- Name must not be blank
- Email must match `android.util.Patterns.EMAIL_ADDRESS`
- Password must be ≥ 6 characters

```
RegisterScreen
└── Box (fillMaxSize)
    └── Column (verticalArrangement = Center, horizontalAlignment = CenterHorizontally, padding 32dp)
        ├── Text("Create Account") — headlineLarge, bold
        ├── Spacer(16dp)
        ├── OutlinedTextField (name)
        │     label = "Full Name"
        │     leadingIcon = Icons.Default.Person
        │     imeAction = Next
        ├── Spacer(8dp)
        ├── OutlinedTextField (email)
        │     [same as LoginScreen]
        ├── Spacer(8dp)
        ├── OutlinedTextField (password)
        │     [same as LoginScreen with visibility toggle]
        ├── Spacer(8dp)
        ├── [if AuthState.Error] Text(errorMessage)
        ├── Spacer(16dp)
        ├── Button(onClick = registerAction, enabled = fieldsNotBlank, fillMaxWidth)
        │     [if loading] CircularProgressIndicator
        │     [else] Text("Register")
        └── TextButton(onClick = navigateToLogin)
              Text("Already have an account? Login Now")
```

**Navigation triggers:**
- `AuthState.Registered` → `navController.navigate(LoginRoute) { popUpTo(RegisterRoute) { inclusive = true } }`
- On success, user is shown the login screen (not auto-logged in) — intentional design choice to keep flows separate.

---

### `ProfileScreen.kt`

**Purpose:** Show authenticated user's information and provide a logout action.

**Data source:** `SessionManager` (SharedPreferences), not a LiveDB query.

```
ProfileScreen
└── Column (fillMaxSize, verticalArrangement = Center, horizontalAlignment = CenterHorizontally, padding 16dp)
    ├── Text("Profile") — headlineLarge, bold
    ├── Spacer(24dp)
    ├── Card(modifier = fillMaxWidth, elevation = CardDefaults.cardElevation(4dp))
    │     └── Column (padding 16dp)
    │           ├── UserInfoField("User ID", userId.toString())
    │           ├── Divider
    │           ├── UserInfoField("Name", userName)
    │           ├── Divider
    │           └── UserInfoField("Email", userEmail)
    ├── Spacer(24dp)
    └── Button(
          onClick = { authViewModel.logout(); navController.navigate(LoginRoute) { popUpTo(MainRoute) { inclusive = true } } }
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        )
          Row {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            Spacer(8dp)
            Text("Logout")
          }
```

**`UserInfoField` composable:**
```
UserInfoField(label: String, value: String)
└── Column (padding vertical 8dp)
    ├── Text(label) — labelSmall, color = onSurfaceVariant
    └── Text(value) — bodyLarge, fontWeight = Medium
```

**UX note:** The logout button is styled in `error` color (red) to signal a destructive action. The `ExitToApp` icon provides an affordance even for users who don't read the label.

---

### `PokemonListScreen.kt`

**Purpose:** Infinite-scroll paginated list of all Pokémon with pull-to-refresh and a search trigger.

**State:**
- `LazyPagingItems<Pokemon>` from `PokemonListViewModel.pokemonPager.collectAsLazyPagingItems()`
- `Boolean` flag `showSearch` for the search dialog overlay

```
PokemonListScreen
└── Scaffold
    ├── topBar = CenterAlignedTopAppBar
    │     title = Text("Pokedex") — headlineSmall
    │     actions = [
    │       IconButton(onClick = { showSearch = true })
    │         Icon(Icons.Default.Search, contentDescription = "Search")
    │     ]
    │
    └── content = Box (fillMaxSize)
        ├── PullToRefreshBox(
        │     isRefreshing = loadState.refresh is Loading,
        │     onRefresh = { pokemonList.refresh() }
        │   )
        │   └── LazyColumn
        │         ├── [loadState.refresh is Loading] → item { LoadingIndicator() }
        │         ├── [loadState.refresh is Error] → item { ErrorView(retryAction) }
        │         ├── items(pokemonList.itemCount) { index ->
        │         │     val pokemon = pokemonList[index]
        │         │     if (pokemon != null) PokemonItem(pokemon, onClick = navigateToDetail)
        │         │   }
        │         └── [loadState.append is Loading] → item { CircularProgressIndicator(Modifier.align(Center)) }
        │
        └── [showSearch] → SearchDialog(
              onDismiss = { showSearch = false },
              onNavigateToDetail = { name ->
                showSearch = false
                navController.navigate(DetailRoute(name))
              }
            )
```

**`PokemonItem` composable:**
```
PokemonItem(pokemon: Pokemon, onClick: () -> Unit)
└── Card(
      modifier = fillMaxWidth, padding(horizontal=16dp, vertical=4dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2dp),
      onClick = onClick
    )
    └── Row (verticalAlignment = CenterVertically, padding 8dp)
        ├── Box (
        │     size = 80.dp,
        │     background = MaterialTheme.colorScheme.surfaceVariant,
        │     shape = RoundedCornerShape(8dp)
        │   )
        │   └── AsyncImage (
        │         model = pokemon.imageUrl,
        │         contentDescription = pokemon.name,
        │         contentScale = ContentScale.Fit,
        │         modifier = fillMaxSize.padding(4dp)
        │       )
        ├── Spacer(12dp)
        └── Text(
              text = pokemon.name.replaceFirstChar { it.uppercase() },
              style = MaterialTheme.typography.titleMedium
            )
```

**Loading states handled:**
1. `refresh is Loading` → full-screen `CircularProgressIndicator` during first load
2. `refresh is Error` → error message + "Retry" `Button` that calls `pokemonList.refresh()`
3. `append is Loading` → small `CircularProgressIndicator` at the bottom of the list
4. `append is Error` → inline retry button at end of list
5. `endOfPaginationReached` → no more indicator, list stops

**Pull-to-refresh:** Uses Compose Material3's `PullToRefreshBox` which provides a native swipe-down gesture, triggers `pokemonList.refresh()` which resets the `RemoteMediator` to `LoadType.REFRESH`.

---

### `PokemonDetailScreen.kt`

**Purpose:** Show full details for a selected Pokémon.

**State:** `PokemonDetailViewModel.detailState: StateFlow<PokemonDetailUiState>`

**URL parameter:** Receives `name: String` from `DetailRoute(name)` via Navigation Compose.

```
PokemonDetailScreen
└── Scaffold
    ├── topBar = TopAppBar
    │     title = Text(name.replaceFirstChar { uppercase })
    │     navigationIcon = IconButton(onClick = navController::navigateUp)
    │       Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
    │
    └── content (when state)
        ├── PokemonDetailUiState.Loading →
        │     Box(fillMaxSize) { CircularProgressIndicator(Modifier.align(Center)) }
        │
        ├── PokemonDetailUiState.Error →
        │     Box(fillMaxSize) {
        │       Column(Center) {
        │         Text("Error: ${error.message}")
        │         Button(onClick = { viewModel.loadDetail(name) }) { Text("Retry") }
        │       }
        │     }
        │
        └── PokemonDetailUiState.Success(detail) →
              Column (
                fillMaxSize, verticalScroll(rememberScrollState()),
                horizontalAlignment = CenterHorizontally,
                padding 16dp
              )
              ├── Box (
              │     size = 200.dp,
              │     background = surfaceVariant,
              │     shape = RoundedCornerShape(16dp)
              │   )
              │   └── AsyncImage (detail.imageUrl, contentScale = Fit)
              ├── Spacer(16dp)
              ├── Text(detail.name.uppercase()) — headlineMedium, bold
              ├── Spacer(8dp)
              ├── Row (horizontalArrangement = spacedBy(8dp))
              │     ├── InfoCard("Height", "${detail.height / 10.0} m")
              │     └── InfoCard("Weight", "${detail.weight / 10.0} kg")
              ├── Spacer(16dp)
              └── Card (fillMaxWidth)
                    Column (padding 16dp)
                    ├── Text("Abilities") — titleMedium, bold
                    └── detail.abilities.forEach { ability →
                          Text("• $ability", style = bodyMedium)
                        }
```

**`InfoCard` composable:**
```
InfoCard(label: String, value: String)
└── Card (elevation 2dp)
    └── Column (padding 12dp, horizontalAlignment = CenterHorizontally)
        ├── Text(label) — labelSmall, color = onSurfaceVariant
        └── Text(value) — titleMedium, bold
```

**Unit conversion note:** PokéAPI returns height in decimetres and weight in hectograms. The Detail screen divides both by 10 for human-readable metres/kilograms respectively.

---

### `SearchScreen.kt` (SearchDialog)

**Purpose:** Full-screen overlay for querying the local FTS index with API fallback.

**State:** `PokemonListViewModel.searchState: StateFlow<SearchUiState>`

```
SearchDialog(onDismiss: () -> Unit, onNavigateToDetail: (String) -> Unit)
└── Dialog (
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    )
    └── Scaffold (fillMaxSize)
        ├── topBar = TopAppBar
        │     title = OutlinedTextField (
        │               value = query,
        │               onValueChange = { query = it; if(it.length >= 2) viewModel.search(it) },
        │               placeholder = Text("Search Pokémon..."),
        │               singleLine = true,
        │               keyboardOptions = KeyboardOptions(imeAction = Search)
        │             )
        │     navigationIcon = IconButton(onClick = onDismiss)
        │       Icon(Icons.AutoMirrored.Filled.ArrowBack)
        │     actions = [
        │       if (searchState is Loading) CircularProgressIndicator(size = 24dp)
        │     ]
        │
        └── content (when searchState)
            ├── SearchUiState.Idle → Box(Center) { Text("Search for Pokémon") }
            ├── SearchUiState.Loading → Box(Center) { CircularProgressIndicator() }
            ├── SearchUiState.Error → Box(Center) { Text("Error: ...") }
            └── SearchUiState.Success(results) →
                  LazyColumn {
                    items(results) { pokemon ->
                      ListItem(
                        headlineContent = { Text(pokemon.name) },
                        leadingContent = {
                          AsyncImage(pokemon.imageUrl, size = 40.dp)
                        },
                        modifier = clickable { onNavigateToDetail(pokemon.name) }
                      )
                      HorizontalDivider()
                    }
                  }
```

**Search debounce:** The `OutlinedTextField`'s `onValueChange` only triggers `viewModel.search()` when the query length ≥ 2 characters. The ViewModel applies a 500ms `debounce` using Kotlin Flow operators to avoid firing a query on every keystroke.

**Search strategy in `PokeRepositoryImpl`:**

1. Query local FTS4 table first (`pokemon_fts MATCH query`)
2. If results are empty, call `PokeService.getPokemonDetail(query)` directly (exact name match)
3. Return whichever produced results

---

### `MainActivity.kt` — Navigation Host

**Adaptive navigation with `NavigationSuiteScaffold`:**

```
NavigationSuiteScaffold
└── items = [
      NavigationItem("Home", Icons.Default.Home, selected = currentRoute == "main_home"),
      NavigationItem("Profile", Icons.Default.AccountBox, selected = currentRoute == "main_profile")
    ]
    content = HorizontalPager OR NavHost (depending on implementation)
```

**NavHost routes:**

```
NavHost(startDestination = if (isLoggedIn) MainRoute else LoginRoute)
├── composable<LoginRoute>  → LoginScreen
├── composable<RegisterRoute> → RegisterScreen
├── composable<MainRoute>   →
│     NavigationSuiteScaffold
│     ├── HOME tab → PokemonListScreen
│     └── PROFILE tab → ProfileScreen
└── composable<DetailRoute> → PokemonDetailScreen(name = route.name)
```

**Back stack management:**
- `LoginRoute` → `MainRoute`: `popUpTo(LoginRoute) { inclusive = true }` prevents back-navigating to login after successful auth.
- `MainRoute` → `DetailRoute`: standard push — back button returns to list.
- Logout: `popUpTo(MainRoute) { inclusive = true }` removes main from back stack.

---

## 6. Data Flow Diagrams

### Pokémon List (Paged, Offline-First)

```
User opens app
    │
    ▼
PokemonListScreen
    │ collectAsLazyPagingItems()
    ▼
PokemonListViewModel.pokemonPager (Flow<PagingData<Pokemon>>)
    │ GetPokemonListUseCase.invoke()
    ▼
PokeRepositoryImpl.getPokemonPager()
    │ Pager(PagingConfig(pageSize=20), remoteMediator=PokemonRemoteMediator)
    ├── PagingSource = PokemonDao.searchPokemon("") ←── reads from Room
    │
    └── PokemonRemoteMediator (on cache miss / refresh)
            │
            ├── DELETE FROM pokemon (on REFRESH)
            ├── DELETE FROM remote_keys (on REFRESH)
            │
            ├── GET /pokemon?limit=20&offset=N  ←── Retrofit → OkHttp → PokéAPI
            │
            ├── INSERT INTO pokemon (id, name, imageUrl)
            └── INSERT INTO remote_keys (pokemonId, prevKey, nextKey)
                        │
                        ▼
                PokemonDao PagingSource emits new page
                        │
                        ▼
                Compose recomposes with new items
```

### Pokémon Detail

```
User taps a list item
    │
    ▼
navController.navigate(DetailRoute(name))
    │
    ▼
PokemonDetailScreen(name)
    │ LaunchedEffect(name) { viewModel.loadDetail(name) }
    ▼
PokemonDetailViewModel.loadDetail(name)
    │ _detailState = Loading
    │ GetPokemonDetailUseCase(name)
    ▼
PokeRepositoryImpl.getPokemonDetail(name)
    │ safeApiCall { pokeService.getPokemonDetail(name) }
    ▼
PokemonDetailResponse (JSON via Moshi)
    │ PokemonDetailMapper.toDomain()
    ▼
_detailState = Success(PokemonDetail)
    │
    ▼
PokemonDetailScreen recomposes → shows image, stats, abilities
```

### Authentication Flow

```
User enters email + password, taps Login
    │
    ▼
AuthViewModel.login(email, password)
    │ _authState = Loading
    │ LoginUseCase(email, password)
    ▼
UserRepositoryImpl.getUserCredentials(email)
    │ UserDao.getUserCredentials(email) → UserCredentials(hash, salt)
    ▼
PasswordHasher.verify(password, salt, hash)
    │ true → getUserByEmail(email) → User
    │ false → Result.failure(AppError.ClientError)
    ▼
AuthViewModel receives Result<User>
    ├── Success → SessionManager.saveLoginSession(); _authState = Success(user)
    └── Failure → _authState = Error(message)
```

---

## 7. Database Schema

### Entity Relationship

```
┌─────────────────┐       ┌──────────────────┐
│    pokemon      │       │   remote_keys     │
│─────────────────│       │──────────────────│
│ id (PK)         │◄──────│ pokemonId (PK)   │
│ name            │       │ prevKey: Int?    │
│ imageUrl        │       │ nextKey: Int?    │
│ height          │       └──────────────────┘
│ weight          │
└────────┬────────┘
         │ contentEntity
┌────────▼────────┐       ┌──────────────────┐
│  pokemon_fts    │       │     users        │
│  (FTS4 virtual) │       │──────────────────│
│─────────────────│       │ id (PK, auto)    │
│ name            │       │ name             │
└─────────────────┘       │ email (unique)   │
                          │ passwordHash     │
                          │ salt             │
                          └──────────────────┘
```

### Room Version History

| Version | Change |
|---|---|
| 1 | Initial schema |
| 2 | Added `height`, `weight` to `PokemonEntity` |
| 3 | Added `PokemonFtsEntity` |
| 4 | Added `UserEntity` |
| 5 | Added `RemoteKeyEntity` |
| 6 | Current — schema stabilization |

All migrations between versions used `fallbackToDestructiveMigration` — acceptable because the Pokémon data is re-fetchable from the API, and users is cleared on reinstall anyway in this local-only architecture.

---

## 8. API Integration

### PokéAPI v2

**Base URL:** `https://pokeapi.co/api/v2/`

**Rate limits:** None officially, but the API asks for responsible use. The app pages at 20 per request.

**Endpoints used:**

| Method | Path | Usage |
|---|---|---|
| GET | `/pokemon?limit={n}&offset={o}` | Paginated list in `RemoteMediator` |
| GET | `/pokemon/{name}` | Detail view + search fallback |

**Not used (potential future):**

| Path | Data Available |
|---|---|
| `/pokemon-species/{id}` | Evolution chain, flavor text descriptions |
| `/evolution-chain/{id}` | Full evolution tree |
| `/type/{id}` | Type weaknesses/strengths |
| `/move/{id}` | Move details |
| `/ability/{id}` | Ability descriptions |

**OkHttp configuration:**

```
OkHttpClient
  ├── connectTimeout = 30s
  ├── readTimeout = 30s
  ├── writeTimeout = 30s
  ├── retryOnConnectionFailure = true
  └── addInterceptor(HttpLoggingInterceptor(BODY))
       [logs full request/response JSON in Logcat]
```

**Moshi configuration:**

```
Moshi
  └── addLast(KotlinJsonAdapterFactory())
       [enables reflectionless JSON parsing of Kotlin data classes]
```

---

## 9. Authentication & Security

### Overview

Authentication is entirely local — there is no remote backend. All user data lives in the on-device Room database.

### Password Security

```
Registration:
  salt = UUID.randomUUID().toString()     // 128-bit random
  hash = SHA-256(salt + password)         // hex string
  DB.insert(UserEntity(hash=hash, salt=salt))

Login:
  UserEntity = DB.query(WHERE email=?)
  candidateHash = SHA-256(UserEntity.salt + inputPassword)
  if candidateHash == UserEntity.hash → success
```

**What this protects against:**
- Plaintext password storage — passwords are never stored raw
- Rainbow table attacks — the random salt means identical passwords produce different hashes
- SQL injection — Room's parameterized queries prevent injection

**What this does NOT protect against:**
- Brute force — SHA-256 is fast; an attacker with DB access can try millions of passwords/sec
- Device root access — if the device is rooted, the Room DB file is accessible

**Production recommendation:** Use `bcrypt` (cost factor 12+) or `Argon2id` which are intentionally slow hashing algorithms designed for passwords.

### Session Management

```kotlin
// SharedPreferences keys
const val KEY_IS_LOGGED_IN = "is_logged_in"
const val KEY_USER_EMAIL = "user_email"
const val KEY_USER_NAME = "user_name"
const val KEY_USER_ID = "user_id"
```

Session is stored in private SharedPreferences (mode `Context.MODE_PRIVATE`). On Android 7+, this is stored in `/data/data/{package}/shared_prefs/` — inaccessible to other apps without root.

**Not implemented (but would improve security):**
- Session expiry / token rotation
- Biometric authentication re-prompt after inactivity
- Encrypted SharedPreferences (`EncryptedSharedPreferences` from `androidx.security.crypto`)

### Network Security

The `AndroidManifest.xml` requests only `INTERNET` permission. No `WRITE_EXTERNAL_STORAGE` or other sensitive permissions.

**Certificate pinning** is commented out in `NetworkModule`. When enabled it would look like:

```kotlin
CertificatePinner.Builder()
    .add("pokeapi.co", "sha256/...")
    .build()
```

This prevents MITM attacks using trusted CA certificates. Disabled currently because PokéAPI rotates certificates regularly and a hard-coded pin would break the app on rotation.

---

## 10. Dependency Injection

### Koin vs. Dagger/Hilt

Koin uses a service locator pattern at runtime. Dagger/Hilt uses compile-time code generation.

| Aspect | Koin 4.0 | Dagger/Hilt |
|---|---|---|
| Error detection | Runtime | Compile-time |
| Setup verbosity | Low (Kotlin DSL) | High (annotations, components) |
| Performance | Slight runtime overhead | Zero overhead (generated code) |
| Multiplatform | Yes (KMP) | Android-only |
| Learning curve | Low | High |
| Gradle plugin | None needed | Hilt Gradle plugin required |

**Why Koin was chosen:**
- Kotlin-first DSL is readable and concise
- No annotation processor overhead during compilation (faster build times)
- `koinViewModel()` integrates seamlessly with Compose
- Single-module app doesn't benefit much from Dagger's compile-time graph validation

**When Dagger/Hilt would be better:**
- Large teams where catching DI errors at compile-time prevents runtime crashes in production
- Apps with complex scoped dependencies (login/logout scope, feature scopes)
- Performance-critical initialization where every millisecond of app startup matters

### Module Structure

```kotlin
// NetworkModule
single { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }
single { OkHttpClient.Builder()...build() }
single { Retrofit.Builder()...build() }
single { get<Retrofit>().create<PokeService>() }

// DatabaseModule
single { Room.databaseBuilder(get(), AppDatabase::class.java, "poke_database")
             .fallbackToDestructiveMigration().build() }
single { get<AppDatabase>().pokemonDao() }
single { get<AppDatabase>().userDao() }
single { get<AppDatabase>().remoteKeyDao() }

// AppModule
single { SessionManager(get()) }
single<PokeRepository> { PokeRepositoryImpl(get(), get(), get(), get()) }
single<UserRepository> { UserRepositoryImpl(get()) }
single { GetPokemonListUseCase(get()) }
// ... etc
viewModel { PokemonListViewModel(get(), get()) }
viewModel { PokemonDetailViewModel(get()) }
viewModel { AuthViewModel(get(), get(), get()) }
viewModel { MainViewModel(get()) }
```

---

## 11. Navigation

### Type-Safe Routes (Navigation Compose 2.9.7+)

```kotlin
@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object MainRoute
@Serializable data class DetailRoute(val name: String)
```

Navigation Compose 2.9.7 introduced first-class support for `@Serializable` route objects, replacing string-based routes entirely. Arguments are deserialized automatically from the back stack entry.

**Why type-safe navigation?**

Old approach (string routes):
```kotlin
navController.navigate("detail/pikachu")   // runtime crash if typo
val name = backStackEntry.arguments?.getString("name")  // nullable, untyped
```

New approach:
```kotlin
navController.navigate(DetailRoute("pikachu"))   // compile-time safe
val route = backStackEntry.toRoute<DetailRoute>()
val name = route.name  // non-nullable, typed
```

**Trade-off:** Requires `kotlinx-serialization-json` on the classpath, adding ~150KB to the APK. Worth it for compile-time safety in any app larger than a demo.

### Back Stack Diagram

```
App Start (isLoggedIn=false):
  [LoginRoute] ← start destination

After login:
  [MainRoute]
  (LoginRoute removed via popUpTo)

Navigate to detail:
  [MainRoute] → [DetailRoute("pikachu")]

Back press from detail:
  [MainRoute]

Logout:
  [LoginRoute]
  (MainRoute removed via popUpTo inclusive=true)
```

---

## 12. State Management

### `StateFlow` vs. `LiveData`

| Aspect | StateFlow | LiveData |
|---|---|---|
| Kotlin-native | Yes | No (Android framework) |
| Compose collection | `collectAsStateWithLifecycle()` | `observeAsState()` |
| Lifecycle-aware | No (needs wrapper) | Yes |
| Initial value | Required | Optional |
| Cold/Hot | Hot | Hot |
| Coroutine integration | Native | Needs `liveData {}` builder |

**Why `StateFlow` over `LiveData`?**

`StateFlow` is part of `kotlinx.coroutines` — no Android dependency. ViewModels expose `StateFlow` which the UI collects with `collectAsStateWithLifecycle()` (lifecycle-aware collection, stops when UI is in background). This is the current Google recommendation for Compose apps.

### Sealed Class UI States

```kotlin
sealed class PokemonDetailUiState {
    object Loading : PokemonDetailUiState()
    data class Success(val data: PokemonDetail) : PokemonDetailUiState()
    data class Error(val error: AppError) : PokemonDetailUiState()
}
```

Using sealed classes over `Boolean` flags:
- **No invalid states**: Can't have `isLoading=true` and `data != null` simultaneously
- **Exhaustive `when`**: Compiler ensures all states are handled
- **Data attached**: `Success` carries the data, `Error` carries the typed error

### `PokemonListViewModel`

```kotlin
class PokemonListViewModel(
    getPokemonListUseCase: GetPokemonListUseCase,
    private val searchPokemonUseCase: SearchPokemonUseCase
) : ViewModel() {

    val pokemonPager: Flow<PagingData<Pokemon>> =
        getPokemonListUseCase()
            .cachedIn(viewModelScope)   // survives config changes

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState = _searchState.asStateFlow()

    fun search(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            searchPokemonUseCase(query)
                .onSuccess { _searchState.value = SearchUiState.Success(it) }
                .onFailure { _searchState.value = SearchUiState.Error(it as AppError) }
        }
    }
}
```

**`cachedIn(viewModelScope)`** is critical — it keeps the `PagingData` stream alive across recompositions and configuration changes (rotation). Without it, the list would reload from scratch on every screen rotation.

---

## 13. Paging Strategy

### Why Paging 3?

PokéAPI has 1000+ Pokémon. Loading all at once would:
- Take 50+ network requests (or one massive response)
- Fill the device's memory with unused data
- Make the app appear slow on first launch

Paging 3 loads exactly what the user can see + a prefetch window.

### Paging Config

```kotlin
PagingConfig(
    pageSize = 20,
    enablePlaceholders = false,
    initialLoadSize = 20,
    prefetchDistance = 5  // default
)
```

**`enablePlaceholders = false`:** Placeholders require knowing the total count upfront, which PokéAPI does return (`count` field) but implementing it adds complexity. With placeholders disabled, the `LazyColumn` only renders real items and shows a loading indicator at the bottom.

### RemoteMediator Pattern

The `RemoteMediator` acts as a coordinator:

```
       User scrolls to bottom
              │
              ▼
     Paging 3 sees end of local data
              │
              ▼
   RemoteMediator.load(APPEND) called
              │
              ▼
      Fetch from API (offset = nextKey)
              │
              ▼
   Write to Room in a transaction
              │
              ▼
  PagingSource (Room) emits invalidation
              │
              ▼
      Compose recomposes with new items
```

### Why NOT just a `PagingSource` (without RemoteMediator)?

A plain `PagingSource` against the API would work but:
- No offline support — app shows empty state without internet
- Every scroll-to-bottom triggers a network call immediately
- Pull-to-refresh has no persistent cache to return to

The `RemoteMediator` + Room combination gives:
- Full offline browsing after first load
- Pull-to-refresh that re-fetches and re-caches
- Fast FTS search over cached data

---

## 14. Error Handling

### `AppError` Hierarchy

```
AppError (sealed, extends Exception)
├── NetworkError(statusCode: Int)   — connectivity issues, specific HTTP codes
├── ServerError                     — 5xx responses
├── ClientError                     — 4xx responses (bad request, not found)
├── TimeoutError                    — SocketTimeoutException
├── ParseError                      — JSON parse failures
├── DatabaseError                   — Room exceptions
└── UnknownError                    — catch-all
```

### `safeApiCall` wrapper

Every network call goes through:

```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: HttpException) {
        Result.failure(
            when (e.code()) {
                in 400..499 -> AppError.ClientError
                in 500..599 -> AppError.ServerError
                else -> AppError.NetworkError(e.code())
            }
        )
    } catch (e: SocketTimeoutException) {
        Result.failure(AppError.TimeoutError)
    } catch (e: IOException) {
        Result.failure(AppError.NetworkError(0))
    } catch (e: JsonDataException) {
        Result.failure(AppError.ParseError)
    } catch (e: Exception) {
        Result.failure(AppError.UnknownError)
    }
}
```

### Error Propagation to UI

```
Repository → Result<T>
    │
    ▼
UseCase → passes Result<T> through
    │
    ▼
ViewModel → maps to sealed UI state
    │
    ▼
Screen → when(state) exhaustive match → shows appropriate UI
```

### What's Missing

- **Retry with exponential backoff** — `TimeoutError` and `NetworkError` should automatically retry 2-3 times with increasing delay before showing an error to the user
- **User-facing error messages per type** — currently all errors show the same generic message; they should be localized strings in `strings.xml`
- **Offline indicator** — a banner showing "You're offline" when `NetworkCapabilities` shows no connectivity

---

## 15. Theme & Design System

### Material3 (Material You)

The app uses Material Design 3 with a static light color scheme. The colors are defined in `Color.kt`:

```kotlin
// Light scheme
val Purple40  = Color(0xFF6650A4)   // primary
val PurpleGrey40 = Color(0xFF625B71) // secondary
val Pink40    = Color(0xFF7D5260)   // tertiary

// Dark scheme (defined, not applied)
val Purple80  = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80    = Color(0xFFEFB8C8)
```

`Theme.kt` creates a `MaterialTheme` with `lightColorScheme(primary=Purple40, ...)`. Dark mode is not currently wired up — `isSystemInDarkTheme()` is not used. Future: a `darkColorScheme` branch using the `*80` palette already defined.

### Typography

Only `bodyLarge` is customized (16sp). All other text styles (`headlineLarge`, `titleMedium`, etc.) use Material3 defaults. No custom font families — uses system default.

### Shapes

Material3 defaults — rounded corners (extraSmall=4dp, small=8dp, medium=12dp, large=16dp, extraLarge=28dp).

---

## 16. Build Configuration

### Version Catalog (`gradle/libs.versions.toml`)

The project uses Gradle's TOML version catalog for centralized dependency management:

```toml
[versions]
agp = "9.1.0"
kotlin = "2.2.10"
ksp = "2.2.10-2.0.2"
composeBom = "2025.12.00"
room = "2.8.4"
paging = "3.3.6"
retrofit = "2.11.0"
moshi = "1.15.2"
okhttp = "5.3.2"
koin = "4.0.2"
coroutines = "1.10.1"
coil = "3.4.0"
navigationCompose = "2.9.7"
kotlinSerialization = "1.8.0"
```

**Why version catalog?** Before version catalogs, each `build.gradle.kts` file had hardcoded version strings like `"2.11.0"`. Updating a library required `grep`-ing all build files. The TOML catalog creates a single source of truth that's refactored as a first-class language element: `libs.retrofit` instead of `"com.squareup.retrofit2:retrofit:2.11.0"`.

### App-level `build.gradle.kts`

```kotlin
android {
    namespace = "code.id.poke"
    compileSdk = 36
    defaultConfig {
        applicationId = "code.id.poke"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}
```

**Min SDK 26 choice:** Android 8.0 (Oreo). As of 2025, ~97% of active Android devices run Oreo or higher. This allows using `java.util.UUID` directly and avoids backporting APIs. Shadow of limitation: cannot use newer APIs like `android.security.identity` (Android 11+) without version checks.

---

## 17. Testing

### Current State

The project has a skeleton `ExampleUnitTest` (the default template) and the standard `androidTest` directory but no meaningful test coverage.

### What Should Be Tested

#### Unit Tests (JVM, no device needed)

| Component | What to test | Tools |
|---|---|---|
| `LoginUseCase` | Correct password → success, wrong password → ClientError | `kotlin.test`, `mockk` |
| `RegisterUseCase` | Duplicate email → failure, valid → success | `kotlin.test`, `mockk` |
| `PasswordHasher` | `verify(hash(p,s), s, p)` == true, different password == false | `kotlin.test` |
| `PokemonMapper` | Correct unit conversion (height/weight), ability extraction | `kotlin.test` |
| `PokemonListViewModel` | Loading state transitions, search debounce | `kotlin.test`, `Turbine` |
| `AuthViewModel` | State transitions on login/register/logout | `kotlin.test`, `Turbine` |

#### Integration Tests (device/emulator)

| Component | What to test | Tools |
|---|---|---|
| `PokemonDao` | Insert + query + FTS search | `Room in-memory`, `JUnit4` |
| `UserDao` | Insert user, duplicate detection, credential lookup | `Room in-memory`, `JUnit4` |
| `RemoteMediator` | REFRESH clears DB, APPEND appends | `MockWebServer`, Room in-memory |

#### UI Tests

| Screen | What to test | Tools |
|---|---|---|
| `LoginScreen` | Button disabled when empty, shows error on wrong password | `Compose Test`, `MockK` |
| `PokemonListScreen` | Items render, pull-to-refresh triggers refresh | `Compose Test` |
| `SearchDialog` | Query dispatched after 2 chars, results list renders | `Compose Test` |

#### Recommended Libraries

```toml
testImplementation("io.mockk:mockk:1.13.x")
testImplementation("app.cash.turbine:turbine:1.x")    # Flow testing
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
androidTestImplementation("com.squareup.okhttp3:mockwebserver:5.3.2")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

---

## 18. Known Limitations & Trade-offs

| Limitation | Impact | Mitigation |
|---|---|---|
| No dark mode | App looks bright at night | Add `darkColorScheme` branch to `Theme.kt` |
| `fallbackToDestructiveMigration` | Schema change wipes all cached Pokémon | Write Room migration scripts |
| SHA-256 password hashing | Brute-forceable given DB access | Migrate to `bcrypt`/`argon2` |
| No session expiry | Session valid indefinitely | Add `loginTimestamp`, expire after N days |
| No certificate pinning | Vulnerable to MITM if CA is compromised | Re-enable pinned certs after cert monitoring |
| No encrypted SharedPreferences | Session readable if device is rooted | Use `EncryptedSharedPreferences` |
| Search only by name | Can't search by type, ability, etc. | Expand FTS table + API data |
| Detail is always network-only | Offline detail view not possible | Cache full detail in Room |
| No retry with backoff | Single failure shows error immediately | Add `retry(3) { delay(exp backoff) }` |
| No unit tests | Regressions caught only at runtime | See Testing section above |
| Abilities show only names | No ability descriptions | Fetch from `/ability/{id}` endpoint |
| Height/weight in list | Shown only in detail screen | Could show in list item subtitle |

---

## 19. Future Improvements & Roadmap

### P0 — Critical (Security / Correctness)

- [ ] **Replace SHA-256 with bcrypt** — Use `at.favre.lib:bcrypt` library, cost factor 12
- [ ] **Encrypted SharedPreferences** — `EncryptedSharedPreferences` from `androidx.security:security-crypto`
- [ ] **Room migration scripts** — Replace `fallbackToDestructiveMigration` with proper `Migration(from, to)` objects
- [ ] **Input sanitization** — Strip leading/trailing whitespace from email before query

### P1 — High Value Features

- [ ] **Dark mode support** — Wire `isSystemInDarkTheme()` in `Theme.kt`, apply the existing `*80` color palette
- [ ] **Offline detail caching** — Store `PokemonDetail` fields in `PokemonEntity` (height, weight, abilities as JSON column)
- [ ] **Ability descriptions** — Fetch `/ability/{name}` and display flavor text in detail screen
- [ ] **Type badges** — Fetch Pokémon types from detail API, show colored chips (Fire=red, Water=blue, etc.)
- [ ] **Evolution chain** — Fetch from `/pokemon-species/{id}` → `/evolution-chain/{id}`, show in detail
- [ ] **Favorites** — `is_favorite: Boolean` column in `PokemonEntity`, filter in Favorites tab (UI icon exists)

### P2 — Quality of Life

- [ ] **Unit tests** — `LoginUseCase`, `PasswordHasher`, `PokemonMapper`, ViewModels (see §17)
- [ ] **Error messages** — Per-`AppError` user-facing strings in `strings.xml`
- [ ] **Retry with exponential backoff** — `Flow.retryWhen { cause, attempt -> attempt < 3 && delay(2^attempt * 1000) }`
- [ ] **Offline banner** — `ConnectivityManager` + `NetworkCallback` to show "Offline" snackbar
- [ ] **Empty state illustrations** — Replace plain text empty states with illustrations
- [ ] **Shimmer loading** — Replace `CircularProgressIndicator` with shimmer skeleton for list items
- [ ] **Search history** — Persist recent searches in Room, show below search field
- [ ] **Deep links** — `android:host="pokecodeid.app"` deep link to detail screen by Pokémon name

### P3 — Architecture Improvements

- [ ] **Coroutines `IO` dispatcher** — Ensure all DB and network calls explicitly use `Dispatchers.IO`
- [ ] **`@WorkerThread` annotations** — Document threading contracts on DAO methods
- [ ] **Feature modules** — Split `auth`, `pokemon`, `search` into Gradle modules for build speed
- [ ] **ProGuard/R8 rules** — Add keep rules for Moshi, Koin reflection, navigation
- [ ] **Baseline profiles** — Generate a Baseline Profile to speed up Compose startup

### P4 — Nice to Have

- [ ] **Widget** — Android home screen widget showing today's random Pokémon
- [ ] **Notifications** — "Pokémon of the Day" push notification (needs backend)
- [ ] **Export favorites** — Share favorite Pokémon as an image/PDF
- [ ] **Pokémon comparison** — Side-by-side stats comparison view
- [ ] **Augmented Reality** — ARCore overlay for Pokémon models (ambitious!)

---

## 20. Tech Stack Alternatives Analysis

### UI: Jetpack Compose vs. XML Views

| Aspect | Jetpack Compose | XML Views |
|---|---|---|
| Paradigm | Declarative | Imperative |
| State handling | Recomposition | Manual `invalidate()`/`notifyDataSetChanged()` |
| Code lines | ~50% less | Verbose |
| Tooling | Compose Preview | Layout Editor |
| Performance | Smart recomposition | Well-optimized `RecyclerView` |
| Learning curve | High (new paradigm) | Medium (established) |
| Interop | Interops with Views | N/A |

**Why Compose was chosen:** Google has declared Compose the future of Android UI. New libraries (Material3, Navigation, Paging) have first-class Compose APIs. New developers learn Compose by default.

**Why XML Views is not used:** XML Views require separate binding/adapter code for every list, state updates are manual and error-prone, and the inflation overhead on startup is measurable.

---

### Networking: Retrofit vs. Ktor vs. Volley

| Aspect | Retrofit | Ktor | Volley |
|---|---|---|---|
| Type safety | High (interfaces) | High (typed) | Low |
| Coroutine support | Via adapters (native in 2.x) | Native | Callbacks |
| Multiplatform | No | Yes (KMP) | No |
| JSON integration | Moshi/Gson/Kotlinx | Built-in kotlinx.serialization | Gson |
| Maturity | Very mature | Mature | Old, maintained |
| Community | Huge | Growing | Shrinking |

**Why Retrofit:** Battle-tested, huge community, seamless Moshi integration, suspend function support without adapters in 2.6+. The standard for Android networking.

**Why Ktor is not used:** Ktor is the better choice for Kotlin Multiplatform projects. For Android-only, Retrofit has more tutorials, more Stack Overflow answers, and tighter integration with the Android ecosystem.

**Why Volley is not used:** Callback-based API doesn't fit coroutines. No built-in JSON type-safety. No active innovation.

---

### Database: Room vs. SQLDelight vs. Realm vs. DataStore

| Aspect | Room | SQLDelight | Realm | DataStore |
|---|---|---|---|---|
| Language | SQL + KSP | SQL (type-safe multiplatform) | Object store | Key-value / Proto |
| Coroutine support | Native | Native | Callbacks/Coroutines | Native |
| KMP support | No | Yes | Yes | Partial |
| FTS support | Yes (FTS3/FTS4) | Yes | Limited | No |
| Paging 3 integration | Native | Third-party | No | No |
| Migration tooling | Manual scripts | Built-in | Built-in | N/A |

**Why Room:** Native Paging 3 integration via `@RawQuery` PagingSource, first-class FTS4 via `@Fts4` annotation, type-safe DAO queries with compile-time verification via KSP, and it is Google's official recommendation.

**Why SQLDelight is not used:** Excellent for KMP projects. For Android-only, Room's Paging 3 integration would need re-implementation. More setup overhead.

**Why Realm is not used:** Object store paradigm doesn't map cleanly to relational PokéAPI responses. Paging support requires custom implementation. Heavier SDK footprint.

**Why DataStore is not used:** DataStore (Preferences or Proto) is designed for small key-value data (like `SessionManager`). Not suitable for structured entities like Pokémon lists.

---

### DI: Koin vs. Dagger/Hilt vs. Manual DI

| Aspect | Koin 4.0 | Dagger/Hilt | Manual DI |
|---|---|---|---|
| Error detection | Runtime | Compile-time | N/A |
| Build time impact | None | +10-30s (annotation processing) | None |
| Kotlin-native | Yes | No (Java annotations) | Yes |
| Compose integration | `koinViewModel()` | `hiltViewModel()` | Manual |
| Testing | `startKoin{ modules(...) }` | `@HiltAndroidTest` | Easy |
| Scope management | `single`, `factory`, `scoped` | `@Singleton`, `@ActivityScoped` | Manual |

**Why Koin:** Fast setup, Kotlin DSL, no annotation processing, good Compose integration. Acceptable for single-module apps where the full Dagger component hierarchy isn't needed.

**Why Hilt is not used:** Hilt is the recommended choice for large/team projects because DI graph errors are caught at compile time. For this project size, the setup cost outweighs the benefit. If the project grows to multiple feature modules, migrating to Hilt would be justified.

---

### Pagination: Paging 3 vs. Manual offset/page handling

| Aspect | Paging 3 | Manual |
|---|---|---|
| Boilerplate | Low (after setup) | Medium |
| Loading states | Built-in `LoadState` | Custom |
| Error + retry | Built-in `retry()` | Custom |
| RemoteMediator | Built-in | Must implement |
| Compose integration | `collectAsLazyPagingItems()` | Custom |
| Complexity | High initial | Low initial |

**Why Paging 3:** The out-of-the-box loading states, error handling, retry logic, and `RemoteMediator` for offline-first are worth the setup cost for a list of 1000+ items.

**Why manual paging is not used:** Manual paging (track `offset` as a var, increment on scroll) loses all the built-in state management. Error + retry requires custom implementation. No native Compose support.

---

### Image Loading: Coil vs. Glide vs. Picasso

| Aspect | Coil 3 | Glide | Picasso |
|---|---|---|---|
| Kotlin-first | Yes | No (Java) | No (Java) |
| Coroutine support | Native | Coroutine extension | No |
| Compose integration | `AsyncImage` composable | No (adapters) | No |
| Memory management | Good | Excellent | Good |
| GIF support | Yes | Yes (better) | No |
| APK size | Small | Medium | Small |
| OkHttp integration | Yes | Partial | No |

**Why Coil:** Kotlin-first, `AsyncImage` composable works out-of-the-box in Compose, shares the same `OkHttpClient` already configured for Retrofit (connection pool reuse, same timeout config). Active development by the Coil team with Compose BOM alignment.

**Why Glide is not used:** No native `AsyncImage` composable — requires `GlideImage` from a separate `accompanist` library. Java-centric API doesn't leverage Kotlin features.

**Why Picasso is not used:** No Compose support. Inactive development. Smaller feature set than Coil or Glide.

---

### JSON: Moshi vs. Gson vs. kotlinx.serialization

| Aspect | Moshi | Gson | kotlinx.serialization |
|---|---|---|---|
| Null safety | Kotlin-aware | Not Kotlin-aware | Kotlin-native |
| KSP codegen | Yes | No | Yes |
| Reflection | Optional (KotlinJsonAdapterFactory) | Always | None (codegen) |
| KMP support | No | No | Yes |
| Retrofit adapter | `MoshiConverterFactory` | `GsonConverterFactory` | Custom |
| Speed | Fast (codegen) | Slower (reflection) | Fastest (codegen) |

**Why Moshi:** More Kotlin-aware than Gson (correctly handles nullability, default values). KSP codegen avoids reflection overhead. `MoshiConverterFactory` for Retrofit is well-maintained.

**Why Gson is not used:** Gson does not understand Kotlin's null safety — it will silently set non-nullable fields to null, causing `NullPointerException` at runtime. Fatal in a typed language.

**Why kotlinx.serialization is not used:** Would be the best choice for a KMP project. For Android-only, Moshi's track record and Retrofit integration is more established. The `@Serializable` annotation was already used for Navigation routes (different use case — navigation argument serialization, not JSON parsing).

---

### Architecture: Clean Architecture vs. Simple MVVM vs. MVI

| Aspect | Clean Architecture | Simple MVVM | MVI |
|---|---|---|---|
| Layers | 3 (UI/Domain/Data) | 2 (UI/Data) | 3 (UI/Model/Intent) |
| Testability | Very high | Medium | High |
| Boilerplate | High | Low | High |
| State management | StateFlow sealed | StateFlow | Immutable state + reducer |
| Learning curve | High | Low | High |
| Unidirectional flow | Partial | No | Strict |

**Why Clean Architecture over Simple MVVM:** The project complexity (paging, remote mediator, FTS search, auth) justifies the abstraction. Repositories can be tested with fake implementations without touching ViewModels.

**Why MVI is not used:** MVI (Model-View-Intent) enforces strict unidirectional data flow with immutable state + pure reducer functions. Better for very complex state (e.g., a form with 20 fields). For this app's relatively simple states, the `sealed class + StateFlow` approach achieves 90% of MVI's benefits without the framework overhead.

---

## Appendix A: Project File Tree

```
PokeCODEID/
├── app/
│   └── src/main/
│       ├── java/code/id/poke/
│       │   ├── MainActivity.kt
│       │   ├── PokeApplication.kt
│       │   ├── di/
│       │   │   ├── AppModule.kt
│       │   │   ├── DatabaseModule.kt
│       │   │   └── NetworkModule.kt
│       │   ├── domain/
│       │   │   ├── error/AppError.kt
│       │   │   ├── model/
│       │   │   │   ├── Pokemon.kt
│       │   │   │   ├── PokemonDetail.kt
│       │   │   │   ├── User.kt
│       │   │   │   └── UserCredentials.kt
│       │   │   ├── repository/
│       │   │   │   ├── PokeRepository.kt
│       │   │   │   └── UserRepository.kt
│       │   │   └── usecase/
│       │   │       ├── GetPokemonDetailUseCase.kt
│       │   │       ├── GetPokemonListUseCase.kt
│       │   │       ├── LoginUseCase.kt
│       │   │       ├── RegisterUseCase.kt
│       │   │       └── SearchPokemonUseCase.kt
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── AppDatabase.kt
│       │   │   │   ├── PokemonDao.kt
│       │   │   │   ├── PokemonEntity.kt
│       │   │   │   ├── PokemonFtsEntity.kt
│       │   │   │   ├── RemoteKeyDao.kt
│       │   │   │   ├── RemoteKeyEntity.kt
│       │   │   │   ├── SessionManager.kt
│       │   │   │   ├── UserDao.kt
│       │   │   │   └── UserEntity.kt
│       │   │   ├── mapper/
│       │   │   │   ├── PokemonMapper.kt
│       │   │   │   └── UserMapper.kt
│       │   │   ├── remote/
│       │   │   │   ├── ApiCall.kt
│       │   │   │   ├── PokeService.kt
│       │   │   │   └── PokemonRemoteMediator.kt
│       │   │   └── repository/
│       │   │       ├── PokeRepositoryImpl.kt
│       │   │       └── UserRepositoryImpl.kt
│       │   ├── ui/
│       │   │   ├── auth/
│       │   │   │   ├── AuthViewModel.kt
│       │   │   │   ├── LoginScreen.kt
│       │   │   │   ├── ProfileScreen.kt
│       │   │   │   └── RegisterScreen.kt
│       │   │   ├── navigation/
│       │   │   │   └── AppRoutes.kt
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Theme.kt
│       │   │   │   └── Type.kt
│       │   │   ├── MainViewModel.kt
│       │   │   ├── PokeViewModel.kt
│       │   │   ├── PokemonDetailScreen.kt
│       │   │   ├── PokemonDetailViewModel.kt
│       │   │   ├── PokemonListScreen.kt
│       │   │   ├── PokemonListViewModel.kt
│       │   │   └── SearchScreen.kt
│       │   └── util/
│       │       └── PasswordHasher.kt
│       ├── res/
│       │   ├── drawable/ (launcher icons, nav icons)
│       │   ├── values/
│       │   │   ├── colors.xml
│       │   │   ├── strings.xml
│       │   │   └── themes.xml
│       │   └── xml/
│       │       ├── backup_rules.xml
│       │       └── data_extraction_rules.xml
│       └── AndroidManifest.xml
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts (root)
├── app/build.gradle.kts
├── CLAUDE.md
└── README.md
```

---

## Appendix B: Quick Start

```bash
# Clone and open in Android Studio Meerkat or later
git clone <repo-url>
cd PokeCODEID

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run lint
./gradlew lint
```

**Requirements:**
- Android Studio Meerkat (2025.1.1) or later
- JDK 17 or later
- Android device or emulator with API 26+
- Active internet connection (for PokéAPI on first launch)

---

*Built with Kotlin 2.2.10 · Jetpack Compose BOM 2025.12.00 · Poke CODE.ID v1.0*
