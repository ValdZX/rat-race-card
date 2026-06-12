# CLAUDE.md

Guidance for working in this repository.

## What this is

**Rat-race-card** — a Kotlin Multiplatform (KMP) + Compose Multiplatform companion app for a "Cashflow / Rat Race" style board game. It tracks each player's finances (cash, deposit, loan, business, land, shares, funds, expenses) and supports two distinct game modes plus an online multiplayer backend.

Targets: **Android, iOS, Desktop (JVM), Web (Wasm/JS)**. App version is set in `composeApp/build.gradle.kts` (`version = "3.4"`).

## Modules

| Module | Purpose |
|--------|---------|
| `composeApp` | The multiplatform client (all UI, game logic, local state). Main module. |
| `shared` | KMP library shared between client and server: data models (`Board`, `Player`, `Cards`, `Auction`, etc.) and the **kotlinx-rpc** service interfaces (`RaceRatService`, `RaceRatCardService`). |
| `server` | Ktor + Netty JVM backend hosting the kRPC services over WebSockets. Persists to MongoDB / Google Firestore + Cloud Storage. |
| `iosApp` | Xcode project wrapping the `ComposeApp` framework. |

`docs/` is **generated build output** (the Wasm production bundle published to GitHub Pages by the `buildDist` Gradle task) — do not hand-edit it. `proto/` is empty/scratch. `temp.txt` is scratch.

## Run mode switch (compile-time)

`App()` (`composeApp/.../card/App.kt`) picks the entry flow at compile time via `BuildConfig.CARD_ONLY_MODE`:

- **Full app** (default) → `FullApp()` — `SelectTypeScreen`, online board + player card.
- **Card-only prod build** → `CardOnlyApp()` — only the player-card flow (`PersonCard2Screen` / `RaceRate2Screen`).

The flag is a generated `BuildConfig` field (gmazzo buildconfig plugin) driven by the `cardOnly` Gradle property. To switch, pass `-PcardOnly=true` on any task or add `cardOnly=true` to `gradle.properties` / `local.properties` — no code edits. Config lives in the `buildConfig {}` block of `composeApp/build.gradle.kts`; it's generated into `commonMain` and wired into the Kotlin source set right after that block.

## Two game modes (important)

The app contains two largely separate gameplay systems. Don't conflate them:

1. **Offline / single-device card mode** — local state managed by a hand-rolled Redux store, `RatRace2CardStore` (`composeApp/.../logic/RatRace2CardStore.kt`). State persisted locally via **KStore** JSON files (`Storages.kt`). Screens live under `screen/second/`. Optional lightweight P2P money-passing uses `RaceRatCardService`.
2. **Online board mode** — full multiplayer board game driven by the server. Client state in `BoardViewModel` (`logic/BoardViewModel.kt`), talking to the server via `RaceRatService`. Screens under `screen/board/`. Server is authoritative; the client observes `Event`/`GlobalEvent` flows.

## Code style

Write self-explanatory code instead of comments. Do **not** add explanatory comments — prefer clear names, small functions, and obvious structure so the code reads on its own. Only keep a comment when it carries information the code genuinely cannot (e.g. a required external link, a non-obvious workaround reason). Match the surrounding style of the file you edit.

## Architecture patterns

- **State management (offline):** custom minimal Redux in `logic/NanoRedux.kt` — `State` / `Action` / `Effect` interfaces and a `Store<S,A,E>` with `observeState()` / `observeSideEffect()` / `dispatch()`. `RatRace2CardStore.dispatch()` is one big `when(action)` reducer producing a new `RatRace2CardState`; one-shot UI events go through `sideEffect`.
- **State management (online):** `BoardViewModel` (AndroidX `ViewModel`) holds `BoardState`, dispatches `BoardUiAction`, and reacts to server `Event` streams.
- **RPC:** `kotlinx-rpc` (kRPC) over Ktor WebSockets, JSON serialization. Service interfaces are annotated `@Rpc` in `shared/`. Client stubs are built in `di/module.kt` (`getRaceRatService()` / `getRaceRatCardService()`); server impls are `RaceRatServiceImpl` / `RaceRatCardServiceImpl`.
- **DI:** Koin (`di/module.kt`, `baseModule`). `App.kt` wraps the UI in `KoinApplication`.
- **Navigation:** Voyager (`Navigator` / `CurrentScreen`, screens are `Screen` classes). Entry screen is `SelectTypeScreen`.
- **Localization:** Localina (`LocalinaApp`). UA/EN.
- **Theming:** `theme/` with `expect`/`actual` per platform; MaterialKolor for dynamic color. Dark mode via `LocalThemeIsDark`.
- **Platform code:** `expect`/`actual`. Common declarations in `App.kt` (`platformContext`, `openUrl`, `share`, `getTts`, `vibrateClick`, `getStore`) with `actual`s in `androidMain` / `iosMain` / `jvmMain` / `wasmJsMain`.

## Where things live (`composeApp/src/commonMain/.../card/`)

- `logic/` — Redux store, reducer, `BoardViewModel`, `Statistics`.
- `screen/second/` — offline card-mode screens (+ `page/` tabs).
- `screen/board/` — online board-mode screens; `cards/` = card deck definitions & logic, `deck/` = card rendering, `page/` = asset tabs, `visualize/` = board drawing.
- `beans/Models.kt` — offline-mode domain models (`Business`, `Land`, `Shares`, `Fund`, `Config`, `PlayerCard`).
- `components/` — reusable Compose widgets.
- `resource/images/` — **vector icons authored as Kotlin `ImageVector` code** (large, low-signal; skip when exploring).
- `theme/`, `di/`, `Storages.kt`, `Utils.kt`.

Shared models the server also uses live in `shared/.../shared/` (`Board.kt`, `Players.kt`, `Cards.kt`, `Auction.kt`, `Place.kt`, `ProfessionCard.kt`).

## Build & run

JDK 17+ required. Set Android SDK path in `local.properties`.

```bash
# Desktop (hot reload run configs "🔥Desktop 1/2" exist in .idea)
./gradlew :composeApp:run

# Android debug APK -> composeApp/build/outputs/apk/debug/RatRaceCard.apk
./gradlew :composeApp:assembleDebug

# Web (Wasm) dev server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun --continue

# Publish web bundle into docs/ (GitHub Pages)
./gradlew buildDist

# Server (Ktor)
./gradlew :server:run          # mainClass: io.ktor.server.netty.EngineMain, port 8080

# iOS: open iosApp/iosApp.xcodeproj in Xcode, or run from Android Studio KMP plugin.
```

Tests:
```bash
./gradlew :composeApp:jvmTest                  # desktop UI tests
./gradlew :composeApp:iosSimulatorArm64Test    # iOS UI tests
./gradlew :composeApp:wasmJsBrowserTest        # web tests
./gradlew :composeApp:pixel5Check              # Android instrumented UI tests
./gradlew :server:test                         # server tests
```

There is no CI config in-repo; verify changes by building the relevant target.

## Gotchas / conventions

- **`di/module.kt` hardcodes `apiUrl`** to a LAN address (`ws://192.168.0.159:8080/api`) with prod URLs commented out. This is the active server endpoint the client connects to — update it for your environment; don't assume it points at production.
- Adding/changing a `@Rpc` method means editing **both** the interface in `shared/` and the impl in `server/`, and (for client) the call sites in the store/view model.
- New `Action`s go in the `RatRace2CardAction` sealed class **and** the `dispatch` `when` in `RatRace2CardStore.kt`. New one-shot UI events go in `RatRace2CardSideEffect`.
- Persisted local state is plain JSON via KStore; changing `@Serializable` model fields can break existing saved files — keep defaults on new fields.
- `resource/images/*.kt` are generated-style vector assets — large and noisy; the user has asked to ignore resource-heavy dirs during analysis.
- Server config/secrets: `.env` (see `.env.example` — `MONGODB_URI`, `MONGODB_DATABASE`, `ALLOWED_ORIGINS`); MongoDB creds also referenced in `gradle.properties`; Android signing config is inline in `composeApp/build.gradle.kts`. Treat all of these as secrets — don't echo or commit new ones.
- The Android `release` build uses minify + shrink (`proguard-rules.pro`).

## Tech stack

Kotlin 2.3.21 · Compose Multiplatform 1.10.3 · AGP 8.12.3 · Koin · Voyager · kotlinx-rpc 0.10.2 · Ktor 3.4.3 · kotlinx-coroutines/serialization/datetime · KStore · MongoDB driver + Google Cloud Firestore/Storage · Napier (logging) · Firebase Analytics/Crashlytics (Android) · MaterialKolor · Compottie (Lottie) · lexilabs sound/haptic · TTS. Versions are centralized in `gradle/libs.versions.toml` (use the `libs.*` accessors).
