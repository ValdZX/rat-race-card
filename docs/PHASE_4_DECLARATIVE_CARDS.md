# Фаза 4: декларативні картки й generic interaction

Механіка картки описується даними: `CardDefinition` складається з `AvailabilityRule`, `CardPresentation`, списку `InteractionSpec` і списку `EffectSpec`.

## Ефекти

`EffectHandler` реєструється за стабільним `EffectTypeId`. `EffectHandlerRegistry` падає на дубльованому типі й повертає `Invalid` для незареєстрованого ефекту або невалідних параметрів.

`StandardEffectTypes` резервує 19 стабільних ID (префікс `core.`), але реалізовано й зареєстровано поки **шість**: `change_cash`, `pay_amount`, `acquire_business`, `acquire_shopping`, `pay_expense`, `end_turn`.

Заявлені, але без handler'ів: `remove_asset`, `change_recurring_income`, `change_recurring_expense`, `offer_purchase`, `offer_sale`, `require_player_predicate`, `require_resource`, `spend_resource`, `draw_card`, `start_auction`, `for_each_eligible_player`, `set_counter`, `emit_notice`. Визначення картки, що посилається на такий ID, не пройде `EffectHandlerRegistry.validate` — це безпечно, але означає, що критерій «нова картка з наявних ефектів» поки виконується лише для сценаріїв купівлі, оплати й разового доходу. Розширення словника ефектів — наступний крок Фази 4.

## Взаємодії

`InteractionSpec` описує форму: `InteractionField` типу `AMOUNT`, `CHOICE` або `CONFIRMATION` з межами суми, швидкими значеннями й варіантами. `branches` мапить вибір користувача на список ефектів.

Рушій публікує `PendingInteraction` у `Board.pendingInteractions`, а клієнт відповідає `GameCommand.ChooseInteraction(interactionId, input)`. Окремого RPC-методу на сценарій більше не потрібно.

`GenericInteractionDialog` рендерить стандартні `standard.purchase`, `standard.sell`, `standard.amount` і `standard.choice`. `CardInteractionRendererRegistry` вирішує, коли натомість потрібен спеціалізований renderer — за `InteractionKindId` або за префіксом legacy definition ID.

## Стан міграції контенту

Мігровано: бізнес-колоди (мала, середня, велика), `Shopping`, `Chance.RandomJob`, `Expenses`. Вони проходять через `GameCommand.StartCard` і `ChooseInteraction`.

Не мігровано і працює через старі RPC-методи: активи й аукціон, багатокористувацькі ринкові події, депутати, корупційні угоди та перевибори. `BoardCard.toCardDefinition(link)` повертає `null` для цих типів, тому `selectCard` тихо лишає їх на legacy-шляху. Це запланований порядок міграції, а не прогалина; завершення знімає відповідні RPC-методи.

## Перевірка

```bash
./gradlew :shared:jvmTest :board:jvmTest
```

`CardDefinitionEngineTest` перевіряє повний цикл definition → pending interaction → effect і round-trip серіалізації `PendingInteraction`; `CardInteractionRendererRegistryTest` — вибір generic чи спеціалізованого renderer'а.
