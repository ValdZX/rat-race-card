package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.SharesType

val eventStoreCards = mapOf(
    // Акції (Shares) — 45 шт.
    1001 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Нафтогаз виграв суд у Газпрому в Стокгольмському арбітражі. Акції «Шевченка-ГазПром» стрімко злітають вгору на фоні ейфорії.",
        price = 2800
    ),
    1002 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Київстар та Vodafone об'єднують вишки. CST-Invest виявляється власником веж, акції компанії злітають.",
        price = 1800
    ),
    1003 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "У Карпатах знайдено нове родовище бурштину. «Global Capital» першою отримує ліцензію на розробку. Вартість акцій зростає.",
        price = 950
    ),
    1004 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Китайські інвестори зацікавились видобутком сланцевого газу на Донеччині. Компанія «Teri Oil» у фаворі.",
        price = 1100
    ),
    1005 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT, // Новий тип, але якщо його немає в SharesType, замінити на CST
        description = "Український стартап з розпізнавання облич купують американці. Акції IT-компанії «Revechat» дорожчають у 10 разів.",
        price = 3000
    ),
    1006 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO, // Новий тип, але якщо його немає в SharesType, замінити на GC
        description = "Незважаючи на посуху, «Агрохолдинг Мрія» збирає рекордний врожай завдяки новим технологіям. Інвестори в захваті.",
        price = 750
    ),
    1007 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Зима видалась аномально холодною. Ціни на газ рвуть стелю, а разом з ними і акції «Шевченка-ГазПром».",
        price = 2100
    ),
    1008 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Нова гра «STALKER 2» вийшла і підірвала чарти. Акції розробника, який входить до CST-Invest, зростають.",
        price = 900
    ),
    1009 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Світові ціни на золото б'ють рекорди через війну на Близькому Сході. «Global Capital» на коні.",
        price = 1300
    ),
    1010 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Уряд знижує рентну плату за нафту. «Teri Oil» збільшує видобуток, акції реагують зростанням.",
        price = 400
    ),
    1011 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "В Україні запустили перший 5G. Акції телеком- та IT-компаній злітають. CST-Invest отримує новий імпульс.",
        price = 1500
    ),
    1012 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Відкриття ринку землі привабило іноземних інвесторів. Акції агрохолдингів дорожчають.",
        price = 550
    ),
    1013 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Скандал у «Нафтогазі» через розкрадання. Акції конкурента «Шевченка-ГазПром» падають, але ваші — зростають завдяки репутації.",
        price = 500
    ),
    1014 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Масштабна DDoS-атака на державні реєстри. Послуги кібербезпеки CST-Invest користуються шаленим попитом.",
        price = 700
    ),
    1015 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Global Capital інвестує в літієві родовища. Новий «золотий» тренд — «біле золото».",
        price = 600
    ),
    1016 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Ціни на нафту марки Brent зростають через скорочення видобутку ОПЕК. «Teri Oil» радіє.",
        price = 850
    ),
    1017 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Уряд цифровізує всі послуги в «Дії». IT-компанії отримують мільярдні контракти.",
        price = 2000
    ),
    1018 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Врожай зерна б'є рекорди. Експорт через «зерновий коридор» триває. Акції агросектору ростуть.",
        price = 420
    ),
    1019 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Україна починає імпорт газу з Європи. «Шевченка-ГазПром» стає ключовим оператором. Акції вгору.",
        price = 1200
    ),
    1020 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Чутки про продаж CST-Invest американському фонду. Акції скуповують у шаленому темпі.",
        price = 2300
    ),
    1021 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Видобуток золота на Волині виявився нерентабельним через глибину залягання. Акції падають.",
        price = 50
    ),
    1022 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Екологічна катастрофа в Чорному морі через розлив нафти. Акції «Teri Oil» стрімко падають.",
        price = 30
    ),
    1023 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Податкова перевірка IT-компаній. Акції «Revechat» тимчасово просідають.",
        price = 150
    ),
    1024 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Кабмін вводить експортне мито на соняшник. Акції олійних компаній падають.",
        price = 90
    ),
    1025 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Ремонт газопроводу. Акції «Шевченка-ГазПром» тимчасово завмерли.",
        price = 200
    ),
    1026 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Збої в роботі інтернету по всій країні. CST-Invest звинувачують у хакерській атаці.",
        price = 80
    ),
    1027 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Global Capital отримує дозвіл на видобуток титану. Акції зростають на новинах.",
        price = 1100
    ),
    1028 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Нафтова платформа в Азовському морі дає першу нафту. Свято в компанії.",
        price = 1700
    ),
    1029 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Засновник «Revechat» потрапив у рейтинг Forbes. Акції компанії дорожчають.",
        price = 2500
    ),
    1030 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Посуха знищила частину врожаю. Агрохолдинги звітують про збитки.",
        price = 60
    ),
    1031 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Україна переходить на власний газ. Акції «Шевченка-ГазПром» злітають до небес.",
        price = 2600
    ),
    1032 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Запуск супутника зв'язку «Січ-2-30». CST-Invest фінансувала проект. Інвестори аплодують.",
        price = 1900
    ),
    1033 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Золотодобувна компанія виплачує рекордні дивіденди. Акції розкуповують як гарячі пиріжки.",
        price = 1400
    ),
    1034 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Енергетична криза в Європі. Українська нафта стає стратегічним ресурсом.",
        price = 950
    ),
    1035 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Хакери атакували «Дію». Акції кіберполіції не ростуть, зате ростуть акції приватних IT-фірм з кіберзахисту.",
        price = 800
    ),
    1036 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Китай відкриває ринок для української курятини. Акції птахофабрик злітають.",
        price = 300
    ),
    1037 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Нардеп пропонує закон про націоналізацію газотранспортної системи. Інвестори в паніці продають акції.",
        price = 100
    ),
    1038 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "Нові тарифи на мобільний зв'язок. Акції CST-Invest зростають на фоні збільшення виручки.",
        price = 450
    ),
    1039 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Волинське золото виявилось... піритом. Акції компанії обвалились, але ви встигли продати?",
        price = 20
    ),
    1040 to BoardCard.EventStore.Shares(
        sharesType = SharesType.TO,
        description = "Болгарія хоче купувати українську нафту. Акції «Teri Oil» поступово зростають.",
        price = 200
    ),
    1041 to BoardCard.EventStore.Shares(
        sharesType = SharesType.IT,
        description = "Масовий відтік IT-спеціалістів за кордон. Акції IT-компаній падають через кадровий голод.",
        price = 180
    ),
    1042 to BoardCard.EventStore.Shares(
        sharesType = SharesType.AGRO,
        description = "Супермаркети піднімають ціни на продукти. Агрохолдинги звітують про надприбутки.",
        price = 700
    ),
    1043 to BoardCard.EventStore.Shares(
        sharesType = SharesType.ShchHP,
        description = "Тепла зима. Газ ніхто не купує. Акції газових компаній падають.",
        price = 120
    ),
    1044 to BoardCard.EventStore.Shares(
        sharesType = SharesType.CST,
        description = "CST-Invest запускає власний стрімінговий сервіс. Конкуренти нервують.",
        price = 650
    ),
    1045 to BoardCard.EventStore.Shares(
        sharesType = SharesType.GC,
        description = "Global Capital знаходить алмази на Вінниччині. Це фантастика, але акції реагують миттєво.",
        price = 2900
    ),

    // Земля (Land) — 20 шт.
    1046 to BoardCard.EventStore.Land(
        description = "Будівництво метро на Виноградар. Земля на околицях Києва різко злітає в ціні.",
        price = 18000
    ),
    1047 to BoardCard.EventStore.Land(
        description = "Під Львовом планують збудувати аеропорт. Ділянки вже скуповують забудовники.",
        price = 12000
    ),
    1048 to BoardCard.EventStore.Land(
        description = "В Одесі відкривають новий індустріальний парк. Земля в передмісті дорожчає.",
        price = 9000
    ),
    1049 to BoardCard.EventStore.Land(
        description = "Біля Дніпра знайшли поклади граніту. Земельні ділянки тепер цікавлять видобувні компанії.",
        price = 7000
    ),
    1050 to BoardCard.EventStore.Land(
        description = "Харків розширюється на північ. Земля під забудову стає золотою.",
        price = 15000
    ),
    1051 to BoardCard.EventStore.Land(
        description = "Чутки про будівництво сміттєпереробного заводу під Києвом обвалили ціни на землю в цьому районі.",
        price = 1500
    ),
    1052 to BoardCard.EventStore.Land(
        description = "Під Одесою планують курортну зону. Ділянки біля моря дорожчають.",
        price = 22000
    ),
    1053 to BoardCard.EventStore.Land(
        description = "Львівська міськрада виділяє землю під соціальне житло. Сусідні ділянки дорожчають.",
        price = 5000
    ),
    1054 to BoardCard.EventStore.Land(
        description = "Біля Дніпра збудуюють логістичний центр «Нової пошти». Земля стає привабливою для бізнесу.",
        price = 11000
    ),
    1055 to BoardCard.EventStore.Land(
        description = "Землі під Харковом виявились заболоченими. Ціна падає.",
        price = 1000
    ),
    1056 to BoardCard.EventStore.Land(
        description = "Ринок землі відкрили для іноземців? Поки ні, але чутки розігрівають ціни.",
        price = 8000
    ),
    1057 to BoardCard.EventStore.Land(
        description = "Під Києвом будують нове крематорію. Земля поруч падає в ціні.",
        price = 2000
    ),
    1058 to BoardCard.EventStore.Land(
        description = "Одеська ОВА виділяє землю для учасників бойових дій. Попит на землю зростає.",
        price = 6500
    ),
    1059 to BoardCard.EventStore.Land(
        description = "У Львові реставрують історичний центр. Земля навколо дорожчає через туристичний потік.",
        price = 14000
    ),
    1060 to BoardCard.EventStore.Land(
        description = "Біля Дніпра відкривають новий кар'єр. Земля перетворюється на пил, ціна падає.",
        price = 1800
    ),
    1061 to BoardCard.EventStore.Land(
        description = "Під Харковом будують еко-селище. Ділянки розкуповують за безцінь, але потім дорожчають.",
        price = 4500
    ),
    1062 to BoardCard.EventStore.Land(
        description = "Київська ОДА вирішила змінити генплан. Ваша ділянка тепер в зоні відчуження... від доріг.",
        price = 3000
    ),
    1063 to BoardCard.EventStore.Land(
        description = "В Одесі закривають ринок «7-й кілометр». Земля поруч з ним дешевшає.",
        price = 2500
    ),
    1064 to BoardCard.EventStore.Land(
        description = "Під Львовом знайшли мінеральні води. Тепер там хочуть збудувати SPA-курорт.",
        price = 17000
    ),
    1065 to BoardCard.EventStore.Land(
        description = "Дніпро розширює межі за рахунок сільгоспземель. Тепер це забудова, а значить гроші.",
        price = 13000
    ),

    // Нерухомість (Estate) — 15 шт.
    1066 to BoardCard.EventStore.Estate(
        description = "Бум заміської нерухомості. Кожен хоче мати свій бункер. Ваш будинок під Києвом тепер коштує шалені гроші.",
        price = 170000
    ),
    1067 to BoardCard.EventStore.Estate(
        description = "Котеджне містечко під Львовом під'єднали до газу. Вартість будинків злітає.",
        price = 135000
    ),
    1068 to BoardCard.EventStore.Estate(
        description = "В Одесі ціни на житло біля моря б'ють рекорди. Ваша дача тепер майже вілла.",
        price = 165000
    ),
    1069 to BoardCard.EventStore.Estate(
        description = "Під Дніпром збудували нову трасу. Тепер доїхати до дачі — 20 хвилин. Ціна зростає.",
        price = 115000
    ),
    1070 to BoardCard.EventStore.Estate(
        description = "Харківське передмістя. Будинки скуповують переселенці. Попит народжує пропозицію і ціну.",
        price = 95000
    ),
    1071 to BoardCard.EventStore.Estate(
        description = "Ваш будинок під Києвом потрапив в зону затоплення через зливки. Ціна падає, але хтось його купить?",
        price = 30000
    ),
    1072 to BoardCard.EventStore.Estate(
        description = "Елітне селище «Швейцарія» під Львовом. Тут живуть мажори. Ваш скромний будинок поряд теж зріс в ціні.",
        price = 175000
    ),
    1073 to BoardCard.EventStore.Estate(
        description = "В Одесі з’явилися підводні човни РФ. Жарт. Але ціни на нерухомість просіли через паніку.",
        price = 70000
    ),
    1074 to BoardCard.EventStore.Estate(
        description = "Під Дніпром відкрили нову школу. Тепер це не просто дача, а місце для життя з дітьми. Ціна вгору.",
        price = 125000
    ),
    1075 to BoardCard.EventStore.Estate(
        description = "Харківська область. Ваш будинок вцілів під час обстрілів. Тепер це хоч і сумно, але підвищує його цінність як рідкісного товару.",
        price = 40000
    ),
    1076 to BoardCard.EventStore.Estate(
        description = "Київське море. Будинки з видом на воду тепер коштують як квартира в центрі. А то й більше.",
        price = 155000
    ),
    1077 to BoardCard.EventStore.Estate(
        description = "Під Льововом зводять смт для релокованого бізнесу. Ваша комерційна нерухомість поруч дорожчає.",
        price = 140000
    ),
    1078 to BoardCard.EventStore.Estate(
        description = "В Одесі відкрили новий аквапарк. Дачі поруч стали літнім хітом.",
        price = 100000
    ),
    1079 to BoardCard.EventStore.Estate(
        description = "Біля Дніпра газифікували село. Тепер ваш будинок не треба топити дровами. Комфорт коштує грошей.",
        price = 145000
    ),
    1080 to BoardCard.EventStore.Estate(
        description = "Під Харковом зробили швидкісний інтернет. Тепер тут можна жити і працювати віддалено. Ціни на нерухомість злітають.",
        price = 105000
    ),

    // Розширення бізнесу (BusinessExtending) — 20 шт.
    1081 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша кав'ярня біля метро стала популярною. Черги зранку. Час розширюватись! Прибутковість зростає.",
        profit = 1500
    ),
    1082 to BoardCard.EventStore.BusinessExtending(
        description = "СТО «У дядька Василя» тепер офіційний партнер Toyota. Клієнтів не обібрати. Прибуток збільшується.",
        profit = 2000
    ),
    1083 to BoardCard.EventStore.BusinessExtending(
        description = "Салон краси «Шик-блиск» виграв тендер на обслуговування обласної адміністрації. Гроші потекли рікою.",
        profit = 1800
    ),
    1084 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша пекарня почала постачати хліб до шкіл. Прибуток стабільно зростає.",
        profit = 900
    ),
    1085 to BoardCard.EventStore.BusinessExtending(
        description = "Відкрили другу точку шаурми біля вокзалу. Тепер гроші є і там, і там.",
        profit = 600
    ),
    1086 to BoardCard.EventStore.BusinessExtending(
        description = "Агентство з оренди квартир «Житло-Сервіс» запустило власний додаток. Комісія зросла.",
        profit = 1300
    ),
    1087 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша фотостудія тепер знімає для ТЦК. Замовлень хоч греблю гати.",
        profit = 700
    ),
    1088 to BoardCard.EventStore.BusinessExtending(
        description = "Ремонтна бригада «Майстер Шеф» взяла підряд на ремонт цілої багатоповерхівки. Прибуток злітає.",
        profit = 1700
    ),
    1089 to BoardCard.EventStore.BusinessExtending(
        description = "Веб-студія запустила курс для пенсіонерів «Як користуватись Дією». Це хіт!",
        profit = 800
    ),
    1090 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша хімчистка отримала контракт на обслуговування готелю. Тепер роботи вистачає.",
        profit = 550
    ),
    1091 to BoardCard.EventStore.BusinessExtending(
        description = "Барбершоп «Козацька чуприна» став мережевим. Відкрили філіал на Троєщині.",
        profit = 1100
    ),
    1092 to BoardCard.EventStore.BusinessExtending(
        description = "Зоомагазин «Хвіст» почав продавати корми власного виробництва. Прибуток подвоївся.",
        profit = 650
    ),
    1093 to BoardCard.EventStore.BusinessExtending(
        description = "Автошкола «Кермо» тепер має власний автодром. Учнів стало більше.",
        profit = 400
    ),
    1094 to BoardCard.EventStore.BusinessExtending(
        description = "Ваша будівельна бригада виграла тендер на відбудову. Гроші є, роботи теж.",
        profit = 2000
    ),
    1095 to BoardCard.EventStore.BusinessExtending(
        description = "Інтернет-магазин дронів розширив асортимент. Тепер продаєте ще й тепловізори. Попит шалений.",
        profit = 1900
    ),
    1096 to BoardCard.EventStore.BusinessExtending(
        description = "Кулінарна студія «Смакота» запустила YouTube-канал. Тепер гроші йдуть ще й від реклами.",
        profit = 350
    ),
    1097 to BoardCard.EventStore.BusinessExtending(
        description = "Стоматологічний кабінет поставив новий 3D-томограф. Ціни на послуги виросли, пацієнти не закінчуються.",
        profit = 1400
    ),
    1098 to BoardCard.EventStore.BusinessExtending(
        description = "Взуттєва майстерня почала ремонтувати брендове взуття. Чек зріс у 3 рази.",
        profit = 300
    ),
    1099 to BoardCard.EventStore.BusinessExtending(
        description = "Квітковий магазин уклав контракт з РАГСами. Тепер весілля приносять стабільний дохід.",
        profit = 750
    ),
    1100 to BoardCard.EventStore.BusinessExtending(
        description = "Кур'єрська служба розширилась на всю область. Тепер доставляємо навіть у найвіддаленіші села.",
        profit = 1000
    )
)