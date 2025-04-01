package com.emoji_picker_compose

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class EmojiCategory(
    @RawRes val resId:Int,
    @DrawableRes val categoryIcon: Int,

    val categoryName: String
)
