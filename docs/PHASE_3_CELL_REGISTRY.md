# Фаза 3: реєстр клітинок

Серверний `when (PlaceType)` замінено реєстрацією правил. Центральний рушій знає лише фази `onPass` і `onLand`.

## Контракт

```kotlin
interface CellRule {
    val type: CellTypeId
    fun validate(parameters: JsonObject): ValidationResult = ValidationResult.Valid
    fun onPass(context: TurnContext, cell: CellInstance): RuleResult = context.result
    fun onLand(context: TurnContext, cell: CellInstance): RuleResult = context.result
}
```

`TurnContext` дає правилу поточний `RuleResult`, `playerId`, індекс клітинки, ознаку приземлення, `GameRandom` і `MoneyService`. Правило не має доступу до Storage, RPC і event bus — воно лише повертає новий `RuleResult`.

`CellRuleRegistry` падає на дубльованому `CellTypeId` під час створення й повертає `ValidationResult.Invalid` для незареєстрованого типу або невалідних параметрів. `GameEngine` валідує всі клітинки дошки перед виконанням команди й відхиляє її з `INVALID_BOARD_DEFINITION`.

Розширення реєстру — це `registry + rule`, без правок рушія.

## Стабільні ID

`CellTypeId` має формат `<пакет>.<тип>` і не залежить від ordinal чи номера sealed-підкласу: `core.salary`, `family.child`, `corruption.tax_inspection`, `dreams.desire`. Повний перелік — `CoreCellTypes.all` (17 типів).

`CellInstance(id, type, parameters)` — серіалізована клітинка. `PlaceType.toCellInstance()` і `CellInstance.toPlaceType()` лишаються adapter'ами між новою моделлю та старим `PlaceType`, поки колишні RPC-методи не мігрували; невідомий тип стає `PlaceType.Custom` і не підмінюється іншим типом.

## Presentation

`CellPresentation` тримає `titleKey`, `iconKey`, `colorToken` і `soundId` окремо від правила; `CellPresentationRegistry` перевіряє унікальність. Базовий набір — `legacyCellPresentationRegistry`.

## Перевірка

```bash
./gradlew :shared:jvmTest :server:test
```

`CellRuleRegistryTest` реєструє тестову клітинку `test.bonus` поза core-набором і проганяє її через рушій без змін `GameEngine`, RPC і центрального UI-dispatcher.
