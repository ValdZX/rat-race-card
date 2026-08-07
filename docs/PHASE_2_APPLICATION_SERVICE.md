# Фаза 2: application service навколо рушія

Чистий рушій і транзакційний шар живуть у `shared` і не залежать від Ktor, Storage, Compose та затримок.

## Межі

- `GameSnapshot` — дошка плюс список гравців; єдиний вхід і вихід рушія.
- `GameEngine.execute(snapshot, envelope)` не виконує I/O і повертає `GameExecution`: `Applied`, `Duplicate` або `Rejected` із `GameCommandRejection`.
- `GameApplicationService` бере mutex дошки, завантажує snapshot, виконує команду, зберігає новий стан і повертає результат. Repository описаний інтерфейсом `GameRepository`; серверна реалізація — `StorageGameRepository`.
- `RuleResult` розділяє `DomainEvent` (факти домену) і `PresentationNotice` (одноразові UI-повідомлення).
- `GameRandom` і `GameClock` передаються ззовні, тому сценарії відтворювані.

## Ревізії та ідемпотентність

`Board.revision` зростає на кожній прийнятій команді. `GameCommandEnvelope.expectedRevision` відхиляє застарілу команду з `REVISION_CONFLICT`. `Board.processedCommandIds` зберігає останні 100 `commandId`; повтор після reconnect повертає `Duplicate` без другого запису.

## Економічні періоди

Тік інфляції живе в `StartCellRule.onPass` через `TurnContext.passStart()`, а не в черзі ходів. Рушій лише виконує правило клітинки; `advance()` економіки не торкається взагалі.

## Спостережуваність

`GameApplicationService` приймає `GameCommandLog` і на кожній команді записує `GameCommandLogEntry`: `boardId`, `commandId`, `playerId`, тип команди, outcome, причину відхилення, `revisionBefore`/`revisionAfter`, `schemaVersion`, `rulesVersion`, `contentPackVersions`, імена доменних подій і тривалості load/engine/commit. `GameCommandLogEntry.format()` дає один рядок `game.command key=value`. Дефолт — `GameCommandLog.None`; сервер підключає `LOGGER` у `RaceRatServiceImpl`. Приватні тексти карток у лог не потрапляють.

## Стан міграції транспорту

`RaceRatService.executeCommand(envelope)` — узагальнений вхід для клієнта. Сервер приймає від клієнта лише `RollDice`, `EndTurn` і `ChooseInteraction`; `CompleteRoll`, `AdvanceTurn`, `MoveTo` та `StartCard` є внутрішніми й відхиляються з `COMMAND_NOT_AVAILABLE`.

Решта старих RPC-методів працює як adapter: вони мутують стан через `updatePlayer`/`updateBoard` і не проходять через `revision` та `processedCommandIds`. Тобто оптимістична конкурентність поки покриває лише команди, що йдуть через рушій. Зняття цього обмеження — предмет завершення Фази 6.

## Перевірка

```bash
./gradlew :shared:jvmTest :server:test
```

Ключові тести: `GameEngineTest` (повний хід без інфраструктури, ідемпотентність, конфлікт ревізій), `GameCommandLogTest`.
