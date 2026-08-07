# Фаза 5: динамічні треки

Кількість кіл більше не зашита в enum. Runtime-модель оперує списком треків і декларативними переходами.

## Модель

- `PlayerLocation(position, trackId)` — позиція гравця. Числового `level` у runtime немає.
- `TrackDefinition(id, order, topology, cells, visual)` — трек довільної довжини. `order` задає вкладеність: більший `order` — зовнішніший трек.
- `TrackTransition(id, from, to, entryCellIndex, conditions)` — умова й точка входу наступного треку.
- `ProgressCondition` описує умову переходу декларативно: `minimumCashFlow`, `minimumBalance`, `requiresApartment`, `requiresCar`, `requiresPlane`, `requiresEstate`, `requiresSelectedDream`.
- `ObjectiveDefinition` виносить умови перемоги з коду в дані дошки.

`TrackTopology` поки підтримує лише `LOOP`; `TrackLayoutEngine` явно падає на іншій топології замість мовчазного fallback.

## Розкладка

`TrackLayoutEngine.layout(tracks, viewport, portrait)` приймає будь-яку кількість треків, сортує їх за `order` і вкладає один в одного, обчислюючи розмір із `TrackVisualHint`. Dp-координати в дані дошки не потрапляють. `boardLayersOf(board)` будує `BoardLayers` прямо з `Board.resolvedTracks()`, а `calculateBoardLayout` повертає `RouteLayout` на кожен трек.

Клієнтський рендеринг звіряється з `RouteLayout.trackId` і `BoardState.trackId`. Відображення `TrackId → BoardLayer` у шляху рендерингу немає, тому третій трек не потребує legacy-мапінгу.

## Міграція старих партій

`PlayerLocationSerializer` читає старе поле `level` і перетворює `0 → inner`, `1 → outer`, а записує лише `trackId`. Невідомий `level` дає явну помилку сумісності замість мовчазного `INNER`.

`legacyLayerOrNull()` лишається тільки там, де UI свідомо показує спеціальні написи для двох базових кіл (`DesignPlayerSheet`, `StatePage`, `LegacyBoardRoutes`); ці місця null-safe і на новому треку деградують без винятку.

## Перевірка

```bash
./gradlew :shared:jvmTest :board:jvmTest
```

- `DynamicTracksTest` створює партію з трьома треками різної довжини й проходить два декларативні переходи.
- `TrackLayoutEngineTest` перевіряє вкладення трьох треків і порядок від зовнішнього до внутрішнього.
- `ThirdTrackRenderTest` будує дошку з треком `elite`, розкладає її в portrait і landscape та рендерить усі три треки.
