package com.mail2dev.planfora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.planfora.ui.theme.*

@Composable
fun PlanForaSurfaceCard(
    title: String? = null,
    modifier: Modifier = Modifier,
    isImportant: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundColor = if (isImportant) MandatoryFill else OptionalFill
    val borderColor = if (isImportant) MandatoryBorder else OptionalBorder
    val labelColor = if (isImportant) SlateTextPrimary else SlateTextSecondary

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (title != null) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            content()
        }
    }
}

@Composable
fun PlanForaFieldGroup(
    modifier: Modifier = Modifier,
    isImportant: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val backgroundColor = if (isImportant) MandatoryFill else OptionalFill
    val borderColor = if (isImportant) MandatoryBorder else OptionalBorder

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun planForaTextFieldColors(isImportant: Boolean = false) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = if (isImportant) MandatoryBorder else OptionalBorder,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = if (isImportant) SlateTextPrimary else SlateTextSecondary,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedTextColor = SlateTextPrimary,
    unfocusedTextColor = if (isImportant) SlateTextPrimary else SlateTextSecondary
)
