package ua.vald_zx.game.rat.race.card.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.shared.Gender
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun GenderSelector(
    selected: Gender,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    maleStyle: GenderOptionStyle = GenderOptionStyle.MaleDefault,
    femaleStyle: GenderOptionStyle = GenderOptionStyle.FemaleDefault,
    animationSpec: AnimationSpec<Float> = tween(400, easing = LinearEasing),
    onGenderChange: (newGender: Gender) -> Unit
) {
    // Arrange the gender options in a horizontal row
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Female gender option
        GenderOption(
            shape = FemaleShape,
            style = femaleStyle,
            selected = (selected == Gender.FEMALE),
            animationSpec = animationSpec,
            modifier = Modifier
                .size(iconSize)
                .clickableGenderOption(Gender.FEMALE, onGenderChange)
        )
        // Male gender option
        GenderOption(
            shape = MaleShape,
            style = maleStyle,
            selected = (selected == Gender.MALE),
            animationSpec = animationSpec,
            modifier = Modifier
                .size(iconSize)
                .clickableGenderOption(Gender.MALE, onGenderChange)
        )
    }
}

fun Modifier.clickableGenderOption(
    gender: Gender,
    onGenderChange: (newGender: Gender) -> Unit
) = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { onGenderChange(gender) }
    )
}

@Composable
fun GenderOption(
    shape: Shape,
    selected: Boolean,
    style: GenderOptionStyle,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = tween(400, easing = LinearEasing)
) {
    // Animate the progress of the selection
    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = animationSpec
    )

    Canvas(
        modifier = modifier.size(48.dp)
    ) {
        // Turn the shape into an outline
        val outline = shape.createOutline(size, layoutDirection, this)
        // Create a path from the outline
        val path = Path().apply { addOutline(outline) }

        // Calculate the diagonal of the Canvas size
        val diagonal = sqrt(size.width.pow(2) + size.height.pow(2))
        // Calculate the radius of the fill circle based on the selection progress
        val radius = diagonal * progress
        // Determine the center of the fill effect based on the effectOrigin
        val circleCenter = Offset(
            x = size.width * style.effectOrigin.pivotFractionX,
            y = size.height * style.effectOrigin.pivotFractionY
        )

        // Clip the drawing area to the shape path
        clipPath(path) {
            // Draw the background color
            drawRect(style.backgroundColor)
            // Draw the fill circle
            drawCircle(
                color = style.fillColor,
                radius = radius,
                center = circleCenter
            )
        }
    }
}

data class GenderOptionStyle(
    val backgroundColor: Color,
    val fillColor: Color,
    val effectOrigin: TransformOrigin
) {
    companion object {
        val MaleDefault = GenderOptionStyle(Color.LightGray, Color.Blue, TransformOrigin(0f, 0f))
        val FemaleDefault = GenderOptionStyle(Color.LightGray, Color.Red, TransformOrigin(1f, 0f))
    }
}

object FemaleShape : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val scale = size.minDimension / 24f

        val path = Path().apply {
            // Head
            addOval(Rect(Offset(12.0f * scale, 4.0f * scale), 2.0f * scale))

            // Body and limbs
            moveTo(16.45f * scale, 14.63f * scale)
            lineTo(13.93f * scale, 8.31f * scale)
            cubicTo(13.61f * scale, 7.52f * scale, 12.85f * scale, 7.01f * scale, 12.0f * scale, 7.0f * scale)
            cubicTo(11.15f * scale, 7.01f * scale, 10.38f * scale, 7.52f * scale, 10.07f * scale, 8.31f * scale)
            lineTo(7.55f * scale, 14.63f * scale)
            cubicTo(7.28f * scale, 15.29f * scale, 7.77f * scale, 16.0f * scale, 8.47f * scale, 16.0f * scale)
            lineTo(10.0f * scale, 16.0f * scale)
            lineTo(10.0f * scale, 21.0f * scale)
            cubicTo(10.0f * scale, 21.55f * scale, 10.45f * scale, 22.0f * scale, 11.0f * scale, 22.0f * scale)
            lineTo(13.0f * scale, 22.0f * scale)
            cubicTo(13.55f * scale, 22.0f * scale, 14.0f * scale, 21.55f * scale, 14.0f * scale, 21.0f * scale)
            lineTo(14.0f * scale, 16.0f * scale)
            lineTo(15.53f * scale, 16.0f * scale)
            cubicTo(16.23f * scale, 16.0f * scale, 16.72f * scale, 15.29f * scale, 16.45f * scale, 14.63f * scale)
            close()
        }

        return Outline.Generic(path)
    }
}

object MaleShape : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val scale = size.minDimension / 24f

        val path = Path().apply {
            // Head
            addOval(Rect(Offset(12f * scale, 4f * scale), 2f * scale))

            // Body and limbs
            moveTo(14.0f * scale, 7.0f * scale)
            lineTo(10.0f * scale, 7.0f * scale)
            cubicTo(8.9f * scale, 7.0f * scale, 8.0f * scale, 7.9f * scale, 8.0f * scale, 9.0f * scale)
            lineTo(8.0f * scale, 14.0f * scale)
            cubicTo(8.0f * scale, 14.55f * scale, 8.45f * scale, 15.0f * scale, 9.0f * scale, 15.0f * scale)
            lineTo(10.0f * scale, 15.0f * scale)
            lineTo(10.0f * scale, 21.0f * scale)
            cubicTo(10.0f * scale, 21.55f * scale, 10.45f * scale, 22.0f * scale, 11.0f * scale, 22.0f * scale)
            lineTo(13.0f * scale, 22.0f * scale)
            cubicTo(13.55f * scale, 22.0f * scale, 14.0f * scale, 21.55f * scale, 14.0f * scale, 21.0f * scale)
            lineTo(14.0f * scale, 15.0f * scale)
            lineTo(15.0f * scale, 15.0f * scale)
            cubicTo(15.55f * scale, 15.0f * scale, 16.0f * scale, 14.55f * scale, 16.0f * scale, 14.0f * scale)
            lineTo(16.0f * scale, 9.0f * scale)
            cubicTo(16.0f * scale, 7.9f * scale, 15.1f * scale, 7.0f * scale, 14.0f * scale, 7.0f * scale)
            close()
        }

        return Outline.Generic(path)
    }
}
