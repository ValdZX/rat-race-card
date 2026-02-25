package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.SharesType

val eventStoreCards = mapOf(
    // Акції (Shares) — 45 шт.
    1 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Нафтогаз виграв суд у Газпрому в Стокгольмському арбітражі. Акції «Шевченка-ГазПром» стрімко злітають вгору на фоні ейфорії.",
        price = 2800
    ),
    2 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Київстар та Vodafone об'єднують вишки. CST-Invest виявляється власником веж, акції компанії злітають.",
        price = 1800
    ),
    3 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "У Карпатах знайдено нове родовище бурштину. «Global Capital» першою отримує ліцензію на розробку. Вартість акцій зростає.",
        price = 950
    ),
    4 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Китайські інвестори зацікавились видобутком сланцевого газу на Донеччині. Компанія «Teri Oil» у фаворі.",
        price = 1100
    ),
    5 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT, // Новий тип, але якщо його немає в SharesType, замінити на CST
        description = "Український стартап з розпізнавання облич купують американці. Акції IT-компанії «Revechat» дорожчають у 10 разів.",
        price = 3000
    ),
    6 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO, // Новий тип, але якщо його немає в SharesType, замінити на GC
        description = "Незважаючи на посуху, «Агрохолдинг Мрія» збирає рекордний врожай завдяки новим технологіям. Інвестори в захваті.",
        price = 750
    ),
    7 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Зима видалась аномально холодною. Ціни на газ рвуть стелю, а разом з ними і акції «Шевченка-ГазПром».",
        price = 2100
    ),
    8 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Нова гра «STALKER 2» вийшла і підірвала чарти. Акції розробника, який входить до CST-Invest, зростають.",
        price = 900
    ),
    9 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Світові ціни на золото б'ють рекорди через війну на Близькому Сході. «Global Capital» на коні.",
        price = 1300
    ),
    10 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Уряд знижує рентну плату за нафту. «Teri Oil» збільшує видобуток, акції реагують зростанням.",
        price = 400
    ),
    11 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "В Україні запустили перший 5G. Акції телеком- та IT-компаній злітають. CST-Invest отримує новий імпульс.",
        price = 1500
    ),
    12 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Відкриття ринку землі привабило іноземних інвесторів. Акції агрохолдингів дорожчають.",
        price = 550
    ),
    13 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Скандал у «Нафтогазі» через розкрадання. Акції конкурента «Шевченка-ГазПром» падають, але ваші — зростають завдяки репутації.",
        price = 500
    ),
    14 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Масштабна DDoS-атака на державні реєстри. Послуги кібербезпеки CST-Invest користуються шаленим попитом.",
        price = 700
    ),
    15 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Global Capital інвестує в літієві родовища. Новий «золотий» тренд — «біле золото».",
        price = 600
    ),
    16 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Ціни на нафту марки Brent зростають через скорочення видобутку ОПЕК. «Teri Oil» радіє.",
        price = 850
    ),
    17 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Уряд цифровізує всі послуги в «Дії». IT-компанії отримують мільярдні контракти.",
        price = 2000
    ),
    18 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Врожай зерна б'є рекорди. Експорт через «зерновий коридор» триває. Акції агросектору ростуть.",
        price = 420
    ),
    19 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Україна починає імпорт газу з Європи. «Шевченка-ГазПром» стає ключовим оператором. Акції вгору.",
        price = 1200
    ),
    20 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Чутки про продаж CST-Invest американському фонду. Акції скуповують у шаленому темпі.",
        price = 2300
    ),
    21 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Видобуток золота на Волині виявився нерентабельним через глибину залягання. Акції падають.",
        price = 50
    ),
    22 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Екологічна катастрофа в Чорному морі через розлив нафти. Акції «Teri Oil» стрімко падають.",
        price = 30
    ),
    23 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Податкова перевірка IT-компаній. Акції «Revechat» тимчасово просідають.",
        price = 150
    ),
    24 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Кабмін вводить експортне мито на соняшник. Акції олійних компаній падають.",
        price = 90
    ),
    25 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Ремонт газопроводу. Акції «Шевченка-ГазПром» тимчасово завмерли.",
        price = 200
    ),
    26 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Збої в роботі інтернету по всій країні. CST-Invest звинувачують у хакерській атаці.",
        price = 80
    ),
    27 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Global Capital отримує дозвіл на видобуток титану. Акції зростають на новинах.",
        price = 1100
    ),
    28 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Нафтова платформа в Азовському морі дає першу нафту. Свято в компанії.",
        price = 1700
    ),
    29 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Засновник «Revechat» потрапив у рейтинг Forbes. Акції компанії дорожчають.",
        price = 2500
    ),
    30 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Посуха знищила частину врожаю. Агрохолдинги звітують про збитки.",
        price = 60
    ),
    31 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Україна переходить на власний газ. Акції «Шевченка-ГазПром» злітають до небес.",
        price = 2600
    ),
    32 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Запуск супутника зв'язку «Січ-2-30». CST-Invest фінансувала проект. Інвестори аплодують.",
        price = 1900
    ),
    33 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Золотодобувна компанія виплачує рекордні дивіденди. Акції розкуповують як гарячі пиріжки.",
        price = 1400
    ),
    34 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Енергетична криза в Європі. Українська нафта стає стратегічним ресурсом.",
        price = 950
    ),
    35 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Хакери атакували «Дію». Акції кіберполіції не ростуть, зате ростуть акції приватних IT-фірм з кіберзахисту.",
        price = 800
    ),
    36 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Китай відкриває ринок для української курятини. Акції птахофабрик злітають.",
        price = 300
    ),
    37 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Нардеп пропонує закон про націоналізацію газотранспортної системи. Інвестори в паніці продають акції.",
        price = 100
    ),
    38 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Нові тарифи на мобільний зв'язок. Акції CST-Invest зростають на фоні збільшення виручки.",
        price = 450
    ),
    39 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Волинське золото виявилось... піритом. Акції компанії обвалились, але ви встигли продати?",
        price = 20
    ),
    40 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Болгарія хоче купувати українську нафту. Акції «Teri Oil» поступово зростають.",
        price = 200
    ),
    41 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Масовий відтік IT-спеціалістів за кордон. Акції IT-компаній падають через кадровий голод.",
        price = 180
    ),
    42 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Супермаркети піднімають ціни на продукти. Агрохолдинги звітують про надприбутки.",
        price = 700
    ),
    43 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Тепла зима. Газ ніхто не купує. Акції газових компаній падають.",
        price = 120
    ),
    44 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "CST-Invest запускає власний стрімінговий сервіс. Конкуренти нервують.",
        price = 650
    ),
    45 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Global Capital знаходить алмази на Вінниччині. Це фантастика, але акції реагують миттєво.",
        price = 2900
    ),

    // Земля (Land) — 20 шт.
    46 to BoardCard.EventStore.Land(
        description = "Будівництво метро на Виноградар. Земля на околицях Києва різко злітає в ціні.",
        price = 18000
    ),
    47 to BoardCard.EventStore.Land(
        description = "Під Львовом планують збудувати аеропорт. Ділянки вже скуповують забудовники.",
        price = 12000
    ),
    48 to BoardCard.EventStore.Land(
        description = "В Одесі відкривають новий індустріальний парк. Земля в передмісті дорожчає.",
        price = 9000
    ),
    49 to BoardCard.EventStore.Land(
        description = "Біля Дніпра знайшли поклади граніту. Земельні ділянки тепер цікавлять видобувні компанії.",
        price = 7000
    ),
    50 to BoardCard.EventStore.Land(
        description = "Харків розширюється на північ. Земля під забудову стає золотою.",
        price = 15000
    ),
    51 to BoardCard.EventStore.Land(
        description = "Чутки про будівництво сміттєпереробного заводу під Києвом обвалили ціни на землю в цьому районі.",
        price = 1500
    ),
    52 to BoardCard.EventStore.Land(
        description = "Під Одесою планують курортну зону. Ділянки біля моря дорожчають.",
        price = 22000
    ),
    53 to BoardCard.EventStore.Land(
        description = "Львівська міськрада виділяє землю під соціальне житло. Сусідні ділянки дорожчають.",
        price = 5000
    ),
    54 to BoardCard.EventStore.Land(
        description = "Біля Дніпра збудуюють логістичний центр «Нової пошти». Земля стає привабливою для бізнесу.",
        price = 11000
    ),
    55 to BoardCard.EventStore.Land(
        description = "Землі під Харковом виявились заболоченими. Ціна падає.",
        price = 1000
    ),
    56 to BoardCard.EventStore.Land(
        description = "Ринок землі відкрили для іноземців? Поки ні, але чутки розігрівають ціни.",
        price = 8000
    ),
    57 to BoardCard.EventStore.Land(
        description = "Під Києвом будують нове крематорію. Земля поруч падає в ціні.",
        price = 2000
    ),
    58 to BoardCard.EventStore.Land(
        description = "Одеська ОВА виділяє землю для учасників бойових дій. Попит на землю зростає.",
        price = 6500
    ),
    59 to BoardCard.EventStore.Land(
        description = "У Львові реставрують історичний центр. Земля навколо дорожчає через туристичний потік.",
        price = 14000
    ),
    60 to BoardCard.EventStore.Land(
        description = "Біля Дніпра відкривають новий кар'єр. Земля перетворюється на пил, ціна падає.",
        price = 1800
    ),
    61 to BoardCard.EventStore.Land(
        description = "Під Харковом будують еко-селище. Ділянки розкуповують за безцінь, але потім дорожчають.",
        price = 4500
    ),
    62 to BoardCard.EventStore.Land(
        description = "Київська ОДА вирішила змінити генплан. Ваша ділянка тепер в зоні відчуження... від доріг.",
        price = 3000
    ),
    63 to BoardCard.EventStore.Land(
        description = "В Одесі закривають ринок «7-й кілометр». Земля поруч з ним дешевшає.",
        price = 2500
    ),
    64 to BoardCard.EventStore.Land(
        description = "Під Львовом знайшли мінеральні води. Тепер там хочуть збудувати SPA-курорт.",
        price = 17000
    ),
    65 to BoardCard.EventStore.Land(
        description = "Дніпро розширює межі за рахунок сільгоспземель. Тепер це забудова, а значить гроші.",
        price = 13000
    ),

    // Нерухомість (Estate) — 15 шт.
    66 to BoardCard.EventStore.Estate(
        description = "Бум заміської нерухомості. Кожен хоче мати свій бункер. Ваш будинок під Києвом тепер коштує шалені гроші.",
        price = 170000
    ),
    67 to BoardCard.EventStore.Estate(
        description = "Котеджне містечко під Львовом під'єднали до газу. Вартість будинків злітає.",
        price = 135000
    ),
    68 to BoardCard.EventStore.Estate(
        description = "В Одесі ціни на житло біля моря б'ють рекорди. Ваша дача тепер майже вілла.",
        price = 165000
    ),
    69 to BoardCard.EventStore.Estate(
        description = "Під Дніпром збудували нову трасу. Тепер доїхати до дачі — 20 хвилин. Ціна зростає.",
        price = 115000
    ),
    70 to BoardCard.EventStore.Estate(
        description = "Харківське передмістя. Будинки скуповують переселенці. Попит народжує пропозицію і ціну.",
        price = 95000
    ),
    71 to BoardCard.EventStore.Estate(
        description = "Ваш будинок під Києвом потрапив в зону затоплення через зливки. Ціна падає, але хтось його купить?",
        price = 30000
    ),
    72 to BoardCard.EventStore.Estate(
        description = "Елітне селище «Швейцарія» під Львовом. Тут живуть мажори. Ваш скромний будинок поряд теж зріс в ціні.",
        price = 175000
    ),
    73 to BoardCard.EventStore.Estate(
        description = "В Одесі з’явилися підводні човни РФ. Жарт. Але ціни на нерухомість просіли через паніку.",
        price = 70000
    ),
    74 to BoardCard.EventStore.Estate(
        description = "Під Дніпром відкрили нову школу. Тепер це не просто дача, а місце для життя з дітьми. Ціна вгору.",
        price = 125000
    ),
    75 to BoardCard.EventStore.Estate(
        description = "Харківська область. Ваш будинок вцілів під час обстрілів. Тепер це хоч і сумно, але підвищує його цінність як рідкісного товару.",
        price = 40000
    ),
    76 to BoardCard.EventStore.Estate(
        description = "Київське море. Будинки з видом на воду тепер коштують як квартира в центрі. А то й більше.",
        price = 155000
    ),
    77 to BoardCard.EventStore.Estate(
        description = "Під Льововом зводять смт для релокованого бізнесу. Ваша комерційна нерухомість поруч дорожчає.",
        price = 140000
    ),
    78 to BoardCard.EventStore.Estate(
        description = "В Одесі відкрили новий аквапарк. Дачі поруч стали літнім хітом.",
        price = 100000
    ),
    79 to BoardCard.EventStore.Estate(
        description = "Біля Дніпра газифікували село. Тепер ваш будинок не треба топити дровами. Комфорт коштує грошей.",
        price = 145000
    ),
    80 to BoardCard.EventStore.Estate(
        description = "Під Харковом зробили швидкісний інтернет. Тепер тут можна жити і працювати віддалено. Ціни на нерухомість злітають.",
        price = 105000
    ),

    // Розширення бізнесу (BusinessExtending) — 20 шт.
    81 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша кав'ярня біля метро стала популярною. Черги зранку. Час розширюватись! Прибутковість зростає.",
        profit = 1500
    ),
    82 to BoardCard.EventStore.BusinessExtending(
        description = "СТО «У дядька Василя» тепер офіційний партнер Toyota. Клієнтів не обібрати. Прибуток збільшується.",
        profit = 2000
    ),
    83 to BoardCard.EventStore.BusinessExtending(
        description = "Салон краси «Шик-блиск» виграв тендер на обслуговування обласної адміністрації. Гроші потекли рікою.",
        profit = 1800
    ),
    84 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша пекарня почала постачати хліб до шкіл. Прибуток стабільно зростає.",
        profit = 900
    ),
    85 to BoardCard.EventStore.BusinessExtending(
        description = "Відкрили другу точку шаурми біля вокзалу. Тепер гроші є і там, і там.",
        profit = 600
    ),
    86 to BoardCard.EventStore.BusinessExtending(
        description = "Агентство з оренди квартир «Житло-Сервіс» запустило власний додаток. Комісія зросла.",
        profit = 1300
    ),
    87 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша фотостудія тепер знімає для ТЦК. Замовлень хоч греблю гати.",
        profit = 700
    ),
    88 to BoardCard.EventStore.BusinessExtending(
        description = "Ремонтна бригада «Майстер Шеф» взяла підряд на ремонт цілої багатоповерхівки. Прибуток злітає.",
        profit = 1700
    ),
    89 to BoardCard.EventStore.BusinessExtending(
        description = "Веб-студія запустила курс для пенсіонерів «Як користуватись Дією». Це хіт!",
        profit = 800
    ),
    90 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша хімчистка отримала контракт на обслуговування готелю. Тепер роботи вистачає.",
        profit = 550
    ),
    91 to BoardCard.EventStore.BusinessExtending(
        description = "Барбершоп «Козацька чуприна» став мережевим. Відкрили філіал на Троєщині.",
        profit = 1100
    ),
    92 to BoardCard.EventStore.BusinessExtending(
        description = "Зоомагазин «Хвіст» почав продавати корми власного виробництва. Прибуток подвоївся.",
        profit = 650
    ),
    93 to BoardCard.EventStore.BusinessExtending(
        description = "Автошкола «Кермо» тепер має власний автодром. Учнів стало більше.",
        profit = 400
    ),
    94 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша будівельна бригада виграла тендер на відбудову. Гроші є, роботи теж.",
        profit = 2000
    ),
    95 to BoardCard.EventStore.BusinessExtending(
        description = "Інтернет-магазин дронів розширив асортимент. Тепер продаєте ще й тепловізори. Попит шалений.",
        profit = 1900
    ),
    96 to BoardCard.EventStore.BusinessExtending(
        description = "Кулінарна студія «Смакота» запустила YouTube-канал. Тепер гроші йдуть ще й від реклами.",
        profit = 350
    ),
    97 to BoardCard.EventStore.BusinessExtending(
        description = "Стоматологічний кабінет поставив новий 3D-томограф. Ціни на послуги виросли, пацієнти не закінчуються.",
        profit = 1400
    ),
    98 to BoardCard.EventStore.BusinessExtending(
        description = "Взуттєва майстерня почала ремонтувати брендове взуття. Чек зріс у 3 рази.",
        profit = 300
    ),
    99 to BoardCard.EventStore.BusinessExtending(
        description = "Квітковий магазин уклав контракт з РАГСами. Тепер весілля приносять стабільний дохід.",
        profit = 750
    ),
    100 to BoardCard.EventStore.BusinessExtending(
        description = "Кур'єрська служба розширилась на всю область. Тепер доставляємо навіть у найвіддаленіші села.",
        profit = 1000
    )
)