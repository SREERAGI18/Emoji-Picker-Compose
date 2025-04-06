package com.emoji_picker_compose.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

object Extensions {
    @Composable
    fun Modifier.noRippleClick(
        onClick:  () -> Unit
    ): Modifier {
        return clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            onClick()
        }
    }
}