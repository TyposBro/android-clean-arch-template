# Android Clean Architecture Template

A production-ready Android project template with **Clean Architecture**, **vertical feature slicing**, **MVVM**, and **Jetpack Compose**. Extracted from a real app serving 40,000+ users.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                        app/                         │
│   Application, MainActivity, NavHost, Room DB, DI   │
└──────────┬──────────────┬───────────────┬───────────┘
           │              │               │
           ▼              ▼               ▼
┌──────────────┐  ┌──────────────┐  ┌─────────────────┐
│   feature/   │  │   shared/    │  │     core/        │
│              │  │              │  │                   │
│  auth        │  │  auth        │  │  auth (tokens)    │
│  onboarding  │  │  preference  │  │  network (http)   │
│  notes       │  │  user        │  │  ui (theme)       │
│  profile     │  │              │  │  analytics (stub)  │
│  settings    │  │              │  │  notification (stub)│
│              │  │              │  │  util              │
└──────┬───────┘  └──────┬───────┘  └───────────────────┘
       │                 │                    ▲
       │                 │                    │
       └─────────────────┴────────────────────┘
              features & shared depend on core
              (core depends on nothing)
```

### Package Structure

```
com.example.app/
├── app/          ← Entry point: Application, MainActivity, NavHost, Room DB
├── core/         ← Infrastructure: auth, network, theme, utilities
├── shared/       ← Cross-feature business logic: auth, preferences, user
└── feature/      ← Vertical feature modules (self-contained)
    ├── auth/
    ├── notes/    ← Full CRUD sample (Room + API + UseCases + Compose)
    ├── onboarding/
    ├── profile/
    └── settings/ ← Minimal sample (preferences only, no API)
```

### Layer Responsibilities

| Layer | Purpose | Depends On |
|-------|---------|-----------|
| **app/** | Wires everything together. Application, Activity, NavHost, Room DB, DI modules | core, shared, feature |
| **core/** | Platform infrastructure. Auth tokens, network client, interceptors, theme, analytics stubs | Nothing (leaf) |
| **shared/** | Business logic used by multiple features. Auth flow, user profile, preferences | core |
| **feature/** | Self-contained vertical slices. Each has data/domain/presentation + DI | core, shared |

### Feature Internal Structure (Clean Architecture)

Each feature follows the same pattern:

```
feature/notes/
├── data/                    ← HOW (implementation details)
│   ├── local/               ← Room DAOs & Entities
│   ├── remote/              ← Retrofit API interfaces
│   └── repositories/        ← Repository implementations
├── di/                      ← Hilt module for this feature
├── domain/                  ← WHAT (business rules)
│   ├── models/              ← Domain models
│   └── usecases/            ← Business operations
└── presentation/            ← UI
    ├── screens/             ← Compose screens
    └── viewmodels/          ← State holders (MVVM)
```

## Tech Stack

| Category | Library |
|----------|---------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture + Use Cases |
| DI | Hilt (compile-time) |
| Navigation | Navigation Compose + centralized NavigationManager |
| Network | Retrofit + OkHttp + Gson |
| Local DB | Room |
| Preferences | DataStore |
| Auth Storage | EncryptedSharedPreferences (with AEAD fallback) |
| Images | Coil 3 |
| Testing | JUnit + MockK + Turbine + Coroutines Test |

## Quick Start

```bash
# 1. Clone
git clone https://github.com/user/android-clean-arch-template.git
cd android-clean-arch-template

# 2. Rename package (required)
./setup.sh com.mycompany.myapp "My App"

# 3. Open in Android Studio and run
```

## Key Patterns

### NavigationManager (Centralized Navigation)

A singleton `NavigationManager` uses a `Channel<NavigationCommand>` to emit navigation events from anywhere (ViewModels, UseCases). `MainActivity` observes and executes them on the `NavController`.

```kotlin
// From any ViewModel:
navigationManager.navigate(AppDestinations.NOTES_LIST)
navigationManager.navigateAndClearBackStack(AppDestinations.LOGIN)
navigationManager.popBackStack()
```

### RepositoryResult + safeApiCall

All API calls return `RepositoryResult<T>` — a sealed class with `Success` and `Error`. The `safeApiCall` wrapper handles exceptions, parses error bodies, and maps HTTP codes.

```kotlin
// In repository:
suspend fun getNotes(): RepositoryResult<List<Note>> = safeApiCall { api.getNotes() }

// In ViewModel:
when (val result = getNotesUseCase()) {
    is RepositoryResult.Success -> _state.value = UiState.Success(result.data)
    is RepositoryResult.Error -> _state.value = UiState.Error(result.message)
}
```

### AuthInterceptor (Automatic Token Refresh)

The `AuthInterceptor` handles:
- **Optimistic refresh**: Refreshes the token 5 minutes before expiry (no 401s)
- **Bearer injection**: Adds `Authorization` header to all non-public routes
- **401 handling**: Auto-logout on unauthorized responses
- **Version header**: Sends `X-App-Version` with every request

### TokenManager (Encrypted Storage)

Stores auth tokens in `EncryptedSharedPreferences` with:
- AEAD corruption detection and automatic recovery
- In-memory fallback if encryption is completely broken
- Background thread initialization to prevent ANRs

## Adding a New Feature

1. Create the directory structure:
```
feature/myfeature/
├── data/
│   ├── remote/MyFeatureApi.kt
│   └── repositories/MyFeatureRepository.kt
├── di/MyFeatureModule.kt
├── domain/
│   ├── models/MyModel.kt
│   └── usecases/GetMyDataUseCase.kt
└── presentation/
    ├── screens/MyFeatureScreen.kt
    └── viewmodels/MyFeatureViewModel.kt
```

2. Create the Retrofit API interface in `data/remote/`
3. Create the repository in `data/repositories/` using `safeApiCall`
4. Create use cases in `domain/usecases/`
5. Create a Hilt module in `di/` to provide the API
6. Create the ViewModel in `presentation/viewmodels/`
7. Create the Compose screen in `presentation/screens/`
8. Add a route in `AppDestinations` and a `composable()` in `AppNavigation`

See `feature/notes/` for a complete reference implementation.

## Customization

| What | Where |
|------|-------|
| Package name | Run `./setup.sh` |
| API base URL | `app/build.gradle.kts` → `buildConfigField("String", "BASE_URL", ...)` |
| Theme colors | `core/ui/theme/Color.kt` |
| Typography | `core/ui/theme/Type.kt` |
| App name | `app/src/main/res/values/strings.xml` |
| Navigation routes | `app/navigation/AppDestinations.kt` |

## Optional Integrations

### Firebase
1. Add `google-services` plugin to root and app `build.gradle.kts`
2. Add Firebase BOM + desired libraries to `libs.versions.toml`
3. Place `google-services.json` in `app/`
4. Implement `AnalyticsService` interface with Firebase Analytics

### Push Notifications
1. Add Firebase Messaging dependency
2. Implement `PushTokenProvider` interface
3. Create `FirebaseMessagingService` subclass

### In-App Purchases
1. Add Google Play Billing library
2. Create `billing/` package in `core/`
3. Follow the Notes feature pattern for subscription management

## License

MIT License - see [LICENSE](LICENSE)
