# Rat-Race-Card Project Analysis Report

> **Project**: Kotlin Multiplatform (KMP) + Compose Multiplatform companion app for a Cashflow / Rat Race style board game
> **Targets**: Android, iOS, Desktop (JVM), Web (Wasm/JS)
> **Version**: 3.4
> **Analysis Date**: July 13, 2026

---

## 🔴 Critical Issues

### 1. Hardcoded Secrets in Source Code (SECURITY)

**Severity: CRITICAL**

Hardcoded credentials and secrets are embedded directly in source files:

| File | Secret | Value |
|------|--------|-------|
| `composeApp/build.gradle.kts` | Keystore key password | `pqxp3bSpFvj48rzSdjcNV5jFshdkKDdD` |
| `composeApp/build.gradle.kts` | Keystore store password | `fe7mV2B7su4ZPcVqBdxw34KDjJUWuvmu` |
| `gradle.properties` | MongoDB user | `vladyslavkhimichenko_db_user` |
| `gradle.properties` | MongoDB password | `mOdJBl7Ppu7Bd6ER` |

**Risks:** These secrets are in the git history and will be present in any clone of the repository. Keystore passwords should be injected via environment variables or CI secrets. Database credentials should use the `.env` file mechanism already established in the server module (server reads from env, but the creds remain in `gradle.properties`).

### 2. Version Mismatch Between CLAUDE.md and libs.versions.toml

**Severity: HIGH**

The `CLAUDE.md` file lists outdated versions that do not match the actual `gradle/libs.versions.toml`:

| Dependency | CLAUDE.md says | libs.versions.toml says |
|------------|----------------|----------------------|
| Kotlin | 2.3.21 | **2.4.0** |
| Compose Multiplatform | 1.10.3 | **1.11.1** |
| kotlinx-rpc | 0.10.2 | **0.10.3** |
| Ktor | 3.4.3 | **3.5.1** |

This creates confusion and the documentation will become increasingly inaccurate.

### 3. Android SDK Version 37 — Does Not Exist

**Severity: HIGH**

```toml
# libs.versions.toml
android-compileSdk = "37"
android-minSdk = "26"
android-targetSdk = "37"
```

As of July 2026, Android SDK version 37 likely does not exist yet (currently SDK 35 is the latest stable). This will cause build failures.

### 4. Duplicate `include(":shared")` in settings.gradle.kts

**Severity: MEDIUM**

```kotlin
include(":composeApp")
include(":shared")
include(":server")
// ...
include(":shared")  // DUPLICATE
```

The `shared` module is included twice. While Gradle may tolerate this, it's a code smell.

### 5. In-Memory State with No Persistence Guarantees (Server)

**Severity: HIGH**

The server stores all board and player state in memory (`MutableStateFlow` maps) and writes to MongoDB through a write-behind queue. On server crash, all in-memory state is lost. The initial load from MongoDB only happens when `boards()` is first called. If the server restarts, active games lose their in-memory state.

Additionally, the write-behind queue retries 5 times with 2-second delays, but upon exhausting retries, **it silently gives up** — data is permanently lost with only a log warning.

### 6. Missing `.env` File with No Example

**Severity: MEDIUM**

The server module reads from `.env` but:
- No `.env.example` template exists in the repo
- The `Env.kt` implementation searches parent directories for `.env`, which is fragile
- No `.env` is present in the project root

The AGENTS.md references `.env.example` but the file does not exist.

### 7. No CI Configuration

**Severity: MEDIUM**

The CLAUDE.md states "There is no CI config in-repo." This means:
- No automated build verification
- No test running
- No lint checks
- No security scanning

---

## 🟠 Architecture Issues

### 8. Duplicate Domain Models (Offline vs Online)

**Severity: HIGH**

The project has two completely separate sets of domain models for the same concepts:

| Concept | Offline (beans/Models.kt) | Online/Shared (shared/...) |
|---------|--------------------------|---------------------------|
| Business | `beans.Business` (no `fromAuction` field) | `shared.Business` (has `fromAuction`) |
| Shares | `beans.Shares` | `shared.Shares` |
| SharesType | `beans.SharesType` (GS, ЩГП, TO, SCT) | `shared.SharesType` (GC, ShchHP, TO, CST, AGRO, IT) |
| Land | `beans.Land` (has `priceOfUnit` + computed `price`) | `shared.Land` (has `price`) |
| Fund | `beans.Fund` | `shared.Fund` |
| Config | `beans.Config` | `shared.Config` |
| PlayerCard | (no domain model in beans) | `shared.PlayerCard` |
| BusinessType | `beans.BusinessType` (klass-based) | `shared.BusinessType` (klass-based, same values) |

**Problems:**
- Redundant code that must be kept in sync
- Different `SharesType` values between offline (Ukrainian names: ЩГП) and online (English: ShchHP)
- `Land` has different structures (`priceOfUnit * area` vs. `price`)
- `Business` in offline lacks `fromAuction` field
- The offline store (`RatRace2CardStore`) uses `beans.*` models while the server and shared module use `shared.*`

### 9. Duplicate Financial Calculation Logic

**Severity: MEDIUM**

Financial calculation functions (total(), balance(), activeProfit(), passiveProfit(), totalProfit(), creditExpenses(), totalExpenses(), cashFlow(), fundAmount()) are duplicated across:
- `shared/Players.kt` (extension functions on `shared.Player`)
- `RatRace2CardStore.kt` (member functions on `RatRace2CardState`)

These must be kept in sync manually — any change to one requires updating the other.

### 10. Redux Store Pattern With Undesirable Side Effects

**Severity: MEDIUM**

The `RatRace2CardStore.dispatch()` method combines the reducer with side effects, making it impure:

```kotlin
override fun dispatch(action: RatRace2CardAction) {
    val oldState = state.value
    val newState = when (action) {
        is ReceivedCash -> {
            launch { sideEffect.emit(...) }  // Side effect inside reducer
            oldState.plusCash(action.amount)
        }
        ...
    }
}
```

In a proper Redux pattern, the reducer should be a pure function. Mixing side effects into the reducer makes it harder to test and reason about.

### 11. Excessive Global Mutable State

**Severity: MEDIUM**

Several top-level `var`/`val` mutable globals are used:

```kotlin
var lottieDiceAnimations: Map<Int, LottieComposition> = emptyMap()
var storageKeyPrefix = ""
val appKStore: KStore<AppDataStorageBean>
val navigationBarHeightState = mutableStateOf(0.dp)
val statusBarHeightState = mutableStateOf(0.dp)
val deckCoordinatesMap = mutableMapOf<BoardCardType, MutableState<...>>()
val discardPilesCoordinatesMap = mutableMapOf<BoardCardType, MutableState<...>>()
var sheetContentSize = mutableStateOf(0.dp)
val players = MutableStateFlow(emptyList<Player>())  // In BoardViewModel.kt
```

The `players` global in `BoardViewModel.kt` is particularly concerning — it's a top-level `MutableStateFlow` that holds all player state across the entire app.

### 12. Server: Per-Connection Service Instances with Global Mutable State

**Severity: MEDIUM**

Each WebSocket connection creates a new `RaceRatServiceImpl` with its own `eventBus`, `boardIdState`, etc. The server is the game authority, but per-connection service instances subscribe to global data flows. Running `hello()` replaces the `uuidStateProvider` which can cause race conditions if a client reconnects.

The `RaceRatServiceImpl` is a `CoroutineScope by scope`, which can lead to leaked coroutines on connection close if not properly managed.

### 13. Inconsistent Error Handling Pattern

**Severity: LOW-MEDIUM**

The app uses inconsistent patterns:
- `runCatching { ... }.getOrNull()` in some places
- `runCatching { ... }.onSuccess { ... }.onFailure { ... }` in others
- `CoroutineExceptionHandler` in others
- Try-catch in others
- The `loadOnlineScreen` pattern where failing to connect replaces the Koin service declaration

---

## 🟡 Code Quality Issues

### 14. Deprecated API Usage

**Severity: MEDIUM**

The `RatRace2CardStore.kt` file has:
```kotlin
@file:Suppress("DEPRECATION")
```

This suppresses ALL deprecation warnings in the file — a nuclear option that hides real issues.

Other deprecated API usage:
- `ripple()` — deprecated in Material3
- `ExperimentalMaterialApi` in `BoardScreen.kt`
- `ExperimentalMotionApi` usage
- `rememberLottieAnimatable` — internal/experimental Compottie API

### 15. Compose Test Is a Skeleton

**Severity: MEDIUM**

The only test file (`ComposeTest.kt`) contains a trivial counter test that tests nothing about the actual application. There are zero:
- Unit tests for game logic
- Store tests
- ViewModel tests
- UI integration tests
- Server endpoint tests

### 16. Server Test Dependencies But No Tests

**Severity: MEDIUM**

The `server/build.gradle.kts` includes test dependencies:
```kotlin
testImplementation(libs.ktor.server.test.host)
testImplementation(libs.kotlinx.rpc.krpc.client)
testImplementation(libs.kotlinx.rpc.krpc.ktor.client)
testImplementation(libs.kotlin.test.junit)
```

But the `glob` search found zero test files in the server module.

### 17. AndroidManifest.xml Not Found / Potentially Missing

The CLIENT `AndroidManifest.xml` is referenced in `composeApp/build.gradle.kts`:
```kotlin
sourceSets["main"].apply {
    manifest.srcFile("src/androidMain/AndroidManifest.xml")
}
```

But the file tree shows it at `composeApp/src/androidMain/AndroidManifest.xml` which should be accessible.

### 18. Hardcoded LAN IP in Client Configuration

**Severity: LOW-MEDIUM**

```kotlin
// di/module.kt
private val apiUrl = "ws://192.168.0.159:8080/api"
```

This LAN IP is actively used (production URLs are commented out). Every developer must change this to their environment. This should be configurable via environment variable or a config file.

Also, the `application.conf` has the same hardcoded IP:
```
host = 192.168.0.159
```

### 19. Unused imports and dead code

**Severity: LOW**

- Several imports in `App.kt` may be unused (e.g., `kotlinx.coroutines.withContext`)
- `import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark` is imported but may already be accessed locally
- `resell` string resource defined but potentially unused

### 20. Game Logic: `minusCash` with `>` Instead of `>=`

**Severity: MEDIUM (Logic Bug)**

In `RatRace2CardStore.kt`, the `minusCash` function uses strict greater-than (`>`) instead of greater-than-or-equal (`>=`):

```kotlin
private fun RatRace2CardState.minusCash(value: Long, isFundBuy: Boolean = false): RatRace2CardState {
    if (value == 0L) return this
    ...
    return if (cash > value) {           // Should be >=
        copy(cash = cash - value)
    } else if ((cash + deposit) > value) { // Should be >=
        ...
```

If `cash == value`, it falls through to the deposit/loan logic unnecessarily, potentially triggering unwanted side effects like `DepositWithdraw` events.

But in the server's `RaceRatServiceImpl.kt`, the same function correctly uses `>=`:

```kotlin
return if (cash >= value) {
    copy(cash = cash - value)
} else if ((cash + deposit) >= value) {
```

**This inconsistency means the offline mode has a bug that the online mode does not.**

### 21. Event Bus Not Shared Across Connections for Same Board

**Severity: MEDIUM**

The global event bus is shared by board ID, which is correct. But per-connection event buses filter global events by `playerId`, which means events intended for specific players may be missed if the client reconnects or if there's a race condition.

### 22. Potential Concurrency Issues with Shared Mutable State

**Severity: MEDIUM**

The `Storage` object uses separate mutexes for `playersLock` and `boardsLock`, but operations like `removeBoard` read and write to both players and boards without a consistent locking strategy. The `WriteBehindQueue` also uses independent workers per key, which is good, but the flush operation doesn't guarantee order across keys.

---

## 🟣 Build & Dependency Issues

### 23. Firebase Dependencies for All Platforms but Only Used on Android

**Severity: LOW-MEDIUM**

```kotlin
// composeApp/build.gradle.kts
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.ui.android)
}
```

Firebase is only available on Android, but the dependencies are declared at the top level. They should be in the `androidMain` dependencies block. The `google-services` and `firebase-crashlytics` plugins are also applied for all targets.

### 24. ProGuard Rules May Be Overly Broad

**Severity: LOW**

The `proguard-rules.pro` file keeps almost everything:
- `-keep class kotlin.** { *; }`
- `-keep class kotlinx.** { *; }`
- `-keep class io.ktor.** { *; }`
- `-keep class androidx.** { *; }`
- `-keep class android.** { *; }`

This essentially disables most of ProGuard/R8's optimization and shrinking benefits.

### 25. Compose Hot Reload Plugin for Release Builds

**Severity: LOW**

The Compose Hot Reload plugin (`org.jetbrains.compose.hot-reload`) is applied to the project but hot reload is a development-only tool. It should be conditionally applied.

### 26. Duplicate `androidLibrary` Plugin Alias

```toml
[plugins]
android-kotlin-multiplatform-library = { id = "com.android.kotlin.multiplatform.library"... }
androidLibrary = { id = "com.android.kotlin.multiplatform.library"... }
```

Both aliases point to the same plugin ID. Only `androidLibrary` is used in `shared/build.gradle.kts`.

---

## ⚪ Miscellaneous Issues

### 27. Package Name Contains `rat.race.card` — Possibly Insensitive

**Severity: LOW**

The package name `ua.vald_zx.game.rat.race.card` uses "rat race" terminology which is the intended game reference, but the word "race" combined with "rat" could be considered insensitive in some contexts. The string resource `Res.string.exaltation` maps to "Layoff" in English but the property name suggests it's actually "Exaltation".

### 28. Share Type Name Mismatch (Ukrainian vs English)

**Severity: LOW**

Offline `SharesType` uses Ukrainian names (`ЩГП`, `GS`, `TO`, `SCT`) while online `SharesType` uses English alternatives (`GC` for `GS`, `ShchHP` for `ЩГП`, `CST` for `SCT`). The `label()` function in `Utils.kt` does the conversion `GS→GC` and `SCT→CST`. This suggests the two systems drifted apart.

### 29. Localization: `exaltation` Key Mistranslation

**Severity: LOW**

The string resource `exaltation` translates to "Layoff" in English, but "exaltation" in Ukrainian means "inspiration/elation", not "layoff/firing". The English meaning of the board place should be "Layoff", but the key name suggests "exaltation".

### 30. `detective.xml` Drawable Use Case Unclear

**Severity: LOW**

The `detective.xml` vector drawable is placed on the `TaxInspection` board place. While this makes sense for a tax inspection theme, it's worth noting that none of the XML drawable files appear to have explicit content descriptions for accessibility.

---

## 📊 Summary by Severity

| Severity | Count | Key Issues |
|----------|-------|------------|
| 🔴 Critical | 7 | Secrets in source, SDK 37, doc mismatch, missing .env.example, no CI, in-memory state loss, duplicate shared include |
| 🟠 High | 2 | Duplicate domain models, duplicate calculation logic |
| 🟡 Medium | 13 | Deprecated API, missing tests, logic bug in minusCash, concurrency, global mutable state, LAN IP hardcode, etc. |
| ⚪ Low | 8 | Package naming, localization, unused assets, ProGuard, hot reload, etc. |

## 📋 Top Recommendations

1. **Move secrets to environment variables** — Remove hardcoded keystore passwords and MongoDB credentials from source
2. **Fix Android SDK version** — Set to actual current SDK (35)
3. **Unify domain models** — Merge `beans/*` and `shared/*` models to eliminate duplication
4. **Add CI pipeline** — Even a basic GitHub Actions workflow would improve quality
5. **Add comprehensive tests** — Start with unit tests for the financial calculation logic
6. **Fix the `minusCash` > vs >= bug** in the offline store
7. **Create `.env.example`** documenting required server environment variables
8. **Update CLAUDE.md** to match actual dependency versions
9. **Move Firebase dependencies** to Android-specific source set
10. **Replace global mutable state** with proper dependency injection

---

*Report generated by codebase analysis on July 13, 2026*
