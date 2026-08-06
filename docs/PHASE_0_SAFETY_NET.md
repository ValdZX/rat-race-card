# Фаза 0: захисна сітка

Цей файл фіксує автоматичні перевірки, які мають залишатися зеленими під час наступних фаз рефакторингу.

| Область правил | Серверні тести |
|---|---|
| Склад і типи клітинок, карткові клітинки, сімейні події, відпочинок, звільнення, банкрутство, податкова | `LegacyRulesCharacterizationTest` |
| Прогресія бізнес-колод, усі механічні варіанти карток, обмеження карток за колом | `LegacyRulesCharacterizationTest`, `CardMechanicsCharacterizationTest`, `ChanceDeckAvailabilityTest` |
| Черга, неактивні й видалені гравці, завершення багатокористувацьких ринкових подій | `LegacyRulesCharacterizationTest` |
| Refill і скид колод | `DeputyDeckCycleTest`, `ChanceDeckAvailabilityTest` |
| Аукціон | `CardMechanicsCharacterizationTest`, shared `AuctionTest` |
| Оплата з фондів, кредитний ліміт, добровільна купівля, `businessLimit` | `FundWithdrawalTest`, `ServerPurchaseGuardTest` |
| Зарплата, cash flow, транспорт, інвестиції та капіталізація | `LegacyRulesCharacterizationTest`, `InvestmentsTest` |
| Вихід на зовнішнє коло, мрія та перемога | `LegacyRulesCharacterizationTest`, shared `PlayerProgressionTest` |
| Стабільність `Board`, `Player`, карток і подій | `GoldenSerializationTest`, `BoardSerializationTest`, fixtures у `server/src/test/resources/fixtures/v0` |
| Керований кубик і випадковий вибір | `GameRandomTest` |

Поточна семантика має `rulesVersion = 1`. Старі snapshots без цього поля завантажуються як версія правил 1.

Основна перевірка фази:

```bash
./gradlew :server:test :shared:jvmTest
```
