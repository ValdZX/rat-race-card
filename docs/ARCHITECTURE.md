# Поточна архітектура

## Платформи й технології

Rat Race Card — Kotlin Multiplatform застосунок із Compose Multiplatform для Android, iOS, Desktop JVM і Web/Wasm. Онлайн-сервер працює на Ktor/Netty JVM. Зв'язок клієнта із сервером реалізований через kotlinx-rpc поверх WebSocket, моделі серіалізуються kotlinx.serialization.

## Модулі

| Модуль | Відповідальність |
|---|---|
| `composeApp` | Точки входу платформ, DI-композиція, вибір режиму, очікування налаштувань і локалі. |
| `core` | Тема, компоненти, ресурси, платформні `expect/actual`, RPC-клієнти, KStore й базовий Redux. |
| `card` | Локальний режим фінансової картки, `RatRace2CardStore`, локальні екрани, adapter до спільних фінансів. |
| `board` | Онлайн-клієнт: `BoardViewModel`, дошка, картки, діалоги, сторінки активів і візуалізація. |
| `shared` | Доменне ядро (рушій, реєстри, фінанси, feature-пакети), спільні серіалізовані моделі й RPC-контракти. |
| `server` | RPC-транспорт, транзакції, зберігання, генерація й залишкові legacy-правила. |

`card` і `board` не залежать один від одного. Обидва використовують `core`; `server` залежить від `shared`, але не від клієнтського модуля `board`.

## Доменне ядро в `shared`

Ядро не залежить від Compose, Ktor, Storage та event bus.

```text
GameCommandEnvelope
  → GameApplicationService  (mutex дошки, load, save, лог)
  → GameEngine.execute      (чистий, без I/O)
  → CellRuleRegistry / EffectHandlerRegistry
  → RuleResult(snapshot, domainEvents, notices)
```

- `GameSnapshot` — дошка плюс гравці; єдиний вхід і вихід рушія.
- `GameEngine` повертає `GameExecution`: `Applied`, `Duplicate` або `Rejected` із `GameCommandRejection`.
- `Board.revision` зростає на кожній прийнятій команді; `expectedRevision` відхиляє застарілу, `processedCommandIds` робить повтор ідемпотентним.
- `RuleResult` розділяє `DomainEvent` і `PresentationNotice`.
- `GameRandom` і `GameClock` передаються ззовні, тому сценарії відтворювані.
- `GameCommandLog` пише один рядок на команду з ревізіями, версіями схеми/правил/пакетів і тривалостями.

Фінансові формули спільні для обох режимів: `FinancialAccount`, `FinancialSnapshot`, `MoneyService`, `PaymentPolicy`. Локальний режим підключається через `OfflineFinanceAdapter` і власних формул не має.

## Реєстри замість центрального `when`

| Реєстр | Ключ | Що дає |
|---|---|---|
| `CellRuleRegistry` | `CellTypeId` (`core.salary`, `corruption.tax_inspection`) | `onPass` / `onLand` без правок рушія |
| `EffectHandlerRegistry` | `EffectTypeId` (`core.pay_amount`) | ефекти декларативних карток |
| `CellPresentationRegistry` | `CellTypeId` | назва, іконка, колірний токен, звук |
| `FeatureRegistry` | `FeatureId` + версія | складання рушія з набору пакетів |
| `CardInteractionRendererRegistry` | `InteractionKindId` | вибір generic чи спеціалізованого Compose renderer'а |

Усі реєстри падають на дубльованому ID і дають явну помилку на незареєстрованому. Незнайомий тип клітинки не підмінюється іншим — він стає `PlaceType.Custom`.

## Онлайн-потік даних

```text
Compose UI
  → BoardViewModel
  → RaceRatService RPC
  → RaceRatServiceImpl
  → GameApplicationService → GameEngine → Storage   (команди рушія)
  → updatePlayer / updateBoard → Storage            (залишкові legacy-методи)
  → Event / GlobalEvent
  → BoardViewModel
  → StateFlow<BoardState>
  → Compose UI
```

`executeCommand(envelope)` — узагальнений вхід. Від клієнта приймаються `RollDice`, `EndTurn` і `ChooseInteraction`; `CompleteRoll`, `AdvanceTurn`, `MoveTo` та `StartCard` внутрішні.

Сервер оновлює гравця під mutex гравця, а дошку — під mutex дошки. `GlobalEvent` синхронізує всіх учасників, `Event` передає персональні повідомлення й UI-ефекти.

## Авторитетність правил

Правила руху, черги, клітинок і мігрованих карток виконує `GameEngine`. Решта сценаріїв (активи, аукціон, ринкові події, депутати, корупція) поки живе в `RaceRatServiceImpl` як legacy-методи; вони мутують стан напряму й не проходять через `revision` та `processedCommandIds`.

Серверні інваріанти добровільної купівлі винесені в `LegacyRuleGuards`: `canMakeVoluntaryPurchase`, `hasAvailableCredit`, `canBuyBusiness` з `businessLimit`, `canBuyWithCashAndDeposit`. Клієнт дублює `canBuy`, `needPayExpenses` і `canMakeBid` лише для доступності кнопок.

## Треки й геометрія

`PlayerLocation(position, trackId)`. Дошка описується списком `TrackDefinition` довільної довжини, переходи — `TrackTransition` з декларативними `ProgressCondition`, умови перемоги — `ObjectiveDefinition`.

`TrackLayoutEngine` вкладає будь-яку кількість треків у viewport за `order` і `TrackVisualHint`; dp-координати в дані дошки не потрапляють. Клієнтський рендеринг звіряється з `TrackId`, а не з `BoardLayer`.

`BoardLayer` лишається як legacy-константа для двох базових кіл: дефолтні списки клітинок, `generatedPlaces` і `trackDefinitions`. `legacyLayerOrNull()` використовується тільки в null-safe місцях UI.

## Контент карток

Мігровані колоди описуються `CardDefinition` з `InteractionSpec` і `EffectSpec`; сервер публікує `PendingInteraction`, клієнт відповідає `ChooseInteraction`. Немігровані типи лишаються `sealed class BoardCard` зі своїми RPC-методами.

Локалізований текст відділений від механіки. `GeneratedText` зберігає назви й описи за локалями; клієнт накладає їх на механічну картку. Якщо згенерованої картки немає, `board` використовує дефолтну мапу колоди.

## Генерація

Генерація має два шари:

1. LLM створює й проходить валідацію `GeneratedBalance`.
2. Детермінований `BoardGenerator` із seed будує механічні картки, професії, мрії та порядок клітинок.

Тексти генеруються окремо й можуть доганяти механіку через збережені чекпоінти. Клієнт слухає легкий прогрес і отримує повну дошку після значущих змін.

## Зберігання, версії й сумісність

Онлайн-стан зберігається через серверний `Storage` у MongoDB або Firestore/Cloud Storage. Кожен snapshot містить `schemaVersion`, `rulesVersion`, `contentPackVersions` і `revision`.

`BoardSnapshotMigrator` виконує послідовні чисті кроки `vN → vN+1`. Snapshot новішої схеми відхиляється. Міграція «на місці» вимагає явного підтвердження й спершу пише оригінал у `board_snapshot_backup`. `Board.requireValidFeatures()` не дає відкрити партію, для пакетів якої немає handler'ів.

Локальний режим використовує JSON у KStore. Його persistence DTO лишаються локальними, але фінансові формули спільні.

## Залишковий борг

| Борг | Наслідок |
|---|---|
| ~60 legacy RPC-методів поза рушієм | `revision`/ідемпотентність покривають лише команди рушія |
| `BoardCard` паралельно з `CardDefinition` | дві моделі контенту одночасно |
| `generatedPlaces` + `trackDefinitions` + `tracks` у `Board` | три представлення розкладки, розв'язуються в `resolvedTracks()` |

Порядок згортання цього боргу описаний у [ARCHITECTURE_REFACTORING.md](ARCHITECTURE_REFACTORING.md).
