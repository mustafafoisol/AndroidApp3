package com.mustafafoisol.androidapp3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.mustafafoisol.androidapp3.ui.theme.BorderLine
import com.mustafafoisol.androidapp3.ui.theme.DashedLine
import com.mustafafoisol.androidapp3.ui.theme.Ink
import com.mustafafoisol.androidapp3.ui.theme.Label
import com.mustafafoisol.androidapp3.ui.theme.Orange
import com.mustafafoisol.androidapp3.ui.theme.Placeholder
import com.mustafafoisol.androidapp3.ui.theme.Surface

/** Section heading: 11px, bold, wide tracking, muted. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp,
        color = Label
    )
}

/** The white, hairline-bordered, 14dp-rounded card the whole form is built from. */
fun Modifier.formCard(radius: Dp = 14.dp): Modifier = this
    .background(Surface, RoundedCornerShape(radius))
    .border(1.dp, BorderLine, RoundedCornerShape(radius))

/** Dashed 14dp-rounded outline used by the "Add item" button. */
fun Modifier.dashedOutline(radius: Dp = 14.dp): Modifier = this.drawBehind {
    val stroke = 1.dp.toPx()
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = stroke / 2f,
                top = stroke / 2f,
                right = size.width - stroke / 2f,
                bottom = size.height - stroke / 2f,
                cornerRadius = CornerRadius(radius.toPx())
            )
        )
    }
    drawPath(
        path = path,
        color = DashedLine,
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
        )
    )
}

/**
 * Borderless text field. Material's TextField brings its own padding and indicator,
 * neither of which the design has, so the form uses BasicTextField throughout.
 */
@Composable
fun PlainField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = textStyle.copy(color = Ink),
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(Orange),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = textStyle.copy(color = Placeholder))
                }
                innerTextField()
            }
        }
    )
}
