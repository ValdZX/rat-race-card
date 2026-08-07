# Фаза 6: feature-пакети й версіонування

Контент рознесений на пакети з маніфестами. Дошка фіксує точні версії пакетів у момент створення.

## Пакети

| `FeatureId` | Вміст | Залежності |
|---|---|---|
| `game.core` | базові клітинки, стандартні ефекти, колоди Chance, три бізнес-колоди, Expenses, Shopping | — |
| `game.family` | `family.child`, `family.love`, `family.divorce` | core |
| `game.investments` | інвестиційні клітинки, колода EventStore | core |
| `game.corruption` | `corruption.deputy`, `corruption.tax_inspection`, колода Deputy | core, investments |
| `game.dreams` | `dreams.desire` | core |

`FeatureManifest` містить `featureId`, версію, залежності з мінімальною версією, `FeatureDefinitions` (типи клітинок, типи ефектів, колоди) і дескриптори міграцій. `FeatureRegistry` при створенні перевіряє унікальність пари feature+версія, додатність версій і відповідність дескрипторів зареєстрованим міграціям.

`FeatureRegistry.runtime(versions)` вибирає конкретні версії, перевіряє залежності й повертає `FeatureRuntime` зі складеними `CellRuleRegistry`, `EffectHandlerRegistry` і впорядкованим списком колод. Незнайома версія пакета дає явну помилку зі списком встановлених версій.

## Фіксація версій у партії

`Board` зберігає `schemaVersion`, `rulesVersion`, `contentPackVersions` і `revision`. `createBoard` приймає `contentPackVersions` (за замовчуванням — `standardContentPackVersions()`), тому оновлення сервера не змінює правил уже створеної дошки.

`Board.requireValidFeatures()` викликається перед відкриттям партії: якщо для збереженого набору пакетів немає handler'ів, партія не відкривається замість того, щоб зіпсувати стан. Core-only гра (без `game.corruption`) стартує й дає коректний набір колод — це фіксує `CoreOnlyFeatureTest`.

## Міграції збережень

`BoardSnapshotMigrator` виконує послідовні чисті кроки `vN → vN+1` (зараз `v0 → v1`). Snapshot новішої схеми, ніж підтримує сервер, відхиляється. Міграція «на місці» в MongoDB (`migrateBoardSnapshot`) вимагає явного `confirmed = true` і спершу пише оригінал у `board_snapshot_backup`.

Golden fixtures лежать у `server/src/test/resources/fixtures/v0` і `v1`.

## Що ще не завершено

Старі RPC-методи для активів, аукціону, ринкових подій, депутатів і корупції не видалені — вони лишаються adapter'ами до завершення міграції контенту з [Фази 4](PHASE_4_DECLARATIVE_CARDS.md). Дубльовані sealed-моделі `BoardCard` живуть паралельно з `CardDefinition`.

`Board` поки тримає три представлення розкладки. `Board.resolvedTracks()` бере перше доступне: `tracks` (динамічні), далі `trackDefinitions` за `BoardLayer`, далі `generatedPlaces` поверх дефолтних списків. Отриманий набір клітинок фільтрується за активними feature-пакетами, тож вимкнений пакет не лишає клітинок без handler'а. Згортання трьох полів до самого `tracks` — залишковий борг міграції.

## Перевірка

```bash
./gradlew :shared:jvmTest :server:test
```

`FeatureRegistryTest`, `CoreOnlyFeatureTest`, `BoardSnapshotMigratorTest`, `GoldenSerializationTest`, `SnapshotBackupTest`.
