package ua.vald_zx.game.rat.race.card.screen.board

import ua.vald_zx.game.rat.race.card.shared.TrackDefinition
import ua.vald_zx.game.rat.race.card.shared.TrackId
import ua.vald_zx.game.rat.race.card.shared.TrackTopology

private const val TRACK_DEPTH_CELLS = 2f
private const val TRACK_GAP_CELLS = 1f / 3f

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
        var available = TrackBounds(0f, 0f, viewport.width, viewport.height)
        return ordered.mapIndexed { index, track ->
            require(track.topology == TrackTopology.LOOP) { "Unsupported track topology: ${track.topology}" }
            require(track.cells.isNotEmpty()) { "Track ${track.id} must contain cells" }
            val horizontalCells = if (portrait) track.visual.verticalCells else track.visual.horizontalCells
            val verticalCells = if (portrait) track.visual.horizontalCells else track.visual.verticalCells
            require(horizontalCells > 0 && verticalCells > 0) { "Track ${track.id} dimensions must be positive" }
            val aspect = horizontalCells.toFloat() / verticalCells.toFloat()
            val size = fit(available.width, available.height, aspect)
            val left = available.left + (available.width - size.width) / 2
            val top = available.top + (available.height - size.height) / 2
            val frame = TrackFrame(
                trackId = track.id,
                order = track.order,
                left = left,
                top = top,
                width = size.width,
                height = size.height,
                cells = loopPoints(track.cells.size, left, top, size.width, size.height),
            )
            if (index < ordered.lastIndex) {
                val horizontalInset = size.width / horizontalCells * (TRACK_DEPTH_CELLS + TRACK_GAP_CELLS)
                val verticalInset = size.height / verticalCells * (TRACK_DEPTH_CELLS + TRACK_GAP_CELLS)
                available = TrackBounds(
                    left = left + horizontalInset,
                    top = top + verticalInset,
                    width = size.width - horizontalInset * 2,
                    height = size.height - verticalInset * 2,
                )
                require(available.width > 0 && available.height > 0) {
                    "Viewport is too small for ${ordered.size} tracks"
                }
            }
            frame
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

private data class TrackBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)
