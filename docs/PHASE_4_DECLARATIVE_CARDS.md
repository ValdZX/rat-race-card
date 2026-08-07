# Фаза 4: декларативні картки й generic interaction

Механіка картки описується даними: `CardDefinition` складається з `AvailabilityRule`, `CardPresentation`, списку `InteractionSpec` і списку `EffectSpec`.

## Ефекти

`EffectHandler` реєструється за стабільним `EffectTypeId`. `EffectHandlerRegistry` падає на дубльованому типі й повертає `Invalid` для незареєстрованого ефекту або невалідних параметрів.

`StandardEffectTypes` резервує 19 стабільних ID (префікс `core.`), і всі 19 мають handler. Тест `everyDeclaredEffectTypeNowHasAHandler` падає, якщо з'явиться заявлений ID без реалізації.

| Ефект | Що робить |
|---|---|
| `change_cash`, `pay_amount`, `pay_expense` | гроші; оплата проходить повний каскад списання |
| `acquire_business`, `acquire_shopping` | набуття активів із наявними правилами прогресії бізнесу |
| `remove_asset` | прибирає бізнес, акції, землю або нерухомість; `selector` = `RANDOM`/`FIRST`/`ALL`. Роботу не чіпає ніколи |
| `change_recurring_income` | додає прибуток одному бізнесу, пропускаючи роботу |
| `change_recurring_expense` | змінює лічильник споживчого активу, від якого залежать регулярні витрати |
| `offer_purchase`, `offer_sale` | публікують наступну `PendingInteraction` — дозволяє багатокрокові картки |
| `require_player_predicate` | guard за `PlayerPredicate`: стать, шлюб, діти, наявність активів, поточний трек |
| `require_resource`, `spend_resource` | guard і витрата `PlayerResource`; похідні ресурси витратити не можна |
| `set_counter` | задає лічильник гравця; похідні ресурси ігноруються |
| `draw_card` | відкриває колоду, відфільтровану за активними feature-пакетами |
| `start_auction` | виставляє лот на дошку |
| `for_each_eligible_player` | виконує вкладені ефекти для кожного гравця за предикатом; `includeSelf=false` пропускає автора ходу |
| `emit_notice` | одноразове UI-повідомлення |
| `end_turn` | завершує хід |

`PlayerResource` розділяє те, що можна задати напряму (готівка, депозит, депутати, лічильники покупок), і похідне (кількість бізнесів, акцій, площа землі). Спроба задати похідний ресурс — no-op, а не тиха неконсистентність.

`for_each_eligible_player` тримає посилання на реєстр через `() -> EffectHandlerRegistry`, щоб вкладені ефекти виконувались тим самим набором handler'ів, і повертає контекст авторові ходу після обходу.

## Взаємодії

`InteractionSpec` описує форму: `InteractionField` типу `AMOUNT`, `CHOICE` або `CONFIRMATION` з межами суми, швидкими значеннями й варіантами. `branches` мапить вибір користувача на список ефектів.

Рушій публікує `PendingInteraction` у `Board.pendingInteractions`, а клієнт відповідає `GameCommand.ChooseInteraction(interactionId, input)`. Окремого RPC-методу на сценарій більше не потрібно.

`GenericInteractionDialog` рендерить стандартні `standard.purchase`, `standard.sell`, `standard.amount` і `standard.choice`. `CardInteractionRendererRegistry` вирішує, коли натомість потрібен спеціалізований renderer — за `InteractionKindId` або за префіксом legacy definition ID.

## Стан міграції контенту

Мігровано на `CardDefinition`: бізнес-колоди (мала, середня, велика), `Shopping`, `Chance.RandomJob`, `Expenses`, а також `EventStore.Reelection`, `EventStore.BusinessExtending` і `EventStore.Announcement`. Останні три не мають взаємодії й виконуються одразу з `definition.effects` через `autoDefinition`.

Не мігровано і працює через старі RPC-методи: купівля землі, нерухомості й акцій, аукціон, багатокористувацькі ринкові продажі, депутати та корупційні угоди. `BoardCard.toCardDefinition(link)` повертає `null` для цих типів, тому `selectCard` лишає їх на legacy-шляху.

Для решти бракує не словника ефектів, а ефектів набуття конкретних активів (`acquire_land`, `acquire_estate`, `acquire_shares`) і механіки багатокористувацької черги відповідей. Це наступний крок.

## Перевірка

```bash
./gradlew :shared:jvmTest :board:jvmTest
```

`CardDefinitionEngineTest` перевіряє повний цикл definition → pending interaction → effect і round-trip серіалізації `PendingInteraction`; `CardInteractionRendererRegistryTest` — вибір generic чи спеціалізованого renderer'а.
