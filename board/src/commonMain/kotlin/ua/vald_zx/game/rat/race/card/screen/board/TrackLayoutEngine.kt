package ua.vald_zx.game.rat.race.card.screen.board

import ua.vald_zx.game.rat.race.card.shared.TrackDefinition
import ua.vald_zx.game.rat.race.card.shared.TrackId
import ua.vald_zx.game.rat.race.card.shared.TrackTopology
import kotlin.math.min

data class TrackViewport(
    val width: Float,
    val height: Float,
)

data class TrackPoint(
    val x: Float,
    val y: Float,
)

data class TrackFrame(
    val trackId: TrackId,
    val order: Int,
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val cells: List<TrackPoint>,
)

class TrackLayoutEngine {
    fun layout(
        tracks: List<TrackDefinition>,
        viewport: TrackViewport,
        portrait: Boolean,
    ): List<TrackFrame> {
        require(viewport.width > 0 && viewport.height > 0) { "Viewport must be positive" }
        require(tracks.isNotEmpty()) { "At least one track is required" }
        require(tracks.map(TrackDefinition::id).distinct().size == tracks.size) { "Track ids must be unique" }
        val ordered = tracks.sortedByDescending(TrackDefinition::order)
        val insetStep = min(viewport.width, viewport.height) / (ordered.size * 5f + 2f)
        return ordered.mapIndexed { index, track ->
            require(track.topology == TrackTopology.LOOP) { "Unsupported track topology: ${track.topology}" }
            require(track.cells.isNotEmpty()) { "Track ${track.id} must contain cells" }
            val inset = insetStep * index
            val availableWidth = viewport.width - inset * 2
            val availableHeight = viewport.height - inset * 2
            val horizontalCells = if (portrait) track.visual.verticalCells else track.visual.horizontalCells
            val verticalCells = if (portrait) track.visual.horizontalCells else track.visual.verticalCells
            val aspect = horizontalCells.toFloat() / verticalCells.toFloat()
            val size = fit(availableWidth, availableHeight, aspect)
            val left = (viewport.width - size.width) / 2
            val top = (viewport.height - size.height) / 2
            TrackFrame(
                trackId = track.id,
                order = track.order,
                left = left,
                top = top,
                width = size.width,
                height = size.height,
                cells = loopPoints(track.cells.size, left, top, size.width, size.height),
            )
        }
    }

    private fun fit(width: Float, height: Float, aspect: Float): TrackViewport {
        val availableAspect = width / height
        return if (availableAspect > aspect) {
            TrackViewport(height * aspect, height)
        } else {
            TrackViewport(width, width / aspect)
        }
    }

    private fun loopPoints(
        count: Int,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
    ): List<TrackPoint> {
        val perimeter = 2 * (width + height)
        return List(count) { index ->
            val distance = perimeter * index / count
            when {
                distance < width -> TrackPoint(left + distance, top)
                distance < width + height -> TrackPoint(left + width, top + distance - width)
                distance < width * 2 + height -> TrackPoint(
                    left + width - (distance - width - height),
                    top + height,
                )

                else -> TrackPoint(left, top + height - (distance - width * 2 - height))
            }
        }
    }
}
