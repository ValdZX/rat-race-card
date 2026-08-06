# Фаза 1: чисті фінанси й спільний домен

Спільне KMP-ядро тимчасово розміщене в модулі `shared`, як передбачено першим етапом плану. Воно не залежить від RPC, Compose, Storage або event bus.

## Межі ядра

- `FinancialAccount` зберігає готівку, депозит, кредит і фонди.
- `FinancialSnapshot` обчислює balance, active/passive profit, credit expenses, total expenses, cash flow і total для обох режимів.
- `MoneyService` виконує додавання готівки, обов’язкову оплату та капіталізацію фондів.
- `PaymentPolicy` задає використання фондів і кредитний ліміт.
- `PaymentResult` повертає новий account і чисті `PaymentEvent`; server і local adapter окремо перетворюють їх на UI-події.
- `appendRecentChange` є спільною операцією історії фінансових змін.
- `GameRandom` і `GameClock` є залежностями з production-реалізаціями та керованими test doubles.

## Сумісність локальних збережень

`RatRace2CardState` і `Statistics` не змінювали назви або форму полів. `Business`, `BusinessType`, `Fund` і `Config` переведені на спільні типи через type aliases; додаткові поля спільних типів мають defaults. Старі локальні `Land` і `Shares` залишені persistence DTO та перетворюються адаптерами.

Compatibility tests завантажують JSON старих `raceRate2.json` і `statistics2.json` без попередньої міграції.

## Перевірка

```bash
./gradlew :shared:jvmTest :card:jvmTest :server:test :board:jvmTest
```
