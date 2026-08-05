package ua.vald_zx.game.rat.race.server.generation

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.CardText
import ua.vald_zx.game.rat.race.card.shared.Dream
import ua.vald_zx.game.rat.race.card.shared.GeneratedText
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.generatedLocales
import java.util.concurrent.atomic.AtomicInteger

internal class LlmTextGenerator(
    private val chat: ChatCompletion = LlmSettings.textChat(),
    private val batchSize: Int = DEFAULT_TEXT_BATCH_SIZE,
    private val reviewer: TextReviewer = AcceptAllTextReviewer,
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun workUnits(
        deckSizes: Collection<Int>,
        professionCount: Int,
        dreamCount: Int,
    ): Int = deckSizes.sumOf(::batches) + batches(professionCount) + batches(dreamCount)

    suspend fun generateComplete(
        world: BoardGeneration,
        cards: Map<BoardCardType, Map<Int, BoardCard>>,
        professions: List<ProfessionCard>,
        dreams: List<Dream> = emptyList(),
        existingTexts: Map<String, GeneratedText> = emptyMap(),
        shareNames: Map<String, String> = emptyMap(),
        onCheckpoint: suspend (
            texts: Map<String, GeneratedText>,
            completed: Int,
            total: Int,
            detail: String,
        ) -> Unit = { _, _, _, _ -> },
    ): Map<String, GeneratedText> {
        val store = Texts(existingTexts)
        val cardBatches = cards.mapValues { (type, deck) ->
            val nameBearingIds = deck.filterValues { it.usesGeneratedName() }.keys
            deck.entries.sortedBy { it.key }.chunked(batchSize).mapIndexed { index, items ->
                CardBatch(type, index, batches(deck.size), items, deck, nameBearingIds)
            }
        }
        val professionBatches = professions.chunked(batchSize).mapIndexed { index, items ->
            ProfessionBatch(index, batches(professions.size), items)
        }
        val dreamBatches = dreams.withIndex().toList().chunked(batchSize).mapIndexed { index, items ->
            DreamBatch(index, batches(dreams.size), items)
        }
        val restored = store.snapshot()
        val total = cardBatches.values.sumOf { it.size } + professionBatches.size + dreamBatches.size
        val done = cardBatches.values.flatten()
            .count { batch -> restored.hasCards(batch.type, batch.items) } +
                professionBatches.count { batch -> restored.hasProfessions(batch.items.map { it.id }) } +
                dreamBatches.count { batch -> restored.hasDreams(batch.items.map { it.value.id }) }
        val progress = Progress(total, done, store, onCheckpoint)
        progress.report("")

        coroutineScope {
            val deckJobs = cardBatches.values.map { batchesOfDeck ->
                async { batchesOfDeck.forEach { generateCardBatch(world, it, shareNames, store, progress) } }
            }
            val professionJob = async {
                professionBatches.forEach { generateProfessionBatch(world, it, store, progress) }
            }
            val dreamJob = async {
                dreamBatches.forEach { generateDreamBatch(world, it, store, progress) }
            }
            (deckJobs + professionJob + dreamJob).awaitAll()
        }

        val generated = store.snapshot()
        generatedLocales.forEach { locale ->
            validateComplete(locale, generated.getValue(locale), cards, professions, dreams)
        }
        progress.finish()
        return generated
    }

    private suspend fun generateCardBatch(
        world: BoardGeneration,
        batch: CardBatch,
        shareNames: Map<String, String>,
        store: Texts,
        progress: Progress,
    ) {
        val ids = batch.items.map { it.key }
        if (store.snapshot().hasCards(batch.type, batch.items)) return
        val detail = "${batch.type.name} ${batch.index + 1}/${batch.count}"
        val rejected = mutableMapOf<Int, MutableList<Map<String, CardText>>>()

        repeat(MAX_TEXT_BATCH_ATTEMPTS) { attempt ->
            val current = store.snapshot()
            val missing = batch.items.filterNot { current.hasCard(batch.type, it.key, it.value) }
            if (missing.isEmpty()) return@repeat
            val expectedIds = missing.map { it.key }.toSet()
            val answer = chat.complete(
                system = systemPrompt(),
                user = deckPrompt(
                    world = world,
                    type = batch.type,
                    briefs = missing.joinToString("\n") { (id, card) -> cardBrief(world, id, card, shareNames) },
                    acceptedNames = current.cardNameContext(batch.type, batch.nameBearingIds),
                    attempt = attempt,
                    rejected = rejected.filterKeys { it in expectedIds },
                ),
            ) ?: return@repeat
            val candidates = answer.parseLocalizedItems().reviewed("${batch.type} cards", rejected)
            val updated = store.acceptCards(
                batch.type,
                candidates,
                expectedIds,
                batch.cardsById,
                batch.nameBearingIds,
            )
            missing.filterNot { updated.hasCard(batch.type, it.key, it.value) }.forEach { (id) ->
                candidates[id]?.let { candidate -> rejected.getOrPut(id, ::mutableListOf) += candidate }
            }
            if (!updated.hasCards(batch.type, batch.items)) progress.report(detail)
        }

        repeat(MAX_REPAIR_BATCH_ATTEMPTS) { repairAttempt ->
            val missing = store.snapshot().let { current ->
                batch.items.filterNot { current.hasCard(batch.type, it.key, it.value) }
            }
            if (missing.isEmpty()) return@repeat
            missing.chunked(REPAIR_BATCH_SIZE).forEach repairBatchLoop@{ repairBatch ->
                val expectedIds = repairBatch.map { it.key }.toSet()
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = deckPrompt(
                        world = world,
                        type = batch.type,
                        briefs = repairBatch.joinToString("\n") { (id, card) -> cardBrief(world, id, card, shareNames) },
                        acceptedNames = store.snapshot().cardNameContext(batch.type, batch.nameBearingIds),
                        attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                        rejected = rejected.filterKeys { it in expectedIds },
                    ),
                ) ?: return@repairBatchLoop
                val candidates = answer.parseLocalizedItems(expectedSingleId = expectedIds.singleOrNull())
                    .reviewed("${batch.type} cards", rejected)
                val updated = store.acceptCards(
                    batch.type,
                    candidates,
                    expectedIds,
                    batch.cardsById,
                    batch.nameBearingIds,
                )
                repairBatch.filterNot { updated.hasCard(batch.type, it.key, it.value) }.forEach { (id) ->
                    candidates[id]?.let { candidate -> rejected.getOrPut(id, ::mutableListOf) += candidate }
                }
                progress.report(detail)
            }
        }

        val stillMissing = store.snapshot().let { current ->
            batch.items.filterNot { current.hasCard(batch.type, it.key, it.value) }
        }
        stillMissing.forEach { missingCard ->
            val id = missingCard.key
            repeat(MAX_SINGLE_ITEM_ATTEMPTS) { repairAttempt ->
                if (store.snapshot().hasCard(batch.type, id, missingCard.value)) return@repeat
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = deckPrompt(
                        world = world,
                        type = batch.type,
                        briefs = cardBrief(world, id, missingCard.value, shareNames),
                        acceptedNames = store.snapshot().cardNameContext(batch.type, batch.nameBearingIds),
                        attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                        rejected = rejected.filterKeys { it == id },
                    ),
                ) ?: return@repeat
                val candidates = answer.parseLocalizedItems(expectedSingleId = id)
                    .reviewed("${batch.type} cards", rejected)
                val updated = store.acceptCards(
                    batch.type,
                    candidates,
                    setOf(id),
                    batch.cardsById,
                    batch.nameBearingIds,
                )
                if (!updated.hasCard(batch.type, id, missingCard.value)) {
                    candidates[id]?.let { candidate ->
                        rejected.getOrPut(id, ::mutableListOf) += candidate
                        llmLogger.warn("Rejected duplicate localized card text for ${batch.type} $id")
                    } ?: llmLogger.warn(
                        "LLM repair response has no usable localized card text for ${batch.type} $id"
                    )
                }
                progress.report(detail)
            }
        }

        val missingIds = store.snapshot().let { current ->
            batch.items.filterNot { current.hasCard(batch.type, it.key, it.value) }.map { it.key }
        }
        check(missingIds.isEmpty()) {
            "Incomplete uk/en texts for ${batch.type}: ${missingIds.take(MAX_ERROR_IDS)}"
        }
        progress.complete(detail)
    }

    private suspend fun generateProfessionBatch(
        world: BoardGeneration,
        batch: ProfessionBatch,
        store: Texts,
        progress: Progress,
    ) {
        val ids = batch.items.map { it.id }
        if (store.snapshot().hasProfessions(ids)) return
        val detail = "professions ${batch.index + 1}/${batch.count}"
        val rejected = mutableMapOf<Int, MutableList<Map<String, CardText>>>()

        repeat(MAX_TEXT_BATCH_ATTEMPTS) { attempt ->
            val current = store.snapshot()
            val missing = batch.items.filterNot { current.hasProfession(it.id) }
            if (missing.isEmpty()) return@repeat
            val expectedIds = missing.map { it.id }.toSet()
            val answer = chat.complete(
                system = systemPrompt(),
                user = professionPrompt(
                    world = world,
                    briefs = missing.joinToString("\n", transform = ::professionBrief),
                    acceptedNames = current.professionNameContext(),
                    attempt = attempt,
                    rejected = rejected.filterKeys { it in expectedIds },
                ),
            ) ?: return@repeat
            val candidates = answer.parseLocalizedItems().reviewed("professions", rejected)
            val updated = store.acceptProfessions(candidates, expectedIds)
            missing.filterNot { updated.hasProfession(it.id) }.forEach { profession ->
                candidates[profession.id]?.let { candidate ->
                    rejected.getOrPut(profession.id, ::mutableListOf) += candidate
                }
            }
            if (!updated.hasProfessions(ids)) progress.report(detail)
        }

        repeat(MAX_REPAIR_BATCH_ATTEMPTS) { repairAttempt ->
            val missing = store.snapshot().let { current ->
                batch.items.filterNot { current.hasProfession(it.id) }
            }
            if (missing.isEmpty()) return@repeat
            missing.chunked(REPAIR_BATCH_SIZE).forEach repairBatchLoop@{ repairBatch ->
                val expectedIds = repairBatch.map { it.id }.toSet()
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = professionPrompt(
                        world = world,
                        briefs = repairBatch.joinToString("\n", transform = ::professionBrief),
                        acceptedNames = store.snapshot().professionNameContext(),
                        attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                        rejected = rejected.filterKeys { it in expectedIds },
                    ),
                ) ?: return@repairBatchLoop
                val candidates = answer.parseLocalizedItems(expectedSingleId = expectedIds.singleOrNull())
                    .reviewed("professions", rejected)
                val updated = store.acceptProfessions(candidates, expectedIds)
                repairBatch.filterNot { updated.hasProfession(it.id) }.forEach { profession ->
                    candidates[profession.id]?.let { candidate ->
                        rejected.getOrPut(profession.id, ::mutableListOf) += candidate
                    }
                }
                progress.report(detail)
            }
        }

        val stillMissing = store.snapshot().let { current ->
            batch.items.filterNot { current.hasProfession(it.id) }
        }
        stillMissing.forEach { missingProfession ->
            val id = missingProfession.id
            repeat(MAX_SINGLE_ITEM_ATTEMPTS) { repairAttempt ->
                if (store.snapshot().hasProfession(id)) return@repeat
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = professionPrompt(
                        world = world,
                        briefs = professionBrief(missingProfession),
                        acceptedNames = store.snapshot().professionNameContext(),
                        attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                        rejected = rejected.filterKeys { it == id },
                    ),
                ) ?: return@repeat
                val candidates = answer.parseLocalizedItems(expectedSingleId = id)
                    .reviewed("professions", rejected)
                val updated = store.acceptProfessions(candidates, setOf(id))
                if (!updated.hasProfession(id)) {
                    candidates[id]?.let { candidate ->
                        rejected.getOrPut(id, ::mutableListOf) += candidate
                        llmLogger.warn("Rejected duplicate localized profession $id")
                    } ?: llmLogger.warn("LLM repair response has no usable localized profession $id")
                }
                progress.report(detail)
            }
        }

        val missingIds = store.snapshot().let { current -> ids.filterNot { current.hasProfession(it) } }
        check(missingIds.isEmpty()) { "Incomplete uk/en professions: ${missingIds.take(MAX_ERROR_IDS)}" }
        progress.complete(detail)
    }

    private suspend fun generateDreamBatch(
        world: BoardGeneration,
        batch: DreamBatch,
        store: Texts,
        progress: Progress,
    ) {
        val ids = batch.items.map { it.value.id }
        if (store.snapshot().hasDreams(ids)) return
        val detail = "dreams ${batch.index + 1}/${batch.count}"
        val slots = batch.items.associate { (index, dream) -> index + 1 to dream }
        val rejected = mutableMapOf<Int, MutableList<Map<String, CardText>>>()

        repeat(MAX_TEXT_BATCH_ATTEMPTS + MAX_REPAIR_BATCH_ATTEMPTS) { attempt ->
            val current = store.snapshot()
            val missing = slots.filterValues { !current.hasDream(it.id) }
            if (missing.isEmpty()) return@repeat
            val answer = chat.complete(
                system = systemPrompt(),
                user = dreamPrompt(
                    world = world,
                    briefs = missing.entries.joinToString("\n") { (slot, dream) ->
                        "$slot. dream priced at ${dream.price}"
                    },
                    acceptedNames = current.dreamNameContext(),
                    attempt = attempt,
                    rejected = rejected.filterKeys { it in missing.keys },
                ),
            ) ?: return@repeat
            val candidates = answer.parseLocalizedItems(expectedSingleId = missing.keys.singleOrNull())
                .reviewed("dreams", rejected)
            val updated = store.acceptDreams(candidates, slots)
            missing.filterValues { !updated.hasDream(it.id) }.forEach { (slot) ->
                candidates[slot]?.let { candidate -> rejected.getOrPut(slot, ::mutableListOf) += candidate }
            }
            progress.report(detail)
        }

        val missingIds = store.snapshot().let { current -> ids.filterNot { current.hasDream(it) } }
        check(missingIds.isEmpty()) { "Incomplete uk/en dreams: ${missingIds.take(MAX_ERROR_IDS)}" }
        progress.complete(detail)
    }

    private inner class Texts(existing: Map<String, GeneratedText>) {
        private val lock = Mutex()
        private var texts: Map<String, GeneratedText> =
            generatedLocales.associateWith { existing[it] ?: GeneratedText() }

        suspend fun snapshot(): Map<String, GeneratedText> = lock.withLock { texts }

        suspend fun acceptCards(
            type: BoardCardType,
            candidates: Map<Int, Map<String, CardText>>,
            allowedIds: Set<Int>,
            cardsById: Map<Int, BoardCard>,
            nameBearingIds: Set<Int>,
        ): Map<String, GeneratedText> = lock.withLock {
            texts = texts.toMutableMap().apply {
                acceptCards(type, candidates, allowedIds, cardsById, nameBearingIds)
            }
            texts
        }

        suspend fun acceptProfessions(
            candidates: Map<Int, Map<String, CardText>>,
            allowedIds: Set<Int>,
        ): Map<String, GeneratedText> = lock.withLock {
            texts = texts.toMutableMap().apply { acceptProfessions(candidates, allowedIds) }
            texts
        }

        suspend fun acceptDreams(
            candidates: Map<Int, Map<String, CardText>>,
            slots: Map<Int, Dream>,
        ): Map<String, GeneratedText> = lock.withLock {
            texts = texts.toMutableMap().apply { acceptDreams(candidates, slots) }
            texts
        }
    }

    private inner class Progress(
        private val total: Int,
        completedBatches: Int,
        private val store: Texts,
        private val onCheckpoint: suspend (Map<String, GeneratedText>, Int, Int, String) -> Unit,
    ) {
        private val completed = AtomicInteger(completedBatches)
        private val lock = Mutex()

        suspend fun report(detail: String) = lock.withLock {
            onCheckpoint(store.snapshot(), completed.get(), total, detail)
        }

        suspend fun complete(detail: String) {
            completed.incrementAndGet()
            report(detail)
        }

        suspend fun finish() = lock.withLock {
            onCheckpoint(store.snapshot(), total, total, "")
        }
    }

    private fun validateComplete(
        locale: String,
        generated: GeneratedText,
        cards: Map<BoardCardType, Map<Int, BoardCard>>,
        professions: List<ProfessionCard>,
        dreams: List<Dream>,
    ) {
        cards.forEach { (type, deck) ->
            val texts = generated.cards[type].orEmpty()
            check(texts.keys == deck.keys) { "Incomplete $locale texts for $type" }
            check(texts.values.all { it.name.isNotBlank() }) { "Empty $locale name for $type" }
            check(texts.values.all { it.description.isNotBlank() }) { "Empty $locale description for $type" }
            val namedTexts = texts.filterKeys { deck.getValue(it).usesGeneratedName() }
            check(namedTexts.values.map { it.name.normalized() }.distinct().size == namedTexts.size) {
                "Repeated $locale names for $type"
            }
            val repeatedDescriptions = texts.entries.groupBy { it.value.description.normalized() }
                .values
                .filter { it.size > 1 }
            check(repeatedDescriptions.all { repeated ->
                repeated.map { (id) -> deck.getValue(id) }.distinct().size == 1
            }) {
                "Repeated $locale descriptions for different $type cards"
            }
        }
        val localizedCards = generated.cards.flatMap { (type, deck) ->
            deck.map { (id, text) -> Triple(type, id, text.description.normalized()) }
        }
        val repeatedDescriptions = localizedCards.groupBy { it.third }.values.filter { it.size > 1 }
        check(repeatedDescriptions.all { repeated ->
            repeated.map { (type, id) -> cards.getValue(type).getValue(id) }.distinct().size == 1
        }) { "Repeated $locale descriptions for different cards" }
        check(generated.professions.keys == professions.map { it.id }.toSet()) {
            "Incomplete $locale profession names"
        }
        check(generated.professionDescriptions.keys == professions.map { it.id }.toSet()) {
            "Incomplete $locale profession descriptions"
        }
        check(generated.professions.values.all { it.isNotBlank() }) { "Empty $locale profession name" }
        check(generated.professionDescriptions.values.all { it.isNotBlank() }) {
            "Empty $locale profession description"
        }
        check(generated.professions.values.map { it.lowercase() }.distinct().size == professions.size) {
            "Repeated $locale profession names"
        }
        check(generated.professionDescriptions.values.map { it.lowercase() }.distinct().size == professions.size) {
            "Repeated $locale profession descriptions"
        }
        check(generated.dreams.keys == dreams.map { it.id }.toSet()) { "Incomplete $locale dreams" }
        check(generated.dreams.values.all { it.isUsable() }) { "Empty $locale dream text" }
        check(generated.dreams.values.map { it.name.normalized() }.distinct().size == dreams.size) {
            "Repeated $locale dream names"
        }
        check(generated.dreams.values.map { it.description.normalized() }.distinct().size == dreams.size) {
            "Repeated $locale dream descriptions"
        }
    }

    private suspend fun Map<Int, Map<String, CardText>>.reviewed(
        kind: String,
        rejected: MutableMap<Int, MutableList<Map<String, CardText>>>,
    ): Map<Int, Map<String, CardText>> {
        val rejectedIds = reviewer.rejectedIds(kind, this)
        rejectedIds.forEach { id ->
            this[id]?.let { candidate -> rejected.getOrPut(id, ::mutableListOf) += candidate }
        }
        return filterKeys { it !in rejectedIds }
    }

    private fun String.parseLocalizedItems(expectedSingleId: Int? = null): Map<Int, Map<String, CardText>> {
        val items = completeJsonObjects()
        if (items.isEmpty()) llmLogger.warn("LLM answer contains no complete JSON items")
        val localizedItems = items.mapNotNull { item ->
            item.localizedTextOrNull()?.let { localized -> item to localized }
        }
        val parsed = localizedItems.mapNotNull { (item, localized) ->
            val id = item["id"]?.jsonPrimitive?.let { value ->
                value.intOrNull ?: value.contentOrNull?.toIntOrNull()
            } ?: return@mapNotNull null
            id to localized
        }.toMap()
        if (expectedSingleId == null || expectedSingleId in parsed || localizedItems.size != 1) return parsed
        val localized = localizedItems.single().second
        return parsed + (expectedSingleId to localized)
    }

    private fun String.completeJsonObjects(): List<JsonObject> {
        val arrayStart = indexOf('[')
        val scanStart = if (arrayStart >= 0) arrayStart + 1 else 0
        val objects = mutableListOf<JsonObject>()
        val objectStarts = mutableListOf<Int>()
        var insideString = false
        var escaped = false
        for (index in scanStart until length) {
            val character = this[index]
            if (insideString) {
                when {
                    escaped -> escaped = false
                    character == '\\' -> escaped = true
                    character == '"' -> insideString = false
                }
                continue
            }
            when (character) {
                '"' -> insideString = true
                '{' -> objectStarts += index
                '}' -> if (objectStarts.isNotEmpty()) {
                    val objectStart = objectStarts.removeLast()
                    runCatching { json.parseToJsonElement(substring(objectStart, index + 1)) as? JsonObject }
                        .onFailure { llmLogger.warn("LLM answered an invalid JSON item: ${it.message}") }
                        .getOrNull()
                        ?.let(objects::add)
                }
                ']' -> if (arrayStart >= 0 && objectStarts.isEmpty()) break
            }
        }
        return objects
    }

    private fun JsonObject.localizedTextOrNull(): Map<String, CardText>? {
        val localized = mutableMapOf<String, CardText>()
        generatedLocales.forEach { locale ->
            val item = this[locale] as? JsonObject ?: return null
            val text = CardText(
                name = item["name"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
                description = item["description"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
            )
            if (!text.isUsable()) return null
            localized[locale] = text
        }
        return localized
    }

    private fun MutableMap<String, GeneratedText>.acceptCards(
        type: BoardCardType,
        candidates: Map<Int, Map<String, CardText>>,
        allowedIds: Set<Int>,
        cardsById: Map<Int, BoardCard>,
        nameBearingIds: Set<Int>,
    ) {
        candidates.forEach { (id, localized) ->
            if (id !in allowedIds) return@forEach
            val card = cardsById[id] ?: return@forEach
            val suitable = generatedLocales.all { locale ->
                localized.getValue(locale).isSuitableFor(card, locale)
            }
            if (!suitable) return@forEach
            val unique = generatedLocales.all { locale ->
                localized.getValue(locale).isUniqueCard(
                    this,
                    locale,
                    type,
                    id,
                    cardsById,
                    nameBearingIds,
                )
            }
            if (!unique) return@forEach
            generatedLocales.forEach { locale -> putCard(locale, type, id, localized.getValue(locale)) }
        }
    }

    private fun MutableMap<String, GeneratedText>.acceptDreams(
        candidates: Map<Int, Map<String, CardText>>,
        slots: Map<Int, Dream>,
    ) {
        candidates.forEach { (slot, localized) ->
            val dream = slots[slot] ?: return@forEach
            val unique = generatedLocales.all { locale ->
                localized.getValue(locale).isUniqueDream(this, locale, dream.id)
            }
            if (!unique) return@forEach
            generatedLocales.forEach { locale ->
                val generated = getValue(locale)
                this[locale] = generated.copy(dreams = generated.dreams + (dream.id to localized.getValue(locale)))
            }
        }
    }

    private fun CardText.isUniqueDream(
        texts: Map<String, GeneratedText>,
        locale: String,
        id: String,
    ): Boolean = texts.getValue(locale).dreams.none { (existingId, existing) ->
        existingId != id && existing.collidesWith(this)
    }

    private fun MutableMap<String, GeneratedText>.acceptProfessions(
        candidates: Map<Int, Map<String, CardText>>,
        allowedIds: Set<Int>,
    ) {
        candidates.forEach { (id, localized) ->
            if (id !in allowedIds) return@forEach
            val unique = generatedLocales.all { locale ->
                localized.getValue(locale).isUniqueProfession(this, locale, id)
            }
            if (!unique) return@forEach
            generatedLocales.forEach { locale -> putProfession(locale, id, localized.getValue(locale)) }
        }
    }

    private fun CardText.isUniqueCard(
        texts: Map<String, GeneratedText>,
        locale: String,
        type: BoardCardType,
        id: Int,
        cardsById: Map<Int, BoardCard>,
        nameBearingIds: Set<Int>,
    ): Boolean {
        val decks = texts.getValue(locale).cards
        val uniqueName = id !in nameBearingIds || decks[type].orEmpty().none { (existingId, existing) ->
            existingId != id && existingId in nameBearingIds && existing.name.normalized() == name.normalized()
        }
        val uniqueDescription = decks.none { (existingType, deck) ->
            deck.any { (existingId, existing) ->
                val sameCard = existingType == type && existingId == id
                val sameMechanics = existingType == type && cardsById[existingId] == cardsById[id]
                !sameCard && !sameMechanics && existing.description.normalized() == description.normalized()
            }
        }
        return uniqueName && uniqueDescription
    }

    private fun CardText.isUniqueProfession(
        texts: Map<String, GeneratedText>,
        locale: String,
        id: Int,
    ): Boolean {
        val generated = texts.getValue(locale)
        return generated.professions.none { (existingId, name) ->
            existingId != id && name.normalized() == this.name.normalized()
        } && generated.professionDescriptions.none { (existingId, description) ->
            existingId != id && description.normalized() == this.description.normalized()
        }
    }

    private fun MutableMap<String, GeneratedText>.putCard(
        locale: String,
        type: BoardCardType,
        id: Int,
        text: CardText,
    ) {
        val generated = getValue(locale)
        this[locale] = generated.copy(
            cards = generated.cards + (type to (generated.cards[type].orEmpty() + (id to text)))
        )
    }

    private fun MutableMap<String, GeneratedText>.putProfession(locale: String, id: Int, text: CardText) {
        val generated = getValue(locale)
        this[locale] = generated.copy(
            professions = generated.professions + (id to text.name),
            professionDescriptions = generated.professionDescriptions + (id to text.description),
        )
    }

    private fun Map<String, GeneratedText>.hasCard(type: BoardCardType, id: Int, card: BoardCard): Boolean =
        generatedLocales.all { locale ->
            this[locale]?.cards?.get(type)?.get(id)?.isSuitableFor(card, locale) == true
        }

    private fun Map<String, GeneratedText>.hasCards(
        type: BoardCardType,
        cards: List<Map.Entry<Int, BoardCard>>,
    ): Boolean = cards.all { hasCard(type, it.key, it.value) }

    private fun Map<String, GeneratedText>.hasProfession(id: Int): Boolean = generatedLocales.all { locale ->
        this[locale]?.professions?.get(id).orEmpty().isNotBlank() &&
                this[locale]?.professionDescriptions?.get(id).orEmpty().isNotBlank()
    }

    private fun Map<String, GeneratedText>.hasProfessions(ids: List<Int>): Boolean = ids.all { hasProfession(it) }

    private fun Map<String, GeneratedText>.hasDream(id: String): Boolean = generatedLocales.all { locale ->
        this[locale]?.dreams?.get(id)?.isUsable() == true
    }

    private fun Map<String, GeneratedText>.hasDreams(ids: List<String>): Boolean = ids.all { hasDream(it) }

    private fun Map<String, GeneratedText>.dreamNameContext(): List<String> {
        val ids = generatedLocales.flatMap { locale -> this[locale]?.dreams?.keys.orEmpty() }.distinct().sorted()
        return ids.mapNotNull { id ->
            localizedNameContext { locale -> this[locale]?.dreams?.get(id)?.name }
        }.takeLast(MAX_ACCEPTED_PROFESSION_NAMES_IN_PROMPT)
    }

    private fun Map<String, GeneratedText>.cardNameContext(
        type: BoardCardType,
        nameBearingIds: Set<Int>,
    ): List<String> {
        val ids = generatedLocales.flatMap { locale ->
            this[locale]?.cards?.get(type)?.keys.orEmpty()
        }.distinct().filter { it in nameBearingIds }.sorted()
        return ids.mapNotNull { id ->
            localizedNameContext { locale -> this[locale]?.cards?.get(type)?.get(id)?.name }
        }.takeLast(MAX_ACCEPTED_CARD_NAMES_IN_PROMPT)
    }

    private fun Map<String, GeneratedText>.professionNameContext(): List<String> {
        val ids = generatedLocales.flatMap { locale -> this[locale]?.professions?.keys.orEmpty() }
            .distinct()
            .sorted()
        return ids.mapNotNull { id ->
            localizedNameContext { locale -> this[locale]?.professions?.get(id) }
        }.take(MAX_ACCEPTED_PROFESSION_NAMES_IN_PROMPT)
    }

    private fun localizedNameContext(name: (String) -> String?): String? {
        val localized = generatedLocales.mapNotNull { locale ->
            name(locale)?.takeIf(String::isNotBlank)?.let { "$locale «$it»" }
        }
        return localized.takeIf { it.size == generatedLocales.size }?.joinToString(" / ")
    }

    private fun batches(size: Int): Int = if (size == 0) 0 else (size + batchSize - 1) / batchSize
}

private data class CardBatch(
    val type: BoardCardType,
    val index: Int,
    val count: Int,
    val items: List<Map.Entry<Int, BoardCard>>,
    val cardsById: Map<Int, BoardCard>,
    val nameBearingIds: Set<Int>,
)

private data class ProfessionBatch(
    val index: Int,
    val count: Int,
    val items: List<ProfessionCard>,
)

private data class DreamBatch(
    val index: Int,
    val count: Int,
    val items: List<IndexedValue<Dream>>,
)

private const val MAX_TEXT_BATCH_ATTEMPTS = 3
private const val MAX_REPAIR_BATCH_ATTEMPTS = 4
private const val REPAIR_BATCH_SIZE = 4
private const val MAX_SINGLE_ITEM_ATTEMPTS = 8
internal const val MAX_REJECTED_TEXTS_IN_PROMPT = 2
private const val MAX_ACCEPTED_CARD_NAMES_IN_PROMPT = 150
private const val MAX_ACCEPTED_PROFESSION_NAMES_IN_PROMPT = 100
private const val MAX_ERROR_IDS = 8
