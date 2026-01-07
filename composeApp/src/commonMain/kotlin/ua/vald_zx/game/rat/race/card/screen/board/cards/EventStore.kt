package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.SharesType

val eventStoreCards = mapOf(
    1 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Політичні події в Україні призвели до хаосу в газових відносинах з Росією. Ціни на газ різко зросли. Приватні інвестори вкладають кошти в альтернативні джерела електроенергії. Компанія «Шевченка-ГазПром» працює на повну, але інвесторів бракує. Акції компанії зросли на 250 %.",
        price = 300
    ),
    2 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Запуск компанії CST-Invest нового стандарту телефонного зв’язку CSMA. Смартфони застарівають, інвестори зацікавлені. Акції зростають до 200 $.",
        price = 200
    ),
    3 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Після неочікуваного зниження на розробку місцезнаходження золота у Волинській області акції компанії Global Capital різко зросли. Інвестори активно купують акції, хоча проект ще не завершений.",
        price = 200
    ),
    4 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Після «помаранчевої революції» та зміни влади у Кремлі різко зросли ціни на російський газ. Компанія «Шевченка-ГазПром» нарощує обороти. Акції компанії щодня дорожчають.",
        price = 1500
    ),
    5 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Через зміну оператора мобільного зв’язку та перерозподіл ринку акції компанії CST-Invest різко зросли. Інвестори активно скуповують акції.",
        price = 400
    ),
    6 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Великі очікування інвесторів. Компанія Global Capital у Волинській області активно працює. Золотоносні поклади підтверджені. Акції компанії стабільно зростають.",
        price = 600
    ),
    7 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Після успішних геологорозвідувальних робіт компанії ТегО у Чорному морі котирування акцій різко підскочили. Фінансові установи радять фіксувати прибуток.",
        price = 600
    ),
    8 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Через знахідки родовищ нафти компанія Teri Oil залучає інвесторів. Аналітики прогнозують зростання.",
        price = 200
    ),
    9 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Після підтвердження перспектив родовищ золота акції Global Capital зростають. Інвестори активно купують.",
        price = 400
    ),
    10 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "На ринку телекомунікацій з’являється нова компанія CST-Invest. Акції «UMC» і «Kyivstar GSM» різко падають, адже CST пропонує наднизькі тарифи.",
        price = 100
    ),
    11 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Глобальне розширення зони покриття й демпінг призводять до стрімкого зростання CST-Invest. Акції різко дорожчають.",
        price = 2500
    ),
    12 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Через фінансові проблеми компанії Teri Oil та втрату довіри інвесторів акції обвалюються.",
        price = 5
    ),
    13 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Позитивні фінансові показники Global Capital різко підвищують котирування. Акції компанії стають привабливими для інвесторів.",
        price = 800
    ),
    14 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "На газовому ринку України з’являється нова компанія «Шевченка-ГазПром». Інвестори активно скуповують акції.",
        price = 250
    ),
    15 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Лобіювання інтересів компанії у владі підвищує довіру інвесторів. Акції ростуть.",
        price = 150
    ),
    16 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "В Азовському морі виявлено значні запаси нафти. Незважаючи на кризу, інвестори знову вірять у Teri Oil.",
        price = 800
    ),
    17 to BoardCard.EventStore.Land(
        description = "Через постійне розширення меж міста та зростання житла земля під Києвом почала дорожчати. Депутати активно скуповують землю. Кожен, хто має землю, може продати її за цією ціною.",
        price = 6000
    ),
    18 to BoardCard.EventStore.Land(
        description = "Через розширення міста та зростання цін земля під Києвом активно скуповується.",
        price = 10000
    ),
    19 to BoardCard.EventStore.Land(
        description = "Через активну забудову та попит на землю під Києвом ціна зростає.",
        price = 5000
    ),
    20 to BoardCard.EventStore.Land(
        description = "Через постійне розширення міста й зростання вартості житла земля під Києвом починає дорожчати. Депутати активно скуповують землю.",
        price = 1000
    ),
    21 to BoardCard.EventStore.Land(
        description = "Через постійне розширення міста й зростання вартості житла земля під Києвом активно дорожчає. Кожен, хто має землю, може продати за цією ціною.",
        price = 8000
    ),
    22 to BoardCard.EventStore.Land(
        description = "Розширення міста та попит на житло різко підняли вартість землі навколо Києва.",
        price = 2000
    ),
    23 to BoardCard.EventStore.Land(
        description = "Земля навколо столиці стає дефіцитною. Депутати та інвестори активно її скуповують.",
        price = 25000
    ),
    24 to BoardCard.EventStore.Land(
        description = "Через розширення міста та зростання вартості житла земля під Києвом дорожчає.",
        price = 3000
    ),
    25 to BoardCard.EventStore.Estate(
        description = "Через високі ціни на нерухомість у столиці люди масово купують заміські будинки. Якщо у вас є будинок або дача — можете продати за цією ціною.",
        price = 175000
    ),
    26 to BoardCard.EventStore.Estate(
        description = "Через високі ціни на нерухомість у столиці люди масово купують заміські будинки. Якщо у вас є будинок або дача під Києвом — можете продати за цією ціною.",
        price = 155000
    ),
    27 to BoardCard.EventStore.Estate(
        description = "У столиці перенаселення! Забезпечені люди шукають житло за містом. Якщо у вас є будинок або дача — можете продати за цією ціною.",
        price = 125000
    ),
    28 to BoardCard.EventStore.Estate(
        description = "У столиці перенаселення! Заможні люди шукають житло за містом. Якщо у вас є будинок або дача під Києвом — можете продати за цією ціною.",
        price = 110000
    ),
    29 to BoardCard.EventStore.Estate(
        description = "У столиці перенаселення. Заможні люди шукають житло за містом. Якщо у вас є будинок або дача під Києвом — можете продати.",
        price = 90000
    ),
    30 to BoardCard.EventStore.Estate(
        description = "У столиці перенаселення. Заможні люди шукають житло за містом. Якщо у вас є будинок або дача під Києвом — ви можете продати її.",
        price = 135000
    ),
    31 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширюється. Якщо у вас є малий бізнес — збільште його прибутковість. Картка діє тільки для вас.",
        profit = 1500
    ),
    32 to BoardCard.EventStore.BusinessExtending(
        description = "Один із малих бізнесів розширюється. Якщо маєш малий бізнес — збільш його прибутковість.",
        profit = 800
    ),
    33 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширюється! Якщо у вас є малий бізнес — збільште його прибутковість. Картка стосується тільки вас.",
        profit = 600
    ),
    34 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширяється! Якщо у вас є малий бізнес — збільште його прибутковість. Картка стосується тільки вас.",
        profit = 2000
    ),
    35 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширяється! Якщо у вас є малий бізнес — збільште його прибутковість. Картка стосується тільки вас.",
        profit = 500
    ),
    36 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширяється. Якщо у вас є малий бізнес — збільште його прибутковість.",
        profit = 100
    ),
    37 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширяється. Картка стосується тільки вас.",
        profit = 400
    ),
    38 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширяється. Картка стосується тільки вас.",
        profit = 300
    ),
    39 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширяється. Якщо у вас є малий бізнес — збільште його прибутковість.",
        profit = 1000
    ),
    40 to BoardCard.EventStore.BusinessExtending(
        description = "Один із ваших малих бізнесів розширяється. Картка стосується тільки вас.",
        profit = 200
    )
)